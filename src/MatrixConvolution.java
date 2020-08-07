import processing.core.PApplet;

public class MatrixConvolution implements PixelFilter {
    int n = 3;
    private static final int [][] BOX_BLUR = {{1,1,1},{1,1,1},{1,1,1}};

    @Override
    public DImage processImage(DImage img) {
        short[][] bwpixels = img.getBWPixelGrid();
        short[][] newImg = new short[img.getHeight()][img.getWidth()];
        for (int r = 0; r < bwpixels.length - n; r++) {
            for (int c = 0; c < bwpixels[r].length - n; c++) {
                int output = matrixConvolutionalFilter(r, c, n, bwpixels, BOX_BLUR);
                newImg[r + n / 2][c + n / 2] = (short)output;
            }
        }
        img.setPixels(newImg);
        return img;
    }

    public MatrixConvolution() {
    }

    private int matrixConvolutionalFilter(int row, int col, int n, short[][] img, int[][] kernel) {
        int output = 0;
        int kernalWeight = 0;
        for (int krow = 0; krow < n; krow++) {
            for (int kcol = 0; kcol < n; kcol++) {
                int kernelVal = kernel[krow][kcol];
                int pixelVal = img[row + krow][col + kcol];
                output += pixelVal * kernelVal;
                kernalWeight += kernel[krow][kcol];
            }
        }
        if (kernalWeight > 0) output = output / kernalWeight;
        if (output < 0) output = 0;
        if (output > 255) output = 255;
        return output;
    }

   

    @Override
    public void drawOverlay(PApplet window, DImage original, DImage filtered) {
    }
}
