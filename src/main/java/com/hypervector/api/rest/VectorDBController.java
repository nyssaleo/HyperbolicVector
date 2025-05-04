package com.hypervector.api.rest;

import com.hypervector.index.common.SearchResult;
import com.hypervector.index.common.VectorIndex;
import com.hypervector.storage.common.CollectionConfig;
import com.hypervector.storage.common.CollectionStats;
import com.hypervector.storage.common.VectorRecord;
import com.hypervector.storage.common.VectorStorage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for the Hyperbolic Vector Database API.
 */
@RestController
@RequestMapping("/api/v1")
public class VectorDBController {

    @Autowired
    private VectorStorage storage;

    @Autowired
    private VectorIndex index;

    /**
     * Get all collections.
     */
    @GetMapping("/collections")
    public ResponseEntity<List<String>> getCollections() {
        List<String> collections = storage.listCollections();
        return ResponseEntity.ok(collections);
    }

    /**
     * Get collection statistics.
     */
    @GetMapping("/collections/{collection}/stats")
    public ResponseEntity<CollectionStats> getCollectionStats(@PathVariable String collection) {
        if (!storage.collectionExists(collection)) {
            return ResponseEntity.notFound().build();
        }

        CollectionStats stats = storage.getCollectionStats(collection);
        return ResponseEntity.ok(stats);
    }

    /**
     * Create a new collection.
     */
    @PostMapping("/collections/{collection}")
    public ResponseEntity<Map<String, Object>> createCollection(
            @PathVariable String collection,
            @RequestBody CreateCollectionRequest request) {

        if (storage.collectionExists(collection)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Collection already exists"));
        }

        try {
            CollectionConfig config;

            switch (request.getVectorType().toLowerCase()) {
                case "euclidean":
                    config = CollectionConfig.createEuclidean(
                            request.getDimension(),
                            parseStorageFormat(request.getStorageFormat()));
                    break;

                case "poincare":
                    config = CollectionConfig.createPoincare(
                            request.getDimension(),
                            parseStorageFormat(request.getStorageFormat()));
                    break;

                default:
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Unsupported vector type: " + request.getVectorType()));
            }

            boolean created = storage.createCollection(collection, config);

            if (created) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Collection created successfully");
                response.put("collection", collection);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("Failed to create collection"));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid request: " + e.getMessage()));
        }
    }

    /**
     * Delete a collection.
     */
    @DeleteMapping("/collections/{collection}")
    public ResponseEntity<Map<String, Object>> deleteCollection(@PathVariable String collection) {
        if (!storage.collectionExists(collection)) {
            return ResponseEntity.notFound().build();
        }

        boolean deleted = storage.deleteCollection(collection);

        if (index.indexExists(collection)) {
            index.deleteIndex(collection);
        }

        if (deleted) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Collection deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete collection"));
        }
    }

    /**
     * Store a vector.
     */
    @PostMapping("/collections/{collection}/vectors")
    public ResponseEntity<Map<String, Object>> storeVector(
            @PathVariable String collection,
            @RequestBody StoreVectorRequest request) {

        if (!storage.collectionExists(collection)) {
            return ResponseEntity.notFound().build();
        }

        try {
            String id;

            // Check if this is a hyperbolic vector
            if (Boolean.TRUE.equals(request.getIsHyperbolic())) {
                id = storage.storeHyperbolicVector(
                        collection,
                        request.getVector(),
                        true, // Always use Poincaré ball model for hyperbolic vectors
                        request.getMetadata());
            } else {
                id = storage.storeVector(
                        collection,
                        request.getVector(),
                        request.getMetadata());
            }

            // Update index if it exists
            if (index.indexExists(collection)) {
                index.addToIndex(collection, List.of(id));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("id", id);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid request: " + e.getMessage()));
        }
    }

    /**
     * Get a vector by ID.
     */
    @GetMapping("/collections/{collection}/vectors/{id}")
    public ResponseEntity<VectorRecord> getVector(
            @PathVariable String collection,
            @PathVariable String id) {

        if (!storage.collectionExists(collection)) {
            return ResponseEntity.notFound().build();
        }

        VectorRecord record = storage.getVector(collection, id);

        if (record == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(record);
    }

    /**
     * Delete a vector.
     */
    @DeleteMapping("/collections/{collection}/vectors/{id}")
    public ResponseEntity<Map<String, Object>> deleteVector(
            @PathVariable String collection,
            @PathVariable String id) {

        if (!storage.collectionExists(collection)) {
            return ResponseEntity.notFound().build();
        }

        boolean deleted = storage.deleteVector(collection, id);

        if (deleted && index.indexExists(collection)) {
            index.removeFromIndex(collection, List.of(id));
        }

        if (deleted) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Vector deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Search for similar vectors.
     */
    @PostMapping("/collections/{collection}/search")
    public ResponseEntity<List<Map<String, Object>>> search(
            @PathVariable String collection,
            @RequestBody SearchRequest request) {

        if (!storage.collectionExists(collection)) {
            return ResponseEntity.notFound().build();
        }

        if (!index.indexExists(collection)) {
            // Build index if it doesn't exist
            index.buildIndex(collection, VectorIndex.IndexType.FLAT, null);
        }

        try {
            // Determine the space type
            VectorIndex.SpaceType spaceType = VectorIndex.SpaceType.EUCLIDEAN;
            if (Boolean.TRUE.equals(request.getIsHyperbolic())) {
                spaceType = VectorIndex.SpaceType.POINCARE_BALL;
            }

            // Perform search
            List<SearchResult> results = index.search(
                    collection,
                    request.getVector(),
                    request.getK(),
                    spaceType);

            // Convert to response format
            List<Map<String, Object>> response = results.stream()
                    .map(this::convertSearchResult)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Build an index for a collection.
     */
    @PostMapping("/collections/{collection}/index")
    public ResponseEntity<Map<String, Object>> buildIndex(
            @PathVariable String collection,
            @RequestBody BuildIndexRequest request) {

        if (!storage.collectionExists(collection)) {
            return ResponseEntity.notFound().build();
        }

        try {
            VectorIndex.IndexType indexType = VectorIndex.IndexType.FLAT;

            if (request.getIndexType() != null) {
                switch (request.getIndexType().toLowerCase()) {
                    case "flat":
                        indexType = VectorIndex.IndexType.FLAT;
                        break;
                    case "hnsw_euclidean":
                        indexType = VectorIndex.IndexType.HNSW_EUCLIDEAN;
                        break;
                    case "hnsw_hyperbolic":
                        indexType = VectorIndex.IndexType.HNSW_HYPERBOLIC;
                        break;
                    default:
                        return ResponseEntity.badRequest()
                                .body(createErrorResponse("Unsupported index type: " + request.getIndexType()));
                }
            }

            // Build the index
            index.buildIndex(collection, indexType, request.getParameters());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Index built successfully");
            response.put("stats", index.getIndexStats(collection));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to build index: " + e.getMessage()));
        }
    }

    /**
     * Convert a SearchResult to a map for JSON response.
     */
    private Map<String, Object> convertSearchResult(SearchResult result) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", result.getId());
        map.put("distance", result.getDistance());
        map.put("score", result.getScore());
        map.put("vector", result.getVector());
        map.put("metadata", result.getRecord().getMetadata());
        return map;
    }

    /**
     * Create an error response map.
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        return response;
    }

    /**
     * Parse storage format from string.
     */
    private CollectionConfig.StorageFormat parseStorageFormat(String formatStr) {
        if (formatStr == null) {
            return CollectionConfig.StorageFormat.FLOAT32;
        }

        switch (formatStr.toLowerCase()) {
            case "float32":
                return CollectionConfig.StorageFormat.FLOAT32;
            case "float16":
                return CollectionConfig.StorageFormat.FLOAT16;
            case "int8":
                return CollectionConfig.StorageFormat.INT8;
            case "nf4":
                return CollectionConfig.StorageFormat.NF4;
            default:
                throw new IllegalArgumentException("Unknown storage format: " + formatStr);
        }
    }

    /**
     * Request class for creating a collection.
     */
    public static class CreateCollectionRequest {
        private int dimension;
        private String vectorType;
        private String storageFormat;

        public int getDimension() {
            return dimension;
        }

        public void setDimension(int dimension) {
            this.dimension = dimension;
        }

        public String getVectorType() {
            return vectorType;
        }

        public void setVectorType(String vectorType) {
            this.vectorType = vectorType;
        }

        public String getStorageFormat() {
            return storageFormat;
        }

        public void setStorageFormat(String storageFormat) {
            this.storageFormat = storageFormat;
        }
    }

    /**
     * Request class for storing a vector.
     */
    public static class StoreVectorRequest {
        private float[] vector;
        private Map<String, Object> metadata;
        private Boolean isHyperbolic;

        public float[] getVector() {
            return vector;
        }

        public void setVector(float[] vector) {
            this.vector = vector;
        }

        public Map<String, Object> getMetadata() {
            return metadata != null ? metadata : new HashMap<>();
        }

        public void setMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
        }

        public Boolean getIsHyperbolic() {
            return isHyperbolic;
        }

        public void setIsHyperbolic(Boolean isHyperbolic) {
            this.isHyperbolic = isHyperbolic;
        }
    }

    /**
     * Request class for searching vectors.
     */
    public static class SearchRequest {
        private float[] vector;
        private int k = 10;
        private Boolean isHyperbolic;

        public float[] getVector() {
            return vector;
        }

        public void setVector(float[] vector) {
            this.vector = vector;
        }

        public int getK() {
            return k;
        }

        public void setK(int k) {
            this.k = k;
        }

        public Boolean getIsHyperbolic() {
            return isHyperbolic;
        }

        public void setIsHyperbolic(Boolean isHyperbolic) {
            this.isHyperbolic = isHyperbolic;
        }
    }

    /**
     * Request class for building an index.
     */
    public static class BuildIndexRequest {
        private String indexType;
        private Map<String, Object> parameters;

        public String getIndexType() {
            return indexType;
        }

        public void setIndexType(String indexType) {
            this.indexType = indexType;
        }

        public Map<String, Object> getParameters() {
            return parameters != null ? parameters : new HashMap<>();
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }
}