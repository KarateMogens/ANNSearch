package lsh;

public class Utils {
    

    public static double dot(double[] aVec, double[] bVec){

        if (aVec.length != bVec.length) {
            throw new IllegalArgumentException("Vectors of unequal length passed");
        }

        double dotProduct = 0.0;
        
        for (int i = 0; i < aVec.length; i++) {
            dotProduct += aVec[i] * bVec[i];
        }

        return dotProduct;
    }

    public static double euclideanDistance(double[] aVec, double[] bVec) {

        // To be implemented

        return -1.0;
    }

    //NEW METHOD HERE
}
