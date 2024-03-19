package lsh;

import java.util.List;
import java.io.Serializable;

public abstract class Tree implements Serializable{

    private Node root;
    private int maxLeafSize;

    
    public Tree(int maxLeafSize) {
        this.maxLeafSize = maxLeafSize;
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Node getRoot() {
        return root;
    }

    public int getMaxLeafSize() {
        return maxLeafSize;
    }


    public class Node implements Serializable {

        private Node leftChild;
        private Node rightChild;

        private float splitValue;
        private int splitIndex;

        private boolean isLeaf;
        private List<Integer> partitionSet;
    
        
    
        public Node() {
            this.isLeaf = false;
        }
        
        /* ----------- Getter and Setters ----------- */
    
        
        public void setLeftChild(Node leftChild) {
            this.leftChild = leftChild;
        }
    
        public void setRightChild(Node rightChild) {
            this.rightChild = rightChild;
        }

        public Node getLeftChild() {
            return leftChild;
        }

        public Node getRightChild() {
            return rightChild;
        }
    
        public void setSplitValue(float splitValue) {
            this.splitValue = splitValue;
        }

        public float getSplitValue() {
            return splitValue;
        }
    
        public void setSplitIndex(int splitIndex) {
            this.splitIndex = splitIndex;
        }

        public int getSplitIndex() {
            return splitIndex;
        }
    
        public boolean getIsLeaf() {
            return this.isLeaf;
        }
    
        public void setIsLeaf(boolean value) {
            this.isLeaf = value;
        }
    
        public List<Integer> getPartitionSet() {
            return partitionSet;
        }
    
        public void setPartitionSet(List<Integer> partitionSet) {
            this.partitionSet = partitionSet;
        }
        
    }
}


