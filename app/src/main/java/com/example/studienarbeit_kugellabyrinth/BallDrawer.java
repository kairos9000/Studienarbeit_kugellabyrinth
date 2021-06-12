package com.example.studienarbeit_kugellabyrinth;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.concurrent.TimeUnit;

/** Draws the Ball, which rolls through the maze
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public class BallDrawer extends AppCompatImageView {

    final String TAG = "BallDrawer";

    /** Distance the ball will move in one step
     */
    float step;
    /** radius of the ball
     */
    float radius;
    /** Initial x position of the ball
     */
    float xInit = 0;
    /** Initial y position of the ball
     */
    float yInit = 0;
    /** Value of the accelerometer in x direction
     */
    float xTilt = 0;
    /** Value of the accelerometer in y direction
     */
    float yTilt = 0;
    /** Position of the ball
     */
    int[] ballPos = new int[2];
    /** initialization of the variables in onDraw
     */
    boolean initializeVariables = true;
    /** the maze as a char array => is drawn in MazeDrawer
     */
    private char[][] maze = new char[50][50];
    /** Black Brush for drawing the ball
     */
    Paint black = new Paint();


    /** Constructs a BallDrawer instance
     * @param context The context of the instance
     * @param attrs Attributes which need to be specified in super() call
     */
    public BallDrawer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /** Updates the values of xTilt and yTilt to use in onDraw()
     *  returns false when the value is in the range of -1 to 1
     *  in both directions to prevent that onDraw() is called even when
     *  the phone or raspberry is not moving
     * @param x The x value of the accelerometer
     * @param y The y value of the accelerometer
     * @return a bool which signals if the ball position should be updated or not
     */
    public boolean updateDirections(float x, float y){
        xTilt = x;
        yTilt = y;
        if(-1 > xTilt || 1 < xTilt){
            return true;
        } else if(-1 > yTilt || 1 < yTilt){
            return true;
        }

        return false;
    }

    /** Gets the maze char array from MainActivity to check the paths of the maze
     * @param charMaze The maze from MainActivity
     */
    public void getMaze(char[][] charMaze) {
        for (int i = 0; i < charMaze.length; i++) {
            System.arraycopy(charMaze[i], 0, maze[i], 0, charMaze.length);
        }
    }

    /** gets the current position of the ball to check if the end
     * of the maze is reached in MainActivity
     * @return a two-element int array, which indicates the x and y position of the ball
     */
    public int[] getBallPos(){
        return ballPos;
    }

    /** Sets the ball position to a specific x or y position if the position is saved in MainActivity
     * @param ballPos The ball position from MainActivity
     */
    public void setBallPos(int [] ballPos){
        this.ballPos[1] = ballPos[1];
        this.ballPos[0] = ballPos[0];
    }


    /** draws the ball at the new position.
     * Method is called with invalidate() from MainActivity
     * @param canvas The canvas in which the ball is drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // setting the black color for the brush
        black.setColor(Color.rgb(0, 0, 0));
        black.setStrokeWidth(1.0f);

        // Initializing Variables only once to avoid longer runtime
        if(initializeVariables){
            step = getWidth() / 23.0f;
            yInit = (getHeight() / 4.0f);
            xInit = step;
            radius = step / 2;

            initializeVariables = false;
        }

        // Tests for every direction if the phone is tilted enough
        // and if there is a wall in that way and if the array has values
        // in that direction to avoid going out of the maze
        // and updates the position of the ball in the array
        if(-2 > xTilt && maze[ballPos[0]][ballPos[1]+1] != 'w' &&
                maze[ballPos[0]][ballPos[1]+1] != '\u0000'){
            ballPos[1] += 1;
        } else if(2 < xTilt  && ballPos[1] != 0 &&
                maze[ballPos[0]][ballPos[1]-1] != 'w'){
            ballPos[1] -= 1;
        } else if(-1 > yTilt && ballPos[0] != 0 &&
                maze[ballPos[0]-1][ballPos[1]] != 'w'){
            ballPos[0] -= 1;
        } else if(2 < yTilt && maze[ballPos[0]+1][ballPos[1]] != 'w' &&
                maze[ballPos[0]+1][ballPos[1]] != '\u0000'){
            ballPos[0] += 1;
        }

        // draws the ball according to the position in the array
        canvas.drawCircle(xInit + (step* ballPos[1]) + radius, yInit + (step * ballPos[0]) + radius, radius, black);
    }
}
