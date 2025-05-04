package com.hypervector.storage.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of vector storage.
 * Useful for testing and small-scale deployments.
 */
public class InMemoryVectorStorage implements VectorStorage {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryVectorStorage.class);

    // Maps collection name to a map of vector ID to vector record
    private final Map<String, Map<String, VectorRecord>> collections = new ConcurrentHashMap<>();

    // Maps collection name to collection configuration
    private final Map<String, CollectionConfig> configs = new ConcurrentHashMap<>();

    @Override
    public String storeVector(String collection, float[] vector, Map<String, Object> metadata) {
        ensureCollectionExists(collection);

        CollectionConfig config = configs.get(collection);
        validateVector(vector, config);

        VectorRecord record = VectorRecord.createEuclidean(vector, metadata);
        collections.get(collection).put(record.getId(), record);

        logger.debug("Stored vector {} in collection {}", record.getId(), collection);
        return record.getId();
    }

    @Override
    public VectorRecord getVector(String collection, String id) {
        if (!collectionExists(collection)) {
            logger.warn("Collection {} does not exist", collection);
            return null;
        }

        VectorRecord record = collections.get(collection).get(id);
        if (record == null) {
            logger.warn("Vector {} not found in collection {}", id, collection);
        }

        return record;
    }

    @Override
    public List<String> storeBatch(String collection, List<float[]> vectors, List<Map<String, Object>> metadata) {
        ensureCollectionExists(collection);

        if (vectors.size() != metadata.size()) {
            throw new IllegalArgumentException(
                    "Vectors and metadata lists must have the same size. Vectors: " +
                            vectors.size() + ", Metadata: " + metadata.size());
        }

        List<String> ids = new ArrayList<>(vectors.size());
        CollectionConfig config = configs.get(collection);

        for (int i = 0; i < vectors.size(); i++) {
            validateVector(vectors.get(i), config);
            VectorRecord record = VectorRecord.createEuclidean(vectors.get(i), metadata.get(i));
            collections.get(collection).put(record.getId(), record);
            ids.add(record.getId());
        }

        logger.debug("Stored {} vectors in collection {}", vectors.size(), collection);
        return ids;
    }

    @Override
    public String storeHyperbolicVector(String collection, float[] vector, boolean isPoincareBall, Map<String, Object> metadata) {
        ensureCollectionExists(collection);

        CollectionConfig config = configs.get(collection);
        validateVector(vector, config);

        VectorRecord.VectorType vectorType = isPoincareBall ?
                VectorRecord.VectorType.HYPERBOLIC_POINCARE :
                VectorRecord.VectorType.HYPERBOLIC_LORENTZ;

        // Ensure the vector is inside the Poincaré ball if needed
        if (isPoincareBall) {
            validatePoincareVector(vector);
        }

        String id = UUID.randomUUID().toString();
        VectorRecord record = new VectorRecord(id, vector, metadata, vectorType);
        collections.get(collection).put(id, record);

        logger.debug("Stored hyperbolic vector {} in collection {}", id, collection);
        return id;
    }

    @Override
    public List<VectorRecord> getVectors(String collection, List<String> ids) {
        if (!collectionExists(collection)) {
            logger.warn("Collection {} does not exist", collection);
            return Collections.emptyList();
        }

        List<VectorRecord> records = new ArrayList<>(ids.size());
        Map<String, VectorRecord> collectionMap = collections.get(collection);

        for (String id : ids) {
            VectorRecord record = collectionMap.get(id);
            if (record != null) {
                records.add(record);
            }
        }

        return records;
    }

    @Override
    public boolean deleteVector(String collection, String id) {
        if (!collectionExists(collection)) {
            logger.warn("Collection {} does not exist", collection);
            return false;
        }

        VectorRecord removed = collections.get(collection).remove(id);
        return removed != null;
    }

    @Override
    public boolean createCollection(String collection, CollectionConfig config) {
        if (collectionExists(collection)) {
            logger.warn("Collection {} already exists", collection);
            return false;
        }

        collections.put(collection, new ConcurrentHashMap<>());
        configs.put(collection, config);

        logger.info("Created collection {}: {}", collection, config);
        return true;
    }

    @Override
    public boolean deleteCollection(String collection) {
        if (!collectionExists(collection)) {
            logger.warn("Collection {} does not exist", collection);
            return false;
        }

        collections.remove(collection);
        configs.remove(collection);

        logger.info("Deleted collection {}", collection);
        return true;
    }

    @Override
    public boolean collectionExists(String collection) {
        return collections.containsKey(collection) && configs.containsKey(collection);
    }

    @Override
    public CollectionStats getCollectionStats(String collection) {
        if (!collectionExists(collection)) {
            logger.warn("Collection {} does not exist", collection);
            return null;
        }

        Map<String, VectorRecord> collectionMap = collections.get(collection);
        CollectionConfig config = configs.get(collection);

        int vectorCount = collectionMap.size();
        long totalSizeBytes = vectorCount * config.getVectorSizeInBytes();

        Map<String, Object> additionalStats = new HashMap<>();
        additionalStats.put("storageFormat", config.getStorageFormat());
        additionalStats.put("compressionEnabled", config.isCompressionEnabled());

        return new CollectionStats(
                collection,
                vectorCount,
                totalSizeBytes,
                config.getDimension(),
                config.getVectorType(),
                additionalStats
        );
    }

    @Override
    public List<String> listCollections() {
        return new ArrayList<>(collections.keySet());
    }

    @Override
    public boolean updateMetadata(String collection, String id, Map<String, Object> metadata) {
        if (!collectionExists(collection)) {
            logger.warn("Collection {} does not exist", collection);
            return false;
        }

        VectorRecord record = collections.get(collection).get(id);
        if (record == null) {
            logger.warn("Vector {} not found in collection {}", id, collection);
            return false;
        }

        record.updateMetadata(metadata);
        return true;
    }

    @Override
    public void close() {
        // Nothing to close for in-memory storage
        logger.info("Closed in-memory vector storage");
    }

    /**
     * Get the configuration for a collection.
     *
     * @param collection Collection name
     * @return Collection configuration or null if not found
     */
    public CollectionConfig getCollectionConfig(String collection) {
        return configs.get(collection);
    }

    /**
     * Get all vectors in a collection.
     *
     * @param collection Collection name
     * @return List of all vector records or empty list if collection doesn't exist
     */
    public List<VectorRecord> getAllVectors(String collection) {
        if (!collectionExists(collection)) {
            logger.warn("Collection {} does not exist", collection);
            return Collections.emptyList();
        }

        return new ArrayList<>(collections.get(collection).values());
    }

    /**
     * Search for vectors by metadata criteria.
     *
     * @param collection Collection name
     * @param criteria Metadata key-value pairs that must all match
     * @return List of matching vector records
     */
    public List<VectorRecord> searchByMetadata(String collection, Map<String, Object> criteria) {
        if (!collectionExists(collection)) {
            logger.warn("Collection {} does not exist", collection);
            return Collections.emptyList();
        }

        return collections.get(collection).values().stream()
                .filter(record -> matchesMetadata(record, criteria))
                .collect(Collectors.toList());
    }

    /**
     * Check if a vector record matches the metadata criteria.
     */
    private boolean matchesMetadata(VectorRecord record, Map<String, Object> criteria) {
        Map<String, Object> metadata = record.getMetadata();

        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            Object value = metadata.get(entry.getKey());
            if (value == null || !value.equals(entry.getValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Ensure a collection exists, creating it with default configuration if it doesn't.
     */
    private void ensureCollectionExists(String collection) {
        if (!collectionExists(collection)) {
            // Create with default Euclidean configuration
            createCollection(collection, CollectionConfig.createEuclidean(
                    128, CollectionConfig.StorageFormat.FLOAT32));
        }
    }

    /**
     * Validate a vector against a collection configuration.
     */
    private void validateVector(float[] vector, CollectionConfig config) {
        if (vector.length != config.getDimension()) {
            throw new IllegalArgumentException(
                    "Vector dimension mismatch. Expected: " + config.getDimension() +
                            ", Got: " + vector.length);
        }
    }

    /**
     * Validate that a vector is inside the Poincaré ball.
     */
    private void validatePoincareVector(float[] vector) {
        double squaredNorm = 0.0;
        for (float v : vector) {
            squaredNorm += v * v;
        }

        if (squaredNorm >= 1.0) {
            throw new IllegalArgumentException(
                    "Vector lies outside the Poincaré ball (norm = " +
                            Math.sqrt(squaredNorm) + " >= 1)");
        }
    }
}