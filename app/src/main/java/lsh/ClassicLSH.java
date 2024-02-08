package lsh;

import java.util.Set;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.List;
import java.io.Serializable;

public class ClassicLSH implements ANNSearchable, Serializable{
    
    protected List<HashTable> hashTableEnsemble;
    protected float[][] corpusMatrix;
    protected String DATADIRECTORY;
    protected String DATASET;

    //Hyperparameters
    protected int L;
    protected int K;
    protected float r;

    public ClassicLSH(int L, int K, float r, float[][] corpusMatrix) {
        this.corpusMatrix = corpusMatrix;
        this.L = L;
        this.K = K;
        this.r = r;
        buildIndexStructure();
    }

    private void buildIndexStructure() {
        
        int d = corpusMatrix[0].length;

        hashTableEnsemble = new LinkedList<>();
        for (int i = 0; i < L; i++) {
            HashTable hashTable = new HashTable(d, K, r);
            hashTable.fit(corpusMatrix);
            hashTableEnsemble.add(hashTable);
        }
    }

    public void reduceIndexSize(int L) {
        hashTableEnsemble = hashTableEnsemble.subList(0, L);
    }

    

    public int[] search(float[] qVec, int k) {

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
