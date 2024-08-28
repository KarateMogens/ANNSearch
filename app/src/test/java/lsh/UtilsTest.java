package lsh;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

public class UtilsTest {

    @Test
    public void testEuclideanDistance() {

        // Test case 1: Equal vectors
        float[] aVec1 = {1, 2, 3};
        float[] bVec1 = {1, 2, 3};
        float expectedDistance1 = 0;
        assertEquals(expectedDistance1, Utils.euclideanDistance(aVec1, bVec1), 0.0001f);

        // Test case 2: Vectors with different values
        float[] aVec2 = {1, 2, 3};
        float[] bVec2 = {4, 5, 6};
        float expectedDistance2 = (float) Math.sqrt(27); // sqrt((4-1)^2 + (5-2)^2 + (6-3)^2)
        assertEquals(expectedDistance2, Utils.euclideanDistance(aVec2, bVec2), 0.0001f);

        // Test case 3: Vectors with negative values
        float[] aVec3 = {-1, -2, -3};
        float[] bVec3 = {1, 2, 3};
        float expectedDistance3 = (float) Math.sqrt(56); // sqrt((-1-1)^2 + (-2-2)^2 + (-3-3)^2)
        assertEquals(expectedDistance3, Utils.euclideanDistance(aVec3, bVec3), 0.0001f);

    }

    @Test
    public void euclideanSquaredDistanceTest() {

        // Test case 1: Equal vectors
        float[] aVec1 = {1, 2, 3};
        float[] bVec1 = {1, 2, 3};
        float expectedDistance1 = 0;
        assertEquals(expectedDistance1, Utils.euclideanSquareDistance(aVec1, bVec1), 0.0001f);

        // Test case 2: Vectors with different values
        float[] aVec2 = {1, 2, 3};
        float[] bVec2 = {4, 5, 6};
        float expectedDistance2 = 27; // (4-1)^2 + (5-2)^2 + (6-3)^2
        assertEquals(expectedDistance2, Utils.euclideanSquareDistance(aVec2, bVec2), 0.0001f);

        // Test case 3: Vectors with negative values
        float[] aVec3 = {-1, -2, -3};
        float[] bVec3 = {1, 2, 3};
        float expectedDistance3 = 56; // (-1-1)^2 + (-2-2)^2 + (-3-3)^2
        assertEquals(expectedDistance3, Utils.euclideanSquareDistance(aVec3, bVec3), 0.00001f);

    }

    @Test
    public void testAngularDistance() {
        float[] aVec1 = {1.0f, 2.0f, 3.0f};
        float[] bVec1 = {4.0f, 5.0f, 6.0f};
        
        float result1 = Utils.angularDistance(aVec1, bVec1);
        
        // Expected angular distance: 1 - cosine similarity
        float expectedAngularDistance = 0.025368f;

        assertEquals(expectedAngularDistance, result1, 0.0001f);

        float[] aVec2 = {0.0f, 1.0f};
        float[] bVec2 = {1.0f, 0.0f};

        float result2 = Utils.angularDistance(aVec2, bVec2);

        // Expected angular distance: 1 - cosine similarity
        expectedAngularDistance = 1.000000f;

        assertEquals(expectedAngularDistance, result2, 0.0001f);
    }

    @Test
    public void dotProductTest() {

        float[] aVec1 = {1, 2, 3};
        float[] bVec1 = {1, 5, 7};
        float expectedDistance1 = 32;
        assertEquals(expectedDistance1, Utils.dot(aVec1, bVec1), 0.0001f);

        float[] aVec2 = {-1, -2, 3};
        float[] bVec2 = {4, 0, -8};
        float expectedDistance2 = -28;
        assertEquals(expectedDistance2, Utils.dot(aVec2, bVec2), 0.0001f);

        float[] aVec3 = {9, 5, -4, 2};
        float[] bVec3 = {-3, -2, 7, -1};
        float expectedDistance3 = -67;
        assertEquals(expectedDistance3, Utils.dot(aVec3, bVec3), 0.0001f);
    }

    @Test
    public void dotProductSparseTest() {
        float[] aVec = {1.0f, 2.0f, 3.0f};
        float[] bVec = {4.0f, 5.0f, 6.0f};
        List<Integer> dimensions = Arrays.asList(0, 1, 2);

        float result = Utils.dot(aVec, bVec, dimensions);

        // Expected dot product: 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        assertEquals(32.0f, result, 0.0001f);

        float[] aVec2 = {1.0f, 2.0f, 3.0f, 4.0f};
        float[] bVec2 = {5.0f, 6.0f, 7.0f, 8.0f};
        List<Integer> dimensions2 = Arrays.asList(1, 3);  // Only consider the 2nd and 4th components

        float result2 = Utils.dot(aVec2, bVec2, dimensions2);

        // Expected dot product: 2*6 + 4*8 = 12 + 32 = 44
        assertEquals(44.0f, result2, 0.0001f);

        float[] aVec3 = {1.0f, 2.0f, 3.0f};
        float[] bVec3 = {4.0f, 5.0f, 6.0f};
        List<Integer> dimensions3 = Arrays.asList();

        float result3 = Utils.dot(aVec3, bVec3, dimensions3);

        // Expected dot product: 0, as no dimensions are specified
        assertEquals(0.0f, result3, 0.0001f);
    }

    @Test
    public void testMagnitude() {
        float[] aVec = {3.0f, 4.0f};  // {3, 4} forms a 3-4-5 right triangle
        float result = Utils.magnitude(aVec);

        // Expected magnitude: sqrt(3^2 + 4^2) = sqrt(9 + 16) = sqrt(25) = 5
        assertEquals(5.0f, result, 0.0001f);

        float[] aVec2 = {1.0f, 2.0f, 3.0f};
        float result2 = Utils.magnitude(aVec2);

        // Expected magnitude: sqrt(1^2 + 2^2 + 3^2) = sqrt(1 + 4 + 9) = sqrt(14)
        assertEquals(Math.sqrt(14), result2, 0.0001f);
    }

    @Test
    public void testNormalize() {
        float[] aVec = {3.0f, 4.0f};  // {3, 4} forms a 3-4-5 right triangle
        float[] result = Utils.normalize(aVec);

        // Expected normalized vector: {3/5, 4/5} = {0.6, 0.8}
        assertArrayEquals(new float[]{0.6f, 0.8f}, result, 0.0001f);

        float[] aVec2 = {1.0f, 2.0f, 3.0f};
        float[] result2 = Utils.normalize(aVec2);

        // Expected normalized vector: {1/sqrt(14), 2/sqrt(14), 3/sqrt(14)}
        float invSqrt14 = 1.0f / (float) Math.sqrt(14);
        assertArrayEquals(new float[]{1.0f * invSqrt14, 2.0f * invSqrt14, 3.0f * invSqrt14}, result2, 0.0001f);
    }

    @Test
    public void testMean() {
        float[] array1 = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        float result1 = Utils.mean(array1);

        // Expected mean: (1 + 2 + 3 + 4 + 5) / 5 = 3
        assertEquals(3.0f, result1, 0.0001f);

        float[] array2 = {0.0f, 0.0f, 0.0f, 0.0f};
        float result2 = Utils.mean(array2);

        // Expected mean: (0 + 0 + 0 + 0) / 4 = 0
        assertEquals(0.0f, result2, 0.0001f);

        float[] array3 = {-1.0f, 1.0f};
        float result3 = Utils.mean(array3);

        // Expected mean: (-1 + 1) / 2 = 0
        assertEquals(0.0f, result3, 0.0001f);

        float[] array4 = {1.5f, 2.5f, 3.5f};
        float result4 = Utils.mean(array4);

        // Expected mean: (1.5 + 2.5 + 3.5) / 3 = 2.5
        assertEquals(2.5f, result4, 0.0001f);
    }

    @Test
    public void testMedian() {
        float[] array1 = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        float result1 = Utils.median(array1);

        // Expected median: 3.0
        assertEquals(3.0f, result1, 0.0001f);

        float[] array2 = {7.0f, 2.0f, 10.0f, 1.0f, 5.0f, 9.0f};
        float result2 = Utils.median(array2);

        // Expected median: 6.0
        assertEquals(6.0f, result2, 0.0001f);

        float[] array3 = {0.0f, 0.0f, 0.0f, 0.0f};
        float result3 = Utils.median(array3);

        // Expected median: 0.0
        assertEquals(0.0f, result3, 0.0001f);

        float[] array4 = {-1.0f, 1.0f, 2.0f, 3.0f};
        float result4 = Utils.median(array4);

        // Expected median: (1.0 + 2.0) / 2 = 1.5
        assertEquals(1.5f, result4, 0.0001f);

        float[] array5 = {3.5f, 2.5f, 1.5f, 4.5f};
        float result5 = Utils.median(array5);

        // Expected median: (2.5 + 3.5) / 2 = 3.0
        assertEquals(3.0f, result5, 0.0001f);
    }

    @Test
    public void testVariance() {
        float[] array1 = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
        float result1 = Utils.variance(array1);

        // Expected variance: ( (1-3)^2 + (2-3)^2 + (3-3)^2 + (4-3)^2 + (5-3)^2 ) / 5 = 2
        // (4 + 1 + 0 + 1 + 4)/5
        assertEquals(2.0f, result1, 0.0001f);

        float[] array2 = {0.0f, 0.0f, 0.0f, 0.0f};
        float result2 = Utils.variance(array2);

        // Expected variance: ( (0-0)^2 + (0-0)^2 + (0-0)^2 + (0-0)^2 ) / 4 = 0
        assertEquals(0.0f, result2, 0.0001f);

        float[] array3 = {-1.0f, 1.0f};
        float result3 = Utils.variance(array3);

        // Expected variance: ( (-1-0)^2 + (1-0)^2 ) / 2 = 1
        // (1 + 1) / 2
        assertEquals(1.0f, result3, 0.0001f);

        float[] array4 = {1.5f, 2.5f, 3.5f};
        float result4 = Utils.variance(array4);

        // Expected variance: ( (1.5-2.5)^2 + (2.5-2.5)^2 + (3.5-2.5)^2 ) / 3 = 1
        // (1 + 0 + 1)/3
        assertEquals(0.66666f, result4, 0.0001f);
    }

    @Test
    public void testCalculateSplit() {
        float[] array1 = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f};
        float result1 = Utils.calculateSplit(array1);

        assertEquals(0.4f, result1, 0.0001f);
    }

    @Test
    public void testBruteForceKNN() {
        // Sample corpus matrix
        float[][] corpusMatrix = {
            {1.0f, 2.0f, 3.0f},
            {4.0f, 5.0f, 6.0f},
            {7.0f, 8.0f, 9.0f},
            {10.0f, 11.0f, 12.0f}
        };

        // Sample query vector
        float[] qVec = {1.0f, 1.0f, 1.0f};
        
        int k = 2;  // Number of nearest neighbors to find

        int[] result = Utils.bruteForceKNN(corpusMatrix, qVec, k);

        // Expected indices of the nearest neighbors in corpusMatrix: {0, 1}
        assertArrayEquals(new int[]{0, 1}, result);

        // Another example with a different query vector
        float[] qVec2 = {9.0f, 9.0f, 9.0f};

        int[] result2 = Utils.bruteForceKNN(corpusMatrix, qVec2, k);

        // Expected indices of the nearest neighbors in corpusMatrix: {2, 3}
        assertArrayEquals(new int[]{2, 3}, result2);

        // Test with k = 1
        int k2 = 1;
        
        int[] result3 = Utils.bruteForceKNN(corpusMatrix, qVec, k2);

        // Expected indices of the nearest neighbors in corpusMatrix: {0}
        assertArrayEquals(new int[]{0}, result3);

        // Test with a larger corpus matrix and k = 3
        float[][] corpusMatrix2 = {
            {2.0f, 3.0f},
            {4.0f, 5.0f},
            {6.0f, 7.0f},
            {8.0f, 9.0f},
            {10.0f, 11.0f}
        };

        float[] qVec3 = {1.0f, 1.0f};

        int k3 = 3;

        int[] result4 = Utils.bruteForceKNN(corpusMatrix2, qVec3, k3);

        // Expected indices of the nearest neighbors in corpusMatrix2: {0, 1, 2}
        assertArrayEquals(new int[]{0, 1, 2}, result4);
    }


    @RepeatedTest(50)
    public void quickSelectTest() {

        Integer[] votes = new Integer[10];
        votes[0] = Integer.valueOf(7);
        votes[1] = Integer.valueOf(6);
        votes[2] = Integer.valueOf(1);
        votes[3] = Integer.valueOf(2);
        votes[4] = Integer.valueOf(9);
        votes[5] = Integer.valueOf(4);
        votes[6] = Integer.valueOf(6);
        votes[7] = Integer.valueOf(21);
        votes[8] = Integer.valueOf(5);
        votes[9] = Integer.valueOf(8);
        int partition = Utils.dijkstraQuickSelect(votes, 0, 9, 4); 
        int actualValue = votes[partition];
        assertEquals(7, actualValue);

        for (int i = partition; i < votes.length; i++) {
            assertTrue(votes[i] <= actualValue);
        }

        // -----

        Integer[] votes2 = new Integer[20];
        votes2[0] = Integer.valueOf(1);
        votes2[1] = Integer.valueOf(-1);
        votes2[2] = Integer.valueOf(2);
        votes2[3] = Integer.valueOf(5);
        votes2[4] = Integer.valueOf(1);
        votes2[5] = Integer.valueOf(2);
        votes2[6] = Integer.valueOf(3);
        votes2[7] = Integer.valueOf(1);
        votes2[8] = Integer.valueOf(1);
        votes2[9] = Integer.valueOf(2);
        votes2[10] = Integer.valueOf(1);
        votes2[11] = Integer.valueOf(-10);
        votes2[12] = Integer.valueOf(2);
        votes2[13] = Integer.valueOf(3);
        votes2[14] = Integer.valueOf(1);
        votes2[15] = Integer.valueOf(2);
        votes2[16] = Integer.valueOf(3);
        votes2[17] = Integer.valueOf(-1);
        votes2[18] = Integer.valueOf(-1);
        votes2[19] = Integer.valueOf(3);
        int partition2 = Utils.dijkstraQuickSelect(votes2, 0, 19, 6); 
        int actualValue2 = votes2[partition2];
        assertEquals(2, actualValue2);

        for (int i = partition2; i < votes2.length; i++) {
            assertTrue(votes2[i] <= actualValue2);
        }

        // ------
        Integer[] votes3 = new Integer[20];
        votes3[0] = Integer.valueOf(2);
        votes3[1] = Integer.valueOf(2);
        votes3[2] = Integer.valueOf(1);
        votes3[3] = Integer.valueOf(1);
        votes3[4] = Integer.valueOf(3);
        votes3[5] = Integer.valueOf(1);
        votes3[6] = Integer.valueOf(2);
        votes3[7] = Integer.valueOf(2);
        votes3[8] = Integer.valueOf(1);
        votes3[9] = Integer.valueOf(2);
        votes3[10] = Integer.valueOf(1);
        votes3[11] = Integer.valueOf(3);
        votes3[12] = Integer.valueOf(2);
        votes3[13] = Integer.valueOf(3);
        votes3[14] = Integer.valueOf(1);
        votes3[15] = Integer.valueOf(2);
        votes3[16] = Integer.valueOf(3);
        votes3[17] = Integer.valueOf(1);
        votes3[18] = Integer.valueOf(2);
        votes3[19] = Integer.valueOf(3);
        int partition3 = Utils.dijkstraQuickSelect(votes3, 0, 19, 6); 
        int actualValue3 = votes3[partition3];
        assertEquals(2, actualValue3);

        for (int i = partition3; i < votes3.length; i++) {
            assertTrue(votes3[i] <= actualValue3);
        }

    }

}