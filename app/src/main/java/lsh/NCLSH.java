package lsh;

import java.util.Set;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.io.Serializable;

public class NCLSH extends LSH implements Serializable {

    private int[][] secondaryIndex;
    private int k;
    
    public NCLSH(int L, int K, float r, float[][] corpusMatrix, int k, int[][] secondaryIndex) {
        super(L, K, r, corpusMatrix);
        this.k = k;
        this.secondaryIndex = secondaryIndex;

    }

    public NCLSH(int L, int K, float r, float[][] corpusMatrix, int k) {
        super(L, K, r, corpusMatrix);
        this.k = k;
        //this.secondaryIndex = Utils.groundTruth(corpusMatrix, k);
    }

    public void setSecondaryIndex(int[][] groundTruth) {
        
        if (groundTruth[0].length == k) {
            this.secondaryIndex = groundTruth;
            return;
        }

        for (int i = 0; i < groundTruth.length; i++) {
            int[] temp = new int[k];
            for (int j = 0; j < k; j++) {
                temp[j] = groundTruth[i][j];
            }
            groundTruth[i] = temp;
        }
        this.secondaryIndex = groundTruth;
    }

    private HashMap<Integer, Float> getVoteMap(float[] qVec) {

        HashMap<Integer, Float> corpusVotes = new HashMap<>();


        for (HashTable hashTable : hashTableEnsemble) {

            List<Integer> queryResult = hashTable.query(qVec);
            // Account for varying partition size
            float voteWeight = 1/(float) queryResult.size();
            for (Integer cIndex : queryResult) {
                // Count votes of neighbors in partition
                for (Integer neighborOfcIndex : secondaryIndex[cIndex]) {
                    if (corpusVotes.containsKey(neighborOfcIndex)) {
                        corpusVotes.replace(neighborOfcIndex, corpusVotes.get(neighborOfcIndex) + voteWeight);
                    } else {
                        corpusVotes.put(cIndex, voteWeight);
                    }
                }
            }
        }

        return corpusVotes;
    }

    public int[] naturalClassifierSearch(float[] qVec, int k, float threshold) {

        HashMap<Integer, Float> corpusVotes = getVoteMap(qVec);

        //Iterate over all elements, adding only candidates to C with adequate vote average
        Set<Integer> candidateSet = new HashSet<>();
        for (Integer cIndex : corpusVotes.keySet()) {
            float voteValue = corpusVotes.get(cIndex);
            if (voteValue/(float) hashTableEnsemble.size() >= threshold) {
                candidateSet.add(cIndex);
            }
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }

    public int[] naturalClassifierSearch(float[] qVec, int k, int candidateSetSize) {
        
        HashMap<Integer, Float> corpusVotes = getVoteMap(qVec);

        // If max candidatesetsize note reached, all elements are part of C
        if (corpusVotes.size() < candidateSetSize) {
            return Utils.bruteForceKNN(corpusMatrix, qVec, corpusVotes.keySet(),  k);
        }

        // Convert all vote entries into an array which is compatible with quickSelect.
        Vote[] votes = new Vote[corpusVotes.size()];
        int ctr = 0;
        for (Integer cIndex : corpusVotes.keySet()) {
            votes[ctr++] = new Vote(cIndex, corpusVotes.get(cIndex));
        }
        int location = Utils.quickSelect(votes, 0, corpusVotes.size()-1, candidateSetSize);
        
        // Pick only top candidates
        List<Integer> candidateSet = new LinkedList<>();
        for (int i = 0; i <= location; i++) {
            candidateSet.add(votes[i].getcIndex());
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }
    
}
