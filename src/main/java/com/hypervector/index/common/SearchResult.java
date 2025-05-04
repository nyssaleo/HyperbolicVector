package com.hypervector.index.common;

import com.hypervector.storage.common.VectorRecord;

/**
 * Represents a search result with vector information and distance.
 */
public class SearchResult implements Comparable<SearchResult> {
    private final String id;
    private final float[] vector;
    private final double distance;
    private final VectorRecord record;

    /**
     * Create a new search result.
     *
     * @param id Vector ID
     * @param vector Vector data
     * @param distance Distance to query vector
     * @param record Full vector record
     */
    public SearchResult(String id, float[] vector, double distance, VectorRecord record) {
        this.id = id;
        this.vector = vector;
        this.distance = distance;
        this.record = record;
    }

    /**
     * Get the vector ID.
     *
     * @return Vector ID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the vector data.
     *
     * @return Vector data
     */
    public float[] getVector() {
        return vector.clone();
    }

    /**
     * Get the distance to the query vector.
     *
     * @return Distance value
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Get the score (inverse of distance).
     * Higher score means better match.
     *
     * @return Score value
     */
    public double getScore() {
        return 1.0 / (1.0 + distance);
    }

    /**
     * Get the full vector record.
     *
     * @return Vector record
     */
    public VectorRecord getRecord() {
        return record;
    }

    @Override
    public int compareTo(SearchResult other) {
        // Sort by distance (ascending)
        return Double.compare(this.distance, other.distance);
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "id='" + id + '\'' +
                ", distance=" + distance +
                ", score=" + getScore() +
                '}';
    }
}