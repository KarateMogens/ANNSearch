package lsh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

public class UtilsTest {

    @Test
    public void testEuclideanDistance() {

        // Test case 1: Equal vectors
        float[] aVec1 = {1.0f, 2.0f, 3.0f};
        float[] bVec1 = {1.0f, 2.0f, 3.0f};
        float expectedDistance1 = 0.0f;
        assertEquals(expectedDistance1, Utils.euclideanDistance(aVec1, bVec1), 0.0001f);

        // Test case 2: Vectors with different values
        float[] aVec2 = {1.0f, 2.0f, 3.0f};
        float[] bVec2 = {4.0f, 5.0f, 6.0f};
        float expectedDistance2 = (float) Math.sqrt(27.0f); // sqrt((4-1)^2 + (5-2)^2 + (6-3)^2)
        assertEquals(expectedDistance2, Utils.euclideanDistance(aVec2, bVec2), 0.0001f);

        // Test case 3: Vectors with negative values
        float[] aVec3 = {-1.0f, -2.0f, -3.0f};
        float[] bVec3 = {1.0f, 2.0f, 3.0f};
        float expectedDistance3 = (float) Math.sqrt(56.0f); // sqrt((-1-1)^2 + (-2-2)^2 + (-3-3)^2)
        assertEquals(expectedDistance3, Utils.euclideanDistance(aVec3, bVec3), 0.0001f);

    }

    @Test
    public void euclideanSquaredDistanceTest() {

        // Test case 1: Equal vectors
        float[] aVec1 = {1.0f, 2.0f, 3.0f};
        float[] bVec1 = {1.0f, 2.0f, 3.0f};
        float expectedDistance1 = 0.0f;
        assertEquals(expectedDistance1, Utils.euclideanSquareDistance(aVec1, bVec1), 0.0001f);

        // Test case 2: Vectors with different values
        float[] aVec2 = {1.0f, 2.0f, 3.0f};
        float[] bVec2 = {4.0f, 5.0f, 6.0f};
        float expectedDistance2 = 27.0f; // (4-1)^2 + (5-2)^2 + (6-3)^2
        assertEquals(expectedDistance2, Utils.euclideanSquareDistance(aVec2, bVec2), 0.0001f);

        // Test case 3: Vectors with negative values
        float[] aVec3 = {-1.0f, -2.0f, -3.0f};
        float[] bVec3 = {1.0f, 2.0f, 3.0f};
        float expectedDistance3 = 56.0f; // (-1-1)^2 + (-2-2)^2 + (-3-3)^2
        assertEquals(expectedDistance3, Utils.euclideanSquareDistance(aVec3, bVec3), 0.00001f);

    }

    @Test
    public void dotProductTest() {

        float[] aVec1 = {1.0f, 2.0f, 3.0f};
        float[] bVec1 = {1.0f, 5.0f, 7.0f};
        float expectedDistance1 = 32.0f;
        assertEquals(expectedDistance1, Utils.dot(aVec1, bVec1), 0.0001f);

        float[] aVec2 = {-1.0f, -2.0f, 3.0f};
        float[] bVec2 = {4.0f, 0.0f, -8.0f};
        float expectedDistance2 = -28.0f;
        assertEquals(expectedDistance2, Utils.dot(aVec2, bVec2), 0.0001f);

        float[] aVec3 = {9.0f, 5.0f, -4.0f, 2.0f};
        float[] bVec3 = {-3.0f, -2.0f, 7.0f, -1.0f};
        float expectedDistance3 = -67.0f;
        assertEquals(expectedDistance3, Utils.dot(aVec3, bVec3), 0.0001f);
    }

    @Test
    public void quickSelectTest() {

        Vote[] votes = new Vote[10];
        votes[0] = new Vote(0, 7.0f);
        votes[1] = new Vote(0, 6.0f);
        votes[2] = new Vote(0, 1.0f);
        votes[3] = new Vote(0, 2.0f);
        votes[4] = new Vote(0, 9.0f);
        votes[5] = new Vote(0, 4.0f);
        votes[6] = new Vote(0, 6.0f);
        votes[7] = new Vote(0, 21.0f);
        votes[8] = new Vote(0, 5.0f);
        votes[9] = new Vote(0, 8.0f);
        int partition = Utils.quickSelect(votes, 0, 9, 4); 
        float actualValue = votes[partition].getVotes();
        assertEquals(7.0f, actualValue);

        for (int i = partition; i < votes.length; i++) {
            assertTrue(votes[i].getVotes() <= actualValue);
        }


        Vote[] votes2 = new Vote[10];
        votes2[0] = new Vote(0, -7.0f);
        votes2[1] = new Vote(0, 6.0f);
        votes2[2] = new Vote(0, 1.0f);
        votes2[3] = new Vote(0, -2.0f);
        votes2[4] = new Vote(0, 9.0f);
        votes2[5] = new Vote(0, 4.0f);
        votes2[6] = new Vote(0, 6.0f);
        votes2[7] = new Vote(0, -21.0f);
        votes2[8] = new Vote(0, 5.0f);
        votes2[9] = new Vote(0, 8.0f);
        int partition2 = Utils.quickSelect(votes2, 0, 9, 4); 
        float actualValue2 = votes2[partition2].getVotes();
        assertEquals(6.0f, actualValue2);

        for (int i = partition2; i < votes2.length; i++) {
            assertTrue(votes2[i].getVotes() <= actualValue2);
        }
    }

}