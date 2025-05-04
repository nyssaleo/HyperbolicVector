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
 * Loader for TPC-H benchmark data
 */
public class TPCHLoader implements DatasetLoader {

    private static final String CUSTOMER_FILE = "/data/tpch/customer.csv";
    private static final String ORDERS_FILE = "/data/tpch/orders.csv";
    private static final String LINEITEM_FILE = "/data/tpch/lineitem.csv";
    private static final String PART_FILE = "/data/tpch/part.csv";
    private static final String SUPPLIER_FILE = "/data/tpch/supplier.csv";
    private static final String NATION_FILE = "/data/tpch/nation.csv";
    private static final String REGION_FILE = "/data/tpch/region.csv";

    @Override
    public Map<String, List<String>> loadHierarchy() throws IOException {
        Map<String, List<String>> hierarchy = new HashMap<>();
        
        // Load region -> nation hierarchy
        loadRegionNationHierarchy(hierarchy);
        
        // Load nation -> supplier hierarchy
        loadNationSupplierHierarchy(hierarchy);
        
        // Load nation -> customer hierarchy
        loadNationCustomerHierarchy(hierarchy);
        
        // Load customer -> orders hierarchy
        loadCustomerOrdersHierarchy(hierarchy);
        
        // Load orders -> lineitem hierarchy
        loadOrdersLineitemHierarchy(hierarchy);
        
        // Load part hierarchy
        loadPartHierarchy(hierarchy);
        
        return hierarchy;
    }
    
    private void loadRegionNationHierarchy(Map<String, List<String>> hierarchy) throws IOException {
        // First load regions
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(REGION_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("r_regionkey")) {
                line = reader.readLine();
            }
            
            while (line != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    String regionId = "R_" + parts[0].trim();
                    
                    if (!hierarchy.containsKey(regionId)) {
                        hierarchy.put(regionId, new ArrayList<>());
                    }
                }
                line = reader.readLine();
            }
        }
        
        // Then load nations and link to regions
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(NATION_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("n_nationkey")) {
                line = reader.readLine();
            }
            
            while (line != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    String nationId = "N_" + parts[0].trim();
                    String regionId = "R_" + parts[2].trim();
                    
                    if (!hierarchy.containsKey(nationId)) {
                        hierarchy.put(nationId, new ArrayList<>());
                    }
                    
                    hierarchy.get(nationId).add(regionId);
                }
                line = reader.readLine();
            }
        }
    }
    
    private void loadNationSupplierHierarchy(Map<String, List<String>> hierarchy) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(SUPPLIER_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("s_suppkey")) {
                line = reader.readLine();
            }
            
            while (line != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    String supplierId = "S_" + parts[0].trim();
                    String nationId = "N_" + parts[3].trim();
                    
                    if (!hierarchy.containsKey(supplierId)) {
                        hierarchy.put(supplierId, new ArrayList<>());
                    }
                    
                    hierarchy.get(supplierId).add(nationId);
                }
                line = reader.readLine();
            }
        }
    }
    
    private void loadNationCustomerHierarchy(Map<String, List<String>> hierarchy) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(CUSTOMER_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("c_custkey")) {
                line = reader.readLine();
            }
            
            int count = 0;
            while (line != null && count < 10000) { // Limit to 10,000 customers
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    String customerId = "C_" + parts[0].trim();
                    String nationId = "N_" + parts[3].trim();
                    
                    if (!hierarchy.containsKey(customerId)) {
                        hierarchy.put(customerId, new ArrayList<>());
                    }
                    
                    hierarchy.get(customerId).add(nationId);
                    count++;
                }
                line = reader.readLine();
            }
        }
    }
    
    private void loadCustomerOrdersHierarchy(Map<String, List<String>> hierarchy) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(ORDERS_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("o_orderkey")) {
                line = reader.readLine();
            }
            
            int count = 0;
            while (line != null && count < 10000) { // Limit to 10,000 orders
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    String orderId = "O_" + parts[0].trim();
                    String customerId = "C_" + parts[1].trim();
                    
                    if (!hierarchy.containsKey(orderId)) {
                        hierarchy.put(orderId, new ArrayList<>());
                    }
                    
                    hierarchy.get(orderId).add(customerId);
                    count++;
                }
                line = reader.readLine();
            }
        }
    }
    
    private void loadOrdersLineitemHierarchy(Map<String, List<String>> hierarchy) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(LINEITEM_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("l_orderkey")) {
                line = reader.readLine();
            }
            
            int count = 0;
            while (line != null && count < 10000) { // Limit to 10,000 lineitems
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    String lineitemId = "L_" + parts[0].trim() + "_" + parts[1].trim();
                    String orderId = "O_" + parts[0].trim();
                    
                    if (!hierarchy.containsKey(lineitemId)) {
                        hierarchy.put(lineitemId, new ArrayList<>());
                    }
                    
                    hierarchy.get(lineitemId).add(orderId);
                    count++;
                }
                line = reader.readLine();
            }
        }
    }
    
    private void loadPartHierarchy(Map<String, List<String>> hierarchy) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(PART_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("p_partkey")) {
                line = reader.readLine();
            }
            
            int count = 0;
            while (line != null && count < 10000) { // Limit to 10,000 parts
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    String partId = "P_" + parts[0].trim();
                    String typeStr = parts[4].trim();
                    
                    // Extract type hierarchy: STANDARD ANODIZED COPPER -> ANODIZED COPPER -> COPPER
                    String[] typeParts = typeStr.split(" ");
                    
                    if (!hierarchy.containsKey(partId)) {
                        hierarchy.put(partId, new ArrayList<>());
                    }
                    
                    StringBuilder currentType = new StringBuilder();
                    for (int i = typeParts.length - 1; i >= 0; i--) {
                        if (i < typeParts.length - 1) {
                            currentType.insert(0, " ");
                        }
                        currentType.insert(0, typeParts[i]);
                        String typeId = "T_" + currentType.toString();
                        
                        if (i == typeParts.length - 1) {
                            // Bottom level type connects to part
                            hierarchy.get(partId).add(typeId);
                        } else {
                            // Higher level types connect to lower level types
                            String nextTypeId = "T_" + currentType.toString();
                            String prevTypeId = "T_" + typeParts[i + 1];
                            for (int j = i + 2; j < typeParts.length; j++) {
                                prevTypeId += " " + typeParts[j];
                            }
                            
                            if (!hierarchy.containsKey(nextTypeId)) {
                                hierarchy.put(nextTypeId, new ArrayList<>());
                            }
                            
                            hierarchy.get(nextTypeId).add(prevTypeId);
                        }
                    }
                    
                    count++;
                }
                line = reader.readLine();
            }
        }
    }

    @Override
    public Map<String, Map<String, Object>> loadAttributes() throws IOException {
        Map<String, Map<String, Object>> attributes = new HashMap<>();
        
        // Load region attributes
        loadRegionAttributes(attributes);
        
        // Load nation attributes
        loadNationAttributes(attributes);
        
        // Load customer attributes
        loadCustomerAttributes(attributes);
        
        // Load order attributes
        loadOrderAttributes(attributes);
        
        // Load part attributes
        loadPartAttributes(attributes);
        
        return attributes;
    }
    
    private void loadRegionAttributes(Map<String, Map<String, Object>> attributes) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(REGION_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("r_regionkey")) {
                line = reader.readLine();
            }
            
            while (line != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    String regionId = "R_" + parts[0].trim();
                    String regionName = parts[1].trim();
                    
                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put("type", "region");
                    attrs.put("name", regionName);
                    
                    attributes.put(regionId, attrs);
                }
                line = reader.readLine();
            }
        }
    }
    
    private void loadNationAttributes(Map<String, Map<String, Object>> attributes) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(NATION_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("n_nationkey")) {
                line = reader.readLine();
            }
            
            while (line != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    String nationId = "N_" + parts[0].trim();
                    String nationName = parts[1].trim();
                    
                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put("type", "nation");
                    attrs.put("name", nationName);
                    
                    attributes.put(nationId, attrs);
                }
                line = reader.readLine();
            }
        }
    }
    
    private void loadCustomerAttributes(Map<String, Map<String, Object>> attributes) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(CUSTOMER_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("c_custkey")) {
                line = reader.readLine();
            }
            
            int count = 0;
            while (line != null && count < 10000) {
                String[] parts = line.split("\\|");
                if (parts.length >= 8) {
                    String customerId = "C_" + parts[0].trim();
                    String customerName = parts[1].trim();
                    String marketSegment = parts[6].trim();
                    
                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put("type", "customer");
                    attrs.put("name", customerName);
                    attrs.put("segment", marketSegment);
                    
                    attributes.put(customerId, attrs);
                    count++;
                }
                line = reader.readLine();
            }
        }
    }
    
    private void loadOrderAttributes(Map<String, Map<String, Object>> attributes) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(ORDERS_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("o_orderkey")) {
                line = reader.readLine();
            }
            
            int count = 0;
            while (line != null && count < 10000) {
                String[] parts = line.split("\\|");
                if (parts.length >= 9) {
                    String orderId = "O_" + parts[0].trim();
                    String orderDate = parts[4].trim();
                    String orderPriority = parts[5].trim();
                    
                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put("type", "order");
                    attrs.put("date", orderDate);
                    attrs.put("priority", orderPriority);
                    
                    attributes.put(orderId, attrs);
                    count++;
                }
                line = reader.readLine();
            }
        }
    }
    
    private void loadPartAttributes(Map<String, Map<String, Object>> attributes) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(PART_FILE), StandardCharsets.UTF_8))) {
            
            // Skip header if exists
            String line = reader.readLine();
            if (line.startsWith("p_partkey")) {
                line = reader.readLine();
            }
            
            int count = 0;
            while (line != null && count < 10000) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    String partId = "P_" + parts[0].trim();
                    String partName = parts[1].trim();
                    String typeStr = parts[4].trim();
                    
                    Map<String, Object> attrs = new HashMap<>();
                    attrs.put("type", "part");
                    attrs.put("name", partName);
                    
                    attributes.put(partId, attrs);
                    
                    // Also add type attributes
                    String[] typeParts = typeStr.split(" ");
                    StringBuilder currentType = new StringBuilder();
                    
                    for (int i = 0; i < typeParts.length; i++) {
                        if (i > 0) {
                            currentType.append(" ");
                        }
                        currentType.append(typeParts[i]);
                        String typeId = "T_" + currentType.toString();
                        
                        if (!attributes.containsKey(typeId)) {
                            Map<String, Object> typeAttrs = new HashMap<>();
                            typeAttrs.put("type", "part_type");
                            typeAttrs.put("name", currentType.toString());
                            attributes.put(typeId, typeAttrs);
                        }
                    }
                    
                    count++;
                }
                line = reader.readLine();
            }
        }
    }

    @Override
    public String getName() {
        return "TPCH";
    }
}
