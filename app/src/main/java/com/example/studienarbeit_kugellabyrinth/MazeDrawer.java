package com.example.studienarbeit_kugellabyrinth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/** draws the maze for the ball to roll through
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public class MazeDrawer extends AppCompatImageView {

    final String TAG = "MazeZeichnen";
    /** array of chars to save the maze to
     */
    private char[][] maze = new char[50][50];
    /** color for the walls
     */
    Paint black = new Paint();
    /** color for the corridors
     */
    Paint white = new Paint();
    /** color for the start
     */
    Paint green = new Paint();
    /** color for the end
     */
    Paint red = new Paint();

    /** Constructs a MazeDrawer instance
     * @param context The context of the instance
     * @param attrs Attributes which need to be specified in super() call
     */
    public MazeDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /** Gets the maze char array from MainActivity to draw it
     * @param charMaze The maze from MainActivity
     */
    public void getMaze(char[][] charMaze) {
        for (int i = 0; i < charMaze.length; i++) {
            System.arraycopy(charMaze[i], 0, maze[i], 0, charMaze.length);
        }
    }

    /** draws the maze (only called once)
     * Method is called with invalidate() from MainActivity
     * @param canvas The canvas in which the maze is drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        black.setColor(Color.rgb(0, 0, 0));
        black.setStrokeWidth(1.0f);

        white.setColor(Color.rgb(255, 255, 255));
        white.setStrokeWidth(1.0f);

        green.setColor(Color.rgb(34, 139, 34));
        green.setStrokeWidth(1.0f);

        red.setColor(Color.rgb(220, 20, 60));
        red.setStrokeWidth(1.0f);

        // gets the width and height of the canvas to draw the maze with the same ratios on
        // different screens
        float currHeightPoint = getHeight() / 4.0f;

        float cellWidth = getWidth() / 23.0f;
        float currWidthPoint = cellWidth;

        // draws the maze by going through the two dimensional array and setting a box
        // of the specified color for each element on the position of the character in the array
        for (char[] chars : maze) {
            for (int j = 0; j < maze.length; j++) {
                if (chars[j] == 'w') {
                    canvas.drawRect(currWidthPoint, currHeightPoint, currWidthPoint + cellWidth, currHeightPoint + cellWidth, black);
                } else if (chars[j] == 'c') {
                    canvas.drawRect(currWidthPoint, currHeightPoint, currWidthPoint + cellWidth, currHeightPoint + cellWidth, white);
                } else if (chars[j] == 's') {
                    canvas.drawRect(currWidthPoint, currHeightPoint, currWidthPoint + cellWidth, currHeightPoint + cellWidth, green);
                } else if (chars[j] == 'e') {
                    canvas.drawRect(currWidthPoint, currHeightPoint, currWidthPoint + cellWidth, currHeightPoint + cellWidth, red);
                }
                currWidthPoint += cellWidth;
            }
            currWidthPoint = cellWidth;
            currHeightPoint += cellWidth;
        }

    }
}
