package lsh;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.io.Serializable;


public class RPTree extends Tree implements Searchable, Serializable {

    private SparseVector[] allRandomVectors;

    public RPTree (int maxLeafSize) {
        super(maxLeafSize);
    }

    public void fit(float[][] corpusMatrix) {

        List<Integer> allPoints = new ArrayList<>(corpusMatrix.length);
        for (int i = 0; i < corpusMatrix.length; i++) {
            allPoints.add(i, i);
        }

        // Create a random vector for each level of tree
        int dimensions = corpusMatrix[0].length;
        // To get base 2 log we take loge(allpoints.size())/loge(2)
        int maxDepth = (int) Math.ceil(Math.log(allPoints.size())/Math.log(2)) +1;
        allRandomVectors = new SparseVector[maxDepth];
        for (int i = 0; i < maxDepth; i++) {
            allRandomVectors[i] = randomVector(dimensions);
        }

        super.setRoot(createTree(corpusMatrix, allPoints, 0));
    }

    public Collection<Integer> search(float[] qVec) {

        Node currentNode = super.getRoot();

        while (!currentNode.getIsLeaf()) {
            float splitValue = currentNode.getSplitValue();
            SparseVector splittingVector = allRandomVectors[currentNode.getSplitIndex()];

            float projFactor = Utils.dot(qVec, splittingVector.getVector(), splittingVector.getNonZeroComponents());
            if (projFactor < splitValue) {
                currentNode = currentNode.getLeftChild();
            } else {
                currentNode = currentNode.getRightChild();
            }
        }

        return currentNode.getPartitionSet();

    }

    private Node createTree(float[][] corpusMatrix, List<Integer> cIndices, int depth) {

        Node currentNode = new Node();

        if (cIndices.size() < super.getMaxLeafSize()) { // Make node leaf node
            currentNode.setIsLeaf(true);
            currentNode.setPartitionSet(cIndices);
        } else {

            ProjectionFactor[] allProjectionFactors = allProjectionFactors(corpusMatrix, cIndices, depth);
            float splitValue = findSplitValue(allProjectionFactors);

            List<Integer> left = new LinkedList<>();
            List<Integer> right = new LinkedList<>();

            // Seperate points to left or right child
            for (ProjectionFactor projectionFactor : allProjectionFactors) {
                if (projectionFactor.projFactor < splitValue) {
                    left.add(projectionFactor.getcIndex());
                } else {
                    right.add(projectionFactor.getcIndex());
                }
            }

            currentNode.setSplitIndex(depth);
            currentNode.setSplitValue(splitValue);

            // Recursively set right and left child
            depth++;
            if (left.size() < 10 || right.size() < 10) {
                System.out.println("bing");
            }
            currentNode.setLeftChild(createTree(corpusMatrix, left, depth));
            currentNode.setRightChild(createTree(corpusMatrix, right, depth));
        }

        return currentNode;
    }

    private ProjectionFactor[] allProjectionFactors(float[][] corpusMatrix, List<Integer> cIndices, int depth) {
        ProjectionFactor[] allProjectionFactors = new ProjectionFactor[cIndices.size()];
        // Pick randomvector of current depth
        SparseVector currentSparseVector = allRandomVectors[depth];
        int ctr = 0;
        for (Integer cIndex : cIndices) {
            float projFactor = Utils.dot(corpusMatrix[cIndex], currentSparseVector.getVector(), currentSparseVector.getNonZeroComponents());
            allProjectionFactors[ctr++] = new ProjectionFactor(cIndex, projFactor);
        }
        return allProjectionFactors;
    }

    private float findSplitValue(ProjectionFactor[] projectionFactors) {
        float[] factorValues = new float[projectionFactors.length];
        int ctr = 0;

        // Create an array of projection factor values
        for (ProjectionFactor projectionFactor : projectionFactors) {
            factorValues[ctr++] = projectionFactor.getProjFactor();
        }

        //Find Median or potentially mean
        return Utils.calculateSplit(factorValues);
    }

    private SparseVector randomVector(int nrDimensions) {

        // 1/sqrt(d) fixed parameter
        float sparsity = 1 / (float) Math.sqrt( (double) nrDimensions);
        float[] randomVec = new float[nrDimensions];
        List<Integer> nonZeroComponents = new LinkedList<>();

        // Create random vector with sparsity 1/sqrt(d)
        Random myRandom = new Random();
        for (int i = 0; i < nrDimensions; i++) {
            float randValue = myRandom.nextFloat();
            if (randValue < sparsity) {
                // Non-zero components are given a value drawn from a Guassian distribution with mean 0.0 and standard deviation 1.0
                randomVec[i] = (float) myRandom.nextGaussian();
                nonZeroComponents.add(i);
            } else {
                randomVec[i] = 0;
            }
        }
        return new SparseVector(randomVec, nonZeroComponents);
    }

    class ProjectionFactor implements Comparable<ProjectionFactor> {

        int cIndex;
        float projFactor;

        public ProjectionFactor(int cIndex, float projFactor) {
            this.cIndex = cIndex;
            this.projFactor = projFactor;
        }

        public int getcIndex() {
            return cIndex;
        }

        public float getProjFactor() {
            return projFactor;
        }

        public int compareTo(ProjectionFactor that) {
            if (this.projFactor < that.getProjFactor()) return -1;
            if (this.projFactor > that.getProjFactor()) return 1;
            return 0;
        }
    }

    class SparseVector implements Serializable {

        private float[] vector;
        private List<Integer> nonZeroComponents;

        public SparseVector(float[] vector, List<Integer> nonZeroComponents) {
            
            this.nonZeroComponents = nonZeroComponents;
            this.vector = vector;
        }

        public float[] getVector() {
            return this.vector;
        }

        public List<Integer> getNonZeroComponents() {
            return this.nonZeroComponents;
        }        

    }
     
}
