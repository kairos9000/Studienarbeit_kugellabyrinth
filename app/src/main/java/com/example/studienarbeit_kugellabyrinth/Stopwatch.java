package com.example.studienarbeit_kugellabyrinth;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class Stopwatch extends Activity {

    final String TAG = "Stopwatch";

    private int seconds = 0;
    private boolean running;
    private boolean wasRunning;


    public void start()
    {
        running = true;
    }

    public void stop()
    {
        running = false;
    }

    public int getSeconds(){
        return seconds;
    }

    public void setSeconds(int seconds){
        this.seconds = seconds;
    }

    public void runTimer(TextView timeView)
    {

        final Handler handler
                = new Handler();

        handler.post(new Runnable() {
            @Override

            public void run()
            {
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                // Format the seconds into hours, minutes,
                // and seconds.
                String time
                        = String
                        .format(Locale.getDefault(),
                                "%02d:%02d",
                                minutes, secs);

                // Set the text view text.
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
