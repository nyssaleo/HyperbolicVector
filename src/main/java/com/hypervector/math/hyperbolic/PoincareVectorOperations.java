package com.hypervector.math.hyperbolic;

import com.hypervector.math.VectorOperations;
import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.euclidean.EuclideanVectorOperations;

/**
 * Implementation of vector operations in the Poincaré ball model of hyperbolic space.
 */
public class PoincareVectorOperations implements VectorOperations<PoincareVector> {

    private final EuclideanVectorOperations euclideanOps = new EuclideanVectorOperations();

    @Override
    public double distance(PoincareVector v1, PoincareVector v2) {
        assertSameDimension(v1, v2);
        assertSameCurvature(v1, v2);

        double curvature = Math.abs(v1.getCurvature());

        // Convert to Euclidean vectors for easier calculations
        EuclideanVector e1 = v1.toEuclidean();
        EuclideanVector e2 = v2.toEuclidean();

        // Calculate squared norms
        double x_norm_squared = e1.squaredNorm();
        double y_norm_squared = e2.squaredNorm();

        // Calculate squared distance between the points
        double squared_dist = euclideanOps.squaredDistance(e1, e2);

        // Calculate the hyperbolic distance using the Poincaré distance formula
        double numerator = 2 * squared_dist;
        double denominator = (1 - x_norm_squared) * (1 - y_norm_squared);
        double fraction = 1 + numerator / denominator;

        // Apply acosh (inverse hyperbolic cosine)
        return (2 / Math.sqrt(curvature)) * acosh(fraction);
    }

    @Override
    public PoincareVector normalize(PoincareVector vector) {
        // In the Poincaré ball model, normalization means rescaling the vector
        // to have a specific norm in hyperbolic space
        // Here we'll use a simple approach to normalize to a specific radius

        double targetNorm = 0.5; // Arbitrary target norm within the unit ball
        double currentNorm = vector.norm();

        if (currentNorm < 1e-10) {
            throw new IllegalArgumentException("Cannot normalize a zero vector");
        }

        double scale = targetNorm / currentNorm;
        double[] data = vector.getData();
        double[] normalized = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            normalized[i] = data[i] * scale;
        }

        return new PoincareVector(normalized, vector.getCurvature());
    }

    @Override
    public double innerProduct(PoincareVector v1, PoincareVector v2) {
        assertSameDimension(v1, v2);
        assertSameCurvature(v1, v2);

        double curvature = Math.abs(v1.getCurvature());

        // Convert to Euclidean vectors
        EuclideanVector e1 = v1.toEuclidean();
        EuclideanVector e2 = v2.toEuclidean();

        // Calculate the conformal factor
        double x_norm_squared = e1.squaredNorm();
        double y_norm_squared = e2.squaredNorm();

        double conformal_factor = 4 / ((1 - x_norm_squared) * (1 - y_norm_squared));

        // Calculate the Euclidean inner product
        double euclidean_product = euclideanOps.innerProduct(e1, e2);

        // Apply the Poincaré inner product formula
        return conformal_factor * euclidean_product;
    }

    @Override
    public PoincareVector add(PoincareVector v1, PoincareVector v2) {
        assertSameDimension(v1, v2);
        assertSameCurvature(v1, v2);

        // In hyperbolic space, addition is not the usual vector addition
        // We use the Möbius addition formula
        return mobiusAddition(v1, v2);
    }

    @Override
    public PoincareVector scale(PoincareVector vector, double scalar) {
        // Scaling in hyperbolic space is not simple multiplication
        // We use the hyperbolic scaling formula

        double[] data = vector.getData();
        double norm = vector.norm();

        if (norm < 1e-10) {
            return vector; // Zero vector remains zero
        }

        // Calculate the scaling factor using hyperbolic scaling formula
        double factor = (1.0 / Math.tanh(scalar * atanh(norm))) / norm;

        double[] scaled = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            scaled[i] = data[i] * factor;
        }

        // Ensure the result is still within the Poincaré ball
        double scaledNorm = 0.0;
        for (double v : scaled) {
            scaledNorm += v * v;
        }

        // If scaling would put us outside the ball, rescale to be inside
        if (scaledNorm >= 1.0) {
            double rescale = 0.99 / Math.sqrt(scaledNorm);
            for (int i = 0; i < scaled.length; i++) {
                scaled[i] *= rescale;
            }
        }

        return new PoincareVector(scaled, vector.getCurvature());
    }

    /**
     * Implementation of Möbius addition in the Poincaré ball model.
     *
     * @param x First vector
     * @param y Second vector
     * @return Result of Möbius addition
     */
    public PoincareVector mobiusAddition(PoincareVector x, PoincareVector y) {
        assertSameDimension(x, y);
        assertSameCurvature(x, y);

        // Convert to Euclidean vectors
        EuclideanVector ex = x.toEuclidean();
        EuclideanVector ey = y.toEuclidean();

        double[] xData = ex.getData();
        double[] yData = ey.getData();

        // Calculate squared norms
        double x_norm_squared = ex.squaredNorm();
        double y_norm_squared = ey.squaredNorm();

        // Calculate dot product
        double dot_product = euclideanOps.innerProduct(ex, ey);

        // Apply the Möbius addition formula
        double denom = 1 + 2 * dot_product + x_norm_squared * y_norm_squared;
        double[] result = new double[xData.length];

        for (int i = 0; i < result.length; i++) {
            double num1 = (1 + 2 * dot_product + y_norm_squared) * xData[i];
            double num2 = (1 - x_norm_squared) * yData[i];
            result[i] = (num1 + num2) / denom;
        }

        return new PoincareVector(result, x.getCurvature());
    }

    /**
     * Calculate the inverse of a vector under Möbius addition.
     *
     * @param x The vector to invert
     * @return The additive inverse in the Poincaré ball
     */
    public PoincareVector mobiusNegation(PoincareVector x) {
        double[] data = x.getData();
        double[] negated = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            negated[i] = -data[i];
        }

        return new PoincareVector(negated, x.getCurvature());
    }

    /**
     * Subtract one vector from another using Möbius addition.
     *
     * @param x First vector
     * @param y Second vector
     * @return Result of x - y in the Poincaré ball
     */
    public PoincareVector mobiusSubtraction(PoincareVector x, PoincareVector y) {
        return mobiusAddition(x, mobiusNegation(y));
    }

    /**
     * Compute the exponential map from the tangent space at the origin to the Poincaré ball.
     *
     * @param v Tangent vector at the origin
     * @return Corresponding point in the Poincaré ball
     */
    public PoincareVector exponentialMap(EuclideanVector v) {
        double norm = v.norm();

        if (norm < 1e-10) {
            return PoincareVector.zeros(v.getDimension());
        }

        double[] data = v.getData();
        double[] result = new double[data.length];

        // Scale factor from the exponential map formula
        double scale = Math.tanh(norm) / norm;

        for (int i = 0; i < data.length; i++) {
            result[i] = scale * data[i];
        }

        return new PoincareVector(result);
    }

    /**
     * Compute the logarithmic map from the Poincaré ball to the tangent space at the origin.
     *
     * @param x Point in the Poincaré ball
     * @return Corresponding tangent vector at the origin
     */
    public EuclideanVector logarithmicMap(PoincareVector x) {
        double norm = x.norm();

        if (norm < 1e-10) {
            return EuclideanVector.zeros(x.getDimension());
        }

        double[] data = x.getData();
        double[] result = new double[data.length];

        // Scale factor from the logarithmic map formula
        double scale = atanh(norm) / norm;

        for (int i = 0; i < data.length; i++) {
            result[i] = scale * data[i];
        }

        return new EuclideanVector(result);
    }

    /**
     * Calculate the inverse hyperbolic cosine of a value.
     *
     * @param x Input value (must be >= 1)
     * @return acosh(x)
     */
    private double acosh(double x) {
        if (x < 1.0) {
            throw new IllegalArgumentException("acosh requires x >= 1, got " + x);
        }
        return Math.log(x + Math.sqrt(x * x - 1));
    }

    /**
     * Calculate the inverse hyperbolic tangent of a value.
     *
     * @param x Input value (must be between -1 and 1)
     * @return atanh(x)
     */
    private double atanh(double x) {
        if (x <= -1.0 || x >= 1.0) {
            throw new IllegalArgumentException("atanh requires -1 < x < 1, got " + x);
        }
        return 0.5 * Math.log((1.0 + x) / (1.0 - x));
    }

    /**
     * Verify that two vectors have the same dimension.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @throws IllegalArgumentException if dimensions don't match
     */
    private void assertSameDimension(PoincareVector v1, PoincareVector v2) {
        if (v1.getDimension() != v2.getDimension()) {
            throw new IllegalArgumentException(
                    "Vector dimensions don't match: " + v1.getDimension() + " vs " + v2.getDimension());
        }
    }

    /**
     * Verify that two vectors have the same curvature.
     *
     * @param v1 First vector
     * @param v2 Second vector
     * @throws IllegalArgumentException if curvatures don't match
     */
    private void assertSameCurvature(PoincareVector v1, PoincareVector v2) {
        if (v1.getCurvature() != v2.getCurvature()) {
            throw new IllegalArgumentException(
                    "Vector curvatures don't match: " + v1.getCurvature() + " vs " + v2.getCurvature());
        }
    }
}