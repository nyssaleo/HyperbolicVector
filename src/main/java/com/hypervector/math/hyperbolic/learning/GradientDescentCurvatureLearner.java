package com.hypervector.math.hyperbolic.learning;

import com.hypervector.math.conversion.VectorSpaceConverter;
import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.euclidean.EuclideanVectorOperations;
import com.hypervector.math.hyperbolic.PoincareVector;
import com.hypervector.math.hyperbolic.PoincareVectorOperations;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of curvature learning using gradient descent optimization.
 * This class learns the optimal curvature for a given hierarchical dataset
 * by minimizing distortion between hyperbolic and hierarchical distances.
 */
public class GradientDescentCurvatureLearner implements CurvatureLearner {
    
    // Default parameters
    private static final double DEFAULT_CURVATURE = -1.0;
    private static final double LEARNING_RATE = 0.01;
    private static final int MAX_ITERATIONS = 100;
    private static final double CONVERGENCE_THRESHOLD = 1e-4;
    
    // Boundary constraints for curvature
    private static final double MIN_CURVATURE = -5.0;
    private static final double MAX_CURVATURE = -0.1;
    
    private final VectorSpaceConverter converter = new VectorSpaceConverter();
    private final EuclideanVectorOperations euclideanOps = new EuclideanVectorOperations();
    private double lastLearnedCurvature = DEFAULT_CURVATURE;
    private final Random random = new Random(42); // Fixed seed for reproducibility
    
    @Override
    public double learnOptimalCurvature(Map<String, List<String>> hierarchicalData, Map<String, Object> options) {
        // Extract options with defaults
        double learningRate = options != null && options.containsKey("learningRate") 
                            ? (double) options.get("learningRate") : LEARNING_RATE;
        int maxIterations = options != null && options.containsKey("maxIterations") 
                          ? (int) options.get("maxIterations") : MAX_ITERATIONS;
        double threshold = options != null && options.containsKey("threshold") 
                         ? (double) options.get("threshold") : CONVERGENCE_THRESHOLD;
        int dimensions = options != null && options.containsKey("dimensions") 
                       ? (int) options.get("dimensions") : 3;
        double maxRadius = options != null && options.containsKey("maxRadius") 
                         ? (double) options.get("maxRadius") : 0.9;
        
        // Step 1: Generate initial embeddings with default curvature
        System.out.println("Starting curvature learning with gradient descent");
        System.out.println("Generating initial embeddings...");
        
        // Create node IDs list
        List<String> nodeIds = new ArrayList<>(hierarchicalData.keySet());
        nodeIds.addAll(hierarchicalData.values().stream()
                .flatMap(List::stream)
                .filter(id -> !hierarchicalData.containsKey(id))
                .collect(Collectors.toList()));
        nodeIds = nodeIds.stream().distinct().collect(Collectors.toList());
        
        // Generate random initial embeddings
        Map<String, EuclideanVector> initialEmbeddings = generateRandomEmbeddings(nodeIds, dimensions);
        
        // Step 2: Compute hierarchical distances
        Map<String, Map<String, Double>> hierarchicalDistances = computeHierarchicalDistances(hierarchicalData);
        
        // Step 3: Gradient descent to optimize curvature
        System.out.println("Starting gradient descent...");
        double currentCurvature = DEFAULT_CURVATURE;
        double prevLoss = Double.MAX_VALUE;
        
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            // Generate hyperbolic embeddings with current curvature
            Map<String, PoincareVector> hyperbolicEmbeddings = initialEmbeddings.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> converter.euclideanToPoincare(entry.getValue(), maxRadius, currentCurvature)
                ));
            
            // Compute current loss
            double loss = computeLoss(hierarchicalDistances, hyperbolicEmbeddings);
            
            // Compute gradient (approximated)
            double epsilon = 0.001;
            double curvaturePlus = Math.max(MIN_CURVATURE, Math.min(MAX_CURVATURE, currentCurvature + epsilon));
            double curvatureMinus = Math.max(MIN_CURVATURE, Math.min(MAX_CURVATURE, currentCurvature - epsilon));
            
            Map<String, PoincareVector> embeddingsPlus = initialEmbeddings.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> converter.euclideanToPoincare(entry.getValue(), maxRadius, curvaturePlus)
                ));
            
            Map<String, PoincareVector> embeddingsMinus = initialEmbeddings.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> converter.euclideanToPoincare(entry.getValue(), maxRadius, curvatureMinus)
                ));
            
            double lossPlus = computeLoss(hierarchicalDistances, embeddingsPlus);
            double lossMinus = computeLoss(hierarchicalDistances, embeddingsMinus);
            
            double gradient = (lossPlus - lossMinus) / (2 * epsilon);
            
            // Update curvature
            currentCurvature = Math.max(MIN_CURVATURE, Math.min(MAX_CURVATURE, 
                                      currentCurvature - learningRate * gradient));
            
            // Check convergence
            if (Math.abs(prevLoss - loss) < threshold) {
                System.out.println("Converged at iteration " + iteration);
                break;
            }
            
            prevLoss = loss;
            
            if (iteration % 10 == 0) {
                System.out.printf("Iteration %d: Curvature = %.4f, Loss = %.4f\n", 
                                 iteration, currentCurvature, loss);
            }
        }
        
        System.out.println("Finished learning. Optimal curvature: " + currentCurvature);
        this.lastLearnedCurvature = currentCurvature;
        return currentCurvature;
    }
    
    @Override
    public double getLastLearnedCurvature() {
        return lastLearnedCurvature;
    }
    
    @Override
    public double getDefaultCurvature() {
        return DEFAULT_CURVATURE;
    }
    
    /**
     * Generate random initial embeddings for nodes.
     */
    private Map<String, EuclideanVector> generateRandomEmbeddings(List<String> nodeIds, int dimensions) {
        Map<String, EuclideanVector> embeddings = new HashMap<>();
        for (String nodeId : nodeIds) {
            double[] vector = new double[dimensions];
            for (int i = 0; i < dimensions; i++) {
                vector[i] = random.nextGaussian() * 0.1; // Small initial values
            }
            embeddings.put(nodeId, new EuclideanVector(vector));
        }
        return embeddings;
    }
    
    /**
     * Compute hierarchical distances between nodes.
     */
    private Map<String, Map<String, Double>> computeHierarchicalDistances(Map<String, List<String>> hierarchicalData) {
        Map<String, Map<String, Double>> distances = new HashMap<>();
        
        // First build a complete graph representation
        Map<String, Set<String>> graph = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : hierarchicalData.entrySet()) {
            String child = entry.getKey();
            List<String> parents = entry.getValue();
            
            if (!graph.containsKey(child)) {
                graph.put(child, new HashSet<>());
            }
            
            for (String parent : parents) {
                if (!graph.containsKey(parent)) {
                    graph.put(parent, new HashSet<>());
                }
                
                // Add bidirectional edges
                graph.get(child).add(parent);
                graph.get(parent).add(child);
            }
        }
        
        // Compute distances using BFS
        for (String source : graph.keySet()) {
            Map<String, Double> distancesFromSource = new HashMap<>();
            Queue<String> queue = new LinkedList<>();
            Set<String> visited = new HashSet<>();
            
            queue.add(source);
            visited.add(source);
            distancesFromSource.put(source, 0.0);
            
            while (!queue.isEmpty()) {
                String current = queue.poll();
                double currentDistance = distancesFromSource.get(current);
                
                for (String neighbor : graph.getOrDefault(current, Collections.emptySet())) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);
                        distancesFromSource.put(neighbor, currentDistance + 1.0);
                    }
                }
            }
            
            distances.put(source, distancesFromSource);
        }
        
        return distances;
    }
    
    /**
     * Compute loss as the mean squared error between hyperbolic and hierarchical distances.
     */
    private double computeLoss(Map<String, Map<String, Double>> hierarchicalDistances, 
                              Map<String, PoincareVector> hyperbolicEmbeddings) {
        double totalLoss = 0.0;
        int count = 0;
        
        PoincareVectorOperations poincareOps = new PoincareVectorOperations();
        
        for (String source : hierarchicalDistances.keySet()) {
            if (!hyperbolicEmbeddings.containsKey(source)) {
                continue;
            }
            
            Map<String, Double> distancesFromSource = hierarchicalDistances.get(source);
            PoincareVector sourceEmbedding = hyperbolicEmbeddings.get(source);
            
            for (Map.Entry<String, Double> entry : distancesFromSource.entrySet()) {
                String target = entry.getKey();
                if (source.equals(target) || !hyperbolicEmbeddings.containsKey(target)) {
                    continue;
                }
                
                double hierarchicalDistance = entry.getValue();
                PoincareVector targetEmbedding = hyperbolicEmbeddings.get(target);
                
                try {
                    double hyperbolicDistance = poincareOps.distance(sourceEmbedding, targetEmbedding);
                    double scaledHierarchicalDistance = hierarchicalDistance / 5.0; // Scale factor
                    
                    double error = hyperbolicDistance - scaledHierarchicalDistance;
                    totalLoss += error * error; // Squared error
                    count++;
                } catch (Exception e) {
                    // Skip if there's a numerical issue
                    continue;
                }
            }
        }
        
        return count > 0 ? totalLoss / count : Double.MAX_VALUE;
    }
}
