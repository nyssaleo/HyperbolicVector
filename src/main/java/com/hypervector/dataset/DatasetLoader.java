package com.hypervector.dataset;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Interface for loading hierarchical datasets
 */
public interface DatasetLoader {
    
    /**
     * Load the dataset
     * 
     * @return Map of node ID to list of parents
     * @throws IOException If loading fails
     */
    Map<String, List<String>> loadHierarchy() throws IOException;
    
    /**
     * Get node attributes (if available)
     * 
     * @return Map of node ID to attributes
     * @throws IOException If loading fails
     */
    Map<String, Map<String, Object>> loadAttributes() throws IOException;
    
    /**
     * Get dataset name
     * 
     * @return The name of the dataset
     */
    String getName();
}
