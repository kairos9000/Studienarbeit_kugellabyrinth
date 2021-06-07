package com.example.studienarbeit_kugellabyrinth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Date;
import java.util.Locale;

public class ScoreboardActivity extends AppCompatActivity {

    final String TAG = "ScoreboardActivity";

    private DatenbankZugriff dbZugriff;
    private ListView anzeigeListe;
    private SimpleCursorAdapter adapter;
    private Cursor cursor;
    Intent intent;
    private String name;
    private int seconds;
    private boolean gameEnded;
    SharedPreferences mPreferences;
    int rank;
    //int anzahlDatensaetze;
    char[][] maze;
    int[] ballPos;
    Button nochmal;
    long currDatensatz;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        nochmal = findViewById(R.id.nochmal);

        intent = getIntent();

        name = mPreferences.getString("name", "");
        seconds = intent.getIntExtra("time", 0);
        gameEnded = intent.getBooleanExtra("gameEnded", false);
        maze = new char[50][50];
        ballPos = new int[2];

        intent = getIntent();
        Object[] objectArray = (Object[]) getIntent().getExtras().getSerializable("maze");
        if (objectArray != null) {

            maze = new char[objectArray.length][];
            for (int i = 0; i < objectArray.length; i++) {
                maze[i] = (char[]) objectArray[i];
            }
        }
        ballPos = intent.getIntArrayExtra("ballPos");

        if(gameEnded){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            nochmal.setVisibility(View.GONE);
        }

        getSupportActionBar().setTitle("Bestenliste");

        String sql = "CREATE TABLE IF NOT EXISTS SPIELER (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Name VARCHAR(40), " +
                "Zeit VARCHAR(20))";

        dbZugriff     = new DatenbankZugriff(this, "SPIELER.dat", sql);


        //anzahlDatensaetze = dbZugriff.getDBLaenge();

        cursor       = dbZugriff.erzeugeListViewCursor();

        anzeigeListe = (ListView) this.findViewById(R.id.listView1);

        String[] anzeigeSpalten = new String[]{ "_id", "Name", "Zeit"}; //
        int[] anzeigeViews      = new int[]{ R.id.textViewID, R.id.textViewName, R.id.textViewZeit};
        adapter                 = new SimpleCursorAdapter(this, R.layout.datensatz, cursor,
                anzeigeSpalten, anzeigeViews,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER );


            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                    if(columnIndex == 0){
                        long id = cursor.getLong(columnIndex);

                        Log.d(TAG, String.valueOf(id));

                        int angepassterRank = rank + 1/* - (2 * anzahlDatensaetze) + 1*/;

                        String rankText = angepassterRank + ".";

                        TextView anzeige = (TextView) view;
                        if(gameEnded && id == currDatensatz){
                            GradientDrawable shape =  new GradientDrawable();
                            shape.setCornerRadius( 8 );
                            shape.setColor(Color.parseColor("#FF6200EE"));
                            anzeige.setBackground(shape);
                            anzeige.setTextColor(Color.parseColor("#FFFFFF"));

                        }
                        anzeige.setText(rankText);
                        rank += 1;
                        return true;
                    }

                    return false; //keine Aenderung

                }
            });
            anzeigeListe.setAdapter(adapter);


        if(gameEnded){
            DatenFormatierenUndNeuenDatensatzErstellen(name, seconds);
        }

        nochmal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScoreboardActivity.this, StartActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    public void DatenFormatierenUndNeuenDatensatzErstellen(String name, int seconds){

        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        String time = String.format(Locale.getDefault(),
                        "%02d:%02d",
                        minutes, secs);


        DatenbankTemplate datensatz = new DatenbankTemplate(name, time);
        currDatensatz = dbZugriff.datensatzEinfuegen(datensatz);
        if(name.length() == 0){
            name = "Spieler" + String.valueOf(currDatensatz);
        }
        dbZugriff.changeCurrName(name, currDatensatz);
        //anzahlDatensaetze = dbZugriff.getDBLaenge();
        anzeigeAktualisieren();
    }

    private void anzeigeAktualisieren() {
        if(cursor != null) {
            cursor.close();
        }

        cursor = dbZugriff.erzeugeListViewCursor();
        adapter.changeCursor(cursor);
    }

    @Override
    protected void onDestroy() {
        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if(dbZugriff != null) {
            dbZugriff.close();
        }

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home){
            Intent intent = new Intent(ScoreboardActivity.this, MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            intent.putExtras(mBundle);
            intent.putExtra("ballPos", ballPos);
            intent.putExtra("time", seconds);
            finish();
            startActivity(intent);
            overridePendingTransition(R.anim.left_in, R.anim.right_out);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        if(gameEnded){
            Intent intent = new Intent(ScoreboardActivity.this, StartActivity.class);
            startActivity(intent);
            finish();
        } else{
            Intent intent = new Intent(ScoreboardActivity.this, MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            intent.putExtras(mBundle);
            intent.putExtra("ballPos", ballPos);
            intent.putExtra("time", seconds);
            startActivity(intent);
            finish();
        }

    }

}
