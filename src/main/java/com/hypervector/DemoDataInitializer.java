package com.hypervector;

import com.hypervector.index.common.VectorIndex;
import com.hypervector.math.conversion.VectorSpaceConverter;
import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.hyperbolic.PoincareVector;
import com.hypervector.storage.common.CollectionConfig;
import com.hypervector.storage.common.VectorStorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Component that initializes sample data for demonstration.
 */
public class DemoDataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DemoDataInitializer.class);

    private static final String EUCLIDEAN_COLLECTION = "euclidean_demo";
    private static final String HYPERBOLIC_COLLECTION = "hyperbolic_demo";
    private static final int VECTOR_DIMENSION = 3;

    private final VectorStorage storage;
    private final VectorIndex index;

    public DemoDataInitializer(VectorStorage storage, VectorIndex index) {
        this.storage = storage;
        this.index = index;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        logger.info("Initializing demo data...");

        try {
            // Create collections
            storage.createCollection(EUCLIDEAN_COLLECTION,
                    CollectionConfig.createEuclidean(VECTOR_DIMENSION, CollectionConfig.StorageFormat.FLOAT32));

            storage.createCollection(HYPERBOLIC_COLLECTION,
                    CollectionConfig.createPoincare(VECTOR_DIMENSION, CollectionConfig.StorageFormat.FLOAT32));

            // Generate hierarchical test data
            generateHierarchicalTestData();

            // Build indices
            index.buildIndex(EUCLIDEAN_COLLECTION, VectorIndex.IndexType.FLAT, null);
            index.buildIndex(HYPERBOLIC_COLLECTION, VectorIndex.IndexType.FLAT, null);

            logger.info("Demo data initialization completed successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize demo data", e);
        }
    }

    /**
     * Generate hierarchical test data for both Euclidean and hyperbolic spaces.
     */
    private void generateHierarchicalTestData() {
        Random random = new Random(42);  // Fixed seed for reproducibility
        VectorSpaceConverter converter = new VectorSpaceConverter();

        // Create vector groups representing a tree structure with 3 levels
        // Level 0: Root (implicit)
        // Level 1: 3 main categories
        // Level 2: 5 subcategories per category

        double maxRadius = 0.9;

        // Level 1: Main categories
        for (int cat = 0; cat < 3; cat++) {
            // Generate a category vector in Euclidean space
            double[] catVectorData = new double[VECTOR_DIMENSION];
            for (int d = 0; d < VECTOR_DIMENSION; d++) {
                catVectorData[d] = 0.3 * cat + random.nextGaussian() * 0.1;
            }

            EuclideanVector catVectorE = new EuclideanVector(catVectorData);

            // Store in Euclidean collection
            Map<String, Object> catMetadata = new HashMap<>();
            catMetadata.put("level", 1);
            catMetadata.put("category", cat);
            catMetadata.put("name", "Category-" + cat);

            storage.storeVector(EUCLIDEAN_COLLECTION, toFloatArray(catVectorData), catMetadata);

            // Convert to Poincaré and store
            PoincareVector catVectorP = converter.euclideanToPoincare(catVectorE, maxRadius, -1.0);

            storage.storeHyperbolicVector(HYPERBOLIC_COLLECTION,
                    toFloatArray(catVectorP.getData()), true, catMetadata);

            // Level 2: Subcategories
            for (int subcat = 0; subcat < 5; subcat++) {
                // Generate subcategory vector (closer to parent in hyperbolic space)
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
                subcatMetadata.put("name", "Category-" + cat + "-Subcat-" + subcat);

                storage.storeVector(EUCLIDEAN_COLLECTION, toFloatArray(subcatVectorData), subcatMetadata);

                // Convert to Poincaré and store
                PoincareVector subcatVectorP = converter.euclideanToPoincare(subcatVectorE, maxRadius, -1.0);

                storage.storeHyperbolicVector(HYPERBOLIC_COLLECTION,
                        toFloatArray(subcatVectorP.getData()), true, subcatMetadata);
            }
        }

        logger.info("Generated {} vectors in Euclidean space",
                storage.getCollectionStats(EUCLIDEAN_COLLECTION).getVectorCount());
        logger.info("Generated {} vectors in Hyperbolic space",
                storage.getCollectionStats(HYPERBOLIC_COLLECTION).getVectorCount());
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
}