package com.hypervector.math.euclidean;

import java.util.Arrays;

/**
 * Represents a vector in Euclidean space.
 */
public class EuclideanVector {
    private final double[] data;

    /**
     * Creates a new vector with the given data.
     *
     * @param data The vector components
     */
    public EuclideanVector(double[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Creates a zero vector of the specified dimension.
     *
     * @param dimension The vector dimension
     * @return A new zero vector
     */
    public static EuclideanVector zeros(int dimension) {
        return new EuclideanVector(new double[dimension]);
    }

    /**
     * Creates a random vector of the specified dimension.
     *
     * @param dimension The vector dimension
     * @return A new random vector with values between 0 and 1
     */
    public static EuclideanVector random(int dimension) {
        double[] data = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            data[i] = Math.random();
        }
        return new EuclideanVector(data);
    }

    /**
     * Get the dimension of this vector.
     *
     * @return The dimension
     */
    public int getDimension() {
        return data.length;
    }

    /**
     * Get the underlying data array.
     *
     * @return A copy of the vector data
     */
    public double[] getData() {
        return Arrays.copyOf(data, data.length);
    }

    /**
     * Get a specific component of the vector.
     *
     * @param index The component index
     * @return The value at the specified index
     */
    public double get(int index) {
        return data[index];
    }

    /**
     * Calculate the squared norm (L2 norm squared) of this vector.
     *
     * @return The squared norm
     */
    public double squaredNorm() {
        double sum = 0.0;
        for (double v : data) {
            sum += v * v;
        }
        return sum;
    }

    /**
     * Calculate the norm (L2 norm) of this vector.
     *
     * @return The Euclidean norm
     */
    public double norm() {
        return Math.sqrt(squaredNorm());
    }

    @Override
    public String toString() {
        return Arrays.toString(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EuclideanVector that = (EuclideanVector) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }
}