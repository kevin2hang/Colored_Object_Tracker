public class Point2 {
    int x, y;

    public Point2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public boolean isSameLocationAs(Point2 p) {
        double diffX = x - p.getX();
        double diffY = y - p.getY();
        if (diffX * diffX + diffY * diffY == 0) return true;
        return false;
    }

    public double getDistanceSquaredFrom(Point2 p) {
        double diffX = x - p.getX();
        double diffY = y - p.getY();
        return diffX * diffX + diffY * diffY;
    }
}
