package com.hypervector.storage;

import com.hypervector.storage.common.CollectionConfig;
import com.hypervector.storage.common.InMemoryVectorStorage;
import com.hypervector.storage.common.VectorRecord;
import com.hypervector.storage.common.CollectionStats;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryVectorStorageTest {

    private InMemoryVectorStorage storage;
    private static final String TEST_COLLECTION = "test_collection";

    @BeforeEach
    public void setUp() {
        storage = new InMemoryVectorStorage();

        // Create a test collection
        CollectionConfig config = CollectionConfig.createEuclidean(
                3, CollectionConfig.StorageFormat.FLOAT32);
        storage.createCollection(TEST_COLLECTION, config);
    }

    @Test
    public void testStoreAndRetrieveVector() {
        // Store a vector
        float[] vector = {1.0f, 2.0f, 3.0f};
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", "test vector");
        metadata.put("category", "test");

        String id = storage.storeVector(TEST_COLLECTION, vector, metadata);
        assertNotNull(id);

        // Retrieve the vector
        VectorRecord record = storage.getVector(TEST_COLLECTION, id);
        assertNotNull(record);

        // Check the retrieved data
        assertArrayEquals(vector, record.getVector());
        assertEquals("test vector", record.getMetadata().get("name"));
        assertEquals("test", record.getMetadata().get("category"));
        assertEquals(VectorRecord.VectorType.EUCLIDEAN, record.getVectorType());
    }

    @Test
    public void testStoreHyperbolicVector() {
        // Store a hyperbolic vector
        float[] vector = {0.1f, 0.2f, 0.3f};
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "hyperbolic");

        String id = storage.storeHyperbolicVector(TEST_COLLECTION, vector, true, metadata);
        assertNotNull(id);

        // Retrieve the vector
        VectorRecord record = storage.getVector(TEST_COLLECTION, id);
        assertNotNull(record);

        // Check the retrieved data
        assertArrayEquals(vector, record.getVector());
        assertEquals("hyperbolic", record.getMetadata().get("type"));
        assertEquals(VectorRecord.VectorType.HYPERBOLIC_POINCARE, record.getVectorType());
    }

    @Test
    public void testInvalidPoincareVector() {
        // Vector outside the Poincaré ball
        float[] invalidVector = {0.7f, 0.7f, 0.7f};
        Map<String, Object> metadata = new HashMap<>();

        // Should throw an exception
        assertThrows(IllegalArgumentException.class, () ->
                storage.storeHyperbolicVector(TEST_COLLECTION, invalidVector, true, metadata));
    }

    @Test
    public void testBatchOperations() {
        // Prepare batch data
        List<float[]> vectors = new ArrayList<>();
        List<Map<String, Object>> metadataList = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            vectors.add(new float[]{i * 0.1f, i * 0.2f, i * 0.3f});

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("index", i);
            metadataList.add(metadata);
        }

        // Store batch
        List<String> ids = storage.storeBatch(TEST_COLLECTION, vectors, metadataList);
        assertEquals(5, ids.size());

        // Retrieve batch
        List<VectorRecord> records = storage.getVectors(TEST_COLLECTION, ids);
        assertEquals(5, records.size());

        // Check each record
        for (int i = 0; i < records.size(); i++) {
            VectorRecord record = records.get(i);
            assertEquals(i, record.getMetadata().get("index"));
            assertArrayEquals(vectors.get(i), record.getVector());
        }
    }

    @Test
    public void testCollectionOperations() {
        // Create a new collection
        String newCollection = "new_collection";
        CollectionConfig config = CollectionConfig.createPoincare(
                5, CollectionConfig.StorageFormat.FLOAT16);

        assertTrue(storage.createCollection(newCollection, config));
        assertTrue(storage.collectionExists(newCollection));

        // Get config
        CollectionConfig retrievedConfig = storage.getCollectionConfig(newCollection);
        assertNotNull(retrievedConfig);
        assertEquals(5, retrievedConfig.getDimension());
        assertEquals(VectorRecord.VectorType.HYPERBOLIC_POINCARE, retrievedConfig.getVectorType());
        assertEquals(CollectionConfig.StorageFormat.FLOAT16, retrievedConfig.getStorageFormat());

        // List collections
        List<String> collections = storage.listCollections();
        assertTrue(collections.contains(TEST_COLLECTION));
        assertTrue(collections.contains(newCollection));

        // Delete collection
        assertTrue(storage.deleteCollection(newCollection));
        assertFalse(storage.collectionExists(newCollection));
    }

    @Test
    public void testMetadataOperations() {
        // Store a vector
        float[] vector = {0.1f, 0.2f, 0.3f};
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("tag", "original");

        String id = storage.storeVector(TEST_COLLECTION, vector, metadata);

        // Update metadata
        Map<String, Object> newMetadata = new HashMap<>();
        newMetadata.put("tag", "updated");
        newMetadata.put("version", 2);

        assertTrue(storage.updateMetadata(TEST_COLLECTION, id, newMetadata));

        // Check updated metadata
        VectorRecord record = storage.getVector(TEST_COLLECTION, id);
        assertEquals("updated", record.getMetadata().get("tag"));
        assertEquals(2, record.getMetadata().get("version"));

        // Search by metadata
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("tag", "updated");

        List<VectorRecord> results = storage.searchByMetadata(TEST_COLLECTION, searchCriteria);
        assertEquals(1, results.size());
        assertEquals(id, results.get(0).getId());
    }

    @Test
    public void testCollectionStats() {
        // Add some vectors
        for (int i = 0; i < 10; i++) {
            storage.storeVector(TEST_COLLECTION,
                    new float[]{i * 0.1f, i * 0.2f, i * 0.3f},
                    Collections.singletonMap("index", i));
        }

        // Get stats
        CollectionStats stats = storage.getCollectionStats(TEST_COLLECTION);
        assertNotNull(stats);

        assertEquals(TEST_COLLECTION, stats.getCollectionName());
        assertEquals(10, stats.getVectorCount());
        assertEquals(3, stats.getDimension());
        assertEquals(VectorRecord.VectorType.EUCLIDEAN, stats.getVectorType());

        // Size check (10 vectors * 3 dimensions * 4 bytes per float)
        assertEquals(120, stats.getTotalSizeBytes());
    }

    @Test
    public void testDeleteVector() {
        // Store a vector
        float[] vector = {0.1f, 0.2f, 0.3f};
        String id = storage.storeVector(TEST_COLLECTION, vector, null);

        // Verify it exists
        assertNotNull(storage.getVector(TEST_COLLECTION, id));

        // Delete it
        assertTrue(storage.deleteVector(TEST_COLLECTION, id));

        // Verify it's gone
        assertNull(storage.getVector(TEST_COLLECTION, id));

        // Try deleting again
        assertFalse(storage.deleteVector(TEST_COLLECTION, id));
    }

    @Test
    public void testAutomaticCollectionCreation() {
        // Try storing in a non-existent collection
        String newCollection = "auto_created";

        // Should create the collection automatically with default config
        String id = storage.storeVector(newCollection, new float[]{0.1f, 0.2f, 0.3f}, null);
        assertNotNull(id);

        // Verify the collection was created
        assertTrue(storage.collectionExists(newCollection));

        // Check the default config
        CollectionConfig config = storage.getCollectionConfig(newCollection);
        assertEquals(VectorRecord.VectorType.EUCLIDEAN, config.getVectorType());
        assertEquals(CollectionConfig.StorageFormat.FLOAT32, config.getStorageFormat());
    }

    @Test
    public void testDimensionValidation() {
        // Try storing a vector with wrong dimension
        float[] invalidVector = {0.1f, 0.2f, 0.3f, 0.4f};  // 4D vector in a 3D collection

        assertThrows(IllegalArgumentException.class, () ->
                storage.storeVector(TEST_COLLECTION, invalidVector, null));
    }
}