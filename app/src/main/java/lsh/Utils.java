package lsh;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

    public static float dot(float[] aVec, float[] bVec, List<Integer> dimensions){

        float dotProduct = 0.0f;
        
        for (Integer componentIndex : dimensions) {
            dotProduct += aVec[componentIndex] * bVec[componentIndex];
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

    public static float magnitude(float[] aVec) {
        double sum = 0.0;
        for (int i = 0; i < aVec.length; i++) {
            sum += Math.pow(aVec[i], 2);
        }
        return (float) Math.sqrt(sum);
    }

    public static float[] normalize(float[] aVec) {
        float[] normVec = Arrays.copyOf(aVec, aVec.length);
        float magnitude = magnitude(aVec);
        for (int i = 0; i < normVec.length; i++) {
            normVec[i] = normVec[i] / magnitude;
        }
        return normVec;
    }

    public static float[][] normalizeCorpus(float[][] corpus) {
        float[][] corpusCopy = Arrays.copyOf(corpus, corpus.length);
        for (int i = 0; i < corpusCopy.length; i++) {
            corpusCopy[i] = normalize(corpusCopy[i]);
        }
        return corpusCopy;
    }

    public static Distance[] bruteForceKNN(float[][] corpusMatrix, float[] qVec, Collection<Integer> candidateSet, int k) {
        
        if (candidateSet.size() < k) {
            // 
            Distance[] kNeighbors = new Distance[k];
            for (int i = 0; i < k; i++) {
                kNeighbors[i] = new Distance(-1, -1);
            }
            return kNeighbors;
        }
        
        PriorityQueue<Distance> maxHeap = new PriorityQueue<>();

        for (Integer index : candidateSet) {
            float distance = euclideanSquareDistance(qVec, corpusMatrix[index]);
            maxHeap.add(new Distance(index, distance));
        }

        Distance[] kNeighbors = new Distance[k];
        
        for (int i = 0; i < k; i++) {
            kNeighbors[i] = maxHeap.poll();
        }
        
        return kNeighbors;
    }

    public static Distance[] bruteForceKNN(float[][] corpusMatrix, float[] qVec, int k) {

        PriorityQueue<Distance> maxHeap = new PriorityQueue<>();

        for (int index = 0; index < corpusMatrix.length; index++) {
            float distance = euclideanSquareDistance(qVec, corpusMatrix[index]);
            maxHeap.add(new Distance(index, distance));
        }

        Distance[] kNeighbors = new Distance[k];
        
        for (int i = 0; i < k; i++) {
            kNeighbors[i] = maxHeap.poll();
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

    private static int partition(Comparable[] a, int lo, int hi) {
        Comparable pivotValue = a[hi];
        int pivotLoc = lo;

        for (int i = lo; i <= hi; i++) {
            if (less(pivotValue, a[i])) {
                Comparable temp = a[i];
                a[i] = a[pivotLoc];
                a[pivotLoc] = temp;
                pivotLoc++;            
            }
        }

        Comparable temp = a[hi];
        a[hi] = a[pivotLoc];
        a[pivotLoc] = temp;
        return pivotLoc;     
    }

    private static boolean less(Comparable u, Comparable v) {
        return u.compareTo(v) < 0;
    }

    public static int quickSelect(Comparable[] a, int lo, 
                                  int hi, int k) 
    { 
        // find the partition 
        int partition = partition(a, lo, hi); 
  
        // if partition value is equal to the kth position, 
        // return index of partitioning element 
        if (partition == k - 1) {
            return partition; 
        }
            
        // if partition index is less than kth position, 
        // search right side of the array. 
        else if (partition < k - 1) {
            return quickSelect(a, partition + 1, hi, k); 
        }
        // if partition index is more than kth position, 
        // search left side of the array. 
        else {
            return quickSelect(a, lo, partition - 1, k);
        } 
    }

    public static int[][] groundTruth(float[][] corpusMatrix, int k) {
        int corpusSize = corpusMatrix.length;
        int[][] secondaryIndex = new int[corpusSize][];         
        for (int i = 0; i < corpusSize; i++) {
            secondaryIndex[i] = new int[k];
            Distance[] result = Utils.bruteForceKNN(corpusMatrix, corpusMatrix[i], k);
            for (int j = 0; j < result.length; j++) {
                secondaryIndex[i][j] = result[j].cIndex;
            }
        }
        return secondaryIndex;
    }

    public static int[][] groundTruthParallel(float[][] corpusMatrix, int k) {
        
        int corpusSize = corpusMatrix.length;
        int[][] secondaryIndex = new int[corpusSize][];

        ExecutorService pool = new ForkJoinPool(1);
		final int perTask = 1000;
        final int taskCount = corpusSize/perTask;
        Future<?>[] myFutures = new Future<?>[taskCount];

		for (int t = 0; t < taskCount; t++) {
			final int from = perTask * t;
			final int to = (t+1 == taskCount) ? corpusSize : perTask * (t + 1);
			myFutures[t] = pool.submit(() -> {
				for (int i = from; i < to; i++){
                    secondaryIndex[i] = new int[k];
                    Distance[] result = Utils.bruteForceKNN(corpusMatrix, corpusMatrix[i], k);
                    for (int j = 0; j < result.length; j++) {
                        secondaryIndex[i][j] = result[j].cIndex;
                    }
					
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

    

    public static float variance(float[] array) {
        
        int size = array.length;

        float mean = Utils.mean(array);
     
        // Compute sum of squared differences with mean.
        double sqDiff = 0;
        for (int i = 0; i < size; i++) {
            sqDiff += (array[i] - mean) * (array[i] - mean);
        }
    
        return (float) (sqDiff / (size -1));

    }

    public static float mean(float[] array) {
        int size = array.length;
        
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += array[i];
        }
        return (float) (sum / size);

    }

    public static float calculateSplit(float[] array) {
        Arrays.sort(array);
        int size = array.length;
        float median;
        if (size % 2 == 0) {
            median = ((float) array[size/2] + (float) array[size/2 - 1])/2;
        } else {
            median = (float) array[size/2];
        }

        // In case the median creates a highly skewed split
        // use mean.
        if (median == array[0] || median == array[size-1]) {
            return Utils.mean(array);
        }

        return median;
    }
    
    /* ----------- Exceptions ----------- */

    public class CandidateSetTooSmallException extends RuntimeException {

        public CandidateSetTooSmallException(String message) {
            super(message);
        }
    }

}
