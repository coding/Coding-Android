package net.coding.program.login.auth.qrscan;

/**
 * Created by chenchao on 15/7/7.
 */
public class ResultPoint {
    private final float x;
    private final float y;

    public ResultPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public static void orderBestPatterns(ResultPoint[] patterns) {
        float zeroOneDistance = distance(patterns[0], patterns[1]);
        float oneTwoDistance = distance(patterns[1], patterns[2]);
        float zeroTwoDistance = distance(patterns[0], patterns[2]);
        ResultPoint pointA;
        ResultPoint pointB;
        ResultPoint pointC;
        if (oneTwoDistance >= zeroOneDistance && oneTwoDistance >= zeroTwoDistance) {
            pointB = patterns[0];
            pointA = patterns[1];
            pointC = patterns[2];
        } else if (zeroTwoDistance >= oneTwoDistance && zeroTwoDistance >= zeroOneDistance) {
            pointB = patterns[1];
            pointA = patterns[0];
            pointC = patterns[2];
        } else {
            pointB = patterns[2];
            pointA = patterns[0];
            pointC = patterns[1];
        }

        if (crossProductZ(pointA, pointB, pointC) < 0.0F) {
            ResultPoint temp = pointA;
            pointA = pointC;
            pointC = temp;
        }

        patterns[0] = pointA;
        patterns[1] = pointB;
        patterns[2] = pointC;
    }

    public static float distance(ResultPoint pattern1, ResultPoint pattern2) {
        float xDiff = pattern1.x - pattern2.x;
        float yDiff = pattern1.y - pattern2.y;
        return (float) Math.sqrt((double) (xDiff * xDiff + yDiff * yDiff));
    }

    private static float crossProductZ(ResultPoint pointA, ResultPoint pointB, ResultPoint pointC) {
        float bX = pointB.x;
        float bY = pointB.y;
        return (pointC.x - bX) * (pointA.y - bY) - (pointC.y - bY) * (pointA.x - bX);
    }

    public final float getX() {
        return this.x;
    }

    public final float getY() {
        return this.y;
    }

    public boolean equals(Object other) {
        if (!(other instanceof ResultPoint)) {
            return false;
        } else {
            ResultPoint otherPoint = (ResultPoint) other;
            return this.x == otherPoint.x && this.y == otherPoint.y;
        }
    }

    public int hashCode() {
        return 31 * Float.floatToIntBits(this.x) + Float.floatToIntBits(this.y);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(25);
        result.append('(');
        result.append(this.x);
        result.append(',');
        result.append(this.y);
        result.append(')');
        return result.toString();
    }
}

