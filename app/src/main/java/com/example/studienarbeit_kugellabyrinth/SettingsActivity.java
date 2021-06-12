package com.example.studienarbeit_kugellabyrinth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/** Activity, which shows the settings the user can configure, which are
 * name, type of sensor and sound at the end of the game
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    final String TAG = "SettingsActivity";

    /** Button to start the game after configuring or to save the settings
     * after they have been configured in game
     */
    Button start;
    /** For using the phone sensors to move the ball
     */
    RadioButton handy;
    /** For using the raspberry SenseHAT sensors to move the ball
     * over a MQTT connection
     */
    RadioButton raspi;
    /** saves the type of sensor the user chose.
     * either "handy" or "raspi"
     */
    String sensorType;
    /** A textView which can be autocompleted with previous inputs, in this
     * case for the ip of the MQTT broker
     */
    AutoCompleteTextView brokerIP;
    /** A textView which can be autocompleted with previous inputs, in this
     * case for the subscription topic of the MQTT connection
     */
    AutoCompleteTextView topic;
    /** Simple EditText to input text
     */
    EditText name;
    /** Spinner element to open a dropdown list for choosing
     * a sound out of an array of five sounds
     */
    Spinner soundSpinner;
    /** The ID of the chosen sound to save it for later use
     */
    long soundID;
    /** to check if this Activity was called before the start of the game
     * or during the game
     */
    boolean inGame = false;
    /** To save the position of the ball
     */
    int[] ballPos;
    /** To save the maze
     */
    char[][] maze;
    /** To save the current time the user needed => will be
     * used if the user goes back to the game to "stop" the time
     */
    int time;
    /** Array consisting of five different sound, which are a trumpet,
     * applause, firecrackers, a TaDa sound and silence
     */
    long[] soundArr = {R.raw.winning, R.raw.applause, R.raw.firecrackers, R.raw.tada, 0L};
    /** The SharedPreferences to save the settings
     */
    SharedPreferences mPreferences;
    /** The editor to edit the SharedPreferences and write the settings into
     * them
     */
    SharedPreferences.Editor editor;

    /** This method is called when SettingsActivity is created.
     * It handles the initialization of the instance variables and the
     * displaying of the elements the user can configure both for before the game
     * and during the game
     * @param savedInstanceState saved variables of the Activity was paused, e.g. when the screen is
     *                           rotated, to get them again and restore the state of the Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // sets the toolbar for all other Activities
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initializes the SharedPreferences
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mPreferences.edit();

        // Initializes the input elements and sets default values when possible
        start = findViewById(R.id.start);
        handy = findViewById(R.id.handy);
        raspi = findViewById(R.id.raspi);
        sensorType = "handy";

        brokerIP = findViewById(R.id.brokerIP);
        topic = findViewById(R.id.topic);
        name = findViewById(R.id.name);

        soundSpinner = findViewById(R.id.soundList);

        // Defines a adapter for the spinner, which contains the IDs of the sounds
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sounds_array, R.layout.spinner_item);
        // Specifies the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Applies the adapter to the spinner
        soundSpinner.setAdapter(adapter);
        soundSpinner.setOnItemSelectedListener(this);
        soundID = soundArr[0];

        // defines a adapter for the brokerIP input field
        // this adapter consists of the last known brokerIP in the SharedPreferences
        ArrayAdapter<String> brokerAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, new String[]{mPreferences.getString("brokerIP", "")});

        // sets the threshold to 1, i.d. only one character of the IP has to be
        // typed to show the saved brokerIP
        brokerIP.setThreshold(1);
        brokerIP.setAdapter(brokerAdapter);

        // the same with topic
        ArrayAdapter<String> topicAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, new String[]{mPreferences.getString("topic", "")});

        topic.setThreshold(1);
        topic.setAdapter(topicAdapter);

        maze = new char[50][50];
        ballPos = new int[2];

        // if the settings are called in game
        Intent intent = getIntent();
        if(getIntent().getExtras() != null){
            inGame = intent.getBooleanExtra("inGame", false);

            // get the user input from before the game and fill the elements with them
            name.setText(mPreferences.getString("name", ""));
            sensorType = mPreferences.getString("sensorType", "handy");
            soundID = mPreferences.getLong("soundID", 0);
            // save the maze and the ball position to restore the game later
            Object[] objectArray = (Object[]) getIntent().getExtras().getSerializable("maze");
            if (objectArray != null) {

                maze = new char[objectArray.length][];
                for (int i = 0; i < objectArray.length; i++) {
                    maze[i] = (char[]) objectArray[i];
                }
            }
            ballPos = intent.getIntArrayExtra("ballPos");
            // save the time to "stop" it
            time = intent.getIntExtra("time", 0);
            brokerIP.setText(mPreferences.getString("brokerIP", ""));
            topic.setText(mPreferences.getString("topic", ""));

            // set the radio buttons to the one chosen in the settings before the game
            if(sensorType.equals("handy")){
                handy.setChecked(true);
                raspi.setChecked(false);
                brokerIP.setVisibility(View.INVISIBLE);
                topic.setVisibility(View.INVISIBLE);
            } else{
                handy.setChecked(false);
                raspi.setChecked(true);
                brokerIP.setVisibility(View.VISIBLE);
                topic.setVisibility(View.VISIBLE);
            }

            // set the sound to the one chosen before the game
            int index = 0;
            for(int i = 0; i < soundArr.length; i++){
                if(soundArr[i] == soundID){
                    index = i;
                }
            }

            soundSpinner.setSelection(index);

            // start button is called "Speichern" instead of "Start"
            start.setText(R.string.speichern);
        }

        // disables or enables the back arrow if the Activity is called in game or
        // before the game to have it, when it's called in game to go back to the game
        getSupportActionBar().setDisplayHomeAsUpEnabled(inGame);
        getSupportActionBar().setDisplayShowHomeEnabled(inGame);
        getSupportActionBar().setTitle("Einstellungen");


        // if the button start is triggered
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if phone sensors are chosen
                if(sensorType.equals("handy")){
                    Intent gameIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    // if the Activity is in game give the maze, ball position and time to MainAcitivity
                    if(inGame){
                        Bundle mBundle = new Bundle();
                        mBundle.putSerializable("maze", maze);
                        gameIntent.putExtras(mBundle);
                        gameIntent.putExtra("ballPos", ballPos);
                        gameIntent.putExtra("time", time);
                    }
                    // apply the user input to the SharedPreferences
                    editor.clear();
                    editor.apply();
                    editor.putString("name", String.valueOf(name.getText()));
                    editor.putString("sensorType", sensorType);
                    editor.putString("brokerIP", String.valueOf(brokerIP.getText()));
                    editor.putString("topic", String.valueOf(topic.getText()));
                    editor.putLong("soundID", soundID);
                    editor.apply();
                    // start the game
                    startActivity(gameIntent);
                    finish();
                    // if the sensors of the raspberry SenseHAT are chosen over a MQTT connection
                } else if(sensorType.equals("raspi")){
                    // tests if a MQTT connection to the given IP can be established
                    MQTTTester mqttTester = new MQTTTester();
                    boolean connected = mqttTester.connect(String.valueOf(brokerIP.getText()));
                    // returns if no connection could be made
                    if(!connected){
                        Toast.makeText(SettingsActivity.this, "Verbindung zum Broker konnte nicht hergestellt werden",
                                Toast.LENGTH_SHORT).show();
                        brokerIP.setTextColor(Color.RED);
                        return;
                    }

                    // if connection has been established
                    if(connected){
                        // disconnect to connect again in MainActivity
                        mqttTester.disconnect();
                        // if the topic is empty nothing can be subscribed
                        if(topic.getText().length() == 0){
                            Toast.makeText(SettingsActivity.this, "Topic kann nicht leer sein",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // same as with phone sensors
                        Intent gameIntent = new Intent(SettingsActivity.this, MainActivity.class);
                        if(inGame){
                            Bundle mBundle = new Bundle();
                            mBundle.putSerializable("maze", maze);
                            gameIntent.putExtras(mBundle);
                            gameIntent.putExtra("ballPos", ballPos);
                            gameIntent.putExtra("time", time);
                        }
                        editor.putString("name", String.valueOf(name.getText()));
                        editor.putString("sensorType", sensorType);
                        editor.putString("brokerIP", String.valueOf(brokerIP.getText()));
                        editor.putString("topic", String.valueOf(topic.getText()));
                        editor.putLong("soundID", soundID);
                        editor.apply();
                        startActivity(gameIntent);
                        finish();
                    }
                }


            }
        });

        // on click listener for the "handy" radio button
        handy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorType = "handy";
                brokerIP.setVisibility(View.INVISIBLE);
                topic.setVisibility(View.INVISIBLE);
            }
        });

        // on click listener for the "raspi" radio button
        raspi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorType = "raspi";
                brokerIP.setVisibility(View.VISIBLE);
                topic.setVisibility(View.VISIBLE);
            }
        });

        // text changed listener to set the color of the text from brokerIP
        // back to white if it was changed to red when no connection could be made
        brokerIP.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0)
                    brokerIP.setTextColor(Color.WHITE);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /** gets called when an item of the spinner is selected to set the soundID to the
     * corresponding ID
     * @param parent AdapterView object of the spinner
     * @param view view object of the spinner
     * @param position index of the position of the chosen element in the spinner
     * @param id of the chosen element
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        soundID = soundArr[position];
    }

    /** if no selection in the spinner is made
     * @param parent AdapterView object of the spinner
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /** Handles action bar item clicks
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // starts MainActivity, because it has to be the in game settings when the
        // back arrow in the action bar is available
        if (id == android.R.id.home){
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            intent.putExtras(mBundle);
            intent.putExtra("ballPos", ballPos);
            intent.putExtra("time", time);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /** Overrides the back button on the phone in the bar at the bottom
     * so that no unexpected behavior happens.
     * Differentiates between the game over settings and the in game settings.
     * Starts a MainActivity goes on and ends the App if its before the game
     * because nothing comes before that
     */
    @Override
    public void onBackPressed(){
        if(inGame){
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            intent.putExtras(mBundle);
            intent.putExtra("ballPos", ballPos);
            intent.putExtra("time", time);
            startActivity(intent);
        }
        finish();

    }

}
