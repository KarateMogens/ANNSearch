package lsh;

import java.util.Set;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;

public class ClassicLSH {
    
    private List<HashTable> hashTableEnsemble;
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

    public void setIndexStructure(List<HashTable> hashTableEnsemble) {
        this.hashTableEnsemble = hashTableEnsemble;
    }

    public List<HashTable> buildIndexStructure() {
        
        int d = corpusMatrix[0].length;

        hashTableEnsemble = new LinkedList<>();
        for (int i = 0; i < L; i++) {
            HashTable hashTable = new HashTable(d, K, r);
            hashTable.fit(corpusMatrix);
            hashTableEnsemble.add(hashTable);
        }

        return hashTableEnsemble;
    }

    public Set<Integer> search(float[] qVec, int k) {

        Set<Integer> candidateSet = new HashSet<>();
        for (HashTable hash : hashTableEnsemble) {
            List<Integer> queryResult = hash.query(qVec);
            if (queryResult == null) {
                continue;
            }
            candidateSet.addAll(hash.query(qVec));
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }

    

}
