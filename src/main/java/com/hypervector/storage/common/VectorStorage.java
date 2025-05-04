package com.hypervector.storage.common;

import java.util.List;
import java.util.Map;

/**
 * Interface for vector storage operations.
 * Defines operations for storing and retrieving vectors in different geometric spaces.
 */
public interface VectorStorage {

    /**
     * Store a vector with an ID.
     *
     * @param collection Collection name
     * @param vector Vector data as float array
     * @param metadata Additional metadata for the vector
     * @return Generated ID for the stored vector
     */
    String storeVector(String collection, float[] vector, Map<String, Object> metadata);

    /**
     * Retrieve a vector by ID.
     *
     * @param collection Collection name
     * @param id Vector ID
     * @return The retrieved vector record or null if not found
     */
    VectorRecord getVector(String collection, String id);

    /**
     * Store multiple vectors in batch.
     *
     * @param collection Collection name
     * @param vectors List of vectors to store
     * @param metadata List of metadata entries, one per vector
     * @return List of generated IDs
     */
    List<String> storeBatch(String collection, List<float[]> vectors, List<Map<String, Object>> metadata);

    /**
     * Store a hyperbolic vector.
     *
     * @param collection Collection name
     * @param vector Vector data
     * @param isPoincareBall Whether the vector is in Poincaré ball model
     * @param metadata Additional metadata
     * @return Generated ID
     */
    String storeHyperbolicVector(String collection, float[] vector, boolean isPoincareBall, Map<String, Object> metadata);

    /**
     * Retrieve vectors by IDs.
     *
     * @param collection Collection name
     * @param ids List of vector IDs
     * @return List of vector records
     */
    List<VectorRecord> getVectors(String collection, List<String> ids);

    /**
     * Delete a vector.
     *
     * @param collection Collection name
     * @param id Vector ID
     * @return true if deleted, false if not found
     */
    boolean deleteVector(String collection, String id);

    /**
     * Create a new collection.
     *
     * @param collection Collection name
     * @param config Collection configuration
     * @return true if created, false if already exists
     */
    boolean createCollection(String collection, CollectionConfig config);

    /**
     * Delete a collection.
     *
     * @param collection Collection name
     * @return true if deleted, false if not found
     */
    boolean deleteCollection(String collection);

    /**
     * Check if a collection exists.
     *
     * @param collection Collection name
     * @return true if exists, false otherwise
     */
    boolean collectionExists(String collection);

    /**
     * Get collection statistics.
     *
     * @param collection Collection name
     * @return Collection stats or null if not found
     */
    CollectionStats getCollectionStats(String collection);

    /**
     * List all collections.
     *
     * @return List of collection names
     */
    List<String> listCollections();

    /**
     * Update vector metadata.
     *
     * @param collection Collection name
     * @param id Vector ID
     * @param metadata New metadata
     * @return true if updated, false if not found
     */
    boolean updateMetadata(String collection, String id, Map<String, Object> metadata);

    /**
     * Close the storage and release resources.
     */
    void close();
}