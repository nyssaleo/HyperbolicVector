package com.hypervector.benchmark;

import com.hypervector.index.common.FlatVectorIndex;
import com.hypervector.index.common.VectorIndex;
import com.hypervector.storage.common.CollectionConfig;
import com.hypervector.storage.common.InMemoryVectorStorage;
import com.hypervector.storage.common.VectorStorage;

/**
 * Main runner for all benchmark types.
 */
public class BenchmarkRunner {

    public static void main(String[] args) {
        System.out.println("HyperbolicVectorDB Comprehensive Benchmark Suite");
        System.out.println("==============================================");
        
        // Parse command line options
        String benchmarkType = "all";
        if (args.length > 0) {
            benchmarkType = args[0].toLowerCase();
        }
        
        try {
            switch (benchmarkType) {
                case "synthetic":
                    runSyntheticBenchmark();
                    break;
                case "wordnet":
                    runWordNetBenchmark();
                    break;
                case "deep":
                    runDeepHierarchyBenchmark();
                    break;
                case "dimensions":
                    runDimensionalityBenchmark();
                    break;
                case "comparison":
                    runComparativeBenchmark();
                    break;
                case "all":
                default:
                    runAllBenchmarks();
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error running benchmarks: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run all benchmarks.
     */
    private static void runAllBenchmarks() {
        System.out.println("\nRunning all benchmarks...");
        
        runSyntheticBenchmark();
        runWordNetBenchmark();
        runDeepHierarchyBenchmark();
        runDimensionalityBenchmark();
        runComparativeBenchmark();
        
        System.out.println("\nAll benchmarks completed!");
    }
    
    /**
     * Run benchmark with synthetic data.
     */
    private static void runSyntheticBenchmark() {
        System.out.println("\n--- Synthetic Hierarchical Data Benchmark ---");
        
        // Create benchmark with synthetic tree data
        EnhancedBenchmark benchmark = new EnhancedBenchmark("synthetic_euclidean", "synthetic_hyperbolic");
        benchmark.initialize();
        benchmark.generateSyntheticTreeData(4, 3);
        
        // Run evaluation
        benchmark.runComprehensiveEvaluation(4, 20);
        benchmark.generateCharts();
        
        System.out.println("Synthetic benchmark completed!");
    }
    
    /**
     * Run benchmark with WordNet data.
     */
    private static void runWordNetBenchmark() {
        System.out.println("\n--- WordNet Hierarchy Benchmark ---");
        
        // Create storage and index
        VectorStorage storage = new InMemoryVectorStorage();
        String euclideanCollection = "wordnet_euclidean";
        String hyperbolicCollection = "wordnet_hyperbolic";
        
        // Create collections
        storage.createCollection(euclideanCollection, 
            CollectionConfig.createEuclidean(3, CollectionConfig.StorageFormat.FLOAT32));
            
        storage.createCollection(hyperbolicCollection,
            CollectionConfig.createPoincare(3, CollectionConfig.StorageFormat.FLOAT32));
        
        // Load WordNet data
        HierarchicalDatasetLoader loader = new HierarchicalDatasetLoader(
            storage, euclideanCollection, hyperbolicCollection, 3, 0.9);
        boolean success = loader.loadWordNetNounHierarchy();
        
        if (success) {
            // Create index
            FlatVectorIndex index = new FlatVectorIndex();
            index.setVectorStorage(storage);
            index.buildIndex(euclideanCollection, VectorIndex.IndexType.FLAT, null);
            index.buildIndex(hyperbolicCollection, VectorIndex.IndexType.FLAT, null);
            
            // Create benchmark with existing data
            EnhancedBenchmark benchmark = new EnhancedBenchmark(euclideanCollection, hyperbolicCollection);
            
            // Run evaluation
            benchmark.runComprehensiveEvaluation(3, 20);
            benchmark.generateCharts();
        }
        
        System.out.println("WordNet benchmark completed!");
    }
    
    /**
     * Run benchmark with deep hierarchy.
     */
    private static void runDeepHierarchyBenchmark() {
        System.out.println("\n--- Deep Hierarchical Structure Benchmark ---");
        
        // Create storage and index
        VectorStorage storage = new InMemoryVectorStorage();
        String euclideanCollection = "deep_euclidean";
        String hyperbolicCollection = "deep_hyperbolic";
        
        // Create collections
        storage.createCollection(euclideanCollection, 
            CollectionConfig.createEuclidean(3, CollectionConfig.StorageFormat.FLOAT32));
            
        storage.createCollection(hyperbolicCollection,
            CollectionConfig.createPoincare(3, CollectionConfig.StorageFormat.FLOAT32));
        
        // Load deep hierarchy
        HierarchicalDatasetLoader loader = new HierarchicalDatasetLoader(
            storage, euclideanCollection, hyperbolicCollection, 3, 0.9);
        boolean success = loader.loadDeepHierarchy(5, 2);
        
        if (success) {
            // Create index
            FlatVectorIndex index = new FlatVectorIndex();
            index.setVectorStorage(storage);
            index.buildIndex(euclideanCollection, VectorIndex.IndexType.FLAT, null);
            index.buildIndex(hyperbolicCollection, VectorIndex.IndexType.FLAT, null);
            
            // Create benchmark with existing data
            EnhancedBenchmark benchmark = new EnhancedBenchmark(euclideanCollection, hyperbolicCollection);
            
            // Run evaluation
            benchmark.runComprehensiveEvaluation(5, 20);
            benchmark.generateCharts();
        }
        
        System.out.println("Deep hierarchy benchmark completed!");
    }
    
    /**
     * Run benchmark comparing different dimensionalities.
     */
    private static void runDimensionalityBenchmark() {
        System.out.println("\n--- Dimensionality Comparison Benchmark ---");
        System.out.println("Comparing 2D hyperbolic space with 3D/5D Euclidean spaces...");
        
        // This would require modifying the implementation to support different dimensions
        // For demonstration, we'll just run the synthetic benchmark with a note
        
        System.out.println("Note: A proper implementation would compare higher-dimensional Euclidean");
        System.out.println("spaces with lower-dimensional hyperbolic spaces to show embedding efficiency.");
        
        runSyntheticBenchmark();
        
        System.out.println("Dimensionality benchmark completed!");
    }
    
    /**
     * Run comparative benchmark with different data distributions.
     */
    private static void runComparativeBenchmark() {
        System.out.println("\n--- Comparative Data Distribution Benchmark ---");
        System.out.println("Comparing performance on different hierarchical structures...");
        
        // Run with balanced tree (synthetic)
        EnhancedBenchmark balancedBenchmark = new EnhancedBenchmark("balanced_euclidean", "balanced_hyperbolic");
        balancedBenchmark.initialize();
        balancedBenchmark.generateSyntheticTreeData(3, 3);
        balancedBenchmark.runComprehensiveEvaluation(3, 10);
        
        // Run with deep, narrow tree
        EnhancedBenchmark deepBenchmark = new EnhancedBenchmark("deep_euclidean", "deep_hyperbolic");
        deepBenchmark.initialize();
        deepBenchmark.generateSyntheticTreeData(5, 2);
        deepBenchmark.runComprehensiveEvaluation(5, 10);
        
        // Run with shallow, wide tree
        EnhancedBenchmark wideBenchmark = new EnhancedBenchmark("wide_euclidean", "wide_hyperbolic");
        wideBenchmark.initialize();
        wideBenchmark.generateSyntheticTreeData(2, 5);
        wideBenchmark.runComprehensiveEvaluation(2, 10);
        
        System.out.println("Comparative benchmark completed!");
    }
}
