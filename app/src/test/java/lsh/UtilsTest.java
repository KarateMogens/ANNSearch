package lsh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

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
        int partition = Utils.quickSelect(votes, 0, 9, 4); 
        int actualValue = votes[partition];
        assertEquals(7, actualValue);

        for (int i = partition; i < votes.length; i++) {
            assertTrue(votes[i] <= actualValue);
        }


        Integer[] votes2 = new Integer[10];
        votes2[0] = Integer.valueOf(-7);
        votes2[1] = Integer.valueOf(6);
        votes2[2] = Integer.valueOf(1);
        votes2[3] = Integer.valueOf(-2);
        votes2[4] = Integer.valueOf(9);
        votes2[5] = Integer.valueOf(4);
        votes2[6] = Integer.valueOf(6);
        votes2[7] = Integer.valueOf(-21);
        votes2[8] = Integer.valueOf(5);
        votes2[9] = Integer.valueOf(8);
        int partition2 = Utils.quickSelect(votes2, 0, 9, 4); 
        int actualValue2 = votes2[partition2];
        assertEquals(6, actualValue2);

        for (int i = partition2; i < votes2.length; i++) {
            assertTrue(votes2[i] <= actualValue2);
        }
    }

}