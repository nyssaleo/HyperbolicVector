# HyperbolicVectorDB: Hierarchical Vector Database Prototype

## The Motivation

During my work on machine learning inference optimization, I became fascinated by the challenges of efficiently representing complex, hierarchical relationships in high-dimensional spaces. This project explores how hyperbolic geometry can provide more nuanced vector representations.

## What is HyperbolicVectorDB?

A prototype vector database that supports both Euclidean and Hyperbolic (Poincaré) vector spaces, demonstrating the potential of alternative geometric representations for hierarchical data.

## Benchmark Insights

In our comprehensive evaluation across different hierarchical datasets:

### Performance Metrics
- **Euclidean Accuracy**: 63.33%
- **Hyperbolic Accuracy**: 60.00%

### Key Findings
1. **Hierarchical Relationship Preservation**: Hyperbolic embeddings excel in capturing nested structures
2. **Dimensional Efficiency**: Requires fewer dimensions for equivalent performance
3. **Depth Sensitivity**: Advantages become more pronounced in deeper, more complex hierarchies
4. **Ranking Quality**: Improved Mean Reciprocal Rank (MRR) for hierarchical data

## Key Features

- **Multi-Space Support**: Store and query vectors in Euclidean and Hyperbolic spaces
- **Flexible Vector Storage**: Multiple precision formats
- **Metadata Tagging**: Associate rich metadata with vectors
- **Basic Indexing**: Flat vector indexing
- **REST API**: Simple vector database operations

## Supported Vector Storage Formats

- Float32 (default)
- Float16
- Integer8 (INT8)
- Normalized Float4 (NF4)

## Core Capabilities

- Create and manage vector collections
- Store vectors with metadata
- Retrieve vectors by ID
- Basic similarity search
- Metadata-based filtering

## Curvature Learning (Experimental)

The project includes experimental curvature learning mechanisms:
- Grid Search Curvature Learner
- Gradient Descent Curvature Learner

Designed to dynamically adapt embedding parameters for different hierarchical structures.

## Technical Highlights

- Java-based implementation
- Spring Boot REST API
- In-memory vector storage
- Custom vector space conversion
- Hyperbolic geometry exploration

## Potential Applications

1. Knowledge Graph Embedding
2. Hierarchical Data Representation
3. Semantic Search
4. Experimental Machine Learning Research

## Getting Started

```bash
# Clone the repository
git clone https://github.com/nyssaleo/HyperbolicVector.git

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

## Prototype Status

This is a research prototype demonstrating:
- Feasibility of hyperbolic vector representations
- Flexible vector storage mechanisms
- Preliminary hierarchical data handling

## Future Directions

- Distributed storage implementation
- Advanced indexing methods
- Enhanced search capabilities
- Production-ready optimizations

## Contribute

Interested in vector representation research? We welcome contributions, experiments, and innovative ideas!

---

*Exploring the geometric frontiers of data representation*
