package com.example.a41p;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    //Creates all objects for app
    private EditText workoutLengthInput;
    private EditText restLengthInput;
    private EditText numSetsInput;
    private Button startButton;
    private TextView textDisplay;
    private ProgressBar progressBar;

    //Creates a couple of global variables
    private CountDownTimer timer;
    private boolean isRunning = false;
    private int currentSet = 1;
    private long elapsedTime = 0;
    private MediaPlayer mediaPlayer;

    private boolean workout = true;

    @SuppressLint("SetTextI18n")
    @Override
    //On app creation
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Set which activity is current
        setContentView(R.layout.activity_main);
        //Create and ready a sound effect
        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound);

        //Create all of the buttons and text based on id's
        workoutLengthInput = findViewById(R.id.workout_length_input);
        restLengthInput = findViewById(R.id.rest_length_input);
        numSetsInput = findViewById(R.id.num_sets_input);
        startButton = findViewById(R.id.start_button);
        textDisplay = findViewById(R.id.text_display);
        progressBar = findViewById(R.id.progress_bar);

        //Set function for when the start button is pressed
        startButton.setOnClickListener(v -> {
            //If the timer is running stops the timer
            if (isRunning) {
                timer.cancel();
                isRunning = false;
                startButton.setText("Start");
            //If timer is not running starts it
            } else {
                //Gets the values inputed by user
                String workoutLengthStr = workoutLengthInput.getText().toString();
                String restLengthStr = restLengthInput.getText().toString();
                String numSetsStr = numSetsInput.getText().toString();

                //If no value is in any of the inputs escape the function and ask for inputs
                if (TextUtils.isEmpty(workoutLengthStr) ||
                        TextUtils.isEmpty(restLengthStr) ||
                        TextUtils.isEmpty(numSetsStr)) {
                    Toast.makeText(MainActivity.this, "Please enter details", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Turn the inputs into strings in milliseconds
                int workoutLength = Integer.parseInt(workoutLengthStr) * 1000;
                int restLength = Integer.parseInt(restLengthStr) * 1000;
                int numSets = Integer.parseInt(numSetsStr);

                //if in workout stage start from workout
                if (workout){
                    startTimer(workoutLength, restLength, numSets);
                }
                //If in rest stage start from rest
                else {
                    startRestTimer(workoutLength, restLength, numSets);
                }

            }
        });

    }

    @SuppressLint("SetTextI18n")
    //Create start timer function that starts a timer based on user inputs
    private void startTimer(int workoutLength, int restLength, int numSets) {
        //Total time is the amount of time of the entire workout of each set + each rest
        final int totalTime = ((workoutLength + restLength) * numSets - restLength);
        workout = true;

        //Create a timer of length workout length
        timer = new CountDownTimer(workoutLength, 1000) {
            @Override
            //Runs every millisecond
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                //Shows timer in min and sec
                @SuppressLint("DefaultLocale") String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);
                textDisplay.setText(timeStr);
                //Calculates what percentage of the time has passed so it can pass to progress bar
                elapsedTime = ((long) (currentSet - 1) * workoutLength) + ((long) (currentSet - 1) * restLength) + (workoutLength - millisUntilFinished);
                int progress = (int) ((float) elapsedTime / totalTime * 100);
                progressBar.setProgress(progress);
            }

            @Override
            //When the timer is finished
            public void onFinish() {
                //Sound effect
                playBeep();
                //increase set number
                currentSet++;
                //If current set is greater then the total number of sets finish
                if (currentSet > numSets) {
                    progressBar.setProgress(100);
                    showFinishDialog();
                } else {
                    //Start rest timer
                    startRestTimer(workoutLength, restLength, numSets);
                }
            }
        };

        isRunning = true;
        startButton.setText("Pause");
        timer.start();
    }

    //Method to run the rest timer near identical to other timer except workout and rest swapped
    private void startRestTimer(int workoutLength, int restLength, int numSets) {
        final int totalTime = ((workoutLength + restLength) * numSets) - restLength;
        workout = false;
        timer = new CountDownTimer(restLength, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                @SuppressLint("DefaultLocale") String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);
                textDisplay.setText("Rest: " + timeStr);

                elapsedTime = ((long) (currentSet - 1) * workoutLength) + ((long) (currentSet - 2) * restLength) + (restLength - millisUntilFinished);
                int progress = (int) ((float) elapsedTime / totalTime * 100);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                playBeep();
                startTimer(workoutLength, restLength, numSets);
            }
        };

        isRunning = true;
        startButton.setText("Pause");
        timer.start();
    }

    //Plays the sound effect
    private void playBeep() {
        mediaPlayer.start();
    }

    @SuppressLint("SetTextI18n")
    private void showFinishDialog() {
        //Creates pop up on app
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Workout complete")
                .setMessage("Congratulations, you've completed the workout!")
                .setPositiveButton("OK", (dialog, which) -> {
                    //Resets all variables to restart app
                    isRunning = false;
                    currentSet = 1;
                    textDisplay.setText("00:00");
                    startButton.setText("Start");
                })
                .setCancelable(false)
                .show();
    }

}