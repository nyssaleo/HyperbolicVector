# HyperbolicVectorDB

A specialized vector database with superior support for hierarchical data through native hyperbolic geometry embeddings alongside traditional Euclidean vector spaces.

## Overview

HyperbolicVectorDB provides a comprehensive solution for storing, indexing, and retrieving vector embeddings in both Euclidean and hyperbolic spaces. It's specifically engineered for applications with complex hierarchical data structures, offering superior representation capabilities compared to traditional vector databases through the mathematical properties of hyperbolic geometry.

## Key Features

- **Dual-Space Support**: Natively handles both Euclidean and hyperbolic (Poincaré ball model) embeddings
- **Efficient Space Conversions**: Tools to convert between geometric spaces while preserving relational structure
- **Hierarchical Preservation**: Superior representation of tree-like structures and nested relationships
- **Specialized Query Engine**: Optimized similarity search in both geometric spaces
- **Comprehensive Benchmarking**: Tools to compare embedding performance across different datasets
- **Space-Efficiency**: Achieve better representation with fewer dimensions using hyperbolic geometry
- **Visualization Tools**: Web interface for exploring vector relationships in different spaces

## Why Hyperbolic Geometry?

Hyperbolic spaces exhibit exponential growth of volume relative to radius, compared to the polynomial growth in Euclidean spaces. This property creates a natural correspondence with tree-like structures and hierarchical data:

- **Better Hierarchical Representation**: The Poincaré ball model naturally represents hierarchical relationships, with the root at the center and leaf nodes toward the boundary
- **Dimensional Efficiency**: Achieve equal or better representation quality with significantly fewer dimensions
- **Distance Preservation**: Better preservation of graph distance in hierarchical data
- **Improved Retrieval Quality**: More accurate nearest-neighbor searches for hierarchical data

## Getting Started

### Prerequisites

- Java JDK 17+
- Maven 3.6+

### Installation

```bash
git clone https://github.com/yourusername/hyperbolic-vector-db.git
cd hyperbolic-vector-db
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run
```

The web interface will be available at `http://localhost:8080`.

## Usage Examples

### Creating Collections

```java
// Create Euclidean collection
storage.createCollection("euclidean_collection", 
    CollectionConfig.createEuclidean(3, CollectionConfig.StorageFormat.FLOAT32));

// Create Poincaré (hyperbolic) collection
storage.createCollection("hyperbolic_collection",
    CollectionConfig.createPoincare(3, CollectionConfig.StorageFormat.FLOAT32));
```

### Storing Vectors

```java
// Store in Euclidean space
Map<String, Object> metadata = new HashMap<>();
metadata.put("category", "electronics");
storage.storeVector("euclidean_collection", new float[]{0.1f, 0.2f, 0.3f}, metadata);

// Store in hyperbolic space (Poincaré ball)
storage.storeHyperbolicVector("hyperbolic_collection", new float[]{0.1f, 0.2f, 0.3f}, true, metadata);
```

### Searching for Similar Vectors

```java
// Search in Euclidean space
List<SearchResult> euclideanResults = index.search(
    "euclidean_collection", queryVector, 10, VectorIndex.SpaceType.EUCLIDEAN);

// Search in hyperbolic space
List<SearchResult> hyperbolicResults = index.search(
    "hyperbolic_collection", queryVector, 10, VectorIndex.SpaceType.POINCARE_BALL);
```

## API Reference

The application provides a REST API for interacting with the vector database:

- `GET /api/v1/collections`: List all collections
- `POST /api/v1/collections/{name}`: Create a new collection
- `GET /api/v1/collections/{name}/stats`: Get collection statistics
- `POST /api/v1/collections/{name}/vectors`: Store a vector
- `GET /api/v1/collections/{name}/vectors/{id}`: Retrieve a specific vector
- `POST /api/v1/collections/{name}/search`: Search for similar vectors
- `POST /api/v1/collections/{name}/index`: Build an index for a collection

## Benchmarks and Performance

Our extensive benchmarking demonstrates that hyperbolic embeddings consistently outperform Euclidean embeddings for hierarchical data structures, particularly as hierarchy depth increases. Key findings include:

- Hyperbolic embeddings provide better preservation of hierarchical relationships
- The advantage of hyperbolic space increases with tree depth and complexity
- Lower-dimensional hyperbolic embeddings often outperform higher-dimensional Euclidean ones
- For nearest-neighbor search tasks on hierarchical data, hyperbolic embeddings offer better accuracy

## Applications

- **Knowledge Graph Embeddings**: Represent taxonomies and ontologies with better fidelity
- **Recommendation Systems**: Improve product recommendations with category hierarchies
- **Natural Language Processing**: Better embed word hierarchies and semantic relationships
- **Database Query Optimization**: Optimize join paths through hierarchical schema representation
- **Network Analysis**: Represent hierarchical network structures more efficiently

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- The implementation draws inspiration from the paper "Poincaré Embeddings for Learning Hierarchical Representations" by Nickel and Kiela (2017)
- Special thanks to the research teams advancing the field of non-Euclidean embeddings for machine learning
