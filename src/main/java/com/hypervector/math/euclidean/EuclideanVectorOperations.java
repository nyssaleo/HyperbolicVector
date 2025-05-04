package com.hypervector.math.euclidean;

import com.hypervector.math.VectorOperations;

/**
 * Implementation of vector operations in Euclidean space.
 */
public class EuclideanVectorOperations implements VectorOperations<EuclideanVector> {

    @Override
    public double distance(EuclideanVector v1, EuclideanVector v2) {
        assertSameDimension(v1, v2);

        double sum = 0.0;
        for (int i = 0; i < v1.getDimension(); i++) {
            double diff = v1.get(i) - v2.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    @Override
    public EuclideanVector normalize(EuclideanVector vector) {
        double norm = vector.norm();
        if (norm < 1e-10) {
            throw new IllegalArgumentException("Cannot normalize a zero vector");
        }

        double[] data = vector.getData();
        double[] normalized = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            normalized[i] = data[i] / norm;
        }

        return new EuclideanVector(normalized);
    }

    @Override
    public double innerProduct(EuclideanVector v1, EuclideanVector v2) {
        assertSameDimension(v1, v2);

        double sum = 0.0;
        for (int i = 0; i < v1.getDimension(); i++) {
            sum += v1.get(i) * v2.get(i);
        }
        return sum;
    }

    @Override
    public EuclideanVector add(EuclideanVector v1, EuclideanVector v2) {
        assertSameDimension(v1, v2);

        double[] result = new double[v1.getDimension()];
        for (int i = 0; i < result.length; i++) {
            result[i] = v1.get(i) + v2.get(i);
        }

        return new EuclideanVector(result);
    }

    @Override
    public EuclideanVector scale(EuclideanVector vector, double scalar) {
        double[] data = vector.getData();
        double[] scaled = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            scaled[i] = data[i] * scalar;
        }

        return new EuclideanVector(scaled);
    }

    /**
     * Subtract one vector from another.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return Result of v1 - v2
     */
    public EuclideanVector subtract(EuclideanVector v1, EuclideanVector v2) {
        assertSameDimension(v1, v2);

        double[] result = new double[v1.getDimension()];
        for (int i = 0; i < result.length; i++) {
            result[i] = v1.get(i) - v2.get(i);
        }

        return new EuclideanVector(result);
    }

    /**
     * Calculate the squared distance between two vectors.
     * This is more efficient when only comparing distances.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @return Squared distance between the vectors
     */
    public double squaredDistance(EuclideanVector v1, EuclideanVector v2) {
        assertSameDimension(v1, v2);

        double sum = 0.0;
        for (int i = 0; i < v1.getDimension(); i++) {
            double diff = v1.get(i) - v2.get(i);
            sum += diff * diff;
        }
        return sum;
    }

    /**
     * Verify that two vectors have the same dimension.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @throws IllegalArgumentException if dimensions don't match
     */
    private void assertSameDimension(EuclideanVector v1, EuclideanVector v2) {
        if (v1.getDimension() != v2.getDimension()) {
            throw new IllegalArgumentException(
                    "Vector dimensions don't match: " + v1.getDimension() + " vs " + v2.getDimension());
        }
    }
}