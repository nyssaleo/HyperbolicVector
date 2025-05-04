package com.hypervector.math.hyperbolic;

import com.hypervector.math.euclidean.EuclideanVector;

import java.util.Arrays;

/**
 * Represents a vector in the Poincaré ball model of hyperbolic space.
 * Points in the Poincaré ball are inside the unit ball in Euclidean space.
 */
public class PoincareVector {
    private final double[] data;
    private final double curvature;

    /**
     * Creates a new Poincaré ball vector with the given data and curvature.
     *
     * @param data The vector components
     * @param curvature The curvature of the hyperbolic space (usually -1)
     * @throws IllegalArgumentException if the norm of the vector is >= 1
     */
    public PoincareVector(double[] data, double curvature) {
        this.data = Arrays.copyOf(data, data.length);
        this.curvature = curvature;

        // Validate that the point is inside the Poincaré ball
        double squaredNorm = 0.0;
        for (double v : data) {
            squaredNorm += v * v;
        }

        if (squaredNorm >= 1.0) {
            throw new IllegalArgumentException("Vector lies outside the Poincaré ball (norm >= 1): " + Math.sqrt(squaredNorm));
        }
    }

    /**
     * Creates a new Poincaré ball vector with the given data and default curvature of -1.
     *
     * @param data The vector components
     * @throws IllegalArgumentException if the norm of the vector is >= 1
     */
    public PoincareVector(double[] data) {
        this(data, -1.0);
    }

    /**
     * Creates a zero vector in the Poincaré ball model.
     *
     * @param dimension The vector dimension
     * @param curvature The curvature of the hyperbolic space
     * @return A new zero vector
     */
    public static PoincareVector zeros(int dimension, double curvature) {
        return new PoincareVector(new double[dimension], curvature);
    }

    /**
     * Creates a zero vector in the Poincaré ball model with default curvature.
     *
     * @param dimension The vector dimension
     * @return A new zero vector
     */
    public static PoincareVector zeros(int dimension) {
        return zeros(dimension, -1.0);
    }

    /**
     * Creates a random vector in the Poincaré ball with the specified dimension.
     * The vector is guaranteed to be inside the unit ball.
     *
     * @param dimension The vector dimension
     * @param curvature The curvature of the hyperbolic space
     * @param maxNorm Maximum norm of the generated vector (must be < 1)
     * @return A new random vector
     */
    public static PoincareVector random(int dimension, double curvature, double maxNorm) {
        if (maxNorm >= 1.0) {
            throw new IllegalArgumentException("maxNorm must be less than 1");
        }

        // Generate random direction
        double[] data = new double[dimension];
        double squaredNorm = 0.0;

        for (int i = 0; i < dimension; i++) {
            data[i] = Math.random() * 2 - 1; // Random value between -1 and 1
            squaredNorm += data[i] * data[i];
        }

        // Normalize direction and scale to random norm less than maxNorm
        double norm = Math.sqrt(squaredNorm);
        double scale = Math.random() * maxNorm / norm;

        for (int i = 0; i < dimension; i++) {
            data[i] *= scale;
        }

        return new PoincareVector(data, curvature);
    }

    /**
     * Creates a random vector in the Poincaré ball with default curvature.
     *
     * @param dimension The vector dimension
     * @param maxNorm Maximum norm of the generated vector (must be < 1)
     * @return A new random vector
     */
    public static PoincareVector random(int dimension, double maxNorm) {
        return random(dimension, -1.0, maxNorm);
    }

    /**
     * Convert an Euclidean vector to a Poincaré vector.
     * This performs a simple scaling to ensure the vector lies within the unit ball.
     *
     * @param vector The Euclidean vector
     * @param curvature The curvature of the hyperbolic space
     * @return A new Poincaré vector
     */
    public static PoincareVector fromEuclidean(EuclideanVector vector, double curvature) {
        double[] data = vector.getData();
        double norm = vector.norm();

        if (norm < 1e-10) {
            return new PoincareVector(data, curvature);
        }

        // Scale to ensure it's inside the ball
        double scale = Math.min(0.9, 0.9 / norm);
        double[] scaled = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            scaled[i] = data[i] * scale;
        }

        return new PoincareVector(scaled, curvature);
    }

    /**
     * Convert to an Euclidean vector (simply uses the same coordinates).
     *
     * @return An Euclidean vector with the same coordinates
     */
    public EuclideanVector toEuclidean() {
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
     * Get the curvature of the hyperbolic space.
     *
     * @return The curvature
     */
    public double getCurvature() {
        return curvature;
    }

    /**
     * Calculate the squared Euclidean norm of this vector.
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
     * Calculate the Euclidean norm of this vector.
     *
     * @return The Euclidean norm
     */
    public double norm() {
        return Math.sqrt(squaredNorm());
    }

    @Override
    public String toString() {
        return "PoincareVector{" +
                "data=" + Arrays.toString(data) +
                ", curvature=" + curvature +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PoincareVector that = (PoincareVector) o;
        return Double.compare(that.curvature, curvature) == 0 &&
                Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(data);
        result = 31 * result + (int) Double.doubleToLongBits(curvature);
        return result;
    }
}