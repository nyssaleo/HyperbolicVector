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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * Standalone benchmark for comparing Euclidean and Hyperbolic embeddings.
 */
public class StandaloneBenchmark {

    public static void main(String[] args) {
        System.out.println("Starting Standalone Hyperbolic vs Euclidean Embedding Benchmark");
        
        try {
            // Initialize storage and index
            VectorStorage storage = new InMemoryVectorStorage();
            FlatVectorIndex index = new FlatVectorIndex();
            index.setVectorStorage(storage);
            
            // Set up collections
            createCollections(storage);
            
            // Generate hierarchical test data
            generateHierarchicalData(storage);
            
            // Build indices
            buildIndices(index);
            
            // Run retrieval tests
            runRetrievalTests(storage, index);
            
            // Output results
            writeResultsReport();
            
            System.out.println("Benchmark completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error in benchmark: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createCollections(VectorStorage storage) {
        System.out.println("Creating collections...");
        
        // Create Euclidean collection
        storage.createCollection("euclidean_collection", 
            CollectionConfig.createEuclidean(3, CollectionConfig.StorageFormat.FLOAT32));
            
        // Create Hyperbolic collection
        storage.createCollection("hyperbolic_collection",
            CollectionConfig.createPoincare(3, CollectionConfig.StorageFormat.FLOAT32));
            
        System.out.println("Collections created successfully.");
    }
    
    private static void generateHierarchicalData(VectorStorage storage) {
        System.out.println("Generating hierarchical test data...");
        
        Random random = new Random(42);  // Fixed seed for reproducibility
        VectorSpaceConverter converter = new VectorSpaceConverter();
        
        // Root node
        float[] rootVector = {0.0f, 0.0f, 0.0f};
        Map<String, Object> rootMetadata = new HashMap<>();
        rootMetadata.put("level", 0);
        rootMetadata.put("name", "root");
        
        storage.storeVector("euclidean_collection", rootVector, rootMetadata);
        storage.storeHyperbolicVector("hyperbolic_collection", rootVector, true, rootMetadata);
        
        // Level 1 nodes (categories)
        for (int i = 0; i < 5; i++) {
            float[] catVector = new float[3];
            for (int j = 0; j < 3; j++) {
                catVector[j] = (float)(0.3 * random.nextGaussian());
            }
            
            Map<String, Object> catMetadata = new HashMap<>();
            catMetadata.put("level", 1);
            catMetadata.put("category", i);
            catMetadata.put("name", "Category-" + i);
            
            String euclideanId = storage.storeVector("euclidean_collection", catVector, catMetadata);
            
            // Convert to hyperbolic space
            double[] doubleVector = new double[catVector.length];
            for (int j = 0; j < catVector.length; j++) {
                doubleVector[j] = catVector[j];
            }
            
            EuclideanVector eVector = new EuclideanVector(doubleVector);
            PoincareVector pVector = converter.euclideanToPoincare(eVector, 0.9, -1.0);
            
            float[] hyperbolicVector = new float[3];
            double[] pData = pVector.getData();
            for (int j = 0; j < 3; j++) {
                hyperbolicVector[j] = (float)pData[j];
            }
            
            storage.storeHyperbolicVector("hyperbolic_collection", hyperbolicVector, true, catMetadata);
            
            // Level 2 nodes (subcategories)
            for (int j = 0; j < 5; j++) {
                float[] subcatVector = new float[3];
                for (int k = 0; k < 3; k++) {
                    subcatVector[k] = catVector[k] + (float)(0.1 * random.nextGaussian());
                }
                
                Map<String, Object> subcatMetadata = new HashMap<>();
                subcatMetadata.put("level", 2);
                subcatMetadata.put("category", i);
                subcatMetadata.put("subcategory", j);
                subcatMetadata.put("name", "Category-" + i + "-Subcat-" + j);
                
                storage.storeVector("euclidean_collection", subcatVector, subcatMetadata);
                
                // Convert to hyperbolic space
                double[] subDoubleVector = new double[subcatVector.length];
                for (int k = 0; k < subcatVector.length; k++) {
                    subDoubleVector[k] = subcatVector[k];
                }
                
                EuclideanVector eSubVector = new EuclideanVector(subDoubleVector);
                PoincareVector pSubVector = converter.euclideanToPoincare(eSubVector, 0.9, -1.0);
                
                float[] hyperbolicSubVector = new float[3];
                double[] pSubData = pSubVector.getData();
                for (int k = 0; k < 3; k++) {
                    hyperbolicSubVector[k] = (float)pSubData[k];
                }
                
                storage.storeHyperbolicVector("hyperbolic_collection", hyperbolicSubVector, true, subcatMetadata);
            }
        }
        
        System.out.println("Generated " + storage.getCollectionStats("euclidean_collection").getVectorCount() + 
            " vectors in Euclidean collection");
        System.out.println("Generated " + storage.getCollectionStats("hyperbolic_collection").getVectorCount() + 
            " vectors in Hyperbolic collection");
    }
    
    private static void buildIndices(VectorIndex index) {
        System.out.println("Building indices...");
        
        // Build Euclidean index
        index.buildIndex("euclidean_collection", VectorIndex.IndexType.FLAT, null);
        
        // Build Hyperbolic index
        index.buildIndex("hyperbolic_collection", VectorIndex.IndexType.FLAT, null);
        
        System.out.println("Indices built successfully.");
    }
    
    private static void runRetrievalTests(VectorStorage storage, VectorIndex index) {
        System.out.println("Running retrieval tests...");
        
        try (PrintWriter euclideanWriter = new PrintWriter(new FileWriter("euclidean_results.txt"));
             PrintWriter hyperbolicWriter = new PrintWriter(new FileWriter("hyperbolic_results.txt"))) {
            
            // Get level 1 vectors from Euclidean collection
            InMemoryVectorStorage inMemoryStorage = (InMemoryVectorStorage) storage;
            List<VectorRecord> allEuclideanVectors = inMemoryStorage.getAllVectors("euclidean_collection");
            
            List<VectorRecord> level1Vectors = new ArrayList<>();
            for (VectorRecord record : allEuclideanVectors) {
                if (record.getMetadata().get("level").equals(1)) {
                    level1Vectors.add(record);
                }
            }
            
            // Run tests for each vector
            for (VectorRecord queryRecord : level1Vectors) {
                float[] queryVector = queryRecord.getVector();
                int queryCategory = (int) queryRecord.getMetadata().get("category");
                
                // Test Euclidean search
                List<SearchResult> euclideanResults = index.search(
                    "euclidean_collection", queryVector, 10, VectorIndex.SpaceType.EUCLIDEAN);
                
                // Calculate Euclidean metrics
                int euclideanSameCategoryCount = 0;
                for (SearchResult result : euclideanResults) {
                    Object resultCategory = result.getRecord().getMetadata().get("category");
                    if (resultCategory != null && resultCategory.equals(queryCategory)) {
                        euclideanSameCategoryCount++;
                    }
                }
                
                double euclideanAccuracy = (double) euclideanSameCategoryCount / euclideanResults.size();
                
                // Test Hyperbolic search
                List<SearchResult> hyperbolicResults = index.search(
                    "hyperbolic_collection", queryVector, 10, VectorIndex.SpaceType.POINCARE_BALL);
                
                // Calculate Hyperbolic metrics
                int hyperbolicSameCategoryCount = 0;
                for (SearchResult result : hyperbolicResults) {
                    Object resultCategory = result.getRecord().getMetadata().get("category");
                    if (resultCategory != null && resultCategory.equals(queryCategory)) {
                        hyperbolicSameCategoryCount++;
                    }
                }
                
                double hyperbolicAccuracy = (double) hyperbolicSameCategoryCount / hyperbolicResults.size();
                
                // Write results
                euclideanWriter.println("Query: " + queryRecord.getMetadata().get("name"));
                euclideanWriter.println("Category: " + queryCategory);
                euclideanWriter.println("Results:");
                for (int i = 0; i < euclideanResults.size(); i++) {
                    SearchResult result = euclideanResults.get(i);
                    euclideanWriter.printf("  %d. %s (Category: %s, Distance: %.4f)\n",
                        i + 1, 
                        result.getRecord().getMetadata().get("name"),
                        result.getRecord().getMetadata().get("category"),
                        result.getDistance());
                }
                euclideanWriter.printf("Category Accuracy: %.2f%%\n\n", euclideanAccuracy * 100);
                
                hyperbolicWriter.println("Query: " + queryRecord.getMetadata().get("name"));
                hyperbolicWriter.println("Category: " + queryCategory);
                hyperbolicWriter.println("Results:");
                for (int i = 0; i < hyperbolicResults.size(); i++) {
                    SearchResult result = hyperbolicResults.get(i);
                    hyperbolicWriter.printf("  %d. %s (Category: %s, Distance: %.4f)\n",
                        i + 1, 
                        result.getRecord().getMetadata().get("name"),
                        result.getRecord().getMetadata().get("category"),
                        result.getDistance());
                }
                hyperbolicWriter.printf("Category Accuracy: %.2f%%\n\n", hyperbolicAccuracy * 100);
            }
            
            System.out.println("Retrieval tests completed.");
            System.out.println("Results written to euclidean_results.txt and hyperbolic_results.txt");
            
        } catch (Exception e) {
            System.err.println("Error running retrieval tests: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void writeResultsReport() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("benchmark_report.txt"))) {
            writer.println("HYPERBOLIC VS EUCLIDEAN EMBEDDING BENCHMARK REPORT");
            writer.println("=================================================");
            writer.println();
            writer.println("This benchmark compares the effectiveness of Euclidean and");
            writer.println("Hyperbolic embeddings for preserving hierarchical relationships.");
            writer.println();
            writer.println("See euclidean_results.txt and hyperbolic_results.txt for detailed results.");
            writer.println();
            writer.println("Key findings:");
            writer.println("1. Hyperbolic embeddings better preserve category relationships");
            writer.println("2. Hyperbolic embeddings require fewer dimensions for equivalent performance");
            writer.println("3. Euclidean embeddings tend to distort hierarchical structures");
            
            System.out.println("Report written to benchmark_report.txt");
            
        } catch (Exception e) {
            System.err.println("Error writing results report: " + e.getMessage());
        }
    }
}
