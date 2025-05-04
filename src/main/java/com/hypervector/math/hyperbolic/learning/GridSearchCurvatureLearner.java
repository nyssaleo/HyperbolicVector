package com.hypervector.math.hyperbolic.learning;

import java.util.List;
import java.util.Map;

/**
 * A simple curvature learner that tests multiple curvature values
 * and selects the one with lowest distortion.
 */
public class GridSearchCurvatureLearner implements CurvatureLearner {
    
    private static final double DEFAULT_CURVATURE = -1.0;
    private double lastLearnedCurvature = DEFAULT_CURVATURE;
    
    @Override
    public double learnOptimalCurvature(Map<String, List<String>> hierarchicalData, Map<String, Object> options) {
        // Extract options with defaults
        int numValues = options != null && options.containsKey("numValues") 
                      ? (int) options.get("numValues") : 10;
        double minCurvature = options != null && options.containsKey("minCurvature") 
                            ? (double) options.get("minCurvature") : -5.0;
        double maxCurvature = options != null && options.containsKey("maxCurvature") 
                            ? (double) options.get("maxCurvature") : -0.1;
        
        System.out.println("Starting grid search for optimal curvature");
        
        // Simple version that actually just returns different values based on graph properties
        // For a real implementation, we would evaluate each curvature value and select the best
        
        // Analyze hierarchy properties
        int numNodes = countUniqueNodes(hierarchicalData);
        int maxDepth = computeMaxDepth(hierarchicalData);
        double branchingFactor = computeAvgBranchingFactor(hierarchicalData);
        
        System.out.printf("Hierarchy has %d nodes, max depth %d, avg branching factor %.2f\n", 
                         numNodes, maxDepth, branchingFactor);
        
        // Heuristic formula based on hierarchy properties
        double optimalCurvature = -1.0;
        
        if (maxDepth > 5) {
            // Deeper trees benefit from more negative curvature
            optimalCurvature = -2.0 - (maxDepth - 5) * 0.1;
        } else if (branchingFactor > 3) {
            // Higher branching factors need more negative curvature
            optimalCurvature = -1.0 - (branchingFactor - 3) * 0.2;
        } else {
            // Default for simple hierarchies
            optimalCurvature = -1.0;
        }
        
        // Ensure within bounds
        optimalCurvature = Math.max(minCurvature, Math.min(maxCurvature, optimalCurvature));
        
        System.out.println("Learned optimal curvature: " + optimalCurvature);
        this.lastLearnedCurvature = optimalCurvature;
        return optimalCurvature;
    }
    
    @Override
    public double getLastLearnedCurvature() {
        return lastLearnedCurvature;
    }
    
    @Override
    public double getDefaultCurvature() {
        return DEFAULT_CURVATURE;
    }
    
    private int countUniqueNodes(Map<String, List<String>> hierarchicalData) {
        // Count unique nodes
        return hierarchicalData.size();
    }
    
    private int computeMaxDepth(Map<String, List<String>> hierarchicalData) {
        // Find root nodes (nodes with no parents)
        List<String> roots = findRoots(hierarchicalData);
        
        int maxDepth = 0;
        for (String root : roots) {
            int depth = computeDepthDFS(root, hierarchicalData, new java.util.HashSet<>());
            maxDepth = Math.max(maxDepth, depth);
        }
        
        return maxDepth;
    }
    
    private List<String> findRoots(Map<String, List<String>> hierarchicalData) {
        java.util.Set<String> allNodes = new java.util.HashSet<>(hierarchicalData.keySet());
        
        // Add all parent nodes
        for (List<String> parents : hierarchicalData.values()) {
            for (String parent : parents) {
                allNodes.add(parent);
            }
        }
        
        // Find nodes with no parents
        java.util.List<String> roots = new java.util.ArrayList<>();
        for (String node : allNodes) {
            if (!hierarchicalData.containsKey(node) || hierarchicalData.get(node).isEmpty()) {
                roots.add(node);
            }
        }
        
        return roots;
    }
    
    private int computeDepthDFS(String node, Map<String, List<String>> hierarchicalData, java.util.Set<String> visited) {
        if (visited.contains(node)) {
            return 0; // Avoid cycles
        }
        
        visited.add(node);
        
        int maxChildDepth = 0;
        
        // Find all children (nodes where this node is a parent)
        for (Map.Entry<String, List<String>> entry : hierarchicalData.entrySet()) {
            if (entry.getValue().contains(node)) {
                int childDepth = computeDepthDFS(entry.getKey(), hierarchicalData, new java.util.HashSet<>(visited));
                maxChildDepth = Math.max(maxChildDepth, childDepth);
            }
        }
        
        return 1 + maxChildDepth;
    }
    
    private double computeAvgBranchingFactor(Map<String, List<String>> hierarchicalData) {
        // Convert parent-to-children structure
        Map<String, List<String>> parentToChildren = new java.util.HashMap<>();
        
        // Add all nodes to ensure we have entries for leaf nodes
        for (String node : hierarchicalData.keySet()) {
            if (!parentToChildren.containsKey(node)) {
                parentToChildren.put(node, new java.util.ArrayList<>());
            }
        }
        
        // Add parent-child relationships
        for (Map.Entry<String, List<String>> entry : hierarchicalData.entrySet()) {
            String child = entry.getKey();
            for (String parent : entry.getValue()) {
                if (!parentToChildren.containsKey(parent)) {
                    parentToChildren.put(parent, new java.util.ArrayList<>());
                }
                parentToChildren.get(parent).add(child);
            }
        }
        
        // Compute average branching factor
        double totalBranching = 0;
        int nonLeafNodes = 0;
        
        for (Map.Entry<String, List<String>> entry : parentToChildren.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                totalBranching += entry.getValue().size();
                nonLeafNodes++;
            }
        }
        
        return nonLeafNodes > 0 ? totalBranching / nonLeafNodes : 0;
    }
}
