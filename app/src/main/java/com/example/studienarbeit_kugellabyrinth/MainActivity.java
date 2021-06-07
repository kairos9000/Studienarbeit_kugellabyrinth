package com.example.studienarbeit_kugellabyrinth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    MazeZeichnen mazeZeichnen;
    KugelZeichnen kugelZeichnen;
    SensorManager mSensorManager;
    TiltEventListener mSensorListener;
    MazeGenerator mazeGenerator;
    int[] ballPos;
    char[][] maze;
    boolean scoreBoardStarted = false;
    MediaPlayer mp;
    Intent intent;
    long soundID;
    int time;
    Stopwatch stopwatch;
    TextView stopwatchView;
    SharedPreferences mPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        maze = new char[50][50];
        ballPos = new int[2];

        intent = getIntent();
        ballPos[0] = 1;
        ballPos[1] = 0;

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(getIntent().getExtras() != null){
            Object[] mazeArr = (Object[]) getIntent().getExtras().getSerializable("maze");
            if (mazeArr != null) {
                maze = new char[mazeArr.length][];
                for (int i = 0; i < mazeArr.length; i++) {
                    maze[i] = (char[]) mazeArr[i];
                }
            }
            int[] posArr = (int[]) getIntent().getExtras().getSerializable("ballPos");
            if (posArr != null) {
                ballPos = new int[posArr.length];
                for (int i = 0; i < posArr.length; i++) {
                    ballPos[i] = (int) posArr[i];
                }
            }

            time = intent.getIntExtra("time", 0);
        }

        //ballPos = intent.getIntArrayExtra("ballPos");

        Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("Labyrinth");

        mazeZeichnen = (MazeZeichnen) findViewById(R.id.mazeZeichnen);
        kugelZeichnen = (KugelZeichnen) findViewById(R.id.kugelZeichnen);

        if(maze[0][0] == '\u0000'){

            mazeGenerator = new MazeGenerator(23, 23);

            byte[][] generatedMaze = mazeGenerator.generate();

            for(int i = 0; i < generatedMaze.length; i++){
                for(int j = 0; j < generatedMaze[0].length; j++){
                    if(generatedMaze[i][j] == 0){
                        maze[i][j] = 'w';
                    } else if(generatedMaze[i][j] == 1) {
                        maze[i][j] = 'c';
                    }
                    else if(generatedMaze[i][j] == 2) {
                        maze[i][j] = 's';
                    }
                    else {
                        maze[i][j] = 'e';
                    }


                }
            }
        }




        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mSensorListener = new TiltEventListener() {
            @Override
            public void onTilt(float x, float y) {
                MainActivity.this.moveBall(x, y);
            }
        };
        mSensorListener.setGravitationalConstant(SensorManager.GRAVITY_EARTH);


        mazeZeichnen.getMaze(maze);
        kugelZeichnen.getMaze(maze);

        mazeZeichnen.invalidate();

        soundID = mPreferences.getLong("soundID", 0);

        if(soundID != 0){
            mp = MediaPlayer.create(this, (int) soundID);
        } else{
            mp = null;
        }


        kugelZeichnen.setBallPos(ballPos);

        kugelZeichnen.invalidate();
        ballPos = kugelZeichnen.getBallPos();
        if(maze[ballPos[0]][ballPos[1]] == 'e' && !scoreBoardStarted){
            scoreBoardStarted = true;
            if(mp != null){
                mp.start();
            }

            Intent scoreboardIntent = new Intent(MainActivity.this, ScoreboardActivity.class);
            scoreboardIntent.putExtra("gameEnded", true);
            stopwatch.stop();
            scoreboardIntent.putExtra("time", stopwatch.getSeconds());
            startActivity(scoreboardIntent);
            finish();

        }

        stopwatch = new Stopwatch();
        stopwatchView = findViewById(R.id.stopwatch);

        stopwatch.setSeconds(time);

        stopwatch.start();
        stopwatch.runTimer(stopwatchView);


    }

    private void moveBall(float x, float y) {
        if(kugelZeichnen.updateDirections(x, y)){
            kugelZeichnen.invalidate();
            ballPos = kugelZeichnen.getBallPos();
            if(maze[ballPos[0]][ballPos[1]] == 'e' && !scoreBoardStarted){
                scoreBoardStarted = true;
                if(mp != null){
                    mp.start();
                }
                Intent scoreboardIntent = new Intent(MainActivity.this, ScoreboardActivity.class);
                scoreboardIntent.putExtra("gameEnded", true);
                stopwatch.stop();
                scoreboardIntent.putExtra("time", stopwatch.getSeconds());
                startActivity(scoreboardIntent);
                finish();
            }
        }

    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent inGameSettingsIntent = new Intent(MainActivity.this, InGameSettings.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            inGameSettingsIntent.putExtras(mBundle);
            inGameSettingsIntent.putExtra("ballPos", ballPos);
            stopwatch.stop();
            inGameSettingsIntent.putExtra("time", stopwatch.getSeconds());
            startActivity(inGameSettingsIntent);
            finish();
        } else if (id == R.id.action_scoreboard) {
            Intent inGameScoreboardIntent = new Intent(MainActivity.this, ScoreboardActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            inGameScoreboardIntent.putExtras(mBundle);
            inGameScoreboardIntent.putExtra("ballPos", ballPos);
            stopwatch.stop();
            inGameScoreboardIntent.putExtra("time", stopwatch.getSeconds());
            inGameScoreboardIntent.putExtra("gameEnded", false);
            startActivity(inGameScoreboardIntent);
            finish();
        }/* else if (id == android.R.id.home){
            Intent intent = new Intent(MainActivity.this, StartActivity.class);
            StartActivity.startActivity.finish();
            finish();
            startActivity(intent);
        }*/

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

}