package com.hypervector.benchmark;

import com.hypervector.dataset.DatasetLoader;
import com.hypervector.dataset.RealDatasetBenchmark;
import com.hypervector.dataset.WordNetLoader;
import com.hypervector.index.common.FlatVectorIndex;
import com.hypervector.storage.common.InMemoryVectorStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Main class to run real-world dataset benchmarks
 */
public class RealWorldBenchmark {
    
    private static final Logger logger = LoggerFactory.getLogger(RealWorldBenchmark.class);
    
    public static void main(String[] args) {
        logger.info("Starting Real-World Dataset Benchmark");
        
        try {
            // Initialize storage and index
            InMemoryVectorStorage storage = new InMemoryVectorStorage();
            FlatVectorIndex index = new FlatVectorIndex();
            index.setVectorStorage(storage);
            
            // Create benchmark
            RealDatasetBenchmark benchmark = new RealDatasetBenchmark(storage, index);
            
            // Create dataset loaders
            DatasetLoader wordnetLoader = new WordNetLoader();
            
            // Load datasets
            benchmark.loadDataset(wordnetLoader);
            
            // Run benchmarks
            int[] kValues = {5, 10, 15, 20};
            
            try (PrintWriter writer = new PrintWriter(new FileWriter("real_world_benchmark_results.csv"))) {
                // Write header
                writer.println("dataset,k,euclidean_hierarchy_score,hyperbolic_hierarchy_score,hierarchy_improvement," + 
                            "euclidean_avg_time_ms,hyperbolic_avg_time_ms,time_ratio");
                
                for (int k : kValues) {
                    Map<String, Object> wordnetResults = benchmark.runBenchmark(wordnetLoader.getName(), k);
                    
                    // Write results
                    writer.printf("%s,%d,%.4f,%.4f,%.4f,%.2f,%.2f,%.2f\n",
                        wordnetLoader.getName(),
                        k,
                        wordnetResults.get("euclidean_hierarchy_score"),
                        wordnetResults.get("hyperbolic_hierarchy_score"),
                        wordnetResults.get("hierarchy_improvement"),
                        wordnetResults.get("euclidean_avg_time_ms"),
                        wordnetResults.get("hyperbolic_avg_time_ms"),
                        wordnetResults.get("time_ratio"));
                }
            }
            
            logger.info("Benchmark completed successfully! Results written to real_world_benchmark_results.csv");
            
        } catch (Exception e) {
            logger.error("Error in benchmark: {}", e.getMessage(), e);
        }
    }
}
