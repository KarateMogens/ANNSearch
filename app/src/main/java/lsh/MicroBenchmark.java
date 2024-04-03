package lsh;

import java.lang.reflect.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MicroBenchmark {

    private static final Logger logger = LogManager.getLogger(MicroBenchmark.class);
 
    Timer timer = new Timer();

    public Results benchmark(ANNSearcher searcher, float[][] testSet, float[][] corpusMatrix, String searchStrategy, String metric, Number ... searchParameters) {

        int k = (int) searchParameters[0];
        float[][] distances = new float[testSet.length][];
        int[][] neighbors = new int[testSet.length][];
        int[] candidateSetSizes = new int[testSet.length];
        float[] time = new float[testSet.length];
        float neighborsFound = 0.0f;
        boolean angular = metric.equals("angular");

        try {
            // Use dummy qVec to access right method
            Object[] parameters = combineArrays(testSet[0], searchParameters);
            // Add candidatesetsize parameter into parameters
            int[] candidateSetSize = {0};
            parameters = combineArrays(candidateSetSize, parameters);
            Method method = ANNSearcher.class.getMethod(searchStrategy, getParameterTypes(parameters));
            
            // Test all qVecs in testSet
            for (int i = 0; i < testSet.length; i++) {
                
                // Exchange dummy qVec with qVec under test
                float[] qvec = testSet[i];
                parameters[1] = qvec;

                // Actual timing
                timer.play();
                int[] result = (int[]) method.invoke(searcher, parameters);
                float elapsedTime = (float) timer.check();
                

                // Write partial results to results arrays
                time[i] = elapsedTime;
                candidateSetSizes[i] = candidateSetSize[0];
                neighborsFound += (float) result.length;
                distances[i] = new float[k];
                neighbors[i] = new int[k];

                for (int j = 0; j < result.length; j++) {
                    neighbors[i][j] = result[j];
                    // Calculate distance between qVec and result
                    if (angular) {
                        distances[i][j] = Utils.angularDistance(qvec, corpusMatrix[result[j]]);
                    } else {
                        distances[i][j] = Utils.euclideanDistance(qvec, corpusMatrix[result[j]]);
                    }
                }
                // In case less than k neighbors are returned, add padding to results with default values
                if (result.length < k) {
                    for (int j = result.length; j < k; j++) {
                        neighbors[i][j] = -1;
                        distances[i][j] = Float.MAX_VALUE;
                    }
                }
                // Calculate average # of neighbors found
                neighborsFound = neighborsFound/testSet.length; 
            }
            logger.info("Benchmarking successful");
        } catch ( InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            logger.error("Benchmarking unsuccesful");
        }
        return new Results(distances, neighbors, time, candidateSetSizes, neighborsFound, k);
    }

    private Class<?>[] getParameterTypes(Object[] parameters) {
        // Get correct parametertypes to locate specified method
        Class<?>[] parameterTypes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterTypes[i] = getParameterType(parameters[i]);
        }
        return parameterTypes;
    }

    private Object[] combineArrays(Object extraParameter, Object[] parameters) {
        Object[] combined = new Object[parameters.length + 1];
        combined[0] = extraParameter;
        System.arraycopy(parameters, 0, combined, 1, parameters.length);
        return combined;
    }

    private Class<?> getParameterType(Object parameter) {
        // Since all primitive types are auto-boxed in the array Object[] parameters, primitive types need to be cast back.
        if (parameter instanceof Integer) {
            return int.class;
        } else if (parameter instanceof Integer[]) {
            return int[].class;
        } else if (parameter instanceof Float) {
            return float.class;
        } else if (parameter instanceof Float[]) {
            return float[].class;
        } else if (parameter instanceof Double) {
            return double.class;
        } else if (parameter instanceof Double[]) {
            return double[].class;
        } else {
            return parameter.getClass();
        }
    }

    class Timer {
        
        private long start = 0;
    
        public double check() {
            try {
                long stop = System.nanoTime();
                return (stop - start)/1e9; // Returns time in seconds
            } finally {
                start = 0; //Reset timer
            } 
        }
    
        public void play() { 
            start = System.nanoTime();
        }

    }

    public class Results {

        // Constructor values
        private final float[][] distances;
        private final int[][] neighbors;
        private final float[] queryTimes;
        private final int[] candidateSetSizes;
        private final int count;
        
        // Calculated values
        private float meanQueryTime;
        private float standardDeviation;
        private float maxTime;
        private float minTime;
        private float meanCandidateSetSize;
        private float medianCandidateSetSize;
        private float meanNeighborsFound;
        private float meanRecall;

        public Results(float[][] distances, int[][] neighbors, float[] queryTimes, int[] candidateSetSizes, float meanNeighborsFound, int k) {
            this.count = k;
            this.meanNeighborsFound = meanNeighborsFound;
            this.distances = distances;
            this.neighbors = neighbors;
            this.queryTimes = queryTimes;
            this.candidateSetSizes = candidateSetSizes;
        }

        public int getCount() {
            return count;
        }

        public float[][] getDistances() {
            return distances;
        }

        public int[][] getNeighbors() {
            return neighbors;
        }

        public float[] getQueryTimes() {
            return queryTimes;
        }

        public void calculateStatistics(int[][] trueNeighbors) {
            this.meanQueryTime = Utils.mean(queryTimes);
            this.standardDeviation = (float) Math.sqrt(Utils.variance(queryTimes));
    
            // Convert candidateSetSize to float[] for mean calculation
            float[] candidateSetSizesFloat = new float[candidateSetSizes.length];
            for (int i = 0; i < candidateSetSizes.length; i++) {
                candidateSetSizesFloat[i] = (float) candidateSetSizes[i];
            }
            this.meanCandidateSetSize = Utils.mean(candidateSetSizesFloat);
            this.medianCandidateSetSize = Utils.median(candidateSetSizesFloat);

            // Find max and min query time
            float max = Float.NEGATIVE_INFINITY;
            float min = Float.POSITIVE_INFINITY;
            for (float time : queryTimes ) {
                if (time > max) {
                    max = time;
                } else if (time < min) {
                    min = time; 
                }
            }
            this.maxTime = max;
            this.minTime = min;

            // Calculate recall
            float[] recalls = new float[neighbors.length];
            for (int i = 0; i < neighbors.length; i++) {
                float recall = 0;
                List<Integer> trueNeighborsList = Arrays.stream(trueNeighbors[i])   // IntStream
                            .limit(count)                                           // Take only k first neighbors
                            .boxed()                                                // Stream<Integer>
                            .collect(Collectors.toList());
                for (int j = 0; j < neighbors[i].length; j++) {
                    if (trueNeighborsList.contains(neighbors[i][j])) {
                        recall += 1.0f;
                    }
                }
                recalls[i] = recall / neighbors[i].length;
            }
            meanRecall = Utils.mean(recalls);
        }

        public float getQueriesPrSecond() {
            return 1/meanQueryTime;
        }

        public float getMeanNeighborsFound() {
            return meanNeighborsFound;
        }

        public float getMedianCandidateSetSize() {
            return medianCandidateSetSize;
        }

        public float getMeanCandidateSetSize() {
            return meanCandidateSetSize;
        }

        public float getMeanRecall() {
            return meanRecall;
        }

        public float getMeanQueryTime() {
            return meanQueryTime;
        }

        public float getStandardDeviation() {
            return standardDeviation;
        }

        public float getMaxTime() {
            return maxTime;
        }

        public float getMinTime() {
            return minTime;
        }

    }
}
