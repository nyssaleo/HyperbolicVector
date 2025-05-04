@echo off
echo Running Standalone Hyperbolic vs Euclidean Benchmark...

cd C:\Users\ankit\IdeaProjects\HyperbolicVectorDB
mvn clean compile
mvn exec:java -Dexec.mainClass="com.hypervector.benchmark.StandaloneBenchmark"

echo Benchmark completed!
