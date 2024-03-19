package lsh;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import java.io.Serializable;

public class RKDTree extends Tree implements Searchable, Serializable {

    public RKDTree(int maxLeafSize) {
        super(maxLeafSize);
    }

    public void fit(float[][] corpusMatrix) {

        // Initially the split at the root node is calculated from the entire corpus
        List<Integer> allPoints = new ArrayList<>(corpusMatrix.length);
        for (int i = 0; i < corpusMatrix.length; i++) {
            allPoints.add(i, i);
        }

        super.setRoot(createTree(corpusMatrix, allPoints));

    }

    private Node createTree(float[][] corpusMatrix, List<Integer> cIndices) {

        Node currentNode = new Node();

        
        if (cIndices.size() < super.getMaxLeafSize()) { // Make node leaf nod
            currentNode.setIsLeaf(true);
            currentNode.setPartitionSet(cIndices);
        } else {
            Dimension splitDimension = findSplitDimension(corpusMatrix, cIndices);
            List<Integer> left = new LinkedList<>();
            List<Integer> right = new LinkedList<>();

            // Seperate points to left or right child
            for (Integer cIndex : cIndices) {
                if (corpusMatrix[cIndex][splitDimension.getIndex()] < splitDimension.getSplitValue()) {
                    left.add(cIndex);
                } else {
                    right.add(cIndex);
                }
            }

            currentNode.setSplitIndex(splitDimension.getIndex());
            currentNode.setSplitValue(splitDimension.getSplitValue());

            // Recursively set right and left child
            currentNode.setLeftChild(createTree(corpusMatrix, left));
            currentNode.setRightChild(createTree(corpusMatrix, right));
        }

        return currentNode;

    } 

    private Dimension findSplitDimension(float[][] corpusMatrix, List<Integer> cIndices) {

        // Number of dimensions of highest variance to choose from
        final int o = 5;

        int noDimensions = corpusMatrix[0].length;
        Dimension[] dimensions = new Dimension[noDimensions];

        // Calculate the variance across each dimension for the subset of points
        for (int d = 0; d < noDimensions; d++ ) {
            float[] dimensionComponents = new float[cIndices.size()];
            int ctr = 0;
            for (Integer cIndex : cIndices) {
                dimensionComponents[ctr] = corpusMatrix[cIndex][d];
                ctr++;
            }
            dimensions[d] = new Dimension(d, Utils.variance(dimensionComponents));
        }
        
        // Quickselect for top o and pick at random
        Utils.quickSelect(dimensions, 0, noDimensions-1, o);
        Random myRandom = new Random();
        Dimension chosenDimension = dimensions[myRandom.nextInt(o)];

        // Calculate split value of chosen dimension
        float[] dimensionComponents = new float[cIndices.size()];
        int ctr = 0;
        for (Integer cIndex : cIndices) {
            dimensionComponents[ctr] = corpusMatrix[cIndex][chosenDimension.getIndex()];
            ctr++;
        }

        chosenDimension.setSplitValue(Utils.calculateSplit(dimensionComponents));

        return chosenDimension;

    }

    public List<Integer> search(float[] qvec) {
        Node currentNode = super.getRoot();

        while (!currentNode.getIsLeaf()) {
            float splitValue = currentNode.getSplitValue();
            int splitIndex = currentNode.getSplitIndex();
            if (qvec[splitIndex] < splitValue) {
                currentNode = currentNode.getLeftChild();
            } else {
                currentNode = currentNode.getRightChild();
            }
        }

        return currentNode.getPartitionSet();
    }

    class Dimension implements Comparable<Dimension>{

        private int index;
        private float variance;
        private float splitValue;

        public Dimension(int index, float variance) {
            this.index = index;
            this.variance = variance;
        }

        public int getIndex() {
            return index;
        }

        public float getVariance() {
            return variance;
        }

        public float getSplitValue() {
            return splitValue;
        }

        public void setSplitValue(float splitValue) {
            this.splitValue = splitValue;
        }

        public int compareTo(Dimension that) {
            if (this.variance < that.getVariance()) return -1;
            if (this.variance > that.getVariance()) return 1;
            return 0;

        }
    }
    
}
