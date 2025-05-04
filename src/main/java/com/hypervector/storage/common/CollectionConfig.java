package com.hypervector.storage.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for a vector collection.
 */
public class CollectionConfig {
    private final int dimension;
    private final VectorRecord.VectorType vectorType;
    private final boolean enableCompression;
    private final StorageFormat storageFormat;
    private final Map<String, Object> additionalConfig;

    /**
     * Storage format for vectors.
     */
    public enum StorageFormat {
        FLOAT32,    // 32-bit floating point (4 bytes per dimension)
        FLOAT16,    // 16-bit floating point (2 bytes per dimension)
        INT8,       // 8-bit integer quantization (1 byte per dimension)
        NF4         // 4-bit normalized float (0.5 byte per dimension)
    }

    /**
     * Create a new collection configuration.
     *
     * @param dimension Vector dimension
     * @param vectorType Type of vectors
     * @param enableCompression Whether to enable compression
     * @param storageFormat Format for vector storage
     * @param additionalConfig Additional configuration parameters
     */
    public CollectionConfig(int dimension,
                            VectorRecord.VectorType vectorType,
                            boolean enableCompression,
                            StorageFormat storageFormat,
                            Map<String, Object> additionalConfig) {
        if (dimension <= 0) {
            throw new IllegalArgumentException("Dimension must be positive");
        }

        this.dimension = dimension;
        this.vectorType = vectorType;
        this.enableCompression = enableCompression;
        this.storageFormat = storageFormat;
        this.additionalConfig = additionalConfig != null ?
                new HashMap<>(additionalConfig) : new HashMap<>();
    }

    /**
     * Create a new Euclidean collection configuration.
     *
     * @param dimension Vector dimension
     * @param storageFormat Format for vector storage
     * @return A new collection configuration
     */
    public static CollectionConfig createEuclidean(int dimension, StorageFormat storageFormat) {
        return new CollectionConfig(
                dimension,
                VectorRecord.VectorType.EUCLIDEAN,
                false,
                storageFormat,
                null
        );
    }

    /**
     * Create a new Poincaré ball collection configuration.
     *
     * @param dimension Vector dimension
     * @param storageFormat Format for vector storage
     * @return A new collection configuration
     */
    public static CollectionConfig createPoincare(int dimension, StorageFormat storageFormat) {
        Map<String, Object> config = new HashMap<>();
        config.put("curvature", -1.0);

        return new CollectionConfig(
                dimension,
                VectorRecord.VectorType.HYPERBOLIC_POINCARE,
                false,
                storageFormat,
                config
        );
    }

    /**
     * Get the vector dimension.
     *
     * @return The dimension
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Get the vector type.
     *
     * @return The vector type
     */
    public VectorRecord.VectorType getVectorType() {
        return vectorType;
    }

    /**
     * Check if compression is enabled.
     *
     * @return true if compression is enabled
     */
    public boolean isCompressionEnabled() {
        return enableCompression;
    }

    /**
     * Get the storage format.
     *
     * @return The storage format
     */
    public StorageFormat getStorageFormat() {
        return storageFormat;
    }

    /**
     * Get additional configuration parameters.
     *
     * @return A copy of the additional config map
     */
    public Map<String, Object> getAdditionalConfig() {
        return new HashMap<>(additionalConfig);
    }

    /**
     * Get a specific additional configuration parameter.
     *
     * @param key Parameter key
     * @return Parameter value or null if not found
     */
    public Object getConfigParam(String key) {
        return additionalConfig.get(key);
    }

    /**
     * Get the size in bytes required to store a single vector.
     *
     * @return Size in bytes
     */
    public int getVectorSizeInBytes() {
        switch (storageFormat) {
            case FLOAT32:
                return dimension * 4;
            case FLOAT16:
                return dimension * 2;
            case INT8:
                return dimension;
            case NF4:
                return (dimension + 1) / 2;  // Round up for odd dimensions
            default:
                throw new IllegalStateException("Unknown storage format: " + storageFormat);
        }
    }

    @Override
    public String toString() {
        return "CollectionConfig{" +
                "dimension=" + dimension +
                ", vectorType=" + vectorType +
                ", enableCompression=" + enableCompression +
                ", storageFormat=" + storageFormat +
                ", additionalConfig=" + additionalConfig +
                '}';
    }
}