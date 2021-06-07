package com.example.studienarbeit_kugellabyrinth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import java.util.concurrent.TimeUnit;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;


public class KugelZeichnen extends AppCompatImageView {

    final String TAG = "KugelZeichnen";

    float step;
    float radius;
    float xInit = 0;
    float yInit = 0;
    float xTilt = 0;
    float yTilt = 0;
    int[] kugelPos = new int[2];
    boolean initializeVariables = true;
    private char[][] maze = new char[50][50];



    public KugelZeichnen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

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


    public void getMaze(char[][] charMaze) {
        for (int i = 0; i < charMaze.length; i++) {
            for (int j = 0; j < charMaze.length; j++) {
                maze[i][j] = charMaze[i][j];
            }
        }
    }

    public int[] getBallPos(){
        return kugelPos;
    }

    public void setBallPos(int [] ballPos){
        kugelPos[1] = ballPos[1];
        kugelPos[0] = ballPos[0];
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint black = new Paint();
        black.setColor(Color.rgb(0, 0, 0));
        black.setStrokeWidth(1.0f);
        Paint white = new Paint();
        white.setColor(Color.rgb(255, 255, 255));
        white.setStrokeWidth(1.0f);

        if(initializeVariables){
            step = getWidth() / 23.0f;
            yInit = (getHeight() / 3.0f);
            xInit = step;
            radius = step / 2;

            initializeVariables = false;
        }

            if(-2 > xTilt && maze[kugelPos[0]][kugelPos[1]+1] != 'w' &&
                    maze[kugelPos[0]][kugelPos[1]+1] != '\u0000'){
                kugelPos[1] += 1;
            } else if(2 < xTilt  && kugelPos[1] != 0 &&
                    maze[kugelPos[0]][kugelPos[1]-1] != 'w'){
                kugelPos[1] -= 1;
            } else if(-1 > yTilt && kugelPos[0] != 0 &&
                    maze[kugelPos[0]-1][kugelPos[1]] != 'w'){
                kugelPos[0] -= 1;
            } else if(2 < yTilt && maze[kugelPos[0]+1][kugelPos[1]] != 'w' &&
                    maze[kugelPos[0]+1][kugelPos[1]] != '\u0000'){
                kugelPos[0] += 1;
            }

        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        canvas.drawCircle(xInit + (step*kugelPos[1]) + radius, yInit + (step * kugelPos[0]) + radius, radius, black);
    }
}
