package com.hypervector.math.hyperbolic.learning;

/**
 * Factory for creating curvature learners
 */
public class CurvatureLearnerFactory {
    
    /**
     * Types of available curvature learners
     */
    public enum LearnerType {
        GRADIENT_DESCENT,
        GRID_SEARCH
    }
    
    /**
     * Create a curvature learner of the specified type
     * 
     * @param type The type of learner to create
     * @return A new curvature learner instance
     */
    public static CurvatureLearner createLearner(LearnerType type) {
        switch (type) {
            case GRADIENT_DESCENT:
                return new GradientDescentCurvatureLearner();
            case GRID_SEARCH:
                return new GridSearchCurvatureLearner();
            default:
                throw new IllegalArgumentException("Unknown learner type: " + type);
        }
    }
    
    /**
     * Create a default curvature learner (currently Grid Search for efficiency)
     * 
     * @return A new curvature learner instance
     */
    public static CurvatureLearner createDefaultLearner() {
        return createLearner(LearnerType.GRID_SEARCH);
    }
}
