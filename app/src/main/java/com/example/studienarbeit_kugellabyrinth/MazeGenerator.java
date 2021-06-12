package com.example.studienarbeit_kugellabyrinth;

/** Maze generator in Java. Published on 2015-07-27
 * @author Joe Wingbermuehle
 * @version 1.0
 * @since 1.0
 */
public class MazeGenerator {

    private static final int WALL = 0;
    private static final int SPACE = 1;
    private static final int START = 2;
    private static final int END = 3;

    private byte[][] data;
    private byte[][] newData;
    private int width;
    private int height;
    private java.util.Random rand = new java.util.Random();

    /** Constructs a MazeGenerator
     * @param width the width of the maze specified in the number of chars
     * @param height the height of the maze specified in the number of chars
     */
    public MazeGenerator(int width, int height) {
        this.width = width;
        this.height = height;
        data = new byte[width][];
        newData = new byte[width-2][height-2];
    }

    /** carves the corridors of the maze in the array according to a maze
     * generator algorithm
     * @param x x position of the starting point => always 2
     * @param y y position of the starting point => always 2
     */
    private void carve(int x, int y) {

        final int[] upx = { 1, -1, 0, 0 };
        final int[] upy = { 0, 0, 1, -1 };

        int dir = rand.nextInt(4);
        int count = 0;
        while(count < 4) {
            final int x1 = x + upx[dir];
            final int y1 = y + upy[dir];
            final int x2 = x1 + upx[dir];
            final int y2 = y1 + upy[dir];
            if(data[x1][y1] == WALL && data[x2][y2] == WALL) {
                data[x1][y1] = SPACE;
                data[x2][y2] = SPACE;
                carve(x2, y2);
            } else {
                dir = (dir + 1) % 4;
                count += 1;
            }
        }
    }

    /** generates a maze with the given width and height
     */
    public byte[][] generate() {
        // initializes whole array as walls
        for(int x = 0; x < width; x++) {
            data[x] = new byte[height];
            for(int y = 0; y < height; y++) {
                data[x][y] = WALL;
            }
        }

        // surround whole maze with space to avoid accidentally
        // going out of the array
        for(int x = 0; x < width; x++) {
            data[x][0] = SPACE;
            data[x][height - 1] = SPACE;
        }
        for(int y = 0; y < height; y++) {
            data[0][y] = SPACE;
            data[width - 1][y] = SPACE;
        }

        // sets starting point and carves corridors in the maze
        data[2][2] = SPACE;
        carve(2, 2);

        // sets starting and end point
        data[2][1] = START;
        data[width - 3][height - 2] = END;


        // deletes surrounding walls
        for(int i = 1; i < data.length - 1; i++){
            for(int j = 1; j < data[0].length - 1; j++){
                newData[i-1][j-1] = data[i][j];
            }
        }

        return newData;
    }

}
