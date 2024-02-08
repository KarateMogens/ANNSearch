package lsh;

import java.util.Set;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

public class NCLSH extends ClassicLSH implements ANNSearchable, Serializable {

    private int[][] secondaryIndex;
    private int k;

    public NCLSH(int L, int K, float r, float[][] corpusMatrix, int k) {
        super(L, K, r, corpusMatrix);
        this.k = k;
        buildSecondaryIndex();
    }

    private void buildSecondaryIndex() {
        int corpusSize = corpusMatrix.length;
        secondaryIndex = new int[corpusSize][];         
        for (int i = 0; i < corpusSize; i++) {
            secondaryIndex[i] = Utils.bruteForceKNN(corpusMatrix, corpusMatrix[i], k);
        }
    }

    public void reduceSecondaryIndexSize(int k) {
        // To implement
    }

    public int[] search(float[] qVec, int k) {

        
        Map<Integer,Integer> voteMap = new HashMap<>(); 
        for (HashTable hash : hashTableEnsemble) {
            List<Integer> queryResult = hash.query(qVec);
            
            if (queryResult == null) {
                continue;
            }

            for (Integer cIndex : queryResult) {
                for (int i = 0; i < secondaryIndex[cIndex].length; i++) {
                    if (voteMap.keySet().contains(secondaryIndex[cIndex][i])) {
                        voteMap.replace(cIndex, voteMap.get(secondaryIndex[cIndex][i])+1);
                    } else {
                        voteMap.put(cIndex, 1);
                    }
                }
            }
        }
        
        Vote[] votes = new Vote[voteMap.size()];
        int ctr = 0;
        for (Integer cIndex : voteMap.keySet()) {
            votes[ctr++] = new Vote(cIndex, -voteMap.get(cIndex));
        }
        int location = Utils.quickSelect(votes, 0, voteMap.size(), 3*k);
        List<Integer> candidateSet = new LinkedList<>();

        for (int i = 0; i <= location; i++) {
            candidateSet.add(votes[i].getcIndex());
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }

    
    
    
    
}
