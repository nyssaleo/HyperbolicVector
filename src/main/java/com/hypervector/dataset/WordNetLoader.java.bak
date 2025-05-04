package com.hypervector.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loader for WordNet hierarchical data
 */
public class WordNetLoader implements DatasetLoader {
    
    private static final String WORDNET_FILE = "/data/wordnet/wordnet-mlj12-noun.txt";
    
    @Override
    public Map<String, List<String>> loadHierarchy() throws IOException {
        Map<String, List<String>> childToParents = new HashMap<>();
        
        try (InputStream is = getClass().getResourceAsStream(WORDNET_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    String child = parts[0];
                    String parent = parts[1];
                    
                    if (!childToParents.containsKey(child)) {
                        childToParents.put(child, new ArrayList<>());
                    }
                    childToParents.get(child).add(parent);
                    
                    // Ensure parent is in the map even if it has no parents
                    if (!childToParents.containsKey(parent)) {
                        childToParents.put(parent, new ArrayList<>());
                    }
                }
            }
        }
        
        return childToParents;
    }
    
    @Override
    public Map<String, Map<String, Object>> loadAttributes() throws IOException {
        // WordNet dataset doesn't have additional attributes in our simplified version
        Map<String, Map<String, Object>> attributes = new HashMap<>();
        
        Map<String, List<String>> hierarchy = loadHierarchy();
        
        for (String node : hierarchy.keySet()) {
            Map<String, Object> nodeAttrs = new HashMap<>();
            nodeAttrs.put("name", node);
            nodeAttrs.put("child_count", countChildren(node, hierarchy));
            attributes.put(node, nodeAttrs);
        }
        
        return attributes;
    }
    
    @Override
    public String getName() {
        return "WordNet";
    }
    
    private int countChildren(String node, Map<String, List<String>> hierarchy) {
        int count = 0;
        for (Map.Entry<String, List<String>> entry : hierarchy.entrySet()) {
            if (entry.getValue().contains(node)) {
                count++;
            }
        }
        return count;
    }
}
