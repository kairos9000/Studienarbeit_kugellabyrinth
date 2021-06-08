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

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    final String TAG = "StartActivity";

    Button start;
    RadioButton handy;
    RadioButton raspi;
    String sensorType;
    AutoCompleteTextView brokerIP;
    AutoCompleteTextView topic;
    EditText name;
    Spinner soundSpinner;
    long soundID;
    boolean inGame = false;
    int[] ballPos;
    char[][] maze;
    int time;

    long[] soundArr = {R.raw.winning, R.raw.applause, R.raw.firecrackers, R.raw.tada, 0L};


    SharedPreferences mPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mPreferences.edit();

        start = findViewById(R.id.start);
        handy = findViewById(R.id.handy);
        raspi = findViewById(R.id.raspi);
        sensorType = "handy";

        brokerIP = findViewById(R.id.brokerIP);
        topic = findViewById(R.id.topic);
        name = findViewById(R.id.name);

        soundSpinner = findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sounds_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        soundSpinner.setAdapter(adapter);
        soundSpinner.setOnItemSelectedListener(this);
        soundID = soundArr[0];

        ArrayAdapter<String> brokerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, new String[] {mPreferences.getString("brokerIP", "")});

        brokerIP.setThreshold(1);
        brokerIP.setAdapter(brokerAdapter);

        ArrayAdapter<String> topicAdapter = new ArrayAdapter<String>(this,
                android.R.layout.select_dialog_item, new String[] {mPreferences.getString("topic", "")});

        topic.setThreshold(1);
        topic.setAdapter(topicAdapter);

        maze = new char[50][50];
        ballPos = new int[2];

        Intent intent = getIntent();
        if(getIntent().getExtras() != null){
            inGame = intent.getBooleanExtra("inGame", false);

            name.setText(mPreferences.getString("name", ""));
            sensorType = mPreferences.getString("sensorType", "handy");
            soundID = mPreferences.getLong("soundID", 0);
            Object[] objectArray = (Object[]) getIntent().getExtras().getSerializable("maze");
            if (objectArray != null) {

                maze = new char[objectArray.length][];
                for (int i = 0; i < objectArray.length; i++) {
                    maze[i] = (char[]) objectArray[i];
                }
            }
            ballPos = intent.getIntArrayExtra("ballPos");
            time = intent.getIntExtra("time", 0);
            brokerIP.setText(mPreferences.getString("brokerIP", ""));
            topic.setText(mPreferences.getString("topic", ""));

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

            int index = 0;
            for(int i = 0; i < soundArr.length; i++){
                if(soundArr[i] == soundID){
                    index = i;
                }
            }

            soundSpinner.setSelection(index);

            start.setText(R.string.speichern);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(inGame);
        getSupportActionBar().setDisplayShowHomeEnabled(inGame);
        getSupportActionBar().setTitle("Einstellungen");



        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sensorType.equals("handy")){
                    Intent gameIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    if(inGame){
                        Bundle mBundle = new Bundle();
                        mBundle.putSerializable("maze", maze);
                        gameIntent.putExtras(mBundle);
                        gameIntent.putExtra("ballPos", ballPos);
                        gameIntent.putExtra("time", time);
                    }
                    editor.clear();
                    editor.apply();
                    editor.putString("name", String.valueOf(name.getText()));
                    editor.putString("sensorType", sensorType);
                    editor.putString("brokerIP", String.valueOf(brokerIP.getText()));
                    editor.putString("topic", String.valueOf(topic.getText()));
                    editor.putLong("soundID", soundID);
                    editor.apply();
                    startActivity(gameIntent);
                    finish();
                } else if(sensorType.equals("raspi")){
                    MQTTTester mqttTester = new MQTTTester();
                    boolean connected = mqttTester.connect(String.valueOf(brokerIP.getText()));
                    if(!connected){
                        Toast.makeText(SettingsActivity.this, "Verbindung zum Broker konnte nicht hergestellt werden",
                                Toast.LENGTH_SHORT).show();
                        brokerIP.setTextColor(Color.RED);
                        return;
                    }

                    if(connected){
                        mqttTester.disconnect();
                        if(topic.getText().length() == 0){
                            Toast.makeText(SettingsActivity.this, "Topic kann nicht leer sein",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
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

        handy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorType = "handy";
                brokerIP.setVisibility(View.INVISIBLE);
                topic.setVisibility(View.INVISIBLE);
            }
        });

        raspi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorType = "raspi";
                brokerIP.setVisibility(View.VISIBLE);
                topic.setVisibility(View.VISIBLE);
            }
        });

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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        soundID = soundArr[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
            finish();
        }else{
            super.onBackPressed();
        }

    }

}
