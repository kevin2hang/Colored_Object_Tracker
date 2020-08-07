import java.util.ArrayList;

public class Cluster2DPoints {
    
        ArrayList<Point2> points = new ArrayList<Point2>();
        Point2 centerPoint;

        public Cluster2DPoints() {

        }

        public void clearCluster(){
            points.clear();
        }

        public void clearPoints() {
            points.clear();
        }

        public void addPoint(Point2 p) {
            points.add(p);
        }

        public Point2 getCenterPoint() {
            return centerPoint;
        }

        public boolean resetCenterPoint() {
            boolean centerPointChanged=false;
            if (!getCenterPoint().isSameLocationAs(getAvgPoint())) centerPointChanged=true;
            this.centerPoint = getAvgPoint();
            clearCluster();
            return centerPointChanged;
        }

        public void setCenterPoint(Point2 centerPoint) {
            this.centerPoint =centerPoint;
        }

        public Point2 getAvgPoint() {
            int x = 0;
            int y = 0;
            for (int i = 0; i < points.size(); i++) {
                Point2 p = points.get(i);
                x += p.getX();
                y += p.getY();
            }
            short xAvg = 0;
            short yAvg = 0;
            if (points.size() > 1) {
                xAvg = (short) (x / points.size());
                yAvg = (short) (y / points.size());
            }
            else {
                return centerPoint;
            }
            return new Point2(xAvg, yAvg);
        }
    

}
