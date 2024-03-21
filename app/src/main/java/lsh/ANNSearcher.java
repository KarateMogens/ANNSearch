package lsh;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Collection;

public class ANNSearcher {

    private List<Searchable> searchables;
    private float[][] corpusMatrix;
    private int[][] neighborsTable;

    public ANNSearcher(List<Searchable> searchables, float[][] corpusMatrix) {
        this.searchables = searchables;
        this.corpusMatrix = corpusMatrix;
        this.neighborsTable = null;
    }

    public float[][] getCorpusMatrix() {
        return corpusMatrix;
    }

    public void setSecondaryIndex(int[][] neighborsTable, int k) {
        
        if (neighborsTable[0].length == k) {
            this.neighborsTable = neighborsTable;
            return;
        }

        for (int i = 0; i < neighborsTable.length; i++) {
            int[] temp = new int[k];
            for (int j = 0; j < k; j++) {
                temp[j] = neighborsTable[i][j];
            }
            neighborsTable[i] = temp;
        }

        this.neighborsTable = neighborsTable;
    }

    /* ----------- SEARCH STRATEGIES ----------- */

    public Utils.Distance[] lookupSearch(float[] qVec, int k) {


        Set<Integer> candidateSet = new HashSet<>();
        
        for (Searchable searchable : searchables) {
            Collection<Integer> searchResult = searchable.search(qVec);
            if (searchResult == null) {
                continue;
            }

            candidateSet.addAll(searchResult);
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);
    }

    public Utils.Distance[] votingSearch(float[] qVec, int k, int threshold) {
    
        Set<Integer> candidateSet = new HashSet<>();
        int[] frequency = new int[corpusMatrix.length];

        for (Searchable searchable : searchables) {
            Collection<Integer> searchResult = searchable.search(qVec);
            if (searchResult == null) {
                continue;
            }

            for (Integer cIndex : searchResult) {
                if (++frequency[cIndex] == threshold) {
                    candidateSet.add(cIndex);
                };

            }
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);
    }

    public Utils.Distance[] naturalClassifierSearch(float[] qVec, int k, float threshold) {

        if (this.neighborsTable[0].length != k) {
            throw new NeighborTablConfigurationException("Neighbor Table dimensions incorrectly configured.");
        }

        HashMap<Integer, Float> corpusVotes = getVoteMap(qVec);

        //Iterate over all elements, adding only candidates to C with adequate vote average
        Set<Integer> candidateSet = new HashSet<>();
        for (Integer cIndex : corpusVotes.keySet()) {
            float voteValue = corpusVotes.get(cIndex);
            if (voteValue/(float) searchables.size() >= threshold) {
                candidateSet.add(cIndex);
            }
        }

        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }

    public Utils.Distance[] naturalClassifierSearchSetSize(float[] qVec, int k, int candidateSetSize) {
        
        if (this.neighborsTable[0].length != k) {
            throw new NeighborTablConfigurationException("Neighbor Table dimensions incorrectly configured. Expected length " + k + ", table has length  " + this.neighborsTable[0].length + ".");
        }
        
        HashMap<Integer, Float> corpusVotes = getVoteMap(qVec);
        
        // If max candidatesetsize note reached, all elements are part of C
        if (corpusVotes.size() < candidateSetSize) {
            return Utils.bruteForceKNN(corpusMatrix, qVec, corpusVotes.keySet(), k);
        }
        
        // Convert all vote entries into an array which is compatible with quickSelect.
        Vote[] votes = new Vote[corpusVotes.size()];
        int ctr = 0;
        for (Integer cIndex : corpusVotes.keySet()) {
            votes[ctr++] = new Vote(cIndex, corpusVotes.get(cIndex));
        }
        int location = Utils.quickSelect(votes, 0, corpusVotes.size()-1, candidateSetSize);
        
        // Pick only top candidates
        Set<Integer> candidateSet = new HashSet<>();
        for (int i = 0; i <= location; i++) {
            candidateSet.add(votes[i].getcIndex());
        }
        
        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }

    // Helper method for Natural Classifier Search
    private HashMap<Integer, Float> getVoteMap(float[] qVec) {

        if (this.neighborsTable == null) {
            throw new NeighborTablConfigurationException("No table of corpus point neighbors was found.");
        }

        HashMap<Integer, Float> corpusVotes = new HashMap<>();

        for (Searchable searchable : searchables) {
            Collection<Integer> searchResult = searchable.search(qVec);
            if (searchResult == null) {
                continue;
            }

            // Account for varying partition size
            float voteWeight = 1/ (float) searchResult.size();
            for (Integer cIndex : searchResult) {
                // Count votes of neighbors in partition
                for (Integer neighborOfcIndex : neighborsTable[cIndex]) {
                    if (corpusVotes.containsKey(neighborOfcIndex)) {
                        corpusVotes.replace(neighborOfcIndex, corpusVotes.get(neighborOfcIndex) + voteWeight);
                    } else {
                        corpusVotes.put(neighborOfcIndex, voteWeight);
                    }
                }
            }
        }

        return corpusVotes;
    }

    public Utils.Distance[] bruteForceSearch(float[] qVec, int k) {
        
        return Utils.bruteForceKNN(corpusMatrix, qVec, k);
        
    }

    class NeighborTablConfigurationException extends RuntimeException {

        public NeighborTablConfigurationException(String errorMessage) {
            super(errorMessage);
        }

    }

    public class Result {
        
        private int[] neighbors;
        private double time;
        private float[] distances;

        public Result(Utils.Distance[] kNeighbors, double time) {
            this.neighbors = new int[kNeighbors.length];
            this.distances = new float[kNeighbors.length];

            for (int i = 0; i < kNeighbors.length; i++) {
                neighbors[i] = kNeighbors[i].getcIndex();
                distances[i] = kNeighbors[i].getDistanceToQ();
            }
            this.time = time;
        }
    
        public int[] getNeighbors() {
            return neighbors;
        }

        public double getTime() {
            return time;
        }

        public float[] getDistances() {
            return distances;
        }
    }

    public class Vote implements Comparable<Vote> {

        private int cIndex;
        private float votes;
    
        public int getcIndex() {
            return cIndex;
        }
    
        public float getVotes() {
            return votes;
        }
    
        public Vote(int cIndex, float votes) {
            this.cIndex = cIndex;
            this.votes = votes;
        }
    
        public int compareTo(Vote that) {
            if (this.votes > that.getVotes()) return 1;
            if (this.votes < that.getVotes()) return -1;
            return 0;
        }
    
    }

    
}
