package com.example.studienarbeit_kugellabyrinth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.util.Log;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/** MainActivity for delegating the game logic and getting and sending the data
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity {

    final String TAG = "MainActivity";

    /** Instance of BallDrawer class to draw and update the ball in the maze
     */
    BallDrawer ballDrawer;
    /** Instance of SensorManager to initialize the sensors for listening
     */
    SensorManager mSensorManager;
    /** Instance of TiltEventListener, to listen to changes in the accelerometer sensors
     */
    TiltEventListener mSensorListener;
    /** Position of the ball as indizes in the maze array
     */
    int[] ballPos;
    /** array of chars to save the maze
     */
    char[][] maze;
    /** Instance of MediaPlayer to play sounds at the end of the game
     */
    MediaPlayer mp;
    /** Instance of Intent to start a new intent from this Activity
     */
    Intent intent;
    /** the time needed to complete the game in seconds is saved in this variable
     */
    int time;
    /** Instance of the Stopwatch class to start and stop the time during the game
     */
    Stopwatch stopwatch;
    /** TextView element to show the time during the game
     */
    TextView stopwatchView;
    /** Instance of SharedPreferences to get the variables set in SettingsActivity
     */
    SharedPreferences mPreferences;
    /** topic to subscribe to in MQTT Connection as a string
     */
    private static String sub_topic;
    /** topic to publish to in MQTT Connection as a string,
     * this topic is hardcoded so it can't be changed from the user
     */
    private static final String pub_topic = "sensehat/message";
    /** Instance of MqttClient to connect to a MQTT Broker
     */
    private MqttClient client;
    /** indicates if the App is connected to a MQTT Broker or not
     */
    boolean connected = false;
    /** indicates if the Intent for the Scoreboard is already started
     * so that the Intent isn't started multiple times
     */
    boolean scoreBoardStarted = false;


    /** This method is called when MainActivity is created.
     * It handles the generation of the maze, the ball position,
     * the drawing of the maze, the initialization of the built in
     * phone sensors and the starting of the timer
     * @param savedInstanceState saved variables of the Activity was paused, e.g. when the screen is
     *                           rotated, to get them again and restore the state of the Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // sets the title of the screen to "Labyrinth"
        getSupportActionBar().setTitle("Labyrinth");

        maze = new char[50][50];
        ballPos = new int[2];

        intent = getIntent();
        // sets the Position of the Ball to the start of the maze
        // the start of the maze is at [1, 0] by default
        ballPos[0] = 1;
        ballPos[1] = 0;

        // get the default SharedPreferences of this App
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // if the Activity has extras from the calling intent, e.g. when it is called
        // by SettingsActivity or ScoreboardActivity in game then read the extras and use
        // them to save the state of the game before
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
                System.arraycopy(posArr, 0, ballPos, 0, posArr.length);
            }

            time = intent.getIntExtra("time", 0);
        } else {
            // If there are no extras then it's a new game and the MazeGenerator has
            // to generate a maze
            MazeGenerator mazeGenerator = new MazeGenerator(23, 23);

            byte[][] generatedMaze = mazeGenerator.generate();

            // read the maze and save it in the maze char array
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

        // assign the selfmade view element to mazeDrawer
        MazeDrawer mazeDrawer = findViewById(R.id.mazeDraw);
        // assign the selfmade view element to ballDrawer
        ballDrawer = findViewById(R.id.ballDraw);

        // initialize the sensors if the user wants to use the built in phone sensors
        if(mPreferences.getString("sensorType", "").equals("handy")){
            mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            // abstract method from TiltEventListener is defined here
            mSensorListener = new TiltEventListener() {
                @Override
                public void onTilt(float x, float y) {
                    MainActivity.this.moveBall(x, y);
                }
            };
        }

        // give mazeDrawer and ballDrawer the maze to work with
        mazeDrawer.getMaze(maze);
        ballDrawer.getMaze(maze);

        // draw the maze
        mazeDrawer.invalidate();

        // assign the sound, which will be played at the end according to the
        // one the user chose
        long soundID = mPreferences.getLong("soundID", 0);

        if(soundID != 0){
            mp = MediaPlayer.create(this, (int) soundID);
        } else{
            mp = null;
        }


        // set the ball either to the start or to the last saved position
        ballDrawer.setBallPos(ballPos);

        // assign and start the stopwatch
        stopwatch = new Stopwatch();
        stopwatchView = findViewById(R.id.stopwatch);

        stopwatch.setSeconds(time);

        stopwatch.start();
        stopwatch.runTimer(stopwatchView);
    }

    /** this method is called everytime a new sensor value arrives either from the phone
     * or with MQTT from the raspberry
     * @param x the tilt in x direction of either the phone or the raspberry
     * @param y the tilt in y direction of either the phone or the raspberry
     */
    private void moveBall(float x, float y) {
        // if the value is greater than 1 or less than -1
        // the position of the ball is updated and newly drawn
        if(ballDrawer.updateDirections(x, y)){
            ballDrawer.invalidate();
            // get the Postition of the ball to check if the end is reached
            ballPos = ballDrawer.getBallPos();
            if(maze[ballPos[0]][ballPos[1]] == 'e' && !scoreBoardStarted){
                // set scoreBoardStarted to true to prevent the method to start multiple intents
                // if the method is called multiple times because more sensor data has arrived
                scoreBoardStarted = true;
                // play the ending sound
                if(mp != null){
                    mp.start();
                }
                // start a intent to show the scoreboard with the new dataset of the current player
                Intent scoreboardIntent = new Intent(MainActivity.this, ScoreboardActivity.class);
                scoreboardIntent.putExtra("gameEnded", true);
                stopwatch.stop();
                scoreboardIntent.putExtra("time", stopwatch.getSeconds());
                // publish a message to the raspberry if it is connected
                // to display a end picture on the SenseHAT
                if(connected){
                    publish(pub_topic, "cheer");
                }
                startActivity(scoreboardIntent);
                finish();
            }
        }

    }

    /** called when MainActivity gets paused.
     * disconnects the MQTT connection to the raspberry if it is
     * connected or ends the listening to the sensor values of the phone
     */
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

    /** called when MainActivity gets started again after it was paused or
     * if it is created => gets called after onCreate is finished.
     * connects to the raspberry and subscribes to the topic given by the user
     * or starts listening to the sensor values of the phone with a period of
     * 0.18 seconds
     */
    @Override
    protected void onResume(){
        super.onResume();
        if(mPreferences.getString("sensorType", "").equals("raspi")){

            connected = connect(mPreferences.getString("brokerIP", ""));
            subscribe(mPreferences.getString("topic", ""));
            Toast.makeText(MainActivity.this, "Verbunden mit Broker " +
                    mPreferences.getString("brokerIP", "") + ". Das Topic " +
                    mPreferences.getString("topic", "") + " wurde abonniert.",
                    Toast.LENGTH_SHORT).show();
        } else{
            mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    180000);
        }

    }

    /** Inflate the menu; this adds items to the action bar if it is present.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /** Handles action bar item clicks
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        // differentiates the items, which can be pressed first two are the top menu
        // in the upper right corner, last one is the back button on the upper left corner
        if (id == R.id.action_settings) {
            Intent inGameSettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            inGameSettingsIntent.putExtras(mBundle);
            inGameSettingsIntent.putExtra("ballPos", ballPos);
            stopwatch.stop();
            inGameSettingsIntent.putExtra("time", stopwatch.getSeconds());
            inGameSettingsIntent.putExtra("inGame", true);
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
        } else if (id == android.R.id.home){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /** Overrides the back button on the phone in the bar at the bottom
     * so that no unexpected behavior happens
     */
    @Override
    public void onBackPressed(){
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Connect to the given broker
     * @param broker Broker to connect to
     */
    public boolean connect (String broker) {
        try {
            broker = "tcp://" + broker + ":1883";
            String clientId = MqttClient.generateClientId();
            final MemoryPersistence persistence = new MemoryPersistence();
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
     * Subscribes to a given topic and defines in the overwritten method what should happen
     * with messages, which arrive over MQTT
     * @param topic Topic to subscribe to
     */
    public void subscribe(String topic) {
        try {
            sub_topic = topic;
            client.subscribe(sub_topic, 0, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage msg) throws Exception {
                    String message = new String(msg.getPayload());
                    String[] messageSplitted = message.split(", ", 3);
                    moveBall(Float.parseFloat(messageSplitted[0]), Float.parseFloat(messageSplitted[1]));
                }
            });
        } catch (MqttException e) {
            Toast.makeText(MainActivity.this, "Topic kann nicht abonniert werden", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Publishes a message via MQTT
     * @param topic topic to publish to (hardcoded here)
     * @param msg message to publish to publish topic
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
     * Unsubscribe from previously subscribed topic and
     * end connection to MQTT Broker
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