package com.example.studienarbeit_kugellabyrinth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class StartActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
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

    long[] soundArr = {R.raw.winning, 0L};


    SharedPreferences mPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);
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



        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gameIntent = new Intent(StartActivity.this, MainActivity.class);
                editor.putString("name", String.valueOf(name.getText()));
                editor.putString("sensorType", sensorType);
                editor.putString("brokerIP", String.valueOf(brokerIP.getText()));
                editor.putString("topic", String.valueOf(topic.getText()));
                editor.putLong("soundID", soundID);
                editor.apply();
                startActivity(gameIntent);
                finish();
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
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        soundID = soundArr[position];
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }




}
