package com.hypervector.index.common;

import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.euclidean.EuclideanVectorOperations;
import com.hypervector.math.hyperbolic.PoincareVector;
import com.hypervector.math.hyperbolic.PoincareVectorOperations;
import com.hypervector.storage.common.VectorRecord;
import com.hypervector.storage.common.VectorStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A simple brute-force "flat" vector index implementation.
 * Performs exact nearest neighbor search by computing distances to all vectors.
 * Not efficient for large collections but useful for testing and as a baseline.
 */
public class FlatVectorIndex implements VectorIndex {

    private static final Logger logger = LoggerFactory.getLogger(FlatVectorIndex.class);

    private VectorStorage storage;
    private final Map<String, IndexStats> indexStatsMap = new ConcurrentHashMap<>();

    // Math operations
    private final EuclideanVectorOperations euclideanOps = new EuclideanVectorOperations();
    private final PoincareVectorOperations poincareOps = new PoincareVectorOperations();

    @Override
    public void buildIndex(String collection, IndexType type, Map<String, Object> parameters) {
        if (type != IndexType.FLAT) {
            throw new IllegalArgumentException("FlatVectorIndex only supports FLAT index type");
        }

        if (storage == null) {
            throw new IllegalStateException("Vector storage not set");
        }

        if (!storage.collectionExists(collection)) {
            throw new IllegalArgumentException("Collection does not exist: " + collection);
        }

        long startTime = System.currentTimeMillis();

        // Just count the vectors, no actual index build needed for flat search
        List<VectorRecord> records = ((com.hypervector.storage.common.InMemoryVectorStorage) storage)
                .getAllVectors(collection);

        long endTime = System.currentTimeMillis();

        // Determine space type from the collection
        SpaceType spaceType = SpaceType.EUCLIDEAN;
        if (records.size() > 0) {
            VectorRecord record = records.get(0);
            if (record.getVectorType() == VectorRecord.VectorType.HYPERBOLIC_POINCARE) {
                spaceType = SpaceType.POINCARE_BALL;
            }
        }

        // Create index statistics
        IndexStats stats = new IndexStats.Builder()
                .setCollectionName(collection)
                .setIndexType(type)
                .setSpaceType(spaceType)
                .setVectorCount(records.size())
                .setDimension(records.isEmpty() ? 0 : records.get(0).getDimension())
                .setIndexSizeBytes(0) // Flat index doesn't use extra memory
                .setBuildTimeMs(endTime - startTime)
                .setParameters(parameters != null ? parameters : new HashMap<>())
                .addMetric("exactSearch", true)
                .build();

        indexStatsMap.put(collection, stats);

        logger.info("Built flat index for collection {}: {} vectors", collection, records.size());
    }

    @Override
    public List<SearchResult> search(String collection, float[] queryVector, int k, SpaceType spaceType) {
        return search(collection, queryVector, null, k, spaceType);
    }

    @Override
    public List<SearchResult> search(String collection, float[] queryVector, FilterExpression filter,
                                     int k, SpaceType spaceType) {
        if (storage == null) {
            throw new IllegalStateException("Vector storage not set");
        }

        if (!storage.collectionExists(collection)) {
            throw new IllegalArgumentException("Collection does not exist: " + collection);
        }

        if (k <= 0) {
            throw new IllegalArgumentException("k must be positive");
        }

        // Get all vectors from storage
        List<VectorRecord> records = ((com.hypervector.storage.common.InMemoryVectorStorage) storage)
                .getAllVectors(collection);

        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        // Apply filter if provided
        if (filter != null) {
            records = records.stream()
                    .filter(filter::evaluate)
                    .collect(Collectors.toList());

            if (records.isEmpty()) {
                return Collections.emptyList();
            }
        }

        // Calculate distances based on space type
        List<SearchResult> results = new ArrayList<>(records.size());

        for (VectorRecord record : records) {
            double distance;

            switch (spaceType) {
                case EUCLIDEAN:
                    EuclideanVector queryE = new EuclideanVector(toDoubleArray(queryVector));
                    EuclideanVector recordE = new EuclideanVector(toDoubleArray(record.getVector()));
                    distance = euclideanOps.distance(queryE, recordE);
                    break;

                case POINCARE_BALL:
                    PoincareVector queryP = new PoincareVector(toDoubleArray(queryVector));
                    PoincareVector recordP = new PoincareVector(toDoubleArray(record.getVector()));
                    distance = poincareOps.distance(queryP, recordP);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported space type: " + spaceType);
            }

            results.add(new SearchResult(record.getId(), record.getVector(), distance, record));
        }

        // Sort by distance and limit to k results
        Collections.sort(results);

        return results.size() <= k ? results : results.subList(0, k);
    }

    @Override
    public void addToIndex(String collection, List<String> ids) {
        // No-op for flat index, as we query storage directly
        if (indexExists(collection)) {
            IndexStats oldStats = indexStatsMap.get(collection);

            // Just update the vector count
            IndexStats newStats = new IndexStats.Builder()
                    .setCollectionName(oldStats.getCollectionName())
                    .setIndexType(oldStats.getIndexType())
                    .setSpaceType(oldStats.getSpaceType())
                    .setVectorCount(oldStats.getVectorCount() + ids.size())
                    .setDimension(oldStats.getDimension())
                    .setIndexSizeBytes(oldStats.getIndexSizeBytes())
                    .setBuildTimeMs(oldStats.getBuildTimeMs())
                    .setParameters(oldStats.getParameters())
                    .setMetrics(oldStats.getMetrics())
                    .build();

            indexStatsMap.put(collection, newStats);
        }
    }

    @Override
    public void removeFromIndex(String collection, List<String> ids) {
        // No-op for flat index, as we query storage directly
        if (indexExists(collection)) {
            IndexStats oldStats = indexStatsMap.get(collection);

            // Just update the vector count
            IndexStats newStats = new IndexStats.Builder()
                    .setCollectionName(oldStats.getCollectionName())
                    .setIndexType(oldStats.getIndexType())
                    .setSpaceType(oldStats.getSpaceType())
                    .setVectorCount(Math.max(0, oldStats.getVectorCount() - ids.size()))
                    .setDimension(oldStats.getDimension())
                    .setIndexSizeBytes(oldStats.getIndexSizeBytes())
                    .setBuildTimeMs(oldStats.getBuildTimeMs())
                    .setParameters(oldStats.getParameters())
                    .setMetrics(oldStats.getMetrics())
                    .build();

            indexStatsMap.put(collection, newStats);
        }
    }

    @Override
    public IndexStats getIndexStats(String collection) {
        return indexStatsMap.get(collection);
    }

    @Override
    public boolean indexExists(String collection) {
        return indexStatsMap.containsKey(collection);
    }

    @Override
    public boolean deleteIndex(String collection) {
        if (!indexExists(collection)) {
            return false;
        }

        indexStatsMap.remove(collection);
        return true;
    }

    @Override
    public void setVectorStorage(VectorStorage storage) {
        this.storage = storage;
    }

    /**
     * Convert float array to double array.
     *
     * @param floatArray Input float array
     * @return Equivalent double array
     */
    private double[] toDoubleArray(float[] floatArray) {
        double[] doubleArray = new double[floatArray.length];
        for (int i = 0; i < floatArray.length; i++) {
            doubleArray[i] = floatArray[i];
        }
        return doubleArray;
    }
}
