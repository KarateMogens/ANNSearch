package lsh;

import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Collection;

public class ANNSearcher {

    private List<Searchable> searchables;
    private float[][] corpusMatrix;
    private Integer[][] neighborsTable;
    private float[] weightedVoteFreq;
    private int[] voteFreq;

    public ANNSearcher(List<Searchable> searchables, float[][] corpusMatrix) {
        this.searchables = searchables;
        this.corpusMatrix = corpusMatrix;
        this.neighborsTable = null;
        this.weightedVoteFreq = new float[corpusMatrix.length];
        this.voteFreq = new int[corpusMatrix.length];
    }

    public float[][] getCorpusMatrix() {
        return corpusMatrix;
    }

    public void setSecondaryIndex(int[][] neighborsTable, int k) {
        
        this.neighborsTable = new Integer[neighborsTable.length][];

        for (int i = 0; i < neighborsTable.length; i++) {
            Integer[] temp = new Integer[k];
            for (int j = 0; j < k; j++) {
                temp[j] = neighborsTable[i][j];
            }
            this.neighborsTable[i] = temp;
        }
    }



    /* ----------- SEARCH STRATEGIES ----------- */

    public int[] lookupSearch(int[] CSize, float[] qVec, int k) {

        Set<Integer> candidateSet = new HashSet<>();
        
        for (Searchable searchable : searchables) {
            // get partitionset of q for each 
            Collection<Integer> searchResult = searchable.search(qVec);
            if (searchResult == null) {
                continue;
            }
            // add all points to candidate set
            candidateSet.addAll(searchResult);
        }
        
        CSize[0] = candidateSet.size();
        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);
    }

    public int[] votingSearch(int[] CSize, float[] qVec, int k, int threshold) {

        resetVotes();
        
        List<Integer> candidateSet = new LinkedList<>();

        for (Searchable searchable : searchables) {
            Collection<Integer> searchResult = searchable.search(qVec);
            if (searchResult == null) {
                continue;
            }

            for (Integer cIndex : searchResult) {
                // Count votes of partition elements
                if (++voteFreq[cIndex] == threshold) {
                    candidateSet.add(cIndex);
                } 
            }
        }

        CSize[0] = candidateSet.size();
        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);
    }

    public int[] naturalClassifierSearch(int CSize[], float[] qVec, int k, float threshold) {

        // Reset counting array
        resetWeightedVotes();

        Set<Integer> candidateSet = new HashSet<>();
        
        for (Searchable searchable : searchables) {
            Collection<Integer> searchResult = searchable.search(qVec);
            if (searchResult == null) {
                continue;
            }
            // Calculate voteweight
            float voteWeight = 1.0f/(searchResult.size()*searchables.size());

            for (Integer cIndex : searchResult) {
                // Count votes of neighbors in partition
                for (Integer neighborOfcIndex : neighborsTable[cIndex]) {
                    // Increment vote and check if above threshold
                    if ((weightedVoteFreq[neighborOfcIndex] += voteWeight) >= threshold) {
                        candidateSet.add(neighborOfcIndex);
                    }
                }
            }
        }

        CSize[0] = candidateSet.size();
        // Brute force candidate set
        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);

    }

    public int[] naturalClassifierSearchRawCount(int[] CSize, float[] qVec, int k, int threshold) {
        
        resetVotes();

        List<Integer> candidateSet = new LinkedList<>();
        
        for (Searchable searchable : searchables) {
            Collection<Integer> searchResult = searchable.search(qVec);
            if (searchResult == null) {
                continue;
            }

            for (Integer cIndex : searchResult) {
                // Count votes of neighbors in partition
                for (Integer neighborOfcIndex : neighborsTable[cIndex]) {
                    if (++voteFreq[neighborOfcIndex] == threshold) {
                        candidateSet.add(neighborOfcIndex);
                    }
                }
            }
        }

        CSize[0] = candidateSet.size();
        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);
        
    }

    public int[] naturalClassifierSearchSetSize(int[] CSize, float[] qVec, int k, int candidateSetSize) {
      
        resetWeightedVotes();

        List<Integer> nonZeroVotes = new LinkedList<>();

        for (Searchable searchable : searchables) {
            Collection<Integer> searchResult = searchable.search(qVec);
            if (searchResult == null) {
                continue;
            }
            // Calculate voteweight
            float voteWeight = 1.0f/(searchResult.size()*searchables.size());

            for (Integer cIndex : searchResult) {
                // Count votes of neighbors in partition
                for (Integer neighborOfcIndex : neighborsTable[cIndex]) {
                    // Increment vote
                    if (weightedVoteFreq[neighborOfcIndex] == 0.0f) {
                        nonZeroVotes.add(neighborOfcIndex);
                    }
                    weightedVoteFreq[neighborOfcIndex] += voteWeight;
                }
            }
        }
        
        // If max candidatesetsize not reached, all elements are part of C
        if (nonZeroVotes.size() < candidateSetSize) {
            CSize[0] = nonZeroVotes.size();
            return Utils.bruteForceKNN(corpusMatrix, qVec, nonZeroVotes, k);
        }
        
        // Convert all vote entries into an array which is compatible with quickSelect.
        Vote[] votes = new Vote[nonZeroVotes.size()];
        int ctr = 0;
        for (Integer cIndex : nonZeroVotes) {
            votes[ctr++] = new Vote(cIndex, weightedVoteFreq[cIndex]);
        }

        // Pick only top candidates
        int location = Utils.quickSelect(votes, 0, nonZeroVotes.size()-1, candidateSetSize);
        List<Integer> candidateSet = new LinkedList<>();
        for (int i = 0; i <= location; i++) {
            candidateSet.add(votes[i].getcIndex());
        }

        CSize[0] = candidateSet.size();
        return Utils.bruteForceKNN(corpusMatrix, qVec, candidateSet, k);
    }

    private void resetWeightedVotes() {
        Arrays.fill(weightedVoteFreq, 0.0f);
    }

    private void resetVotes() {
        Arrays.fill(voteFreq, 0);
    }
   
    public int[] bruteForceSearch(int[] CSize, float[] qVec, int k) {
        
        CSize[0] = corpusMatrix.length;
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
