package lsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/* NOT TESTED - ONLY ROUGH IMPLEMENTATION */
public class C2LSH {

    List<HashFunction> hashFunctions;
    List<Map<Integer,List<Integer>>> compoundHashTable;
    float[][] corpusMatrix;

    public C2LSH(int d, int K, float[][] corpusMatrix) {
        this.corpusMatrix = corpusMatrix;
        hashFunctions = new ArrayList<>(K);
        compoundHashTable = new ArrayList<>(K);
        for (int i = 0; i < K; i++) {
            // Always set r to 1.
            hashFunctions.add(i, new HashFunction(corpusMatrix[0].length, 1));
        }
        fit();
    }

    private void fit() {

        for (int i = 0; i < hashFunctions.size(); i ++) {
            // Create a map for each hashfunction, corresponding to a level-1 hashtable
            Map<Integer, List<Integer>> hashTable = new HashMap<>();
            for (int cIndex = 0; cIndex < corpusMatrix.length; cIndex++) {
                int hashValue =  hashFunctions.get(i).hash(corpusMatrix[cIndex]);

                if (hashTable.containsKey(hashValue)) {
                    hashTable.get(hashValue).add(cIndex);
                } else {
                    List<Integer> list = new LinkedList<>();
                    list.add(cIndex);
                    hashTable.put(hashValue, list);
                }
            }
            compoundHashTable.add(i, hashTable);
        }
        
    }


    public int[] search(float[] qVec, int k, int minSize, int threshold) {
       
        HashSet<Integer> candidateSet = new HashSet<>();
        int R = 1;
        int c = 3;

        // Initialization and first round

        // Vector to count frequency of corpusPoints
        int[] frequency = new int[corpusMatrix.length];
        PointerSet[] hashFunctionPointers = new PointerSet[compoundHashTable.size()];
        for (int i = 0; i < compoundHashTable.size(); i++) {

            // Initialize PointerSet with bid of qVec
            int bid = hashFunctions.get(i).hash(qVec);
            hashFunctionPointers[i] = new PointerSet(bid);

            // Count frequency of corpusPoints in same lvl-1 bucket as qVec
            List<Integer> bidBucket = compoundHashTable.get(i).get(bid);
            for (Integer cIndex : bidBucket) {
                if (++frequency[cIndex] == threshold) {
                    candidateSet.add(cIndex);
                }
            }
            
            if (candidateSet.size() >= minSize) {
                return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);
            }
        }

        // Run until candidateSet has acceptable size
        while (true) {
            int oldR = R;
            R = c*R;
            for (int i = 0; i < compoundHashTable.size(); i++) {

                hashFunctionPointers[i].increaseWidth(R);
            }

            for (int iteration = oldR; iteration < R; iteration++) {
                for (int i = 0; i < compoundHashTable.size(); i++) {

                    int nextBid = hashFunctionPointers[i].getNext();

                    List<Integer> bidBucket = compoundHashTable.get(i).get(nextBid);
                    for (Integer cIndex : bidBucket) {
                        if (++frequency[cIndex] == threshold) {
                            candidateSet.add(cIndex);
                        }
                    }

                    if (candidateSet.size() >= minSize) {
                        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);
                    }

                }
            }
        }
    }

    class PointerSet {

        int pStart;
        int pLeft;
        int bid;
        int pRight;
        int pEnd;

        public PointerSet(int bid) {
            this.pStart = bid;
            this.pLeft = bid;
            this.bid = bid;
            this.pRight = bid;
            this.pEnd = bid;
        }

        public void increaseWidth(int R) {
            pStart = (bid/R)*R;
            pEnd = pStart + R-1;
        }

        public int getNext() {
            if (pStart != pLeft) {
                return --pLeft;
            } else if (pEnd != pRight) {
                return ++pRight;
            }
            // Must return something - should never actually be reached if query() is correct
            return bid;
            
        }
    }

}