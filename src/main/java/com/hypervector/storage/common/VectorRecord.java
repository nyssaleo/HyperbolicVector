package com.hypervector.storage.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a stored vector record with its metadata.
 */
public class VectorRecord {
    private String id;
    private float[] vector;
    private Map<String, Object> metadata;
    private VectorType vectorType;
    private long creationTime;
    private long updateTime;

    /**
     * Enumeration of supported vector types.
     */
    public enum VectorType {
        EUCLIDEAN,
        HYPERBOLIC_POINCARE,
        HYPERBOLIC_LORENTZ
    }

    /**
     * Create a new vector record.
     *
     * @param id Vector ID
     * @param vector Vector data
     * @param metadata Vector metadata
     * @param vectorType Type of vector geometry
     */
    public VectorRecord(String id, float[] vector, Map<String, Object> metadata, VectorType vectorType) {
        this.id = id;
        this.vector = vector;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.vectorType = vectorType;
        this.creationTime = System.currentTimeMillis();
        this.updateTime = this.creationTime;
    }

    /**
     * Create a new Euclidean vector record with a generated ID.
     *
     * @param vector Vector data
     * @param metadata Vector metadata
     * @return A new vector record
     */
    public static VectorRecord createEuclidean(float[] vector, Map<String, Object> metadata) {
        return new VectorRecord(UUID.randomUUID().toString(), vector, metadata, VectorType.EUCLIDEAN);
    }

    /**
     * Create a new Poincaré ball vector record with a generated ID.
     *
     * @param vector Vector data
     * @param metadata Vector metadata
     * @return A new vector record
     */
    public static VectorRecord createPoincare(float[] vector, Map<String, Object> metadata) {
        return new VectorRecord(UUID.randomUUID().toString(), vector, metadata, VectorType.HYPERBOLIC_POINCARE);
    }

    /**
     * Get the vector ID.
     *
     * @return The ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the vector data.
     *
     * @return A copy of the vector data
     */
    public float[] getVector() {
        return vector.clone();
    }

    /**
     * Get the vector metadata.
     *
     * @return A copy of the metadata map
     */
    public Map<String, Object> getMetadata() {
        return new HashMap<>(metadata);
    }

    /**
     * Get the vector type.
     *
     * @return The vector type
     */
    public VectorType getVectorType() {
        return vectorType;
    }

    /**
     * Get the creation timestamp.
     *
     * @return Creation time in milliseconds since epoch
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Get the last update timestamp.
     *
     * @return Update time in milliseconds since epoch
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * Update the vector metadata.
     *
     * @param metadata New metadata
     */
    public void updateMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.updateTime = System.currentTimeMillis();
    }

    /**
     * Update the vector data.
     *
     * @param vector New vector data
     */
    public void updateVector(float[] vector) {
        this.vector = vector.clone();
        this.updateTime = System.currentTimeMillis();
    }

    /**
     * Get the dimension of the vector.
     *
     * @return The vector dimension
     */
    public int getDimension() {
        return vector.length;
    }

    @Override
    public String toString() {
        return "VectorRecord{" +
                "id='" + id + '\'' +
                ", dimension=" + getDimension() +
                ", type=" + vectorType +
                ", metadataKeys=" + metadata.keySet() +
                '}';
    }
}