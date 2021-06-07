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

public class MazeZeichnen extends AppCompatImageView {

    final String TAG = "MazeZeichnen";
    private char[][] maze = new char[50][50];


    public MazeZeichnen(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public void getMaze(char[][] charMaze) {
        for (int i = 0; i < charMaze.length; i++) {
            for (int j = 0; j < charMaze.length; j++) {
                maze[i][j] = charMaze[i][j];
            }
        }
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
        Paint green = new Paint();
        green.setColor(Color.rgb(34, 139, 34));
        green.setStrokeWidth(1.0f);
        Paint red = new Paint();
        red.setColor(Color.rgb(220, 20, 60));
        red.setStrokeWidth(1.0f);


        float currHeightPoint = getHeight() / 3.0f;

        float cellWidth = getWidth() / 23.0f;
        float currWidthPoint = cellWidth;


        for (int i = 0; i < maze.length; i++) {
            for (int j = 0; j < maze.length; j++) {
                if (maze[i][j] == 'w') {
                    canvas.drawRect(currWidthPoint, currHeightPoint, currWidthPoint + cellWidth, currHeightPoint + cellWidth, black);
                } else if (maze[i][j] == 'c') {
                    canvas.drawRect(currWidthPoint, currHeightPoint, currWidthPoint + cellWidth, currHeightPoint + cellWidth, white);
                } else if(maze[i][j] == 's'){
                    canvas.drawRect(currWidthPoint, currHeightPoint, currWidthPoint + cellWidth, currHeightPoint + cellWidth, green);
                } else if(maze[i][j] == 'e') {
                    canvas.drawRect(currWidthPoint, currHeightPoint, currWidthPoint + cellWidth, currHeightPoint + cellWidth, red);
                }
                currWidthPoint += cellWidth;
            }
            currWidthPoint = cellWidth;
            currHeightPoint += cellWidth;
        }

    }
}
