package lsh;

import java.lang.reflect.*;

import lsh.MicroBenchmark.Timer;

public class MicroBenchmark {
 
    Timer timer = new Timer();

    public Results benchmark(ANNSearcher searcher, float[][] testSet, String searchStrategy, Number ... searchParameters) {

        float[][] distances = new float[testSet.length][];
        int[][] neighbors = new int[testSet.length][];
        float[] time = new float[testSet.length];

        try {
            Object[] parameterTest = combineArrays(testSet[0], searchParameters);
            Method method = ANNSearcher.class.getMethod(searchStrategy, getParameterTypes(parameterTest));
            for (int i = 0; i < testSet.length; i++) {
                
                Object[] parameters = combineArrays(testSet[i], searchParameters);
                timer.play();
                Utils.Distance[] result = (Utils.Distance[]) method.invoke(searcher, parameters);
                time[i] = (float) timer.check();

                distances[i] = new float[result.length];
                neighbors[i] = new int[result.length];
                for (int j = 0; j < result.length; j++) {
                    distances[i][j] = result[j].getDistanceToQ();
                    neighbors[i][j] = result[j].getcIndex();
                }
            }
        } catch ( InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        } 
        
        return new Results(distances, neighbors, time);
    }

    private Class<?>[] getParameterTypes(Object[] parameters) {
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
        if (parameter instanceof Integer) {
            return int.class;
        } else if (parameter instanceof Integer[]) {
            return int[].class;
        } else if (parameter instanceof Float) {
            return float.class;
        } else if (parameter instanceof Float[]) {
            return float[].class;
        } else {
            return parameter.getClass();
        }

    }

    class Timer {
        
        private long start = 0;
    
        public double check() {
            try {
                long stop = System.nanoTime();
                return (stop - start)/1e9;
            } finally {
                start = 0; //Reset timer
            } 
        }
    
        public void play() { 
            start = System.nanoTime();
        }

    }

    public class Results {

        private final float[][] distances;
        private final int[][] neighbors;
        private final float[] queryTimes;
        
        private float meanQueryTime;
        private float standardDeviation;
        private float maxTime;
        private float minTime;

        public Results(float[][] distances, int[][] neighbors, float[] queryTimes) {
            this.distances = distances;
            this.neighbors = neighbors;
            this.queryTimes = queryTimes;
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

        public void statistics() {
            this.meanQueryTime = Utils.mean(queryTimes);
            this.standardDeviation = (float) Math.sqrt(Utils.variance(queryTimes));
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
