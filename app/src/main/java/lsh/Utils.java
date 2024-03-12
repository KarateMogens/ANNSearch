package lsh;

import java.io.File;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class Utils {
    

    public static float dot(float[] aVec, float[] bVec){

        if (aVec.length != bVec.length) {
            throw new IllegalArgumentException("Vectors of unequal length passed");
        }

        float dotProduct = 0.0f;
        
        for (int i = 0; i < aVec.length; i++) {
            dotProduct += aVec[i] * bVec[i];
        }

        return dotProduct;
    }

    public static float euclideanDistance(float[] aVec, float[] bVec) {
        
        if (aVec.length != bVec.length) {
            throw new IllegalArgumentException("Vectors of unequal length passed");
        }

        float squaredDistance = 0.0f;
        for (int i = 0; i < bVec.length; i++) {
            squaredDistance += Math.pow(aVec[i]-bVec[i], 2);
        }
        return (float) Math.sqrt(squaredDistance);

    }

    public static float euclideanSquareDistance(float[] aVec, float[] bVec) {
        
        if (aVec.length != bVec.length) {
            throw new IllegalArgumentException("Vectors of unequal length passed");
        }

        float squaredDistance = 0.0f;
        for (int i = 0; i < bVec.length; i++) {
            squaredDistance += Math.pow(aVec[i]-bVec[i], 2);
        }
        return squaredDistance;

    }

    public static int[] bruteForceKNN(float[][] corpusMatrix, float[] qVec, Iterable<Integer> candidateSet, int k) {
        PriorityQueue<Distance> maxHeap = new PriorityQueue<>();

        for (Integer index : candidateSet) {
            float distance = euclideanDistance(qVec, corpusMatrix[index]);
            maxHeap.add(new Distance(index, distance));
        }

        int[] kNeighbors = new int[k];
        
        for (int i = 0; i < k; i++) {
            kNeighbors[i] = maxHeap.poll().getcIndex();
        }
        
        return kNeighbors;
    }

    public static int[] bruteForceKNN(float[][] corpusMatrix, float[] qVec, int k) {
        PriorityQueue<Distance> maxHeap = new PriorityQueue<>();

        for (int index = 0; index < corpusMatrix.length; index++) {
            float distance = euclideanDistance(qVec, corpusMatrix[index]);
            maxHeap.add(new Distance(index, distance));
        }

        int[] kNeighbors = new int[k];
        
        for (int i = 0; i < k; i++) {
            kNeighbors[i] = maxHeap.poll().getcIndex();
        }
        
        return kNeighbors;
    }

    static class Distance implements Comparable<Distance>{

        private int cIndex;
        private float distanceToQ;

        public int getcIndex() {
            return cIndex;
        }

        public float getDistanceToQ() {
            return distanceToQ;
        }

        public Distance(int cIndex, float distanceToQ) {
            this.cIndex = cIndex;
            this.distanceToQ = distanceToQ;
        }

        public int compareTo(Distance that) {
            if (distanceToQ < that.getDistanceToQ()) {
                return -1;
            } else if (distanceToQ > that.getDistanceToQ()) {
                return 1;
            }
            return 0;
        }
    }

    public static boolean fileExists(String filePathString) {
        File filePath = new File(filePathString);
        if (filePath.exists() && !filePath.isDirectory()) {
            return true;
        }
        return false;
    }

    private static int partition(Vote[] votes, int lo, int hi) {
        float pivotValue = votes[hi].getVotes();
        int pivotLoc = lo;

        for (int i = lo; i <= hi; i++) {
            if (votes[i].getVotes() > pivotValue){
                Vote temp = votes[i];
                votes[i] = votes[pivotLoc];
                votes[pivotLoc] = temp;
                pivotLoc++;            
            }
        }

        Vote temp = votes[hi];
        votes[hi] = votes[pivotLoc];
        votes[pivotLoc] = temp;
        return pivotLoc;     
    }

    public static int quickSelect(Vote[] votes, int lo, 
                                  int hi, int k) 
    { 
        // find the partition 
        int partition = partition(votes, lo, hi); 
  
        // if partition value is equal to the kth position, 
        // return index of partitioning element 
        if (partition == k - 1) {
            return partition; 
        }
            
        // if partition index is less than kth position, 
        // search right side of the array. 
        else if (partition < k - 1) {
            return quickSelect(votes, partition + 1, hi, k); 
        }
        // if partition index is more than kth position, 
        // search left side of the array. 
        else {
            return quickSelect(votes, lo, partition - 1, k);
        } 
    }

    public static int[][] groundTruth(float[][] corpusMatrix, int k) {
        int corpusSize = corpusMatrix.length;
        int[][] secondaryIndex = new int[corpusSize][];         
        for (int i = 0; i < corpusSize; i++) {
            secondaryIndex[i] = Utils.bruteForceKNN(corpusMatrix, corpusMatrix[i], k);
        }
        return secondaryIndex;
    }

    public static int[][] groundTruthParallel(float[][] corpusMatrix, int k) {
        
        int corpusSize = corpusMatrix.length;
        int[][] secondaryIndex = new int[corpusSize][];

        ExecutorService pool = new ForkJoinPool();
		final int perTask = 1000;
        final int taskCount = corpusSize/perTask;
        Future<?>[] myFutures = new Future<?>[taskCount];

		for (int t = 0; t < taskCount; t++) {
			final int from = perTask * t;
			final int to = (t+1 == taskCount) ? corpusSize : perTask * (t + 1);
			myFutures[t] = pool.submit(() -> {
				for (int i = from; i < to; i++){
					secondaryIndex[i] = Utils.bruteForceKNN(corpusMatrix, corpusMatrix[i], k);
				}
                System.out.println("Done");
            });
		}
		try {
			for (int t = 0; t < taskCount; t++)
				myFutures[t].get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
			pool.shutdown();
		}
		return secondaryIndex;
    }
    
    public static boolean isPrime(long n) {
        long k = 2;
        while (k * k <= n && n % k != 0) {
            k++;
        }
        return n >= 2 && k * k > n;
      }

    public class CandidateSetTooSmallException extends RuntimeException {

        public CandidateSetTooSmallException(String message) {
            super(message);
        }
    }

}
