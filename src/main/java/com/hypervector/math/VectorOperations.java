package com.hypervector.math;

/**
 * Interface defining common vector operations regardless of geometry.
 */
public interface VectorOperations<T> {

    /**
     * Calculate the distance between two vectors.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return Distance between the vectors based on the specific metric
     */
    double distance(T v1, T v2);

    /**
     * Normalize the vector according to the space rules.
     *
     * @param vector The vector to normalize
     * @return Normalized vector
     */
    T normalize(T vector);

    /**
     * Compute the inner product of two vectors.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return Inner product value
     */
    double innerProduct(T v1, T v2);

    /**
     * Add two vectors.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return Result of addition
     */
    T add(T v1, T v2);

    /**
     * Scale a vector by a scalar value.
     *
     * @param vector The vector to scale
     * @param scalar The scalar value
     * @return Scaled vector
     */
    T scale(T vector, double scalar);
}