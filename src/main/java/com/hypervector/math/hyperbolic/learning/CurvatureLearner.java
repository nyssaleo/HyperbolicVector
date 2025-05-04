package com.hypervector.math.hyperbolic.learning;

import java.util.List;
import java.util.Map;

/**
 * Interface for learning optimal curvature values for hyperbolic embeddings.
 */
public interface CurvatureLearner {
    
    /**
     * Learn the optimal curvature for a hierarchical dataset.
     * 
     * @param hierarchicalData Map of node IDs to parent node IDs representing a hierarchy
     * @param options Optional configuration parameters
     * @return The optimal curvature value (typically negative)
     */
    double learnOptimalCurvature(Map<String, List<String>> hierarchicalData, Map<String, Object> options);
    
    /**
     * Get the last learned curvature value.
     * 
     * @return The last learned curvature value or default if none learned yet
     */
    double getLastLearnedCurvature();
    
    /**
     * Get the default curvature value.
     * 
     * @return The default curvature value
     */
    double getDefaultCurvature();
}
