package com.hypervector.math.hyperbolic.learning;

import com.hypervector.dataset.DatasetLoader;
import com.hypervector.dataset.WordNetLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple test class for curvature learning
 */
public class CurvatureLearningTest {
    
    public static void main(String[] args) {
        try {
            // Load a real dataset
            System.out.println("Loading WordNet dataset...");
            DatasetLoader wordnetLoader = new WordNetLoader();
            Map<String, List<String>> hierarchy = wordnetLoader.loadHierarchy();
            
            System.out.println("Loaded hierarchy with " + hierarchy.size() + " nodes");
            
            // Create curvature learners
            CurvatureLearner gridLearner = CurvatureLearnerFactory.createLearner(
                CurvatureLearnerFactory.LearnerType.GRID_SEARCH);
            
            // Learn optimal curvature
            System.out.println("\nLearning curvature with Grid Search...");
            Map<String, Object> options = new HashMap<>();
            options.put("numValues", 20);
            options.put("minCurvature", -3.0);
            options.put("maxCurvature", -0.5);
            
            double gridCurvature = gridLearner.learnOptimalCurvature(hierarchy, options);
            System.out.println("Grid Search optimal curvature: " + gridCurvature);
            
            // Only test gradient descent on smaller subset for efficiency
            System.out.println("\nCreating smaller subset for gradient descent test...");
            Map<String, List<String>> smallHierarchy = new HashMap<>();
            int count = 0;
            for (Map.Entry<String, List<String>> entry : hierarchy.entrySet()) {
                smallHierarchy.put(entry.getKey(), entry.getValue());
                if (++count >= 100) break; // Limit size for quick test
            }
            
            CurvatureLearner gradientLearner = CurvatureLearnerFactory.createLearner(
                CurvatureLearnerFactory.LearnerType.GRADIENT_DESCENT);
            
            System.out.println("\nLearning curvature with Gradient Descent...");
            options = new HashMap<>();
            options.put("maxIterations", 30); // Limit iterations for quick test
            options.put("dimensions", 3);
            
            double gradientCurvature = gradientLearner.learnOptimalCurvature(smallHierarchy, options);
            System.out.println("Gradient Descent optimal curvature: " + gradientCurvature);
            
            System.out.println("\nCurvature learning test complete");
            
        } catch (IOException e) {
            System.err.println("Error loading dataset: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error during curvature learning: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
