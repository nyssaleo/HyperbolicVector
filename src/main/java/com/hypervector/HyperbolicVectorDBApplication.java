package com.hypervector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.hypervector.index.common.FlatVectorIndex;
import com.hypervector.index.common.VectorIndex;
import com.hypervector.storage.common.CollectionConfig;
import com.hypervector.storage.common.InMemoryVectorStorage;
import com.hypervector.storage.common.VectorStorage;

/**
 * Spring Boot Application for Hyperbolic Vector Database.
 */
@SpringBootApplication
public class HyperbolicVectorDBApplication {

    public static void main(String[] args) {
        SpringApplication.run(HyperbolicVectorDBApplication.class, args);
    }

    /**
     * Create the vector storage bean.
     */
    @Bean
    public VectorStorage vectorStorage() {
        return new InMemoryVectorStorage();
    }

    /**
     * Create the vector index bean.
     */
    @Bean
    public VectorIndex vectorIndex(VectorStorage storage) {
        FlatVectorIndex index = new FlatVectorIndex();
        index.setVectorStorage(storage);
        return index;
    }

    /**
     * Initialize the demo data for testing.
     */
    @Bean
    public DemoDataInitializer demoDataInitializer(VectorStorage storage, VectorIndex index) {
        return new DemoDataInitializer(storage, index);
    }
}