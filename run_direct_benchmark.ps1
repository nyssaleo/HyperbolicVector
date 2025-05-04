# Compile the project with dependencies
Write-Host "Compiling project with dependencies..." -ForegroundColor Cyan
mvn clean compile dependency:copy-dependencies

# Set classpath with all dependencies
$classpath = "target/classes;target/dependency/*"

# Run the direct benchmark
Write-Host "Running direct comparison benchmark..." -ForegroundColor Green
java -cp $classpath com.hypervector.benchmark.DirectBenchmark

# Open the HTML report
Write-Host "Opening benchmark report..." -ForegroundColor Yellow
Start-Process "direct_benchmark_report.html"

Write-Host "Benchmark complete!" -ForegroundColor Green
