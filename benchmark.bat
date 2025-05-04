@echo off
echo Running Hyperbolic vs Euclidean Embedding Benchmark...
echo.

:: Use maven to execute with all dependencies
mvn exec:java -Dexec.mainClass="com.hypervector.benchmark.EmbeddingBenchmarkRunner"

echo.
echo Benchmark completed!
