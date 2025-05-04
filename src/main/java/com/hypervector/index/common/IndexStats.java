package com.hypervector.index.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Statistics and information about a vector index.
 */
public class IndexStats {
    private final String collectionName;
    private final VectorIndex.IndexType indexType;
    private final VectorIndex.SpaceType spaceType;
    private final int vectorCount;
    private final int dimension;
    private final long indexSizeBytes;
    private final long buildTimeMs;
    private final Map<String, Object> parameters;
    private final Map<String, Object> metrics;

    /**
     * Create a new index statistics object.
     *
     * @param collectionName Collection name
     * @param indexType Type of index
     * @param spaceType Type of space
     * @param vectorCount Number of vectors indexed
     * @param dimension Vector dimension
     * @param indexSizeBytes Size of the index in bytes
     * @param buildTimeMs Time taken to build the index
     * @param parameters Index parameters
     * @param metrics Performance metrics
     */
    public IndexStats(String collectionName,
                      VectorIndex.IndexType indexType,
                      VectorIndex.SpaceType spaceType,
                      int vectorCount,
                      int dimension,
                      long indexSizeBytes,
                      long buildTimeMs,
                      Map<String, Object> parameters,
                      Map<String, Object> metrics) {
        this.collectionName = collectionName;
        this.indexType = indexType;
        this.spaceType = spaceType;
        this.vectorCount = vectorCount;
        this.dimension = dimension;
        this.indexSizeBytes = indexSizeBytes;
        this.buildTimeMs = buildTimeMs;
        this.parameters = new HashMap<>(parameters);
        this.metrics = new HashMap<>(metrics);
    }

    /**
     * Get the collection name.
     *
     * @return Collection name
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Get the index type.
     *
     * @return Index type
     */
    public VectorIndex.IndexType getIndexType() {
        return indexType;
    }

    /**
     * Get the space type.
     *
     * @return Space type
     */
    public VectorIndex.SpaceType getSpaceType() {
        return spaceType;
    }

    /**
     * Get the number of vectors in the index.
     *
     * @return Vector count
     */
    public int getVectorCount() {
        return vectorCount;
    }

    /**
     * Get the vector dimension.
     *
     * @return Dimension
     */
    public int getDimension() {
        return dimension;
    }

    /**
     * Get the index size in bytes.
     *
     * @return Size in bytes
     */
    public long getIndexSizeBytes() {
        return indexSizeBytes;
    }

    /**
     * Get the time taken to build the index.
     *
     * @return Build time in milliseconds
     */
    public long getBuildTimeMs() {
        return buildTimeMs;
    }

    /**
     * Get the index parameters.
     *
     * @return Parameters map
     */
    public Map<String, Object> getParameters() {
        return new HashMap<>(parameters);
    }

    /**
     * Get the performance metrics.
     *
     * @return Metrics map
     */
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }

    /**
     * Get a specific parameter.
     *
     * @param key Parameter key
     * @return Parameter value or null if not found
     */
    public Object getParameter(String key) {
        return parameters.get(key);
    }

    /**
     * Get a specific metric.
     *
     * @param key Metric key
     * @return Metric value or null if not found
     */
    public Object getMetric(String key) {
        return metrics.get(key);
    }

    /**
     * Get the index size in a human-readable format.
     *
     * @return Human-readable size string
     */
    public String getHumanReadableSize() {
        if (indexSizeBytes < 1024) {
            return indexSizeBytes + " B";
        } else if (indexSizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", indexSizeBytes / 1024.0);
        } else if (indexSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", indexSizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", indexSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Get the build time in a human-readable format.
     *
     * @return Human-readable time string
     */
    public String getHumanReadableBuildTime() {
        if (buildTimeMs < 1000) {
            return buildTimeMs + " ms";
        } else if (buildTimeMs < 60 * 1000) {
            return String.format("%.2f sec", buildTimeMs / 1000.0);
        } else {
            return String.format("%.2f min", buildTimeMs / (60 * 1000.0));
        }
    }

    @Override
    public String toString() {
        return "IndexStats{" +
                "collection='" + collectionName + '\'' +
                ", type=" + indexType +
                ", spaceType=" + spaceType +
                ", vectors=" + vectorCount +
                ", dimension=" + dimension +
                ", size=" + getHumanReadableSize() +
                ", buildTime=" + getHumanReadableBuildTime() +
                '}';
    }

    /**
     * Builder for creating IndexStats objects.
     */
    public static class Builder {
        private String collectionName;
        private VectorIndex.IndexType indexType;
        private VectorIndex.SpaceType spaceType;
        private int vectorCount;
        private int dimension;
        private long indexSizeBytes;
        private long buildTimeMs;
        private Map<String, Object> parameters = new HashMap<>();
        private Map<String, Object> metrics = new HashMap<>();

        public Builder setCollectionName(String collectionName) {
            this.collectionName = collectionName;
            return this;
        }

        public Builder setIndexType(VectorIndex.IndexType indexType) {
            this.indexType = indexType;
            return this;
        }

        public Builder setSpaceType(VectorIndex.SpaceType spaceType) {
            this.spaceType = spaceType;
            return this;
        }

        public Builder setVectorCount(int vectorCount) {
            this.vectorCount = vectorCount;
            return this;
        }

        public Builder setDimension(int dimension) {
            this.dimension = dimension;
            return this;
        }

        public Builder setIndexSizeBytes(long indexSizeBytes) {
            this.indexSizeBytes = indexSizeBytes;
            return this;
        }

        public Builder setBuildTimeMs(long buildTimeMs) {
            this.buildTimeMs = buildTimeMs;
            return this;
        }

        public Builder setParameters(Map<String, Object> parameters) {
            this.parameters = new HashMap<>(parameters);
            return this;
        }

        public Builder addParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        public Builder setMetrics(Map<String, Object> metrics) {
            this.metrics = new HashMap<>(metrics);
            return this;
        }

        public Builder addMetric(String key, Object value) {
            this.metrics.put(key, value);
            return this;
        }

        public IndexStats build() {
            return new IndexStats(
                    collectionName,
                    indexType,
                    spaceType,
                    vectorCount,
                    dimension,
                    indexSizeBytes,
                    buildTimeMs,
                    parameters,
                    metrics
            );
        }
    }
}