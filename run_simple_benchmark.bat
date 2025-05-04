@echo off
echo Running Simple Hyperbolic vs Euclidean Benchmark...

cd C:\Users\ankit\IdeaProjects\HyperbolicVectorDB
mvn compile
mvn exec:java -Dexec.mainClass="com.hypervector.benchmark.SimpleBenchmark"

echo Benchmark completed!
