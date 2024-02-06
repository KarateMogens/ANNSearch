package lsh;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import ch.systemsx.cisd.hdf5.HDF5FactoryProvider;
import ch.systemsx.cisd.hdf5.IHDF5Reader;

public class NearestNeighborSearch {
    
    private Set<EnsembleHash> allEnsembleHash;
    private float[][] corpusMatrix;

    public NearestNeighborSearch(int L, int K, float r, String dataset) {

        IHDF5Reader reader = HDF5FactoryProvider.get().openForReading(new File(dataset));
        corpusMatrix = reader.readFloatMatrix("train");
        int d = corpusMatrix[0].length;

        allEnsembleHash = new HashSet<>();
        for (int i = 0; i < L; i++) {
            EnsembleHash hashTable = new EnsembleHash(d, K, r);
            hashTable.fit(corpusMatrix);
            allEnsembleHash.add(hashTable);
        }
    }

    public Set<Integer> search(float[] qVec, int k) {

        Set<Integer> candidateSet = new HashSet<>();
        for (EnsembleHash hash : allEnsembleHash) {
            List<Integer> queryResult = hash.query(qVec);
            if (queryResult == null) {
                continue;
            }
            candidateSet.addAll(hash.query(qVec));
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }

   
}
