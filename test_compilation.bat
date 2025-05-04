@echo off
echo Compiling and running test...

cd C:\Users\ankit\IdeaProjects\HyperbolicVectorDB
javac -d target\classes src\main\java\com\hypervector\benchmark\CompilationTest.java
java -cp target\classes com.hypervector.benchmark.CompilationTest

echo Test complete!
