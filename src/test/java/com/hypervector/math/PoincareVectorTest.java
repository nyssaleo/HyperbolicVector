package com.hypervector.math;

import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.euclidean.EuclideanVectorOperations;
import com.hypervector.math.hyperbolic.PoincareVector;
import com.hypervector.math.hyperbolic.PoincareVectorOperations;
import com.hypervector.math.conversion.VectorSpaceConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

public class PoincareVectorTest {

    private PoincareVectorOperations poincareOps;
    private EuclideanVectorOperations euclideanOps;
    private VectorSpaceConverter converter;

    @BeforeEach
    public void setUp() {
        poincareOps = new PoincareVectorOperations();
        euclideanOps = new EuclideanVectorOperations();
        converter = new VectorSpaceConverter();
    }

    @Test
    public void testPoincareVectorCreation() {
        // Test valid creation
        double[] data = {0.2, 0.3, 0.1};
        PoincareVector v = new PoincareVector(data);

        assertEquals(3, v.getDimension());
        assertEquals(Math.sqrt(0.2*0.2 + 0.3*0.3 + 0.1*0.1), v.norm(), 1e-10);

        // Test invalid creation (outside ball)
        double[] invalidData = {0.7, 0.7, 0.7};
        assertThrows(IllegalArgumentException.class, () -> new PoincareVector(invalidData));
    }

    @Test
    public void testPoincareDistance() {
        PoincareVector v1 = new PoincareVector(new double[]{0.1, 0.2, 0.0});
        PoincareVector v2 = new PoincareVector(new double[]{0.2, 0.1, 0.0});

        double distance = poincareOps.distance(v1, v2);
        assertTrue(distance > 0);

        // Distance to self should be zero
        assertEquals(0.0, poincareOps.distance(v1, v1), 1e-10);

        // Distance should be symmetric
        assertEquals(distance, poincareOps.distance(v2, v1), 1e-10);
    }

    @Test
    public void testMobiusAddition() {
        PoincareVector v1 = new PoincareVector(new double[]{0.1, 0.0, 0.0});
        PoincareVector v2 = new PoincareVector(new double[]{0.0, 0.1, 0.0});

        PoincareVector sum = poincareOps.mobiusAddition(v1, v2);

        // Result should be inside the ball
        assertTrue(sum.norm() < 1.0);

        // Möbius addition is not commutative, so test both directions
        PoincareVector sum2 = poincareOps.mobiusAddition(v2, v1);
        assertNotEquals(sum, sum2);

        // But both results should be inside the ball
        assertTrue(sum2.norm() < 1.0);
    }

    @Test
    public void testMobiusNegation() {
        PoincareVector v = new PoincareVector(new double[]{0.3, 0.4, 0.0});
        PoincareVector negV = poincareOps.mobiusNegation(v);

        // Simple case: negation should flip the sign of each component
        assertEquals(-0.3, negV.get(0), 1e-10);
        assertEquals(-0.4, negV.get(1), 1e-10);
        assertEquals(0.0, negV.get(2), 1e-10);

        // Adding a vector and its negation should give an approximate zero vector
        PoincareVector sum = poincareOps.mobiusAddition(v, negV);
        assertEquals(0.0, sum.norm(), 1e-10);
    }

    @Test
    public void testEuclideanToPoincareConversion() {
        EuclideanVector eVec = new EuclideanVector(new double[]{2.0, 3.0, 4.0});
        double maxRadius = 0.9;

        PoincareVector pVec = converter.euclideanToPoincare(eVec, maxRadius, -1.0);

        // Result should be inside the ball
        assertTrue(pVec.norm() < maxRadius);

        // Direction should be preserved
        assertEquals(eVec.get(0) / eVec.get(1), pVec.get(0) / pVec.get(1), 1e-10);

        // Convert back
        EuclideanVector eVec2 = converter.poincareToEuclidean(pVec, maxRadius);

        // Direction should be preserved in round-trip
        assertEquals(eVec.get(0) / eVec.get(1), eVec2.get(0) / eVec2.get(1), 1e-10);
        assertEquals(eVec.get(1) / eVec.get(2), eVec2.get(1) / eVec2.get(2), 1e-10);

        // Norm should be approximately preserved for small vectors
        EuclideanVector smallVec = new EuclideanVector(new double[]{0.1, 0.2, 0.05});
        PoincareVector pSmallVec = converter.euclideanToPoincare(smallVec, maxRadius, -1.0);
        EuclideanVector smallVec2 = converter.poincareToEuclidean(pSmallVec, maxRadius);

        assertEquals(smallVec.norm(), smallVec2.norm(), 1e-3);
    }

    @Test
    public void testBatchConversion() {
        EuclideanVector[] eVectors = {
                new EuclideanVector(new double[]{1.0, 0.0, 0.0}),
                new EuclideanVector(new double[]{0.0, 1.0, 0.0}),
                new EuclideanVector(new double[]{0.0, 0.0, 1.0})
        };

        double maxRadius = 0.8;
        PoincareVector[] pVectors = converter.batchConvertEuclideanToPoincare(eVectors, maxRadius, -1.0);

        // Check distance preservation ratios
        double eDistance12 = euclideanOps.distance(eVectors[0], eVectors[1]);
        double eDistance23 = euclideanOps.distance(eVectors[1], eVectors[2]);
        double eRatio = eDistance12 / eDistance23;

        double pDistance12 = poincareOps.distance(pVectors[0], pVectors[1]);
        double pDistance23 = poincareOps.distance(pVectors[1], pVectors[2]);
        double pRatio = pDistance12 / pDistance23;

        // Ratios should be approximately preserved
        assertEquals(eRatio, pRatio, 0.1);

        // All vectors should be inside the ball
        for (PoincareVector p : pVectors) {
            assertTrue(p.norm() < maxRadius);
        }
    }

    @Test
    public void testExponentialAndLogarithmicMaps() {
        // Create a tangent vector in Euclidean space
        EuclideanVector tangentVector = new EuclideanVector(new double[]{0.5, 0.5, 0.5});

        // Map to the Poincaré ball
        PoincareVector ballPoint = poincareOps.exponentialMap(tangentVector);

        // Map back to the tangent space
        EuclideanVector tangentVector2 = poincareOps.logarithmicMap(ballPoint);

        // Check if round-trip preserves the vector
        for (int i = 0; i < tangentVector.getDimension(); i++) {
            assertEquals(tangentVector.get(i), tangentVector2.get(i), 1e-10);
        }
    }
}