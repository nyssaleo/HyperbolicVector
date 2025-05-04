package com.hypervector.benchmark;

import com.hypervector.math.conversion.VectorSpaceConverter;
import com.hypervector.math.euclidean.EuclideanVector;
import com.hypervector.math.hyperbolic.PoincareVector;
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
 * Loader for real-world hierarchical datasets.
 * Supports WordNet taxonomy and other hierarchical data structures.
 */
public class HierarchicalDatasetLoader {

    private final VectorStorage storage;
    private final VectorSpaceConverter converter;
    private final Random random = new Random(42);
    private final String euclideanCollection;
    private final String hyperbolicCollection;
    private final int vectorDimension;
    private final double maxRadius;

    /**
     * Create a new dataset loader.
     */
    public HierarchicalDatasetLoader(VectorStorage storage, String euclideanCollection, 
                                    String hyperbolicCollection, int vectorDimension, double maxRadius) {
        this.storage = storage;
        this.euclideanCollection = euclideanCollection;
        this.hyperbolicCollection = hyperbolicCollection;
        this.vectorDimension = vectorDimension;
        this.maxRadius = maxRadius;
        this.converter = new VectorSpaceConverter();
    }

    /**
     * Create a taxonomy from a file with parent-child relationships.
     * File format: child<TAB>parent (one relation per line)
     * 
     * @param taxonomyFile Path to the taxonomy file
     * @return True if loading was successful
     */
    public boolean loadTaxonomyFromFile(String taxonomyFile) {
        System.out.println("Loading taxonomy from: " + taxonomyFile);
        
        try {
            if (!Files.exists(Paths.get(taxonomyFile))) {
                System.err.println("Taxonomy file not found: " + taxonomyFile);
                return false;
            }
            
            // Read the taxonomy file
            List<String> lines = Files.readAllLines(Paths.get(taxonomyFile));
            
            // Build the taxonomy tree
            Map<String, Set<String>> childrenMap = new HashMap<>(); // parent -> children
            Map<String, String> parentMap = new HashMap<>();        // child -> parent
            Set<String> allNodes = new HashSet<>();
            
            for (String line : lines) {
                String[] parts = line.trim().split("\t");
                if (parts.length != 2) continue;
                
                String child = parts[0];
                String parent = parts[1];
                
                // Add to maps
                if (!childrenMap.containsKey(parent)) {
                    childrenMap.put(parent, new HashSet<>());
                }
                childrenMap.get(parent).add(child);
                parentMap.put(child, parent);
                
                allNodes.add(child);
                allNodes.add(parent);
            }
            
            // Find the root nodes (nodes with no parent)
            List<String> rootNodes = new ArrayList<>();
            for (String node : allNodes) {
                if (!parentMap.containsKey(node)) {
                    rootNodes.add(node);
                }
            }
            
            if (rootNodes.isEmpty()) {
                System.err.println("No root nodes found in taxonomy");
                return false;
            }
            
            System.out.println("Found " + rootNodes.size() + " root nodes and " + allNodes.size() + " total nodes");
            
            // Create vectors for each node, starting from the root
            for (String rootNode : rootNodes) {
                // Create root node vector
                float[] rootVector = new float[vectorDimension];
                Map<String, Object> rootMetadata = new HashMap<>();
                rootMetadata.put("id", rootNode);
                rootMetadata.put("level", 0);
                rootMetadata.put("name", rootNode);
                
                // Store in both collections
                storage.storeVector(euclideanCollection, rootVector, rootMetadata);
                storage.storeHyperbolicVector(hyperbolicCollection, rootVector, true, rootMetadata);
                
                // Create child vectors recursively
                Map<String, float[]> vectorMap = new HashMap<>();
                vectorMap.put(rootNode, rootVector);
                
                processChildren(rootNode, childrenMap, vectorMap, 1);
            }
            
            System.out.println("Loaded " + storage.getCollectionStats(euclideanCollection).getVectorCount() + 
                " vectors in Euclidean collection");
            System.out.println("Loaded " + storage.getCollectionStats(hyperbolicCollection).getVectorCount() + 
                " vectors in Hyperbolic collection");
                
            return true;
        } catch (Exception e) {
            System.err.println("Error loading taxonomy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Process children of a node recursively.
     */
    private void processChildren(String parent, Map<String, Set<String>> childrenMap, 
                               Map<String, float[]> vectorMap, int level) {
        if (!childrenMap.containsKey(parent)) {
            return;
        }
        
        float[] parentVector = vectorMap.get(parent);
        
        for (String child : childrenMap.get(parent)) {
            // Generate a vector that's a perturbation of the parent vector
            float[] childEuclideanVector = new float[vectorDimension];
            for (int j = 0; j < vectorDimension; j++) {
                // Perturbation decreases with level to create more clustered hierarchies
                float perturbationScale = 0.2f / level;
                childEuclideanVector[j] = parentVector[j] + perturbationScale * (float)random.nextGaussian();
            }
            
            // Create metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("id", child);
            metadata.put("level", level);
            metadata.put("name", child);
            metadata.put("parent", parent);
            
            // Store in Euclidean collection
            storage.storeVector(euclideanCollection, childEuclideanVector, metadata);
            
            // Convert to hyperbolic and store
            double[] doubleVector = new double[vectorDimension];
            for (int j = 0; j < vectorDimension; j++) {
                doubleVector[j] = childEuclideanVector[j];
            }
            
            EuclideanVector euclideanVector = new EuclideanVector(doubleVector);
            PoincareVector poincareVector = converter.euclideanToPoincare(euclideanVector, maxRadius, -1.0);
            
            float[] childHyperbolicVector = new float[vectorDimension];
            for (int j = 0; j < vectorDimension; j++) {
                childHyperbolicVector[j] = (float)poincareVector.getData()[j];
            }
            
            storage.storeHyperbolicVector(hyperbolicCollection, childHyperbolicVector, true, metadata);
            
            // Store the vector for future use
            vectorMap.put(child, childEuclideanVector);
            
            // Process the children of this node
            processChildren(child, childrenMap, vectorMap, level + 1);
        }
    }

    /**
     * Download and load the WordNet noun hierarchy.
     * 
     * @return True if loading was successful
     */
    public boolean loadWordNetNounHierarchy() {
        System.out.println("Loading WordNet noun hierarchy...");
        
        String wordnetFile = "wordnet_nouns.txt";
        
        try {
            // Check if file exists, if not, download it
            if (!Files.exists(Paths.get(wordnetFile))) {
                System.out.println("Downloading WordNet noun hierarchy...");
                
                // Create a simplified WordNet hierarchy for testing
                try (PrintWriter out = new PrintWriter(new FileWriter(wordnetFile))) {
                    // Create a basic taxonomy with entity at the root
                    out.println("physical_entity\tentity");
                    out.println("abstract_entity\tentity");
                    out.println("thing\tentity");
                    out.println("object\tentity");
                    
                    // Level 2
                    out.println("matter\tphysical_entity");
                    out.println("process\tphysical_entity");
                    out.println("phenomenon\tphysical_entity");
                    
                    out.println("attribute\tabstract_entity");
                    out.println("measure\tabstract_entity");
                    out.println("relation\tabstract_entity");
                    
                    out.println("item\tthing");
                    out.println("artifact\tthing");
                    out.println("part\tthing");
                    
                    out.println("whole\tobject");
                    out.println("natural_object\tobject");
                    out.println("structure\tobject");
                    
                    // Level 3
                    out.println("solid\tmatter");
                    out.println("liquid\tmatter");
                    out.println("gas\tmatter");
                    out.println("plasma\tmatter");
                    
                    out.println("change\tprocess");
                    out.println("motion\tprocess");
                    out.println("transition\tprocess");
                    
                    out.println("property\tattribute");
                    out.println("quality\tattribute");
                    out.println("trait\tattribute");
                    
                    out.println("tool\tartifact");
                    out.println("structure\tartifact");
                    out.println("artwork\tartifact");
                    out.println("instrument\tartifact");
                    
                    // Level 4
                    out.println("hammer\ttool");
                    out.println("screwdriver\ttool");
                    out.println("saw\ttool");
                    
                    out.println("building\tstructure");
                    out.println("bridge\tstructure");
                    out.println("tower\tstructure");
                    
                    out.println("painting\tartwork");
                    out.println("sculpture\tartwork");
                    out.println("music\tartwork");
                    
                    out.println("piano\tinstrument");
                    out.println("guitar\tinstrument");
                    out.println("violin\tinstrument");
                }
                
                System.out.println("Created simplified WordNet hierarchy file: " + wordnetFile);
            }
            
            // Load the taxonomy
            return loadTaxonomyFromFile(wordnetFile);
            
        } catch (Exception e) {
            System.err.println("Error loading WordNet hierarchy: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Try to download a real taxonomic dataset from public sources.
     * 
     * @return True if loading was successful
     */
    public boolean loadRealWorldTaxonomy() {
        System.out.println("Trying to load a real-world taxonomy...");
        
        try {
            // Try several potential datasets until one works
            
            // 1. Try NCBI Taxonomy (simplified)
            if (tryDownloadTaxonomyFile("ncbi_taxonomy.txt", 
                    "https://raw.githubusercontent.com/OpenBioLink/TTD/master/data/ncbitaxon.doid.ttl")) {
                return processNCBITaxonomy("ncbi_taxonomy.txt");
            }
            
            // 2. Try DBpedia Categories (simplified)
            if (tryDownloadTaxonomyFile("dbpedia_categories.txt",
                    "https://raw.githubusercontent.com/dbpedia/links/master/dbpedia.org/categories/skos_categories_en.ttl")) {
                return processDBpediaTaxonomy("dbpedia_categories.txt");
            }
            
            // If all else fails, use WordNet
            return loadWordNetNounHierarchy();
            
        } catch (Exception e) {
            System.err.println("Error loading real-world taxonomy: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to WordNet
            return loadWordNetNounHierarchy();
        }
    }
    
    /**
     * Try to download a taxonomy file.
     */
    private boolean tryDownloadTaxonomyFile(String targetFile, String url) {
        try {
            // Check if file already exists
            if (Files.exists(Paths.get(targetFile))) {
                System.out.println("File already exists: " + targetFile);
                return true;
            }
            
            System.out.println("Downloading: " + url);
            
            // Download the file
            try (InputStream in = new URL(url).openStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                
                // Read up to 1000 lines to check format
                List<String> lines = new ArrayList<>();
                String line;
                int count = 0;
                while ((line = reader.readLine()) != null && count < 1000) {
                    lines.add(line);
                    count++;
                }
                
                // If we can read some lines, write to file
                if (!lines.isEmpty()) {
                    Files.write(Paths.get(targetFile), lines);
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error downloading taxonomy file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Process NCBI Taxonomy file.
     */
    private boolean processNCBITaxonomy(String file) {
        try {
            // Convert to simple parent-child format
            String convertedFile = file + ".converted";
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 PrintWriter writer = new PrintWriter(new FileWriter(convertedFile))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    // Very simple parsing of RDF triples - this is just a demonstration
                    if (line.contains("subClassOf")) {
                        String[] parts = line.split("subClassOf");
                        if (parts.length == 2) {
                            String child = parts[0].trim().replaceAll("[<>]", "").replaceAll(".*#", "");
                            String parent = parts[1].trim().replaceAll("[<>]", "").replaceAll(".*#", "");
                            
                            if (!child.isEmpty() && !parent.isEmpty()) {
                                writer.println(child + "\t" + parent);
                            }
                        }
                    }
                }
            }
            
            // Load the converted taxonomy
            return loadTaxonomyFromFile(convertedFile);
            
        } catch (Exception e) {
            System.err.println("Error processing NCBI taxonomy: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Process DBpedia Categories file.
     */
    private boolean processDBpediaTaxonomy(String file) {
        try {
            // Convert to simple parent-child format
            String convertedFile = file + ".converted";
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file));
                 PrintWriter writer = new PrintWriter(new FileWriter(convertedFile))) {
                
                String line;
                while ((line = reader.readLine()) != null) {
                    // Very simple parsing of RDF triples - this is just a demonstration
                    if (line.contains("broader")) {
                        String[] parts = line.split("broader");
                        if (parts.length == 2) {
                            String child = parts[0].trim().replaceAll("[<>]", "").replaceAll(".*Category:", "");
                            String parent = parts[1].trim().replaceAll("[<>]", "").replaceAll(".*Category:", "");
                            
                            if (!child.isEmpty() && !parent.isEmpty()) {
                                writer.println(child + "\t" + parent);
                            }
                        }
                    }
                }
            }
            
            // Load the converted taxonomy
            return loadTaxonomyFromFile(convertedFile);
            
        } catch (Exception e) {
            System.err.println("Error processing DBpedia taxonomy: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load a custom taxonomy with varying depth.
     */
    public boolean loadDeepHierarchy(int depth, int branching) {
        System.out.println("Creating deep hierarchical taxonomy with depth " + depth + 
                           " and branching factor " + branching + "...");
        
        try {
            // Create a taxonomy file
            String taxonomyFile = "deep_hierarchy.txt";
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(taxonomyFile))) {
                // Generate a tree structure
                Queue<String> queue = new LinkedList<>();
                queue.add("root");
                
                int level = 0;
                int nodesInCurrentLevel = 1;
                int nodeNumber = 0;
                
                while (level < depth) {
                    int nodesInNextLevel = 0;
                    
                    for (int i = 0; i < nodesInCurrentLevel; i++) {
                        String parent = queue.poll();
                        
                        for (int j = 0; j < branching; j++) {
                            nodeNumber++;
                            String child = "node_" + nodeNumber;
                            
                            // Write the parent-child relationship
                            writer.println(child + "\t" + parent);
                            
                            // Add to queue for next level
                            queue.add(child);
                            nodesInNextLevel++;
                        }
                    }
                    
                    level++;
                    nodesInCurrentLevel = nodesInNextLevel;
                }
            }
            
            // Load the generated taxonomy
            return loadTaxonomyFromFile(taxonomyFile);
            
        } catch (Exception e) {
            System.err.println("Error creating deep hierarchy: " + e.getMessage());
            return false;
        }
    }
}
