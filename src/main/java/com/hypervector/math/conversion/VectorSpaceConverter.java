package com.hypervector.math.conversion;

import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.euclidean.EuclideanVectorOperations;
import com.hypervector.math.hyperbolic.PoincareVector;
import com.hypervector.math.hyperbolic.PoincareVectorOperations;

/**
 * Utility class for converting vectors between different geometric spaces.
 */
public class VectorSpaceConverter {

    private final EuclideanVectorOperations euclideanOps = new EuclideanVectorOperations();
    private final PoincareVectorOperations poincareOps = new PoincareVectorOperations();

    /**
     * Convert an Euclidean vector to a point in the Poincaré ball.
     * This uses a distance-preserving mapping that maintains relative distances
     * as much as possible for vectors near the origin.
     *
     * @param vector The Euclidean vector to convert
     * @param maxRadius Maximum radius in the Poincaré ball (must be < 1)
     * @param curvature The curvature of the hyperbolic space
     * @return The corresponding Poincaré vector
     */
    public PoincareVector euclideanToPoincare(EuclideanVector vector, double maxRadius, double curvature) {
        if (maxRadius >= 1.0 || maxRadius <= 0.0) {
            throw new IllegalArgumentException("maxRadius must be between 0 and 1");
        }

        double[] data = vector.getData();
        double norm = vector.norm();

        if (norm < 1e-10) {
            return PoincareVector.zeros(vector.getDimension(), curvature);
        }

        // Use arctan to map [0, infinity) -> [0, pi/2)
        // Then scale to [0, maxRadius)
        double scale = maxRadius * (2 / Math.PI) * Math.atan(norm) / norm;

        double[] poincareData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            poincareData[i] = data[i] * scale;
        }

        return new PoincareVector(poincareData, curvature);
    }

    /**
     * Convert a Poincaré ball vector to an Euclidean vector.
     * This attempts to invert the euclideanToPoincare transformation.
     *
     * @param vector The Poincaré vector to convert
     * @param maxRadius The maximum radius used in the original conversion
     * @return The corresponding Euclidean vector
     */
    public EuclideanVector poincareToEuclidean(PoincareVector vector, double maxRadius) {
        if (maxRadius >= 1.0 || maxRadius <= 0.0) {
            throw new IllegalArgumentException("maxRadius must be between 0 and 1");
        }

        double[] data = vector.getData();
        double norm = vector.norm();

        if (norm < 1e-10) {
            return EuclideanVector.zeros(vector.getDimension());
        }

        // Invert the arctan mapping
        // tan(pi * norm / (2 * maxRadius)) * norm / norm
        double scale = Math.tan(Math.PI * norm / (2 * maxRadius)) / norm;

        double[] euclideanData = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            euclideanData[i] = data[i] * scale;
        }

        return new EuclideanVector(euclideanData);
    }

    /**
     * Convert between Euclidean and Poincaré spaces while preserving relative distances.
     * This method finds vectors in the target space that maintain the same distance
     * relationships as the original vectors in the source space.
     *
     * @param vectors Array of Euclidean vectors to convert
     * @param maxRadius Maximum radius in the Poincaré ball
     * @param curvature The curvature of the hyperbolic space
     * @return Array of corresponding Poincaré vectors
     */
    public PoincareVector[] batchConvertEuclideanToPoincare(EuclideanVector[] vectors,
                                                            double maxRadius,
                                                            double curvature) {
        if (vectors.length == 0) {
            return new PoincareVector[0];
        }

        // Find the maximum distance between any two vectors
        double maxDistance = 0.0;
        for (int i = 0; i < vectors.length; i++) {
            for (int j = i + 1; j < vectors.length; j++) {
                double dist = euclideanOps.distance(vectors[i], vectors[j]);
                maxDistance = Math.max(maxDistance, dist);
            }
        }

        // Scale factor to map the largest distance to the target hyperbolic distance
        double scaleFactor = 1.0;
        if (maxDistance > 0) {
            scaleFactor = (maxRadius * 0.9) / maxDistance;
        }

        // Convert each vector
        PoincareVector[] result = new PoincareVector[vectors.length];
        for (int i = 0; i < vectors.length; i++) {
            EuclideanVector scaled = euclideanOps.scale(vectors[i], scaleFactor);
            result[i] = euclideanToPoincare(scaled, maxRadius, curvature);
        }

        return result;
    }

    /**
     * Batch convert from Poincaré to Euclidean space.
     *
     * @param vectors Array of Poincaré vectors to convert
     * @param maxRadius The maximum radius used in the original conversion
     * @return Array of corresponding Euclidean vectors
     */
    public EuclideanVector[] batchConvertPoincareToEuclidean(PoincareVector[] vectors, double maxRadius) {
        EuclideanVector[] result = new EuclideanVector[vectors.length];

        for (int i = 0; i < vectors.length; i++) {
            result[i] = poincareToEuclidean(vectors[i], maxRadius);
        }

        return result;
    }
}