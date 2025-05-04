@echo off
echo Running Hyperbolic vs Euclidean Embedding Benchmark...

cd C:\Users\ankit\IdeaProjects\HyperbolicVectorDB
mvn clean compile
mvn exec:java -Dexec.mainClass="com.hypervector.benchmark.CleanBenchmark"

echo Benchmark completed!
