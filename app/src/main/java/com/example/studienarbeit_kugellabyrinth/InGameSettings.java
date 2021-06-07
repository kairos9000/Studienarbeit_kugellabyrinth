package com.example.studienarbeit_kugellabyrinth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.IntStream;

public class InGameSettings extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    final String TAG = "InGameSettings";

    Button speichern;
    RadioButton handy;
    RadioButton raspi;
    EditText newBrokerIP;
    EditText newTopic;
    EditText newName;
    int[] ballPos;
    char[][] maze;
    Intent intent;
    String oldName;
    String sensorType;
    long oldSoundID;
    long newSoundID;
    Spinner soundSpinner;
    String oldBrokerIP;
    String oldTopic;
    int time;

    long[] soundArr = {R.raw.winning, 0L};

    SharedPreferences mPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ingamesettings_activity);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = mPreferences.edit();

        speichern = findViewById(R.id.start);
        handy = findViewById(R.id.handy);
        raspi = findViewById(R.id.raspi);


        newBrokerIP = findViewById(R.id.brokerIP);
        newTopic = findViewById(R.id.topic);
        newName = findViewById(R.id.name);

        maze = new char[50][50];
        ballPos = new int[2];

        intent = getIntent();
        oldName = mPreferences.getString("name", "");
        sensorType = mPreferences.getString("sensorType", "handy");
        oldSoundID = mPreferences.getLong("soundID", 0);
        Object[] objectArray = (Object[]) getIntent().getExtras().getSerializable("maze");
        if (objectArray != null) {

            maze = new char[objectArray.length][];
            for (int i = 0; i < objectArray.length; i++) {
                maze[i] = (char[]) objectArray[i];
            }
        }
        ballPos = intent.getIntArrayExtra("ballPos");
        time = intent.getIntExtra("time", 0);
        oldBrokerIP = mPreferences.getString("brokerIP", "");
        oldTopic = mPreferences.getString("topic", "");

        newName.setText(oldName);


        if(sensorType.equals("handy")){
            handy.setChecked(true);
            raspi.setChecked(false);
            newBrokerIP.setVisibility(View.INVISIBLE);
            newTopic.setVisibility(View.INVISIBLE);
        } else{
            handy.setChecked(false);
            raspi.setChecked(true);
            newBrokerIP.setVisibility(View.VISIBLE);
            newTopic.setVisibility(View.VISIBLE);
            newTopic.setText(oldTopic);
            newBrokerIP.setText(oldBrokerIP);
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Einstellungen");

        soundSpinner = findViewById(R.id.spinner2);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sounds_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        soundSpinner.setAdapter(adapter);
        soundSpinner.setOnItemSelectedListener(this);


        int index = 0;
        for(int i = 0; i < soundArr.length; i++){
            if(soundArr[i] == oldSoundID){
                index = i;
            }
        }

        soundSpinner.setSelection(index);

        speichern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent gameIntent = new Intent(InGameSettings.this, MainActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("maze",  maze);
                gameIntent.putExtras(mBundle);
                gameIntent.putExtra("ballPos", ballPos);
                gameIntent.putExtra("time", time);
                editor.clear();
                editor.apply();
                editor.putString("name", String.valueOf(newName.getText()));
                editor.putString("sensorType", sensorType);
                editor.putString("brokerIP", String.valueOf(newBrokerIP.getText()));
                editor.putString("topic", String.valueOf(newTopic.getText()));
                editor.putLong("soundID", newSoundID);
                editor.apply();
                startActivity(gameIntent);
                finish();
            }
        });

        handy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorType = "handy";
                newBrokerIP.setVisibility(View.INVISIBLE);
                newTopic.setVisibility(View.INVISIBLE);
            }
        });

        raspi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensorType = "raspi";
                newBrokerIP.setVisibility(View.VISIBLE);
                newTopic.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        newSoundID = soundArr[position];
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
            Intent intent = new Intent(InGameSettings.this, MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            intent.putExtras(mBundle);
            intent.putExtra("ballPos", ballPos);
            intent.putExtra("time", time);
            finish();
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(InGameSettings.this, MainActivity.class);
        Bundle mBundle = new Bundle();
        mBundle.putSerializable("maze",  maze);
        intent.putExtras(mBundle);
        intent.putExtra("ballPos", ballPos);
        intent.putExtra("time", time);
        startActivity(intent);
        finish();

    }
}