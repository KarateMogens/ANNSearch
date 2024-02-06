package lsh;

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

        // To be implemented

        return -1.0f;
    }

    //NEW METHOD HERE
}
