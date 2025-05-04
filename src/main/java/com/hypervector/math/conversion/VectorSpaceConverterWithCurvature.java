package com.hypervector.math.conversion;

import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.euclidean.EuclideanVectorOperations;
import com.hypervector.math.hyperbolic.PoincareVector;
import com.hypervector.math.hyperbolic.PoincareVectorOperations;
import com.hypervector.math.hyperbolic.learning.CurvatureLearner;
import com.hypervector.math.hyperbolic.learning.CurvatureLearnerFactory;

/**
 * Extension of VectorSpaceConverter that supports adaptive curvature.
 */
public class VectorSpaceConverterWithCurvature extends VectorSpaceConverter {
    
    private final CurvatureLearner curvatureLearner;
    private double currentCurvature;
    
    /**
     * Create a converter with a specified curvature learner.
     * 
     * @param learner The curvature learner to use
     */
    public VectorSpaceConverterWithCurvature(CurvatureLearner learner) {
        this.curvatureLearner = learner;
        this.currentCurvature = learner.getDefaultCurvature();
    }
    
    /**
     * Create a converter with the default curvature learner.
     */
    public VectorSpaceConverterWithCurvature() {
        this(CurvatureLearnerFactory.createDefaultLearner());
    }
    
    /**
     * Convert an Euclidean vector to Poincare using the current learned curvature.
     * 
     * @param vector The Euclidean vector to convert
     * @param maxRadius Maximum radius in Poincare ball
     * @return The corresponding Poincare vector
     */
    public PoincareVector euclideanToPoincareAdaptive(EuclideanVector vector, double maxRadius) {
        return super.euclideanToPoincare(vector, maxRadius, currentCurvature);
    }
    
    /**
     * Update the current curvature based on the learner's last result.
     */
    public void updateCurvature() {
        this.currentCurvature = curvatureLearner.getLastLearnedCurvature();
    }
    
    /**
     * Set a specific curvature value directly.
     * 
     * @param curvature The curvature value to set
     */
    public void setCurvature(double curvature) {
        this.currentCurvature = curvature;
    }
    
    /**
     * Get the current curvature value.
     * 
     * @return The current curvature
     */
    public double getCurrentCurvature() {
        return currentCurvature;
    }
    
    /**
     * Get the curvature learner.
     * 
     * @return The curvature learner instance
     */
    public CurvatureLearner getCurvatureLearner() {
        return curvatureLearner;
    }
}

