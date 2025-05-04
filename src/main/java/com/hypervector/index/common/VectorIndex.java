package com.hypervector.index.common;

import com.hypervector.storage.common.VectorRecord;

import java.util.List;
import java.util.Map;

/**
 * Interface for vector indexing operations.
 * Defines operations for building and searching vector indices.
 */
public interface VectorIndex {

    /**
     * Available index types.
     */
    enum IndexType {
        HNSW_EUCLIDEAN,   // Hierarchical Navigable Small World for Euclidean space
        HNSW_HYPERBOLIC,  // HNSW adapted for hyperbolic space
        IVF,              // Inverted File Index
        FLAT              // Brute-force exact search
    }

    /**
     * Geometric spaces for vectors.
     */
    enum SpaceType {
        EUCLIDEAN,       // Standard Euclidean space
        POINCARE_BALL,   // Poincaré ball model of hyperbolic space
        LORENTZ          // Lorentz model of hyperbolic space
    }

    /**
     * Build an index for a collection.
     *
     * @param collection Collection name
     * @param type Index type to build
     * @param parameters Additional parameters for the index
     * @throws IllegalArgumentException if parameters are invalid
     */
    void buildIndex(String collection, IndexType type, Map<String, Object> parameters);

    /**
     * Search the index for similar vectors.
     *
     * @param collection Collection name
     * @param queryVector Query vector
     * @param k Number of nearest neighbors to return
     * @param spaceType Geometric space type
     * @return List of search results
     */
    List<SearchResult> search(String collection, float[] queryVector, int k, SpaceType spaceType);

    /**
     * Search with additional filter.
     *
     * @param collection Collection name
     * @param queryVector Query vector
     * @param filter Filter expression for metadata
     * @param k Number of nearest neighbors to return
     * @param spaceType Geometric space type
     * @return List of filtered search results
     */
    List<SearchResult> search(String collection, float[] queryVector, FilterExpression filter,
                              int k, SpaceType spaceType);

    /**
     * Add new vectors to an existing index.
     *
     * @param collection Collection name
     * @param ids List of vector IDs to add
     */
    void addToIndex(String collection, List<String> ids);

    /**
     * Delete vectors from an index.
     *
     * @param collection Collection name
     * @param ids List of vector IDs to remove
     */
    void removeFromIndex(String collection, List<String> ids);

    /**
     * Get information about an index.
     *
     * @param collection Collection name
     * @return Index statistics
     */
    IndexStats getIndexStats(String collection);

    /**
     * Check if an index exists for a collection.
     *
     * @param collection Collection name
     * @return true if index exists
     */
    boolean indexExists(String collection);

    /**
     * Delete an index.
     *
     * @param collection Collection name
     * @return true if deleted
     */
    boolean deleteIndex(String collection);

    /**
     * Set the vector storage.
     *
     * @param storage Vector storage to use
     */
    void setVectorStorage(com.hypervector.storage.common.VectorStorage storage);
}