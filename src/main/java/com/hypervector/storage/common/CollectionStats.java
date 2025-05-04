package com.hypervector.storage.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Statistics for a vector collection.
 */
public class CollectionStats {
    private final String collectionName;
    private final int vectorCount;
    private final long totalSizeBytes;
    private final int dimension;
    private final VectorRecord.VectorType vectorType;
    private final Map<String, Object> additionalStats;

    /**
     * Create a new collection statistics object.
     *
     * @param collectionName Name of the collection
     * @param vectorCount Number of vectors in the collection
     * @param totalSizeBytes Total size in bytes
     * @param dimension Vector dimension
     * @param vectorType Vector type
     * @param additionalStats Additional statistics
     */
    public CollectionStats(String collectionName,
                           int vectorCount,
                           long totalSizeBytes,
                           int dimension,
                           VectorRecord.VectorType vectorType,
                           Map<String, Object> additionalStats) {
        this.collectionName = collectionName;
        this.vectorCount = vectorCount;
        this.totalSizeBytes = totalSizeBytes;
        this.dimension = dimension;
        this.vectorType = vectorType;
        this.additionalStats = additionalStats != null ?
                new HashMap<>(additionalStats) : new HashMap<>();
    }

    /**
     * Get the collection name.
     *
     * @return The collection name
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Get the number of vectors.
     *
     * @return Vector count
     */
    public int getVectorCount() {
        return vectorCount;
    }

    /**
     * Get the total size in bytes.
     *
     * @return Size in bytes
     */
    public long getTotalSizeBytes() {
        return totalSizeBytes;
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
     * Get additional statistics.
     *
     * @return A copy of the additional stats map
     */
    public Map<String, Object> getAdditionalStats() {
        return new HashMap<>(additionalStats);
    }

    /**
     * Get a specific additional statistic.
     *
     * @param key Statistic key
     * @return Statistic value or null if not found
     */
    public Object getStat(String key) {
        return additionalStats.get(key);
    }

    /**
     * Get the average bytes per vector.
     *
     * @return Average bytes per vector or 0 if no vectors
     */
    public double getAverageBytesPerVector() {
        return vectorCount > 0 ? (double) totalSizeBytes / vectorCount : 0;
    }

    /**
     * Get the collection size in a human-readable format.
     *
     * @return Human-readable size string
     */
    public String getHumanReadableSize() {
        if (totalSizeBytes < 1024) {
            return totalSizeBytes + " B";
        } else if (totalSizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", totalSizeBytes / 1024.0);
        } else if (totalSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", totalSizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", totalSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return "CollectionStats{" +
                "collectionName='" + collectionName + '\'' +
                ", vectorCount=" + vectorCount +
                ", size=" + getHumanReadableSize() +
                ", dimension=" + dimension +
                ", vectorType=" + vectorType +
                '}';
    }
}