# ANNSearch Source Code

This repository contains all source code for the ANNSearch implementation written by Malte Helin Johnsen for his Master's thesis written in the spring of 2024 at ITU. 


### Requirements
Java 11

---

### Main Directory

The repository is a Gradle project. The most relevant source code can be found in the directory:

`app/src/main/java/lsh`

---

### Thesis Abstract

The $k$-Approximate Nearest Neighbor Search problem is a fundamental algorithmic problem that has applications in many domains of computer science. At a high level, an algorithmic solution to this problem is composed of an index structure and a search strategy. In recent years novel search strategies have been proposed for tree-based index structures, which have been shown to improve the performance over the standard Lookup Search strategy. An experimental evaluation of these search strategies for locality-sensitive hashing-based index structures has remained unexplored. 

This thesis introduces a novel search strategy, the Quick-select Natural Classifier Select, and experimentally evaluates this and three other search strategies across tree-based and locality-sensitive hashing-based index structures on datasets commonly used for benchmarking $k$-ANNS algorithms. The results demonstrate that novel search strategies outperform the standard Lookup Search strategy when paired with the two locality-sensitive hashing-based index structures, LSH and C2LSH. For the LSH index structure, the Natural Classifier Search strategy achieves a performance increase of 145\% on the Fashion-MNIST dataset, while the Voting Search strategy achieves a performance increase of 65\% on the SIFT dataset.

Additionally, the experimental evaluation shows that the Natural Classifier Search strategy is outperformed by the Voting Search strategy for both the LSH and RP-Forest index structures on the SIFT and GloVe-100 datasets.  Finally, the LSH and RP-Forest index structures with Voting Search achieve the best performance of the evaluated algorithms on the GloVe-100 dataset.
