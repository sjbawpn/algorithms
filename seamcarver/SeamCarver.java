import java.awt.Color;

public class SeamCarver {
    
    // private Picture picture;
    private Color[][] colorGrid;
    private double[][] energy;
    private int height;
    private int width;

    public SeamCarver(Picture picture) {
        // this.picture = picture;
        height = picture.height();
        width = picture.width();
        energy = new double[width][height];
        colorGrid = new Color[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0;  y < height; y++) {
                colorGrid[x][y] = picture.get(x, y);
                if (y == 0 || y == height - 1 || x == 0 || x == width - 1)
                    energy[x][y] = 195075;
                else {
                    Color prevX = picture.get(x - 1, y);
                    Color nextX = picture.get(x + 1, y);
                    Color prevY = picture.get(x, y - 1);
                    Color nextY = picture.get(x, y + 1);

                    double xGrad = getGradient(prevX, nextX);
                    double yGrad = getGradient(prevY, nextY);

                    energy[x][y] = xGrad + yGrad;
                }
            }
        }
    }

    private double getGradient(Color c1, Color c2) {
        
        double r = Math.abs(c2.getRed() - c1.getRed());
        double g = Math.abs(c2.getGreen() - c1.getGreen());
        double b = Math.abs(c2.getBlue() - c1.getBlue());

        return r*r + g*g + b*b;
    }

    // current picture
    public Picture picture() {
        Picture pic = new Picture(width, height);
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                pic.set(x, y, colorGrid[x][y]);
        return pic;
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    // energy of pixel at column x and row y in current picture
    public double energy(int x, int y) {
        return energy[x][y];
    }

    // sequence of indices of horizontal seam in current picture
    public int[] findHorizontalSeam() {
        transpose();
        int[] result = findVerticalSeam();
        transpose();
        return result;
    }

    private void transpose() {
        double[][] tEnergy = new double[height][width];
        Color[][] tColorGrid = new Color[height][width];
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                tEnergy[y][x] = energy[x][y];
                tColorGrid[y][x] = colorGrid[x][y];
            }
        int buffer = width;
        width = height;
        height = buffer;
        energy = tEnergy;
        colorGrid = tColorGrid;

    }


    // sequence of indices of vertical seam in current picture
    public int[] findVerticalSeam() {
        int[] result = new int[height];
        int[][] edgeTo = new int[height][width];
        double[][] energyTo = new double[height][width];

        double smallestBottomEnergy = Integer.MAX_VALUE;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double minEnergyTo = Integer.MAX_VALUE;
                int edgeToMin = -1;

                if (y == 0) {
                    energyTo[y][x] = energy[x][y];
                    edgeTo[y][x] = edgeToMin;
                    continue; 

                }
                double currentEnergyTo = Double.POSITIVE_INFINITY;

                // calculate total energy from edge above
                // to the right if there is one
                if (x > 0) {
                    currentEnergyTo = energyTo[y-1][x-1] + energy[x][y];
                    if (currentEnergyTo < minEnergyTo) {
                        minEnergyTo = currentEnergyTo;
                        edgeToMin = x - 1;   
                    }
                }

                // calculate total energy from edge directly above
                currentEnergyTo = energyTo[y-1][x] + energy[x][y];
                if (currentEnergyTo < minEnergyTo) {
                    minEnergyTo = currentEnergyTo;
                    edgeToMin = x;   
                }

                // calculate total energy from edge above
                // to the right if there is one
                if (x < width - 1) {
                    currentEnergyTo = energyTo[y-1][x+1] + energy[x][y];
                    if (currentEnergyTo < minEnergyTo) {
                        minEnergyTo = currentEnergyTo;
                        edgeToMin = x + 1;   
                    }
                }

                energyTo[y][x] = minEnergyTo;
                edgeTo[y][x] = edgeToMin;

                if (y == height - 1) {
                    if (minEnergyTo < smallestBottomEnergy) {
                        smallestBottomEnergy = minEnergyTo;
                        result[y] = x;
                    }
                }
                
            }
        }

        for (int y = height - 2; y >= 0; y--) {
            result[y] = edgeTo[y+1][result[y+1]]; 
        }
        return result;
    }

    // remove of indices of horizontal seam in current picture
    public void removeHorizontalSeam(int[] a) {
        transpose();
        removeVerticalSeam(a);
        transpose();
    }


    // remove of indices of vertical seam in current picture
    public void removeVerticalSeam(int[] a) {
        if (width <= 1 || height <= 1)
            throw new IllegalArgumentException("Height or Width <= 1");
        if (a.length != height)
            throw new IllegalArgumentException("length != height");
        
        for (int i = 0; i < height; i++) {
            if (i > 0 && (a[i-1] > a[i] + 1 || a[i-1] < a[i] - 1))
                throw new IllegalArgumentException("array is not contiguous");
            if (a[i] > width - 2 || a[i] < 0)
                throw new IllegalArgumentException("Value out of bound");
            System.out.println(a[i] + " " + width);

        }
        Color[][] newColorGrid = new Color[width-1][height];
        double[][] newEnergy = new double[width-1][height];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++) {
                if (x < a[y]) {
                    newColorGrid[x][y] = colorGrid[x][y];
                    if (x == a[y] - 1) {
                        if (x == width - 2 || x == 0)
                            newEnergy[x][y] = 195075;
                        else if (y != 0 && y != height - 1)
                            newEnergy[x][y] = getGradient(colorGrid[x-1][y],
                                                            colorGrid[x+2][y])
                                            + getGradient(colorGrid[x][y-1],
                                                            colorGrid[x][y+1]);
                    } else 
                        newEnergy[x][y] = energy[x][y];
                } else if (x > a[y]) {
                    newColorGrid[x-1][y] = colorGrid[x][y];
                    if (x == a[y] + 1) {
                        if (x == 1 || x == width - 1)
                            newEnergy[x-1][y] = 195075;
                        else if (y != 0 && y != height - 1)
                            newEnergy[x-1][y] = getGradient(colorGrid[x-2][y],
                                                                colorGrid[x+1][y])
                                                + getGradient(colorGrid[x][y-1],
                                                                colorGrid[x][y+1]);

                    } else
                        newEnergy[x-1][y] = energy[x][y];
                }

            }
       colorGrid = newColorGrid;
       energy = newEnergy;
       width = width - 1; 
    }

    public static void main(String[] args) {
        Picture pic = new Picture(args[0]);
        SeamCarver sc = new SeamCarver(pic);
        for (int y = 0; y < sc.height(); y++) {
            for (int x = 0; x < sc.width(); x++) {                           
                System.out.print(sc.energy(x, y) + "  ");
            }
            System.out.println();
        }

        int[] vs = sc.findVerticalSeam();
        int[] hs = sc.findHorizontalSeam();
        sc.picture();
        for (int s : vs) {
            System.out.println(s);
        }
        System.out.println();
        for (int s : hs) {
            System.out.println(s);
        }
        int[] a = new int[]{2, 3, 4, 5, 6, 5};
        sc.removeHorizontalSeam(a);
        for (int y = 0; y < sc.height(); y++) {
            for (int x = 0; x < sc.width(); x++) {                           
                System.out.print(sc.energy(x, y) + "  ");
            }
            System.out.println();
        }
        hs = sc.findHorizontalSeam();
        for (int s : hs) {
            System.out.println(s);
        }
    }
}
