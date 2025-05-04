package com.hypervector.index;

import com.hypervector.index.common.FilterExpression;
import com.hypervector.index.common.FlatVectorIndex;
import com.hypervector.index.common.SearchResult;
import com.hypervector.index.common.VectorIndex;
import com.hypervector.storage.common.CollectionConfig;
import com.hypervector.storage.common.InMemoryVectorStorage;
import com.hypervector.storage.common.VectorRecord;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class FlatVectorIndexTest {

    private InMemoryVectorStorage storage;
    private FlatVectorIndex index;
    private static final String EUCLIDEAN_COLLECTION = "euclidean_test";
    private static final String POINCARE_COLLECTION = "poincare_test";

    @BeforeEach
    public void setUp() {
        storage = new InMemoryVectorStorage();
        index = new FlatVectorIndex();
        index.setVectorStorage(storage);

        // Create collections
        storage.createCollection(EUCLIDEAN_COLLECTION,
                CollectionConfig.createEuclidean(3, CollectionConfig.StorageFormat.FLOAT32));

        storage.createCollection(POINCARE_COLLECTION,
                CollectionConfig.createPoincare(3, CollectionConfig.StorageFormat.FLOAT32));

        // Add some vectors to Euclidean collection
        for (int i = 0; i < 10; i++) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", i);
            metadata.put("category", i % 3);

            storage.storeVector(EUCLIDEAN_COLLECTION,
                    new float[]{i * 0.1f, i * 0.2f, i * 0.3f},
                    metadata);
        }

        // Add some vectors to Poincaré collection
        for (int i = 0; i < 10; i++) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", i);
            metadata.put("category", i % 3);

            // Ensure vectors are inside Poincaré ball
            float scale = 0.08f;
            storage.storeHyperbolicVector(POINCARE_COLLECTION,
                    new float[]{i * scale, i * scale, i * scale},
                    true, metadata);
        }

        // Build indices
        index.buildIndex(EUCLIDEAN_COLLECTION, VectorIndex.IndexType.FLAT, null);
        index.buildIndex(POINCARE_COLLECTION, VectorIndex.IndexType.FLAT, null);
    }

    @Test
    public void testEuclideanSearch() {
        float[] query = {0.5f, 1.0f, 1.5f};
        int k = 3;

        List<SearchResult> results = index.search(EUCLIDEAN_COLLECTION, query, k, VectorIndex.SpaceType.EUCLIDEAN);

        // Should return k results
        assertEquals(k, results.size());

        // Results should be sorted by distance
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i).getDistance() >= results.get(i-1).getDistance());
        }

        // First result should be closest to query
        // In this case, it should be close to vector at index 5
        VectorRecord record = results.get(0).getRecord();
        assertEquals(5, record.getMetadata().get("id"));
    }

    @Test
    public void testPoincareSearch() {
        float[] query = {0.25f, 0.25f, 0.25f};
        int k = 3;

        List<SearchResult> results = index.search(POINCARE_COLLECTION, query, k, VectorIndex.SpaceType.POINCARE_BALL);

        // Should return k results
        assertEquals(k, results.size());

        // Results should be sorted by distance
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i).getDistance() >= results.get(i-1).getDistance());
        }

        // The closest result should be the one with values closest to the query
        float[] firstVec = results.get(0).getVector();
        double distance = Math.sqrt(
                Math.pow(firstVec[0] - query[0], 2) +
                        Math.pow(firstVec[1] - query[1], 2) +
                        Math.pow(firstVec[2] - query[2], 2));

        assertTrue(distance < 0.3); // Just a rough check
    }

    @Test
    public void testFilteredSearch() {
        float[] query = {0.5f, 1.0f, 1.5f};
        int k = 5;

        // Create a filter for category = 1
        FilterExpression filter = FilterExpression.eq("category", 1);

        List<SearchResult> results = index.search(EUCLIDEAN_COLLECTION, query, filter, k, VectorIndex.SpaceType.EUCLIDEAN);

        // Should return all vectors with category = 1, up to k
        assertTrue(results.size() <= k);

        // All results should have category = 1
        for (SearchResult result : results) {
            assertEquals(1, result.getRecord().getMetadata().get("category"));
        }
    }

    @Test
    public void testComplexFilter() {
        float[] query = {0.5f, 1.0f, 1.5f};
        int k = 5;

        // Create a complex filter: category = 1 OR (id > 7 AND category = 2)
        FilterExpression filter = FilterExpression.eq("category", 1)
                .or(
                        FilterExpression.gt("id", 7)
                                .and(FilterExpression.eq("category", 2))
                );

        List<SearchResult> results = index.search(EUCLIDEAN_COLLECTION, query, filter, k, VectorIndex.SpaceType.EUCLIDEAN);

        // All results should match the filter
        for (SearchResult result : results) {
            Map<String, Object> metadata = result.getRecord().getMetadata();
            int category = (int) metadata.get("category");
            int id = (int) metadata.get("id");

            assertTrue(
                    category == 1 || (id > 7 && category == 2),
                    "Result should match filter: " + metadata
            );
        }
    }

    @Test
    public void testIndexStats() {
        // Check stats for Euclidean collection
        assertNotNull(index.getIndexStats(EUCLIDEAN_COLLECTION));
        assertEquals(10, index.getIndexStats(EUCLIDEAN_COLLECTION).getVectorCount());
        assertEquals(VectorIndex.IndexType.FLAT, index.getIndexStats(EUCLIDEAN_COLLECTION).getIndexType());
        assertEquals(VectorIndex.SpaceType.EUCLIDEAN, index.getIndexStats(EUCLIDEAN_COLLECTION).getSpaceType());

        // Check stats for Poincaré collection
        assertNotNull(index.getIndexStats(POINCARE_COLLECTION));
        assertEquals(10, index.getIndexStats(POINCARE_COLLECTION).getVectorCount());
        assertEquals(VectorIndex.IndexType.FLAT, index.getIndexStats(POINCARE_COLLECTION).getIndexType());
        assertEquals(VectorIndex.SpaceType.POINCARE_BALL, index.getIndexStats(POINCARE_COLLECTION).getSpaceType());
    }

    @Test
    public void testDeleteIndex() {
        assertTrue(index.indexExists(EUCLIDEAN_COLLECTION));
        assertTrue(index.deleteIndex(EUCLIDEAN_COLLECTION));
        assertFalse(index.indexExists(EUCLIDEAN_COLLECTION));

        // Trying to delete again should return false
        assertFalse(index.deleteIndex(EUCLIDEAN_COLLECTION));
    }
}