package lsh;

import java.io.File;
import java.util.Collection;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;



public class Utils {
    

    public static float dot(float[] aVec, float[] bVec){

        if (aVec.length != bVec.length) {
            throw new IllegalArgumentException("Vectors of unequal length passed");
        }

        float dotProduct = 0.0f;
        
        for (int i = 0; i < aVec.length; i++) {
            dotProduct += aVec[i] * bVec[i];
        }

        return dotProduct;
    }

    public static float euclideanDistance(float[] aVec, float[] bVec) {
        
        if (aVec.length != bVec.length) {
            throw new IllegalArgumentException("Vectors of unequal length passed");
        }

        float squaredDistance = 0.0f;
        for (int i = 0; i < bVec.length; i++) {
            squaredDistance += Math.pow(aVec[i]-bVec[i], 2);
        }
        return (float) Math.sqrt(squaredDistance);

    }

    public static Set<Integer> bruteForceKNN(float[][] corpusMatrix, float[] qVec, Collection<Integer> candidateSet, int k) {
        PriorityQueue<Distance> maxHeap = new PriorityQueue<>();

        for (Integer index : candidateSet) {
            float distance = euclideanDistance(qVec, corpusMatrix[index]);
            maxHeap.add(new Distance(index, distance));
        }

        Set<Integer> kNeighborSet = new HashSet<>();
        
        for (int i = 0; i < k; i++) {
            kNeighborSet.add(maxHeap.poll().getcIndex());
        }
        
        return kNeighborSet;
    }

    public static Set<Integer> bruteForceKNN(float[][] corpusMatrix, float[] qVec, int k) {
        PriorityQueue<Distance> maxHeap = new PriorityQueue<>();

        for (int index = 0; index < corpusMatrix.length; index++) {
            float distance = euclideanDistance(qVec, corpusMatrix[index]);
            maxHeap.add(new Distance(index, distance));
        }

        Set<Integer> kNeighborSet = new HashSet<>();
        
        for (int i = 0; i < k; i++) {
            kNeighborSet.add(maxHeap.poll().getcIndex());
        }
        
        return kNeighborSet;
    }

    public static boolean fileExists(String filePathString) {

        File filePath = new File(filePathString);
        if (filePath.exists() && !filePath.isDirectory()) {
            return true;
        }
        return false;
    }

    static class Distance implements Comparable<Distance>{

        private int cIndex;
        private float distanceToQ;

        public int getcIndex() {
            return cIndex;
        }

        public float getDistanceToQ() {
            return distanceToQ;
        }

        public Distance(int cIndex, float distanceToQ) {
            this.cIndex = cIndex;
            this.distanceToQ = distanceToQ;
        }

        public int compareTo(Distance that) {

            if (distanceToQ < that.getDistanceToQ()) {
                return -1;
            } else if (distanceToQ > that.getDistanceToQ()) {
                return 1;
            }
            return 0;


        }
    }

}
