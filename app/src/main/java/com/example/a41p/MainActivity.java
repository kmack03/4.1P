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

    private EditText workoutLengthInput;
    private EditText restLengthInput;
    private EditText numSetsInput;
    private Button startButton;
    private TextView textDisplay;
    private ProgressBar progressBar;

    private CountDownTimer timer;
    private boolean isRunning = false;
    private int currentSet = 1;
    private long elapsedTime = 0;
    private MediaPlayer mediaPlayer;

    private boolean workout = true;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaPlayer = MediaPlayer.create(this, R.raw.beep_sound);

        // Initialize UI elements
        workoutLengthInput = findViewById(R.id.workout_length_input);
        restLengthInput = findViewById(R.id.rest_length_input);
        numSetsInput = findViewById(R.id.num_sets_input);
        startButton = findViewById(R.id.start_button);
        textDisplay = findViewById(R.id.text_display);
        progressBar = findViewById(R.id.progress_bar);

        // Set button click listeners
        startButton.setOnClickListener(v -> {
            if (isRunning) {
                // Pause the timer
                timer.cancel();
                isRunning = false;
                startButton.setText("Start");
            } else {
                // Start the timer
                String workoutLengthStr = workoutLengthInput.getText().toString();
                String restLengthStr = restLengthInput.getText().toString();
                String numSetsStr = numSetsInput.getText().toString();

                if (TextUtils.isEmpty(workoutLengthStr) ||
                        TextUtils.isEmpty(restLengthStr) ||
                        TextUtils.isEmpty(numSetsStr)) {
                    // Show error message if any input is empty
                    Toast.makeText(MainActivity.this, "Please enter details", Toast.LENGTH_SHORT).show();
                    return;
                }

                int workoutLength = Integer.parseInt(workoutLengthStr) * 1000;
                int restLength = Integer.parseInt(restLengthStr) * 1000;
                int numSets = Integer.parseInt(numSetsStr);

                if (workout){
                    startTimer(workoutLength, restLength, numSets);
                }

                else {
                    startRestTimer(workoutLength, restLength, numSets);
                }

            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void startTimer(int workoutLength, int restLength, int numSets) {
        final int totalTime = ((workoutLength + restLength) * numSets - restLength);
        workout = true;

        // Set up and start the timer
        timer = new CountDownTimer(workoutLength, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the text display with the remaining time
                long seconds = millisUntilFinished / 1000;
                @SuppressLint("DefaultLocale") String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);
                textDisplay.setText(timeStr);

                elapsedTime = ((long) (currentSet - 1) * workoutLength) + ((long) (currentSet - 1) * restLength) + (workoutLength - millisUntilFinished);
                int progress = (int) ((float) elapsedTime / totalTime * 100);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                // Beep and switch to rest timer
                playBeep();
                currentSet++;

                if (currentSet > numSets) {
                    // End of workout
                    progressBar.setProgress(100);
                    showFinishDialog();
                } else {
                    startRestTimer(workoutLength, restLength, numSets);
                }
            }
        };

        isRunning = true;
        startButton.setText("Pause");
        timer.start();
    }

    private void startRestTimer(int workoutLength, int restLength, int numSets) {
        final int totalTime = ((workoutLength + restLength) * numSets) - restLength;
        workout = false;
        // Set up and start the rest timer
        timer = new CountDownTimer(restLength, 1000) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the text display with the remaining time
                long seconds = millisUntilFinished / 1000;
                @SuppressLint("DefaultLocale") String timeStr = String.format("%02d:%02d", seconds / 60, seconds % 60);
                textDisplay.setText("Rest: " + timeStr);

                elapsedTime = ((long) (currentSet - 1) * workoutLength) + ((long) (currentSet - 2) * restLength) + (restLength - millisUntilFinished);
                int progress = (int) ((float) elapsedTime / totalTime * 100);
                progressBar.setProgress(progress);
            }

            @Override
            public void onFinish() {
                // Beep and switch to workout timer
                playBeep();
                startTimer(workoutLength, restLength, numSets);
            }
        };

        isRunning = true;
        startButton.setText("Pause");
        timer.start();
    }

    private void playBeep() {
        mediaPlayer.start();
    }

    @SuppressLint("SetTextI18n")
    private void showFinishDialog() {
        // Show a dialog to indicate the end of the workout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Workout complete")
                .setMessage("Congratulations, you've completed the workout!")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Reset the UI and variables
                    isRunning = false;
                    currentSet = 1;
                    textDisplay.setText("00:00");
                    startButton.setText("Start");
                })
                .setCancelable(false)
                .show();
    }

}