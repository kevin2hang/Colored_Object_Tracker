import processing.core.PApplet;

import javax.swing.*;
import java.util.ArrayList;

public class TrackColor implements PixelFilter, Clickable {
    private int k = 0;
    private ArrayList<ColorValue> colorsToTrack = new ArrayList<>();
    int threshold = 40;
    MatrixConvolution BoxBlur = new MatrixConvolution();
    int widthOfImage;
    int heightOfImage;
    ArrayList<Cluster2DPoints> clusters = new ArrayList();
    short[][] currentPixels;
    ArrayList<Point2> points = new ArrayList<>();

    public TrackColor() {
    }

    @Override
    public DImage processImage(DImage img) {
        short[][] bwpixels = img.getBWPixelGrid();
        widthOfImage = bwpixels[0].length;
        heightOfImage = bwpixels.length;
        if (k != 0) {

            short[][] reds = img.getRedChannel();
            short[][] greens = img.getGreenChannel();
            short[][] blues = img.getBlueChannel();


            for (int r = 0; r < reds.length; r++) {
                for (int c = 0; c < reds[r].length; c++) {
                    boolean isADesiredColor = false;

                    for (int i = 0; i < colorsToTrack.size(); i++) {
                        ColorValue color = colorsToTrack.get(i);
                        if (colorDistanceFrom(reds[r][c], greens[r][c], blues[r][c], color.getR(), color.getG(), color.getB()) < threshold) {
                            bwpixels[r][c] = 255;
                            isADesiredColor = true;
                        }
                    }

                    if (isADesiredColor == false) bwpixels[r][c] = 0;
                }
            }
            img.setPixels(bwpixels);

            img = BoxBlur.processImage(img);

            short[][] blurredBWPixels = img.getBWPixelGrid();
            sharpen(blurredBWPixels);
            img.setPixels(blurredBWPixels);
            currentPixels = blurredBWPixels;
            KMeansClusteringLocation(blurredBWPixels);

            for (int r = 0; r < blurredBWPixels.length; r++) {
                for (int c = 0; c < blurredBWPixels[r].length; c++) {
                    if (blurredBWPixels[r][c] < 150) {
                        reds[r][c] = 0;
                        greens[r][c] = 0;
                        blues[r][c] = 0;
                    }
                }
            }
            img.setColorChannels(reds, greens, blues);
        }
        return img;
    }

    private void sharpen(short[][] blurredBWPixels) {
        for (int r = 0; r < blurredBWPixels.length; r++) {
            for (int c = 0; c < blurredBWPixels[r].length; c++) {
                if (blurredBWPixels[r][c] > 150) blurredBWPixels[r][c] = 255;
                else blurredBWPixels[r][c] = 0;
            }
        }
    }

    private void KMeansClusteringLocation(short[][] blurredBWPixels) {
        while (clusters.size() < k) {
            Cluster2DPoints c = new Cluster2DPoints();
            Point2 centerPoint = new Point2((int) Math.random() * widthOfImage, (int) Math.random() * heightOfImage);
            c.setCenterPoint(centerPoint);
            clusters.add(c);
        }

        updatePointsList();

        boolean clusterCenterPointStayedTheSame = false;
        while (!clusterCenterPointStayedTheSame) {
            sortPointsIntoClusters();
            clusterCenterPointStayedTheSame = setClusterCenterPointsToAvg();
        }
    }

    public void updatePointsList() {
        points.clear();
        for (int r = 0; r < currentPixels.length; r++) {
            for (int c = 0; c < currentPixels[r].length; c++) {
                short bwColorVal = currentPixels[r][c];
                if (bwColorVal >= 240) {
                    Point2 p = new Point2(c, r);
                    points.add(p);
                }
            }
        }
    }

    private boolean setClusterCenterPointsToAvg() {
        boolean clusterCentersAllStayTheSame = true;
        for (int i = 0; i < clusters.size(); i++) {
            Cluster2DPoints cluster = clusters.get(i);
            boolean clusterCenterChanged = cluster.resetCenterPoint();
            if (clusterCenterChanged) clusterCentersAllStayTheSame = false;
        }
        return clusterCentersAllStayTheSame;
    }

    public void sortPointsIntoClusters() {
        for (int i = 0; i < clusters.size(); i++) {
            clusters.get(i).clearPoints();
        }
        for (int i = 0; i < points.size(); i++) {
            placePointIntoCluster(points.get(i));
        }
    }

    private void placePointIntoCluster(Point2 p) {
        double minDistance = p.getDistanceSquaredFrom(clusters.get(0).getCenterPoint());
        int clusterIndex = 0;
        for (int i = 1; i < clusters.size(); i++) {
            Cluster2DPoints cluster = clusters.get(i);
            double distance = p.getDistanceSquaredFrom(cluster.getCenterPoint());
            if (distance < minDistance) {
                minDistance = distance;
                clusterIndex = i;
            }
        }
        clusters.get(clusterIndex).addPoint(p);
    }

    @Override
    public void drawOverlay(PApplet window, DImage original, DImage filtered) {
        if (clusters.size() >= 1) {
            for (int i = 0; i < clusters.size(); i++) {
                Point2 centerPoint = clusters.get(i).getCenterPoint();
                window.fill(255, 255, 255);
                window.ellipse(centerPoint.getX(), centerPoint.getY(), 20, 20);
//                System.out.println(centerPoint.getX()+", "+centerPoint.getY());
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, DImage img) {
        if (mouseX < widthOfImage && mouseX >= 0 && mouseY < heightOfImage && mouseY >= 0) {
            short[][] reds = img.getRedChannel();
            short[][] greens = img.getGreenChannel();
            short[][] blues = img.getBlueChannel();
            ColorValue c = new ColorValue();
            c.setR(reds[mouseY][mouseX]);
            c.setG(greens[mouseY][mouseX]);
            c.setB(blues[mouseY][mouseX]);
            colorsToTrack.add(c);
            k++;
        }
    }

    @Override
    public void keyPressed(char key) {
        if (key == 'z' && colorsToTrack.size() >= 0) {
            colorsToTrack.remove(colorsToTrack.size() - 1);
            clusters.remove(clusters.size() - 1);
            k--;
        }
        if (key == '+') {
            threshold += 5;
        }
        if (key == '-' && threshold >= 20) {
            threshold -= 5;
        }
        if (key == 'k') {
            String input = JOptionPane.showInputDialog("How many objects do you want to track?");
            k = Integer.parseInt(input);
        }
    }

    public double colorDistanceFrom(int r, int g, int b, int r1, int g1, int b1) {
        return Math.sqrt((r1 - r) * (r1 - r) + (g1 - g) * (g1 - g) + (b1 - b) * (b1 - b));
    }
}
