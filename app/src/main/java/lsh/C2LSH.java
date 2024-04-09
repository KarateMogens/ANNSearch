package lsh;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

public class C2LSH implements Searchable, Serializable {

    List<HashFunction> hashFunctions;
    List<Map<Integer,List<Integer>>> compoundHashTable;
    final int c = 3;
    int d;
    int minSize;
    int threshold;
    int corpusMatrixSize;

    public C2LSH(int d, int K, int minSize, int threshold) {
        this.d = d;
        hashFunctions = new ArrayList<>(K);
        compoundHashTable = new ArrayList<>(K);
        for (int i = 0; i < K; i++) {
            // Always set r to 1.
            // This causes way too large buckets Angular distance case (max distance is 2)
            hashFunctions.add(i, new HashFunction(d, 1));
        }
        this.minSize = minSize;
        this.threshold = threshold;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void fit(float[][] corpusMatrix) {
        this.corpusMatrixSize = corpusMatrix.length;

        for (int i = 0; i < hashFunctions.size(); i ++) {
            // Create a map for each hashfunction where the key is a hashed value
            // and the value is the corpus points that hash to the value
            Map<Integer, List<Integer>> hashTable = new HashMap<>();
            // Hash all corpus points and add them to map
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

    public Collection<Integer> search(float[] qVec) {

        // Vector to count frequency of corpusPoints
        int[] frequency = new int[corpusMatrixSize];
        PointerSet[] hashFunctionPointers = new PointerSet[compoundHashTable.size()];
        List<Integer> candidateSet = new LinkedList<>();
        
        //oldR is not used in first iteration
        int oldR = 0;
        int R = 1;

        while (true) {
            // Prepare pointers for new iteration
            for (int i = 0; i < compoundHashTable.size(); i++) {
                if (R == 1) {
                    // Initialize PointerSet with bid of qVec
                    int bid = hashFunctions.get(i).hash(qVec);
                    hashFunctionPointers[i] = new PointerSet(bid);
                } else {
                    // Rehashing (increasing bucket width)
                    hashFunctionPointers[i].increaseWidth(R);
                } 
            }
            // Iterate until rehash is necessary
            for (int iteration = oldR; iteration < R; iteration++) {
                // Count frequencies
                for (int i = 0; i < compoundHashTable.size(); i++) {
                    int nextBid = hashFunctionPointers[i].next();
                    List<Integer> bidBucket = compoundHashTable.get(i).get(nextBid);
                    if (bidBucket == null) {
                        continue;
                    }
                    for (Integer cIndex : bidBucket) {
                        if (++frequency[cIndex] == threshold) {
                            candidateSet.add(cIndex);
                        }
                    }
                }
                // Check if candidateset is large enough
                if (candidateSet.size() >= minSize) {
                    return candidateSet;
                }
            }
            // Not enough candidates found -> rehash
            oldR = R;
            R = R*c;
        }
    }

    class PointerSet implements Serializable {

        int pStart;
        int pLeft;
        int bid;
        int pRight;
        int pEnd;
        int sideCount;

        public PointerSet(int bid) {
            this.pStart = bid;
            this.pLeft = bid;
            this.bid = bid;
            this.pRight = bid;
            this.pEnd = bid;
            sideCount = 0;
        }

        public void increaseWidth(int R) {
            // Shift pStart left on the real line, pEnd right on the real line.
            pStart = (int) Math.floor((double) bid/R)*R;
            pEnd = pStart + R-1;
        }

        public int next() {
            // Keep track of which side to expand first (if possible)
            if (sideCount++ % 2 == 0) {
                if (pLeft != pStart) {
                    return --pLeft;
                } else if (pRight != pEnd) {
                    return ++pRight;
                }
            } else {
                if (pRight != pEnd) {
                    return ++pRight;
                } else if (pLeft != pStart) {
                    return --pLeft;
                } 
            }
            // Used in the first iteration
            return bid;
        }
    }

}