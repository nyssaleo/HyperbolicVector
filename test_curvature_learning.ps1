Write-Host "Testing curvature learning..."
# The issue is with how PowerShell handles the quotes in the Maven command
# This format works reliably in PowerShell
mvn exec:java -D"exec.mainClass=com.hypervector.math.hyperbolic.learning.CurvatureLearningTest"
