package com.hypervector;

import com.hypervector.index.common.FlatVectorIndex;
import com.hypervector.index.common.SearchResult;
import com.hypervector.index.common.VectorIndex;
import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.conversion.VectorSpaceConverter;
import com.hypervector.math.hyperbolic.PoincareVector;
import com.hypervector.storage.common.CollectionConfig;
import com.hypervector.storage.common.InMemoryVectorStorage;
import com.hypervector.storage.common.VectorRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Demo application to showcase the hyperbolic vector database capabilities.
 */
public class HyperbolicVectorDBDemo {

    private static final int VECTOR_DIMENSION = 3;
    private static final int NUM_VECTORS = 1000;
    private static final String EUCLIDEAN_COLLECTION = "euclidean_vectors";
    private static final String HYPERBOLIC_COLLECTION = "hyperbolic_vectors";
    private static final double MAX_RADIUS = 0.9; // Maximum radius for Poincaré ball

    public static void main(String[] args) {
        System.out.println("Hyperbolic Vector Database Demo");
        System.out.println("===============================\n");

        // Set up storage and index
        InMemoryVectorStorage storage = new InMemoryVectorStorage();
        FlatVectorIndex index = new FlatVectorIndex();
        index.setVectorStorage(storage);
        VectorSpaceConverter converter = new VectorSpaceConverter();

        // Create collections
        storage.createCollection(EUCLIDEAN_COLLECTION,
                CollectionConfig.createEuclidean(VECTOR_DIMENSION, CollectionConfig.StorageFormat.FLOAT32));
        storage.createCollection(HYPERBOLIC_COLLECTION,
                CollectionConfig.createPoincare(VECTOR_DIMENSION, CollectionConfig.StorageFormat.FLOAT32));

        // Generate hierarchical test data
        System.out.println("Generating hierarchical test data...");
        generateHierarchicalTestData(storage);

        // Build indices
        System.out.println("Building indices...");
        index.buildIndex(EUCLIDEAN_COLLECTION, VectorIndex.IndexType.FLAT, null);
        index.buildIndex(HYPERBOLIC_COLLECTION, VectorIndex.IndexType.FLAT, null);

        // Perform search tests
        System.out.println("\nPerforming search tests...");

        // Test 1: Query for a vector that belongs to a specific category
        float[] euclideanQueryVector = new float[]{0.3f, 0.4f, 0.5f};

        // Transform the query vector for hyperbolic space
        EuclideanVector eQueryVector = new EuclideanVector(toDoubleArray(euclideanQueryVector));
        PoincareVector pQueryVector = converter.euclideanToPoincare(eQueryVector, MAX_RADIUS, -1.0);
        float[] hyperbolicQueryVector = toFloatArray(pQueryVector.getData());

        System.out.println("\nTest 1: Searching for similar vectors");
        System.out.println("Euclidean Query: [" +
                euclideanQueryVector[0] + ", " +
                euclideanQueryVector[1] + ", " +
                euclideanQueryVector[2] + "]");
        System.out.println("Hyperbolic Query: [" +
                hyperbolicQueryVector[0] + ", " +
                hyperbolicQueryVector[1] + ", " +
                hyperbolicQueryVector[2] + "]");

        System.out.println("\nEuclidean Space Search Results:");
        List<SearchResult> euclideanResults = index.search(
                EUCLIDEAN_COLLECTION, euclideanQueryVector, 5, VectorIndex.SpaceType.EUCLIDEAN);
        printSearchResults(euclideanResults);

        System.out.println("\nHyperbolic Space Search Results:");
        List<SearchResult> hyperbolicResults = index.search(
                HYPERBOLIC_COLLECTION, hyperbolicQueryVector, 5, VectorIndex.SpaceType.POINCARE_BALL);
        printSearchResults(hyperbolicResults);

        // Test 2: Compare the hierarchical clustering effectiveness
        System.out.println("\nTest 2: Hierarchical Clustering Effectiveness");
        analyzeHierarchicalClustering(euclideanResults, hyperbolicResults);

        System.out.println("\nDemo completed successfully!");
    }

    /**
     * Generate hierarchical test data for both Euclidean and hyperbolic spaces.
     * The data simulates a tree-like structure with parent-child relationships.
     */
    private static void generateHierarchicalTestData(InMemoryVectorStorage storage) {
        Random random = new Random(42); // Fixed seed for reproducibility
        VectorSpaceConverter converter = new VectorSpaceConverter();

        // Create vector groups representing a hierarchical structure
        // Level 0: Root
        // Level 1: 5 main categories
        // Level 2: 10 subcategories per category
        // Level 3: 20 items per subcategory

        EuclideanVector rootE = EuclideanVector.zeros(VECTOR_DIMENSION);

        // Convert to Poincaré representation
        PoincareVector rootP = converter.euclideanToPoincare(rootE, MAX_RADIUS, -1.0);

        // Level 1: Main categories
        for (int cat = 0; cat < 5; cat++) {
            // Generate a vector for this category in Euclidean space
            double[] catVectorData = new double[VECTOR_DIMENSION];
            for (int d = 0; d < VECTOR_DIMENSION; d++) {
                catVectorData[d] = 0.5 + random.nextGaussian() * 0.1;
            }

            EuclideanVector catVectorE = new EuclideanVector(catVectorData);

            // Store in Euclidean collection
            Map<String, Object> catMetadata = new HashMap<>();
            catMetadata.put("level", 1);
            catMetadata.put("category", cat);
            catMetadata.put("type", "category");

            storage.storeVector(EUCLIDEAN_COLLECTION, toFloatArray(catVectorData), catMetadata);

            // Convert to Poincaré and store
            PoincareVector catVectorP = converter.euclideanToPoincare(catVectorE, MAX_RADIUS, -1.0);

            storage.storeHyperbolicVector(HYPERBOLIC_COLLECTION,
                    toFloatArray(catVectorP.getData()), true, catMetadata);

            // Level 2: Subcategories
            for (int subcat = 0; subcat < 10; subcat++) {
                // Generate subcategory vector as a perturbation of the category vector
                double[] subcatVectorData = new double[VECTOR_DIMENSION];
                for (int d = 0; d < VECTOR_DIMENSION; d++) {
                    subcatVectorData[d] = catVectorData[d] + random.nextGaussian() * 0.05;
                }

                EuclideanVector subcatVectorE = new EuclideanVector(subcatVectorData);

                // Store in Euclidean collection
                Map<String, Object> subcatMetadata = new HashMap<>();
                subcatMetadata.put("level", 2);
                subcatMetadata.put("category", cat);
                subcatMetadata.put("subcategory", subcat);
                subcatMetadata.put("type", "subcategory");

                storage.storeVector(EUCLIDEAN_COLLECTION, toFloatArray(subcatVectorData), subcatMetadata);

                // Convert to Poincaré and store
                PoincareVector subcatVectorP = converter.euclideanToPoincare(subcatVectorE, MAX_RADIUS, -1.0);

                storage.storeHyperbolicVector(HYPERBOLIC_COLLECTION,
                        toFloatArray(subcatVectorP.getData()), true, subcatMetadata);

                // Level 3: Items
                for (int item = 0; item < 20; item++) {
                    // Generate item vector as a perturbation of the subcategory vector
                    double[] itemVectorData = new double[VECTOR_DIMENSION];
                    for (int d = 0; d < VECTOR_DIMENSION; d++) {
                        itemVectorData[d] = subcatVectorData[d] + random.nextGaussian() * 0.02;
                    }

                    EuclideanVector itemVectorE = new EuclideanVector(itemVectorData);

                    // Store in Euclidean collection
                    Map<String, Object> itemMetadata = new HashMap<>();
                    itemMetadata.put("level", 3);
                    itemMetadata.put("category", cat);
                    itemMetadata.put("subcategory", subcat);
                    itemMetadata.put("item", item);
                    itemMetadata.put("type", "item");

                    storage.storeVector(EUCLIDEAN_COLLECTION, toFloatArray(itemVectorData), itemMetadata);

                    // Convert to Poincaré and store
                    PoincareVector itemVectorP = converter.euclideanToPoincare(itemVectorE, MAX_RADIUS, -1.0);

                    storage.storeHyperbolicVector(HYPERBOLIC_COLLECTION,
                            toFloatArray(itemVectorP.getData()), true, itemMetadata);
                }
            }
        }

        System.out.println("Generated " + storage.getCollectionStats(EUCLIDEAN_COLLECTION).getVectorCount() +
                " vectors in Euclidean space");
        System.out.println("Generated " + storage.getCollectionStats(HYPERBOLIC_COLLECTION).getVectorCount() +
                " vectors in Hyperbolic space");
    }

    /**
     * Print search results in a user-friendly format.
     */
    private static void printSearchResults(List<SearchResult> results) {
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            Map<String, Object> metadata = result.getRecord().getMetadata();

            System.out.printf("%d. ID: %s, Distance: %.4f, ",
                    i + 1, result.getId(), result.getDistance());

            System.out.printf("Type: %s, Level: %d, Category: %d",
                    metadata.get("type"), metadata.get("level"), metadata.get("category"));

            if (metadata.containsKey("subcategory")) {
                System.out.printf(", Subcategory: %d", metadata.get("subcategory"));
            }

            if (metadata.containsKey("item")) {
                System.out.printf(", Item: %d", metadata.get("item"));
            }

            System.out.println();
        }
    }

    /**
     * Analyze the hierarchical clustering effectiveness of both spaces.
     * Compares how well each space preserves the hierarchical relationships.
     */
    private static void analyzeHierarchicalClustering(List<SearchResult> euclideanResults,
                                                      List<SearchResult> hyperbolicResults) {
        // Analyze category distribution in Euclidean results
        System.out.println("\nEuclidean Space Category Distribution:");
        analyzeResultDistribution(euclideanResults);

        // Analyze category distribution in Hyperbolic results
        System.out.println("\nHyperbolic Space Category Distribution:");
        analyzeResultDistribution(hyperbolicResults);
    }

    /**
     * Analyze the distribution of categories, subcategories, and levels in search results.
     */
    private static void analyzeResultDistribution(List<SearchResult> results) {
        Map<Integer, Integer> levelCounts = new HashMap<>();
        Map<Integer, Integer> categoryCounts = new HashMap<>();

        for (SearchResult result : results) {
            Map<String, Object> metadata = result.getRecord().getMetadata();

            int level = (int) metadata.get("level");
            int category = (int) metadata.get("category");

            // Count occurrences of each level
            levelCounts.put(level, levelCounts.getOrDefault(level, 0) + 1);

            // Count occurrences of each category
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }

        // Output level distribution
        System.out.println("Level distribution:");
        for (Map.Entry<Integer, Integer> entry : levelCounts.entrySet()) {
            System.out.printf("  Level %d: %d occurrences\n", entry.getKey(), entry.getValue());
        }

        // Output category distribution
        System.out.println("Category distribution:");
        for (Map.Entry<Integer, Integer> entry : categoryCounts.entrySet()) {
            System.out.printf("  Category %d: %d occurrences\n", entry.getKey(), entry.getValue());
        }

        // Calculate entropy (lower entropy means better clustering)
        double levelEntropy = calculateEntropy(levelCounts, results.size());
        double categoryEntropy = calculateEntropy(categoryCounts, results.size());

        System.out.printf("Level entropy: %.4f\n", levelEntropy);
        System.out.printf("Category entropy: %.4f\n", categoryEntropy);

        // Calculate purity (higher purity means better clustering)
        double purity = calculatePurity(results);
        System.out.printf("Cluster purity: %.4f\n", purity);
    }

    /**
     * Calculate the entropy of a distribution.
     * Lower entropy indicates better clustering.
     */
    private static double calculateEntropy(Map<Integer, Integer> counts, int total) {
        double entropy = 0.0;

        for (int count : counts.values()) {
            double probability = (double) count / total;
            entropy -= probability * (Math.log(probability) / Math.log(2)); // Log base 2
        }

        return entropy;
    }

    /**
     * Calculate the purity of the search results.
     * Higher purity indicates better clustering.
     */
    private static double calculatePurity(List<SearchResult> results) {
        // For simplicity, we'll use the most common category as the cluster assignment
        Map<Integer, Integer> categoryCounts = new HashMap<>();

        for (SearchResult result : results) {
            Map<String, Object> metadata = result.getRecord().getMetadata();
            int category = (int) metadata.get("category");
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }

        // Find the most common category
        int maxCount = 0;
        for (int count : categoryCounts.values()) {
            maxCount = Math.max(maxCount, count);
        }

        return (double) maxCount / results.size();
    }

    /**
     * Convert a double array to a float array.
     */
    private static float[] toFloatArray(double[] doubleArray) {
        float[] floatArray = new float[doubleArray.length];
        for (int i = 0; i < doubleArray.length; i++) {
            floatArray[i] = (float) doubleArray[i];
        }
        return floatArray;
    }

    /**
     * Convert a float array to a double array.
     */
    private static double[] toDoubleArray(float[] floatArray) {
        double[] doubleArray = new double[floatArray.length];
        for (int i = 0; i < floatArray.length; i++) {
            doubleArray[i] = floatArray[i];
        }
        return doubleArray;
    }
}