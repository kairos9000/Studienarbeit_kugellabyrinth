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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/** Activity, which shows a slice of the database containing the name and time
 * of previous players as a scoreboard
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public class ScoreboardActivity extends AppCompatActivity {

    final String TAG = "ScoreboardActivity";

    /** Instance of DatabaseTransformations to access the operations on the database
     */
    private DatabaseTransformations dbTransformations;
    /** Adapter to handle the cursor for displaying the database
     */
    private SimpleCursorAdapter adapter;
    /** Cursor to iterate over database entries
     */
    private Cursor cursor;
    /** to get the extras of the calling Activity
     */
    Intent intent;
    /** time the player needed to complete the maze
     */
    private int seconds;
    /** signals if the game is at an end, to differentiate between
     * the in game scoreboard display and the display after the game
     * with the data of the game
     */
    private boolean gameEnded;
    /** To get the data of the player, specifically the name
     */
    SharedPreferences mPreferences;
    /** To rank the players from one to ten
     */
    int rank = 1;
    /** To save the maze if the in game scoreboard is called
     */
    char[][] maze;
    /** To save the ball position if the in game scoreboard is called
     */
    int[] ballPos;
    /** Triggers an intent to start the SettingsActivity to begin a new game
     */
    Button again;
    /** holds the id of the last inserted dataset to determine the rank of
     * the current game
     */
    long currDataset;


    /** This method is called when ScoreboardActivity is created.
     * It handles the initialization of the instance variables and the
     * displaying of the database entries by defining an adapter
     * @param savedInstanceState saved variables of the Activity was paused, e.g. when the screen is
     *                           rotated, to get them again and restore the state of the Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoreboard);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        again = findViewById(R.id.again);

        intent = getIntent();

        // gets name and time to write into the database
        String name = mPreferences.getString("name", "");
        seconds = intent.getIntExtra("time", 0);
        gameEnded = intent.getBooleanExtra("gameEnded", false);
        maze = new char[50][50];
        ballPos = new int[2];

        intent = getIntent();

        // saves maze and ball position if the in game Scoreboard is called => will be
        // used to restore the previous state of the game
        Object[] objectArray = (Object[]) getIntent().getExtras().getSerializable("maze");
        if (objectArray != null) {

            maze = new char[objectArray.length][];
            for (int i = 0; i < objectArray.length; i++) {
                maze[i] = (char[]) objectArray[i];
            }
        }
        ballPos = intent.getIntArrayExtra("ballPos");

        // disables the back arrow on the upper right corner of the screen if
        // the scoreboard after the game is shown to prevent the user to go back to the finished game
        if(gameEnded){
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            again.setVisibility(View.GONE);
        }

        // sets the title of the screen to "Bestenliste"
        getSupportActionBar().setTitle("Bestenliste");

        // creates the database if it doesn't exist
        String sql = "CREATE TABLE IF NOT EXISTS SPIELER (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Name VARCHAR(40), " +
                "Zeit VARCHAR(20))";

        // initializes a DatabaseTransformations object to access the transformations on the database
        dbTransformations = new DatabaseTransformations(this, "SPIELER.dat", sql);

        // creates a cursor on the database to iterate over the database
        cursor = dbTransformations.createListViewCursor();

        // initializes the ListView to display the database as a list
        ListView displayList = (ListView) this.findViewById(R.id.databaseList);

        // defines the columns to be displayed
        String[] displayColumns = new String[]{ "_id", "Name", "Zeit"};
        // defines the elements the data is placed in
        int[] displayViews = new int[]{ R.id.textViewID, R.id.textViewName, R.id.textViewTime};
        // defines a adapter to show the entries
        adapter                 = new SimpleCursorAdapter(this, R.layout.datensatz, cursor,
                displayColumns, displayViews,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER );

            // binds the adapter to the View to display the entries
            adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                    // if the column index is zero, i.e. the id
                    if(columnIndex == 0){
                        // get the id
                        long id = cursor.getLong(columnIndex);

                        String rankText = rank + ".";

                        // get the view, the id would be written into
                        TextView anzeige = (TextView) view;
                        // if the game is over and the id is the same as the last inserted id
                        // mark the view of the id with a colored background and rounded edges
                        if(gameEnded && id == currDataset){
                            GradientDrawable shape =  new GradientDrawable();
                            shape.setCornerRadius( 8 );
                            shape.setColor(Color.parseColor("#096f45"));
                            anzeige.setBackground(shape);
                            anzeige.setTextColor(Color.parseColor("#FFFFFF"));

                        }
                        // set the id to rank to get a rating from 1 to 10
                        anzeige.setText(rankText);
                        rank += 1;
                        return true;
                    }

                    return false; //no changes

                }
            });
            displayList.setAdapter(adapter);

        // insert the new dataset into the database if the game is over
        if(gameEnded){
            formatDataAndInsert(name, seconds);
        }

        // start a SettingsActivity if the player wants to play again
        again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ScoreboardActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });


    }

    /** Formats the name and the time the player needed and formats it to
     * insert is as a new entry
     * @param name The name of the player
     * @param seconds Time the player needed to complete the game in seconds
     */
    public void formatDataAndInsert(String name, int seconds){

        // formats the seconds to minutes and seconds
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;

        String time = String.format(Locale.getDefault(),
                        "%02d:%02d",
                        minutes, secs);


        // creates a new dataset
        DatabaseTemplate dataset = new DatabaseTemplate(name, time);
        currDataset = dbTransformations.insertDataset(dataset);
        // if the name is empty set the name to name + id of his dataset to
        // get unique names and change the name to this name
        if(name.length() == 0){
            name = "Spieler" + currDataset;
            dbTransformations.changeCurrName(name, currDataset);
        }

        // updates the view of the scoreboard
        updateDisplay();
        // get the position ordered by time to get the position of the player in the database
        // and show it as a toast
        int pos = dbTransformations.getPosition(currDataset);
        Toast.makeText(ScoreboardActivity.this, "Sie sind Platz "+ pos + " geworden.", Toast.LENGTH_LONG).show();
    }

    /** updates the listView with the new database entry
     */
    private void updateDisplay() {
        if(cursor != null) {
            cursor.close();
        }

        cursor = dbTransformations.createListViewCursor();
        adapter.changeCursor(cursor);
    }

    /** is called when the Activity is destroyed, i.e. it finishes.
     * closes any open cursors and the access to DatabaseTransformations
     */
    @Override
    protected void onDestroy() {
        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        if(dbTransformations != null) {
            dbTransformations.close();
        }

        super.onDestroy();
    }

    /** Handles action bar item clicks
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // starts MainActivity, because it has to be the in game scoreboard when the
        // back arrow in the action bar is available
        if (id == android.R.id.home){
            Intent intent = new Intent(ScoreboardActivity.this, MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            intent.putExtras(mBundle);
            intent.putExtra("ballPos", ballPos);
            intent.putExtra("time", seconds);
            startActivity(intent);
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

    /** Overrides the back button on the phone in the bar at the bottom
     * so that no unexpected behavior happens.
     * Differentiates between the game over scoreboard and the in game scoreboard.
     * Starts a SettingsActivity when the game is over and starts the MainAcitivity "again"
     * when the game is not over
     */
    @Override
    public void onBackPressed(){
        Intent intent;
        if(gameEnded){
            intent = new Intent(ScoreboardActivity.this, SettingsActivity.class);
        } else{
            intent = new Intent(ScoreboardActivity.this, MainActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putSerializable("maze",  maze);
            intent.putExtras(mBundle);
            intent.putExtra("ballPos", ballPos);
            intent.putExtra("time", seconds);
        }
        startActivity(intent);
        finish();
    }
}
