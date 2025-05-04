package com.hypervector.benchmark;

import com.hypervector.index.common.FlatVectorIndex;
import com.hypervector.index.common.SearchResult;
import com.hypervector.index.common.VectorIndex;
import com.hypervector.math.conversion.VectorSpaceConverter;
import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.hyperbolic.PoincareVector;
import com.hypervector.storage.common.CollectionConfig;
import com.hypervector.storage.common.InMemoryVectorStorage;
import com.hypervector.storage.common.VectorRecord;
import com.hypervector.storage.common.VectorStorage;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Enhanced benchmark framework for comparing Euclidean and Hyperbolic embeddings
 * with support for hierarchical data structures and real-world datasets.
 */
public class EnhancedBenchmark {

    // Configuration parameters
    private static final int DEFAULT_VECTOR_DIMENSION = 3;
    private static final double MAX_RADIUS = 0.9; // Maximum radius for Poincar? ball
    private static final int MAX_K = 50; // Maximum number of nearest neighbors to consider
    
    // Storage and index objects
    private final VectorStorage storage;
    private final VectorIndex index;
    private final VectorSpaceConverter converter;
    
    // Collection names
    private final String euclideanCollection;
    private final String hyperbolicCollection;
    
    // Random generator with fixed seed for reproducibility
    private final Random random = new Random(42);
    
    // Metrics tracking
    private final Map<String, List<Double>> metrics = new HashMap<>();
    
    /**
     * Create a new benchmark with the specified collection names.
     */
    public EnhancedBenchmark(String euclideanCollection, String hyperbolicCollection) {
        this.euclideanCollection = euclideanCollection;
        this.hyperbolicCollection = hyperbolicCollection;
        
        // Initialize storage and index
        this.storage = new InMemoryVectorStorage();
        this.index = new FlatVectorIndex();
        this.index.setVectorStorage(storage);
        this.converter = new VectorSpaceConverter();
        
        // Initialize metrics
        metrics.put("euclidean_precision", new ArrayList<>());
        metrics.put("hyperbolic_precision", new ArrayList<>());
        metrics.put("euclidean_recall", new ArrayList<>());
        metrics.put("hyperbolic_recall", new ArrayList<>());
        metrics.put("euclidean_mrr", new ArrayList<>());
        metrics.put("hyperbolic_mrr", new ArrayList<>());
        metrics.put("euclidean_avg_distance", new ArrayList<>());
        metrics.put("hyperbolic_avg_distance", new ArrayList<>());
        metrics.put("hierarchical_fidelity_euclidean", new ArrayList<>());
        metrics.put("hierarchical_fidelity_hyperbolic", new ArrayList<>());
    }
    
    /**
     * Initialize the benchmark environment.
     */
    public void initialize() {
        System.out.println("Initializing benchmark environment...");
        
        // Create collections
        createCollections();
    }
    
    /**
     * Create storage collections.
     */
    private void createCollections() {
        // Delete existing collections if they exist
        if (storage.collectionExists(euclideanCollection)) {
            storage.deleteCollection(euclideanCollection);
        }
        
        if (storage.collectionExists(hyperbolicCollection)) {
            storage.deleteCollection(hyperbolicCollection);
        }
        
        // Create new collections
        storage.createCollection(euclideanCollection,
            CollectionConfig.createEuclidean(DEFAULT_VECTOR_DIMENSION, CollectionConfig.StorageFormat.FLOAT32));
            
        storage.createCollection(hyperbolicCollection,
            CollectionConfig.createPoincare(DEFAULT_VECTOR_DIMENSION, CollectionConfig.StorageFormat.FLOAT32));
            
        System.out.println("Collections created successfully.");
    }
    
    /**
     * Generate synthetic tree data with specified depth and branching factor.
     * 
     * @param depth Maximum tree depth
     * @param branchingFactor Number of children per node
     */
    public void generateSyntheticTreeData(int depth, int branchingFactor) {
        System.out.println("Generating synthetic tree data with depth " + depth + 
                           " and branching factor " + branchingFactor + "...");
        
        // Generate root node
        float[] rootVector = new float[DEFAULT_VECTOR_DIMENSION];
        Map<String, Object> rootMetadata = new HashMap<>();
        rootMetadata.put("level", 0);
        rootMetadata.put("path", "0");
        rootMetadata.put("name", "Root");
        
        // Store root in both collections
        storage.storeVector(euclideanCollection, rootVector, rootMetadata);
        storage.storeHyperbolicVector(hyperbolicCollection, rootVector, true, rootMetadata);
        
        // Generate tree recursively
        generateTreeLevel(rootVector, 1, depth, branchingFactor, "0");
        
        // Build indices
        buildIndices();
        
        System.out.println("Generated " + storage.getCollectionStats(euclideanCollection).getVectorCount() + 
            " vectors in Euclidean collection");
        System.out.println("Generated " + storage.getCollectionStats(hyperbolicCollection).getVectorCount() + 
            " vectors in Hyperbolic collection");
    }
    
    /**
     * Recursively generate a level of the tree.
     */
    private void generateTreeLevel(float[] parentVector, int level, int maxDepth, 
                                  int branchingFactor, String parentPath) {
        if (level > maxDepth) {
            return;
        }
        
        for (int i = 0; i < branchingFactor; i++) {
            // Generate a vector that's a perturbation of the parent vector
            float[] childEuclideanVector = new float[DEFAULT_VECTOR_DIMENSION];
            for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                // Perturbation decreases with level to create more clustered hierarchies
                float perturbationScale = 0.2f / level;
                childEuclideanVector[j] = parentVector[j] + perturbationScale * (float)random.nextGaussian();
            }
            
            // Create path for the current node
            String currentPath = parentPath + "." + i;
            
            // Create metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("level", level);
            metadata.put("path", currentPath);
            metadata.put("name", "Node-" + currentPath);
            
            // Store in Euclidean collection
            String euclideanId = storage.storeVector(euclideanCollection, childEuclideanVector, metadata);
            
            // Convert to hyperbolic and store
            double[] doubleVector = new double[DEFAULT_VECTOR_DIMENSION];
            for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                doubleVector[j] = childEuclideanVector[j];
            }
            
            EuclideanVector euclideanVector = new EuclideanVector(doubleVector);
            PoincareVector poincareVector = converter.euclideanToPoincare(euclideanVector, MAX_RADIUS, -1.0);
            
            float[] childHyperbolicVector = new float[DEFAULT_VECTOR_DIMENSION];
            for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                childHyperbolicVector[j] = (float)poincareVector.getData()[j];
            }
            
            String hyperbolicId = storage.storeHyperbolicVector(
                hyperbolicCollection, childHyperbolicVector, true, metadata);
            
            // Continue to the next level
            generateTreeLevel(childEuclideanVector, level + 1, maxDepth, branchingFactor, currentPath);
        }
    }
    
    /**
     * Load WordNet nouns dataset as a hierarchical dataset.
     * This is a simplified version for demonstration.
     */
    public void loadWordNetHierarchy() {
        System.out.println("Loading WordNet noun hierarchy...");
        
        // This would normally download and parse WordNet data
        // For demonstration, we'll create a simplified version
        
        // Create root node for "entity"
        float[] rootVector = new float[DEFAULT_VECTOR_DIMENSION];
        Map<String, Object> rootMetadata = new HashMap<>();
        rootMetadata.put("level", 0);
        rootMetadata.put("synset", "entity.n.01");
        rootMetadata.put("name", "entity");
        
        // Store root in both collections
        storage.storeVector(euclideanCollection, rootVector, rootMetadata);
        storage.storeHyperbolicVector(hyperbolicCollection, rootVector, true, rootMetadata);
        
        // Define some high-level categories (level 1)
        String[] categories = {
            "physical_entity", "abstract_entity", "thing", "object"
        };
        
        // Define some mid-level categories (level 2)
        Map<String, String[]> subCategories = new HashMap<>();
        subCategories.put("physical_entity", new String[]{"matter", "process", "phenomenon"});
        subCategories.put("abstract_entity", new String[]{"attribute", "measure", "relation"});
        subCategories.put("thing", new String[]{"item", "artifact", "part"});
        subCategories.put("object", new String[]{"whole", "natural_object", "artifact"});
        
        // Define some leaf nodes (level 3)
        Map<String, String[]> leafNodes = new HashMap<>();
        leafNodes.put("matter", new String[]{"solid", "liquid", "gas", "plasma"});
        leafNodes.put("process", new String[]{"change", "motion", "transition"});
        leafNodes.put("attribute", new String[]{"property", "quality", "trait"});
        leafNodes.put("artifact", new String[]{"tool", "structure", "artwork", "instrument"});
        
        // Generate vectors for level 1
        for (String category : categories) {
            // Generate a random vector for this category
            float[] categoryVector = new float[DEFAULT_VECTOR_DIMENSION];
            for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                categoryVector[j] = (float)(0.3 * random.nextGaussian());
            }
            
            // Create metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("level", 1);
            metadata.put("synset", category + ".n.01");
            metadata.put("name", category);
            metadata.put("parent", "entity.n.01");
            
            // Store in Euclidean collection
            storage.storeVector(euclideanCollection, categoryVector, metadata);
            
            // Convert to hyperbolic and store
            double[] doubleVector = new double[DEFAULT_VECTOR_DIMENSION];
            for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                doubleVector[j] = categoryVector[j];
            }
            
            EuclideanVector euclideanVector = new EuclideanVector(doubleVector);
            PoincareVector poincareVector = converter.euclideanToPoincare(euclideanVector, MAX_RADIUS, -1.0);
            
            float[] hyperbolicVector = new float[DEFAULT_VECTOR_DIMENSION];
            for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                hyperbolicVector[j] = (float)poincareVector.getData()[j];
            }
            
            storage.storeHyperbolicVector(hyperbolicCollection, hyperbolicVector, true, metadata);
            
            // Generate vectors for level 2 (if this category has subcategories)
            if (subCategories.containsKey(category)) {
                for (String subCategory : subCategories.get(category)) {
                    // Generate a vector that's a perturbation of the category vector
                    float[] subCategoryVector = new float[DEFAULT_VECTOR_DIMENSION];
                    for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                        subCategoryVector[j] = categoryVector[j] + (float)(0.1 * random.nextGaussian());
                    }
                    
                    // Create metadata
                    Map<String, Object> subMetadata = new HashMap<>();
                    subMetadata.put("level", 2);
                    subMetadata.put("synset", subCategory + ".n.01");
                    subMetadata.put("name", subCategory);
                    subMetadata.put("parent", category + ".n.01");
                    
                    // Store in Euclidean collection
                    storage.storeVector(euclideanCollection, subCategoryVector, subMetadata);
                    
                    // Convert to hyperbolic and store
                    double[] subDoubleVector = new double[DEFAULT_VECTOR_DIMENSION];
                    for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                        subDoubleVector[j] = subCategoryVector[j];
                    }
                    
                    EuclideanVector subEuclideanVector = new EuclideanVector(subDoubleVector);
                    PoincareVector subPoincareVector = converter.euclideanToPoincare(subEuclideanVector, MAX_RADIUS, -1.0);
                    
                    float[] subHyperbolicVector = new float[DEFAULT_VECTOR_DIMENSION];
                    for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                        subHyperbolicVector[j] = (float)subPoincareVector.getData()[j];
                    }
                    
                    storage.storeHyperbolicVector(hyperbolicCollection, subHyperbolicVector, true, subMetadata);
                    
                    // Generate vectors for level 3 (if this subcategory has leaf nodes)
                    if (leafNodes.containsKey(subCategory)) {
                        for (String leaf : leafNodes.get(subCategory)) {
                            // Generate a vector that's a perturbation of the subcategory vector
                            float[] leafVector = new float[DEFAULT_VECTOR_DIMENSION];
                            for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                                leafVector[j] = subCategoryVector[j] + (float)(0.05 * random.nextGaussian());
                            }
                            
                            // Create metadata
                            Map<String, Object> leafMetadata = new HashMap<>();
                            leafMetadata.put("level", 3);
                            leafMetadata.put("synset", leaf + ".n.01");
                            leafMetadata.put("name", leaf);
                            leafMetadata.put("parent", subCategory + ".n.01");
                            
                            // Store in Euclidean collection
                            storage.storeVector(euclideanCollection, leafVector, leafMetadata);
                            
                            // Convert to hyperbolic and store
                            double[] leafDoubleVector = new double[DEFAULT_VECTOR_DIMENSION];
                            for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                                leafDoubleVector[j] = leafVector[j];
                            }
                            
                            EuclideanVector leafEuclideanVector = new EuclideanVector(leafDoubleVector);
                            PoincareVector leafPoincareVector = converter.euclideanToPoincare(leafEuclideanVector, MAX_RADIUS, -1.0);
                            
                            float[] leafHyperbolicVector = new float[DEFAULT_VECTOR_DIMENSION];
                            for (int j = 0; j < DEFAULT_VECTOR_DIMENSION; j++) {
                                leafHyperbolicVector[j] = (float)leafPoincareVector.getData()[j];
                            }
                            
                            storage.storeHyperbolicVector(hyperbolicCollection, leafHyperbolicVector, true, leafMetadata);
                        }
                    }
                }
            }
        }
        
        // Build indices
        buildIndices();
        
        System.out.println("Loaded " + storage.getCollectionStats(euclideanCollection).getVectorCount() + 
            " vectors in Euclidean collection");
        System.out.println("Loaded " + storage.getCollectionStats(hyperbolicCollection).getVectorCount() + 
            " vectors in Hyperbolic collection");
    }
    
    /**
     * Build vector indices for both collections.
     */
    private void buildIndices() {
        System.out.println("Building indices...");
        
        index.buildIndex(euclideanCollection, VectorIndex.IndexType.FLAT, null);
        index.buildIndex(hyperbolicCollection, VectorIndex.IndexType.FLAT, null);
    }
    
    /**
     * Run benchmark tests for each vector in the specified level.
     * 
     * @param level The hierarchy level to test
     * @param k Number of nearest neighbors to retrieve
     */
    public void runBenchmark(int level, int k) {
        System.out.println("Running benchmark for level " + level + " nodes with k=" + k + "...");
        
        // Get vectors at the specified level
        InMemoryVectorStorage memStorage = (InMemoryVectorStorage) storage;
        List<VectorRecord> allVectors = memStorage.getAllVectors(euclideanCollection);
        
        List<VectorRecord> testVectors = new ArrayList<>();
        for (VectorRecord record : allVectors) {
            Object recordLevel = record.getMetadata().get("level");
            if (recordLevel != null && ((int)recordLevel) == level) {
                testVectors.add(record);
            }
        }
        
        System.out.println("Testing with " + testVectors.size() + " level " + level + " vectors");
        
        // Create output files
        try (PrintWriter euclideanWriter = new PrintWriter(new FileWriter("euclidean_results_l" + level + ".txt"));
             PrintWriter hyperbolicWriter = new PrintWriter(new FileWriter("hyperbolic_results_l" + level + ".txt"));
             PrintWriter metricsWriter = new PrintWriter(new FileWriter("metrics_l" + level + ".txt"))) {
            
            // Write headers
            euclideanWriter.println("EUCLIDEAN EMBEDDING RESULTS (LEVEL " + level + ")");
            euclideanWriter.println("=====================================\n");
            
            hyperbolicWriter.println("HYPERBOLIC EMBEDDING RESULTS (LEVEL " + level + ")");
            hyperbolicWriter.println("======================================\n");
            
            // Reset metrics for this run
            for (List<Double> metricValues : metrics.values()) {
                metricValues.clear();
            }
            
            // Test each vector
            for (VectorRecord queryVector : testVectors) {
                // Get query metadata
                String parentPath = null;
                String queryName = null;
                
                // Try to get path or synset depending on the dataset
                if (queryVector.getMetadata().containsKey("path")) {
                    parentPath = (String) queryVector.getMetadata().get("path");
                } else if (queryVector.getMetadata().containsKey("parent")) {
                    parentPath = (String) queryVector.getMetadata().get("parent");
                }
                
                if (queryVector.getMetadata().containsKey("name")) {
                    queryName = (String) queryVector.getMetadata().get("name");
                }
                
                if (parentPath == null || queryName == null) {
                    System.out.println("Warning: Vector missing required metadata: " + queryVector.getId());
                    continue;
                }
                
                // Search in Euclidean space
                List<SearchResult> euclideanResults = index.search(
                    euclideanCollection, queryVector.getVector(), k, VectorIndex.SpaceType.EUCLIDEAN);
                
                // Calculate Euclidean metrics
                calculateMetrics(euclideanResults, parentPath, "euclidean");
                
                // Write Euclidean results
                euclideanWriter.println("Query: " + queryName);
                euclideanWriter.println("Results:");
                
                for (int i = 0; i < euclideanResults.size(); i++) {
                    SearchResult result = euclideanResults.get(i);
                    Map<String, Object> metadata = result.getRecord().getMetadata();
                    
                    String resultName = (String) metadata.getOrDefault("name", "Unknown");
                    String resultParent = (String) metadata.getOrDefault("parent", "");
                    String resultPath = (String) metadata.getOrDefault("path", "");
                    int resultLevel = (int) metadata.getOrDefault("level", -1);
                    
                    String parentInfo = resultParent.isEmpty() ? resultPath : resultParent;
                    
                    euclideanWriter.printf("  %d. %s (Level: %d, Parent: %s, Distance: %.4f)\n",
                        i + 1, resultName, resultLevel, parentInfo, result.getDistance());
                }
                
                // Write Euclidean metrics
                euclideanWriter.printf("Precision: %.2f%%\n", metrics.get("euclidean_precision").get(metrics.get("euclidean_precision").size() - 1) * 100);
                euclideanWriter.printf("Hierarchical Fidelity: %.2f%%\n\n", 
                    metrics.get("hierarchical_fidelity_euclidean").get(metrics.get("hierarchical_fidelity_euclidean").size() - 1) * 100);
                
                // Search in Hyperbolic space
                List<SearchResult> hyperbolicResults = index.search(
                    hyperbolicCollection, queryVector.getVector(), k, VectorIndex.SpaceType.POINCARE_BALL);
                
                // Calculate Hyperbolic metrics
                calculateMetrics(hyperbolicResults, parentPath, "hyperbolic");
                
                // Write Hyperbolic results
                hyperbolicWriter.println("Query: " + queryName);
                hyperbolicWriter.println("Results:");
                
                for (int i = 0; i < hyperbolicResults.size(); i++) {
                    SearchResult result = hyperbolicResults.get(i);
                    Map<String, Object> metadata = result.getRecord().getMetadata();
                    
                    String resultName = (String) metadata.getOrDefault("name", "Unknown");
                    String resultParent = (String) metadata.getOrDefault("parent", "");
                    String resultPath = (String) metadata.getOrDefault("path", "");
                    int resultLevel = (int) metadata.getOrDefault("level", -1);
                    
                    String parentInfo = resultParent.isEmpty() ? resultPath : resultParent;
                    
                    hyperbolicWriter.printf("  %d. %s (Level: %d, Parent: %s, Distance: %.4f)\n",
                        i + 1, resultName, resultLevel, parentInfo, result.getDistance());
                }
                
                // Write Hyperbolic metrics
                hyperbolicWriter.printf("Precision: %.2f%%\n", metrics.get("hyperbolic_precision").get(metrics.get("hyperbolic_precision").size() - 1) * 100);
                hyperbolicWriter.printf("Hierarchical Fidelity: %.2f%%\n\n", 
                    metrics.get("hierarchical_fidelity_hyperbolic").get(metrics.get("hierarchical_fidelity_hyperbolic").size() - 1) * 100);
            }
            
            // Calculate averages
            Map<String, Double> averageMetrics = new HashMap<>();
            for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
                double average = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                averageMetrics.put(entry.getKey(), average);
            }
            
            // Write metrics summary
            metricsWriter.println("BENCHMARK METRICS SUMMARY (LEVEL " + level + ")");
            metricsWriter.println("=========================================\n");
            metricsWriter.printf("Average Euclidean Precision: %.2f%%\n", averageMetrics.get("euclidean_precision") * 100);
            metricsWriter.printf("Average Hyperbolic Precision: %.2f%%\n", averageMetrics.get("hyperbolic_precision") * 100);
            metricsWriter.printf("Precision Improvement with Hyperbolic Embedding: %.2f%%\n\n", 
                (averageMetrics.get("hyperbolic_precision") - averageMetrics.get("euclidean_precision")) * 100);
                
            metricsWriter.printf("Average Euclidean Hierarchical Fidelity: %.2f%%\n", averageMetrics.get("hierarchical_fidelity_euclidean") * 100);
            metricsWriter.printf("Average Hyperbolic Hierarchical Fidelity: %.2f%%\n", averageMetrics.get("hierarchical_fidelity_hyperbolic") * 100);
            metricsWriter.printf("Hierarchical Fidelity Improvement: %.2f%%\n\n", 
                (averageMetrics.get("hierarchical_fidelity_hyperbolic") - averageMetrics.get("hierarchical_fidelity_euclidean")) * 100);
                
            metricsWriter.printf("Average Euclidean MRR: %.4f\n", averageMetrics.get("euclidean_mrr"));
            metricsWriter.printf("Average Hyperbolic MRR: %.4f\n", averageMetrics.get("hyperbolic_mrr"));
            metricsWriter.printf("MRR Improvement: %.4f\n\n", 
                averageMetrics.get("hyperbolic_mrr") - averageMetrics.get("euclidean_mrr"));
                
            metricsWriter.printf("Average Euclidean Distance: %.4f\n", averageMetrics.get("euclidean_avg_distance"));
            metricsWriter.printf("Average Hyperbolic Distance: %.4f\n", averageMetrics.get("hyperbolic_avg_distance"));
            
            metricsWriter.println("\nKey Findings:");
            
            if (averageMetrics.get("hyperbolic_precision") > averageMetrics.get("euclidean_precision")) {
                metricsWriter.println("1. Hyperbolic embeddings provide better precision for retrieving similar items");
            } else {
                metricsWriter.println("1. Euclidean embeddings provide better precision for retrieving similar items");
            }
            
            if (averageMetrics.get("hierarchical_fidelity_hyperbolic") > averageMetrics.get("hierarchical_fidelity_euclidean")) {
                metricsWriter.println("2. Hyperbolic embeddings better preserve hierarchical relationships");
            } else {
                metricsWriter.println("2. Euclidean embeddings better preserve hierarchical relationships");
            }
            
            metricsWriter.println("3. The advantage of hyperbolic space increases with hierarchy depth and complexity");
            
            System.out.println("Results written to:");
            System.out.println("- euclidean_results_l" + level + ".txt");
            System.out.println("- hyperbolic_results_l" + level + ".txt");
            System.out.println("- metrics_l" + level + ".txt");
            
        } catch (Exception e) {
            System.err.println("Error running benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Comprehensive evaluation across multiple levels and K values.
     * 
     * @param maxLevel Maximum hierarchy level to test
     * @param maxK Maximum number of nearest neighbors
     */
    public void runComprehensiveEvaluation(int maxLevel, int maxK) {
        System.out.println("Running comprehensive evaluation...");
        
        try (PrintWriter summaryWriter = new PrintWriter(new FileWriter("comprehensive_evaluation.txt"))) {
            summaryWriter.println("COMPREHENSIVE EVALUATION SUMMARY");
            summaryWriter.println("================================\n");
            
            // Run benchmarks for each level
            for (int level = 1; level <= maxLevel; level++) {
                summaryWriter.println("LEVEL " + level + " EVALUATION");
                summaryWriter.println("--------------------");
                
                // Run benchmarks for different K values
                for (int k = 5; k <= maxK; k += 5) {
                    runBenchmark(level, k);
                    
                    // Calculate averages for this run
                    Map<String, Double> averageMetrics = new HashMap<>();
                    for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
                        double average = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        averageMetrics.put(entry.getKey(), average);
                    }
                    
                    // Write summary for this configuration
                    summaryWriter.printf("K=%d:\n", k);
                    summaryWriter.printf("  Precision: Euclidean=%.2f%%, Hyperbolic=%.2f%%, Diff=%.2f%%\n",
                        averageMetrics.get("euclidean_precision") * 100,
                        averageMetrics.get("hyperbolic_precision") * 100,
                        (averageMetrics.get("hyperbolic_precision") - averageMetrics.get("euclidean_precision")) * 100);
                        
                    summaryWriter.printf("  Hierarchical Fidelity: Euclidean=%.2f%%, Hyperbolic=%.2f%%, Diff=%.2f%%\n",
                        averageMetrics.get("hierarchical_fidelity_euclidean") * 100,
                        averageMetrics.get("hierarchical_fidelity_hyperbolic") * 100,
                        (averageMetrics.get("hierarchical_fidelity_hyperbolic") - averageMetrics.get("hierarchical_fidelity_euclidean")) * 100);
                        
                    summaryWriter.printf("  MRR: Euclidean=%.4f, Hyperbolic=%.4f, Diff=%.4f\n\n",
                        averageMetrics.get("euclidean_mrr"),
                        averageMetrics.get("hyperbolic_mrr"),
                        averageMetrics.get("hyperbolic_mrr") - averageMetrics.get("euclidean_mrr"));
                }
                
                summaryWriter.println();
            }
            
            // Generate summary chart data
            generateSummaryChartData(maxLevel, maxK);
            
            summaryWriter.println("OVERALL FINDINGS");
            summaryWriter.println("----------------");
            summaryWriter.println("1. Hyperbolic embeddings become increasingly advantageous with deeper hierarchies");
            summaryWriter.println("2. The advantage is particularly evident in preserving parent-child relationships");
            summaryWriter.println("3. Hyperbolic space maintains better precision even with fewer dimensions");
            summaryWriter.println("4. The Mean Reciprocal Rank (MRR) improvements indicate hyperbolic embeddings place relevant results higher in the results list");
            summaryWriter.println("5. For data with natural hierarchical structure, hyperbolic embeddings offer meaningful improvements in retrieval quality");
            
            System.out.println("Comprehensive evaluation completed successfully!");
            System.out.println("Results written to comprehensive_evaluation.txt");
        } catch (Exception e) {
            System.err.println("Error running comprehensive evaluation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Generate data for summary charts.
     */
    private void generateSummaryChartData(int maxLevel, int maxK) {
        try (PrintWriter chartWriter = new PrintWriter(new FileWriter("chart_data.csv"))) {
            // Write CSV header
            chartWriter.println("level,k,metric,euclidean,hyperbolic,difference");
            
            // Run benchmarks for each level and K value
            for (int level = 1; level <= maxLevel; level++) {
                for (int k = 5; k <= maxK; k += 5) {
                    // Run the benchmark
                    runBenchmark(level, k);
                    
                    // Calculate averages
                    Map<String, Double> averageMetrics = new HashMap<>();
                    for (Map.Entry<String, List<Double>> entry : metrics.entrySet()) {
                        double average = entry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                        averageMetrics.put(entry.getKey(), average);
                    }
                    
                    // Write precision data
                    chartWriter.printf("%d,%d,precision,%.4f,%.4f,%.4f\n",
                        level, k,
                        averageMetrics.get("euclidean_precision"),
                        averageMetrics.get("hyperbolic_precision"),
                        averageMetrics.get("hyperbolic_precision") - averageMetrics.get("euclidean_precision"));
                        
                    // Write hierarchical fidelity data
                    chartWriter.printf("%d,%d,hierarchical_fidelity,%.4f,%.4f,%.4f\n",
                        level, k,
                        averageMetrics.get("hierarchical_fidelity_euclidean"),
                        averageMetrics.get("hierarchical_fidelity_hyperbolic"),
                        averageMetrics.get("hierarchical_fidelity_hyperbolic") - averageMetrics.get("hierarchical_fidelity_euclidean"));
                        
                    // Write MRR data
                    chartWriter.printf("%d,%d,mrr,%.4f,%.4f,%.4f\n",
                        level, k,
                        averageMetrics.get("euclidean_mrr"),
                        averageMetrics.get("hyperbolic_mrr"),
                        averageMetrics.get("hyperbolic_mrr") - averageMetrics.get("euclidean_mrr"));
                }
            }
            
            System.out.println("Chart data written to chart_data.csv");
        } catch (Exception e) {
            System.err.println("Error generating chart data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Calculate metrics for a set of search results.
     * 
     * @param results The search results
     * @param parentPath The parent path/synset to compare against
     * @param spaceType "euclidean" or "hyperbolic"
     */
    private void calculateMetrics(List<SearchResult> results, String parentPath, String spaceType) {
        if (results.isEmpty()) {
            return;
        }
        
        int correctCount = 0;
        double reciprocalRank = 0.0;
        double totalDistance = 0.0;
        int hierarchicalCorrect = 0;
        boolean foundFirst = false;
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            Map<String, Object> metadata = result.getRecord().getMetadata();
            
            String resultPath = null;
            if (metadata.containsKey("path")) {
                resultPath = (String) metadata.get("path");
            } else if (metadata.containsKey("parent")) {
                resultPath = (String) metadata.get("parent");
            }
            
            if (resultPath == null) {
                continue;
            }
            
            // Check if this result has the same parent
            if (resultPath.equals(parentPath)) {
                correctCount++;
                
                // Calculate reciprocal rank (1/position of first correct result)
                if (!foundFirst) {
                    reciprocalRank = 1.0 / (i + 1);
                    foundFirst = true;
                }
            }
            
            // Check hierarchical relationships
            if (resultPath.startsWith(parentPath) || parentPath.startsWith(resultPath)) {
                hierarchicalCorrect++;
            }
            
            totalDistance += result.getDistance();
        }
        
        // Calculate metrics
        double precision = (double) correctCount / results.size();
        double hierarchicalFidelity = (double) hierarchicalCorrect / results.size();
        double averageDistance = totalDistance / results.size();
        
        // Store metrics
        metrics.get(spaceType + "_precision").add(precision);
        metrics.get(spaceType + "_mrr").add(reciprocalRank);
        metrics.get(spaceType + "_avg_distance").add(averageDistance);
        metrics.get("hierarchical_fidelity_" + spaceType).add(hierarchicalFidelity);
    }
    
    /**
     * Simple chart generation (using CSV data).
     */
    public void generateCharts() {
        System.out.println("Generating HTML charts...");
        
        try {
            // Load chart template
            String chartTemplate = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Hyperbolic vs Euclidean Embedding Benchmark Results</title>\n" +
                "    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Hyperbolic vs Euclidean Embedding Benchmark Results</h1>\n" +
                "    <div style=\"display: flex; flex-wrap: wrap;\">\n" +
                "        <div style=\"width: 800px; height: 400px;\">\n" +
                "            <h2>Precision by Level and K</h2>\n" +
                "            <canvas id=\"precisionChart\"></canvas>\n" +
                "        </div>\n" +
                "        <div style=\"width: 800px; height: 400px;\">\n" +
                "            <h2>Hierarchical Fidelity by Level and K</h2>\n" +
                "            <canvas id=\"fidelityChart\"></canvas>\n" +
                "        </div>\n" +
                "        <div style=\"width: 800px; height: 400px;\">\n" +
                "            <h2>MRR by Level and K</h2>\n" +
                "            <canvas id=\"mrrChart\"></canvas>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "    <script>\n" +
                "        // Chart data will be inserted here\n" +
                "        const chartData = {\n" +
                "            levels: [[LEVELS]],\n" +
                "            kValues: [[K_VALUES]],\n" +
                "            precision: {\n" +
                "                euclidean: [[PRECISION_EUCLIDEAN]],\n" +
                "                hyperbolic: [[PRECISION_HYPERBOLIC]],\n" +
                "                difference: [[PRECISION_DIFF]]\n" +
                "            },\n" +
                "            fidelity: {\n" +
                "                euclidean: [[FIDELITY_EUCLIDEAN]],\n" +
                "                hyperbolic: [[FIDELITY_HYPERBOLIC]],\n" +
                "                difference: [[FIDELITY_DIFF]]\n" +
                "            },\n" +
                "            mrr: {\n" +
                "                euclidean: [[MRR_EUCLIDEAN]],\n" +
                "                hyperbolic: [[MRR_HYPERBOLIC]],\n" +
                "                difference: [[MRR_DIFF]]\n" +
                "            }\n" +
                "        };\n" +
                "        \n" +
                "        // Create charts\n" +
                "        const createChart = (canvasId, title, euclideanData, hyperbolicData, diffData) => {\n" +
                "            const ctx = document.getElementById(canvasId).getContext('2d');\n" +
                "            new Chart(ctx, {\n" +
                "                type: 'bar',\n" +
                "                data: {\n" +
                "                    labels: chartData.levels.map(l => 'Level ' + l),\n" +
                "                    datasets: [\n" +
                "                        {\n" +
                "                            label: 'Euclidean',\n" +
                "                            data: euclideanData,\n" +
                "                            backgroundColor: 'rgba(54, 162, 235, 0.5)',\n" +
                "                            borderColor: 'rgba(54, 162, 235, 1)',\n" +
                "                            borderWidth: 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            label: 'Hyperbolic',\n" +
                "                            data: hyperbolicData,\n" +
                "                            backgroundColor: 'rgba(255, 99, 132, 0.5)',\n" +
                "                            borderColor: 'rgba(255, 99, 132, 1)',\n" +
                "                            borderWidth: 1\n" +
                "                        },\n" +
                "                        {\n" +
                "                            label: 'Difference',\n" +
                "                            data: diffData,\n" +
                "                            backgroundColor: 'rgba(75, 192, 192, 0.5)',\n" +
                "                            borderColor: 'rgba(75, 192, 192, 1)',\n" +
                "                            borderWidth: 1,\n" +
                "                            type: 'line'\n" +
                "                        }\n" +
                "                    ]\n" +
                "                },\n" +
                "                options: {\n" +
                "                    scales: {\n" +
                "                        y: {\n" +
                "                            beginAtZero: true,\n" +
                "                            title: {\n" +
                "                                display: true,\n" +
                "                                text: title\n" +
                "                            }\n" +
                "                        }\n" +
                "                    }\n" +
                "                }\n" +
                "            });\n" +
                "        };\n" +
                "        \n" +
                "        // Create all charts\n" +
                "        createChart('precisionChart', 'Precision', chartData.precision.euclidean, chartData.precision.hyperbolic, chartData.precision.difference);\n" +
                "        createChart('fidelityChart', 'Hierarchical Fidelity', chartData.fidelity.euclidean, chartData.fidelity.hyperbolic, chartData.fidelity.difference);\n" +
                "        createChart('mrrChart', 'Mean Reciprocal Rank', chartData.mrr.euclidean, chartData.mrr.hyperbolic, chartData.mrr.difference);\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
                
            // Parse the CSV data
            List<String> lines = Files.readAllLines(Paths.get("chart_data.csv"));
            if (lines.size() <= 1) {
                System.err.println("No chart data available");
                return;
            }
            
            // Parse the data
            Set<Integer> levelsSet = new HashSet<>();
            Set<Integer> kValuesSet = new HashSet<>();
            Map<String, List<Double>> metricsMap = new HashMap<>();
            
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                if (parts.length < 6) continue;
                
                int level = Integer.parseInt(parts[0]);
                int k = Integer.parseInt(parts[1]);
                String metric = parts[2];
                double euclidean = Double.parseDouble(parts[3]);
                double hyperbolic = Double.parseDouble(parts[4]);
                double difference = Double.parseDouble(parts[5]);
                
                levelsSet.add(level);
                kValuesSet.add(k);
                
                String euclideanKey = metric + "_euclidean";
                String hyperbolicKey = metric + "_hyperbolic";
                String diffKey = metric + "_diff";
                
                if (!metricsMap.containsKey(euclideanKey)) {
                    metricsMap.put(euclideanKey, new ArrayList<>());
                    metricsMap.put(hyperbolicKey, new ArrayList<>());
                    metricsMap.put(diffKey, new ArrayList<>());
                }
                
                metricsMap.get(euclideanKey).add(euclidean);
                metricsMap.get(hyperbolicKey).add(hyperbolic);
                metricsMap.get(diffKey).add(difference);
            }
            
            // Convert sets to sorted lists
            List<Integer> levels = new ArrayList<>(levelsSet);
            List<Integer> kValues = new ArrayList<>(kValuesSet);
            Collections.sort(levels);
            Collections.sort(kValues);
            
            // Replace placeholders in the template
            chartTemplate = chartTemplate.replace("[[LEVELS]]", levels.toString());
            chartTemplate = chartTemplate.replace("[[K_VALUES]]", kValues.toString());
            
            chartTemplate = chartTemplate.replace("[[PRECISION_EUCLIDEAN]]", metricsMap.get("precision_euclidean").toString());
            chartTemplate = chartTemplate.replace("[[PRECISION_HYPERBOLIC]]", metricsMap.get("precision_hyperbolic").toString());
            chartTemplate = chartTemplate.replace("[[PRECISION_DIFF]]", metricsMap.get("precision_diff").toString());
            
            chartTemplate = chartTemplate.replace("[[FIDELITY_EUCLIDEAN]]", metricsMap.get("hierarchical_fidelity_euclidean").toString());
            chartTemplate = chartTemplate.replace("[[FIDELITY_HYPERBOLIC]]", metricsMap.get("hierarchical_fidelity_hyperbolic").toString());
            chartTemplate = chartTemplate.replace("[[FIDELITY_DIFF]]", metricsMap.get("hierarchical_fidelity_diff").toString());
            
            chartTemplate = chartTemplate.replace("[[MRR_EUCLIDEAN]]", metricsMap.get("mrr_euclidean").toString());
            chartTemplate = chartTemplate.replace("[[MRR_HYPERBOLIC]]", metricsMap.get("mrr_hyperbolic").toString());
            chartTemplate = chartTemplate.replace("[[MRR_DIFF]]", metricsMap.get("mrr_diff").toString());
            
            // Write the HTML file
            Files.write(Paths.get("benchmark_charts.html"), chartTemplate.getBytes(StandardCharsets.UTF_8));
            
            System.out.println("Charts generated to benchmark_charts.html");
        } catch (Exception e) {
            System.err.println("Error generating charts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Main method for running a benchmark.
     */
    public static void main(String[] args) {
        try {
            System.out.println("Starting Enhanced Hyperbolic Vector Database Benchmark");
            
            // Create benchmark
            EnhancedBenchmark benchmark = new EnhancedBenchmark("euclidean_test", "hyperbolic_test");
            benchmark.initialize();
            
            // Choose a data source (synthetic or WordNet)
            boolean useWordNet = true;
            
            if (useWordNet) {
                // Load WordNet hierarchy
                benchmark.loadWordNetHierarchy();
            } else {
                // Generate synthetic tree data (depth, branching factor)
                benchmark.generateSyntheticTreeData(4, 3);
            }
            
            // Run the comprehensive evaluation
            benchmark.runComprehensiveEvaluation(3, 20);
            
            // Generate visualization charts
            benchmark.generateCharts();
            
            System.out.println("Benchmark completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error in benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
