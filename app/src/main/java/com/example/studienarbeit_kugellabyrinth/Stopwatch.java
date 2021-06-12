package com.example.studienarbeit_kugellabyrinth;

import android.app.Activity;
import android.os.Handler;
import android.widget.TextView;

import java.util.Locale;

/** Counts the time passed after starting the game
 * Class is taken from this website
 * https://www.geeksforgeeks.org/how-to-create-a-stopwatch-app-using-android-studio/
 * @author Philip Bartmann
 * @version 1.0
 * @since 1.0
 */
public class Stopwatch extends Activity {

    final String TAG = "Stopwatch";

    /** Time in seconds passed after starting the game
     */
    private int seconds = 0;
    /** Indicates if the timer is running or not
     */
    private boolean running;

    /** sets the variable running to true, so that the timer can start
     */
    public void start()
    {
        running = true;
    }

    /** sets the variable running to false, so that the timer stops
     */
    public void stop()
    {
        running = false;
    }

    /** gets the value of the variable seconds to save it or use in the database
     */
    public int getSeconds(){
        return seconds;
    }

    /** sets the seconds to a value to continue where the MainActivity was destroyed
     * and restore the previous value
     * @param seconds The value the variable seconds of the class will be set to in seconds
     */
    public void setSeconds(int seconds){
        this.seconds = seconds;
    }

    /** starts the timer if running is true and increments the value every second
     * @param timeView View element the formatted time will be written into
     */
    public void runTimer(TextView timeView)
    {

        // new Handler to increment every second
        final Handler handler
                = new Handler();

        // runs the method run every second
        handler.post(new Runnable() {
            @Override

            public void run()
            {
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                // Format the seconds into minutes,
                // and seconds.
                String time
                        = String
                        .format(Locale.getDefault(),
                                "%02d:%02d",
                                minutes, secs);

                // Set the textView to the current time
                timeView.setText(time);

                // If running is true, increment the
                // seconds variable.
                if (running) {
                    seconds++;
                }

                // Post the code again
                // with a delay of 1 second.
                handler.postDelayed(this, 1000);
            }
        });
    }
}
