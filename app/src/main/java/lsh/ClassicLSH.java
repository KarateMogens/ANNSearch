package lsh;

import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;



public class ClassicLSH {
    
    private List<HashTable> allEnsembleHash;
    private float[][] corpusMatrix;
    

    private String DATADIRECTORY;
    private String DATASET;

    //Hyperparameters
    private int L;
    private int K;
    private float r;

    public ClassicLSH(int L, int K, float r, float[][] corpusMatrix) {
        this.corpusMatrix = corpusMatrix;
        this.L = L;
        this.K = K;
        this.r = r;
    }

    private void buildIndexStructure() {
        
        int d = corpusMatrix[0].length;

        allEnsembleHash = new LinkedList<>();
        for (int i = 0; i < L; i++) {
            HashTable hashTable = new HashTable(d, K, r);
            hashTable.fit(corpusMatrix);
            allEnsembleHash.add(hashTable);
        }
    }

    public Set<Integer> search(float[] qVec, int k) {

        Set<Integer> candidateSet = new HashSet<>();
        for (HashTable hash : allEnsembleHash) {
            List<Integer> queryResult = hash.query(qVec);
            if (queryResult == null) {
                continue;
            }
            candidateSet.addAll(hash.query(qVec));
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }

    

}
