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

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

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
    private static String sub_topic;      // ggf. Anpassen
    private static final String pub_topic = "sensehat/message"; // ggf. Anpassen
    private final int qos = 0; // MQTT quality of service
    private final MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient client;
    boolean connected = false;



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

        if(mPreferences.getString("sensorType", "").equals("handy")){
            mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            mSensorListener = new TiltEventListener() {
                @Override
                public void onTilt(float x, float y) {
                    MainActivity.this.moveBall(x, y);
                }
            };
            mSensorListener.setGravitationalConstant(SensorManager.GRAVITY_EARTH);
        }


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
            if(connected){
                publish(pub_topic, "schwarz rot gelb");
            }
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
                if(connected){
                    publish(pub_topic, "schwarz rot gelb");
                }
                startActivity(scoreboardIntent);
                finish();
            }
        }

    }

    @Override
    protected void onPause() {
        if(mPreferences.getString("sensorType", "").equals("raspi")){
            disconnect();
            connected = false;
        }else {
            mSensorManager.unregisterListener(mSensorListener);
        }

        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(mPreferences.getString("sensorType", "").equals("raspi")){
            connected = connect(mPreferences.getString("brokerIP", ""));
            subscribe(mPreferences.getString("topic", ""));
        } else{
            mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }

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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Connect to broker and
     * @param broker Broker to connect to
     */
    public boolean connect (String broker) {
        try {
            broker = "tcp://" + broker + ":1883";
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            return true;
        } catch (MqttException me) {
            return false;
        }

    }

    /**
     * Subscribes to a given topic
     * @param topic Topic to subscribe to
     */
    public boolean subscribe(String topic) {
        try {
            client.subscribe(topic, qos, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage msg) throws Exception {
                    String message = new String(msg.getPayload());
                    String[] messageSplitted = message.split(", ", 3);
                    moveBall(Float.parseFloat(messageSplitted[0]), Float.parseFloat(messageSplitted[1]));
                }
            });
            sub_topic = topic;
            return true;
        } catch (MqttException e) {
            return false;
        }
    }


    /**
     * Publishes a message via MQTT (with fixed topic)
     * @param topic topic to publish with
     * @param msg message to publish with publish topic
     */
    public void publish(String topic, String msg) {
        MqttMessage message = new MqttMessage(msg.getBytes());
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribe from default topic (please unsubscribe from further
     * topics prior to calling this function)
     */
    public void disconnect() {
        try {
            client.unsubscribe(sub_topic);
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        try {
            client.disconnect();
        } catch (MqttException me) {
            Log.e(TAG, me.getMessage());
        }
    }

}