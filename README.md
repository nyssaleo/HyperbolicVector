# HyperbolicVectorDB: Exploring Geometric Data Representations

## Background

When i was wokring on machine learning inference optimization work, I faced a recurring thought problem: representing complex, nested relationships in high-dimensional spaces. This project began as an exploration of how alternative geometric models might offer new perspectives on data embedding.

## Project Status: Prototype in Development

HyperbolicVectorDB is an early-stage research project investigating vector embeddings using hyperbolic geometry. My goal is to understand how different geometric spaces can represent hierarchical data structures.

## Current Capabilities

### Vector Space Support
- Euclidean vector representations
- Poincaré ball (Hyperbolic) vector representations
- Basic vector space conversion

### Storage Mechanisms
- In-memory vector storage
- Metadata tagging
- Multiple precision formats:
  - Float32 (default)
  - Float16
  - Integer8 (INT8)
  - Normalized Float4 (NF4)

### Experimental Features
- Adaptive curvature learning
  - Grid Search Curvature Learner
  - Gradient Descent Curvature Learner
- Basic REST API for vector operations

## Mathematical Foundations

The project explores how hyperbolic geometry can represent hierarchical relationships differently from Euclidean spaces:
- Constant negative curvature
- Exponential volume growth
- Natural representation of tree-like structures

## Preliminary Findings

Early benchmarks suggest nuanced differences in embedding capabilities:
- Slight variations in accuracy between Euclidean and Hyperbolic representations
- Potential for more efficient representations in deeply nested structures

## Supported Datasets (Experimental)
- WordNet Taxonomy
- TPC-H Schema Hierarchies

## Getting Started

```bash
# Clone the repository
git clone https://github.com/nyssaleo/HyperbolicVector.git

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

## Research Focus

This prototype aims to:
- Investigate geometric data representation techniques
- Explore challenges in hyperbolic embedding
- Develop foundational understanding of alternative vector spaces

## Future Directions
- Improved indexing methods
- Distributed storage implementation
- Advanced search capabilities

## Contribute

Interested in geometric data representation research? We welcome:
- Theoretical insights
- Implementation improvements
- Performance analysis
- Use case explorations

---
