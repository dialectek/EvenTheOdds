// Even (the odds) main activity.

package com.dialectek.even_the_odds;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class MainActivity extends AppCompatActivity
{
    public static final String TAG = "MainActivity";
    public static final int MEDIA_RES_ID = R.raw.jazz_in_paris;

    public static String       RecordingFile = null;
    private Button recordButton;

    private TextView mTextDebug;
    private SeekBar mSeekbarAudio;
    private ScrollView mScrollContainer;
    private PlayerAdapter mPlayerAdapter;
    private boolean mUserIsSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        RecordingFile  = getFilesDir().getAbsolutePath() + "/audiorecording.3gp";
        File file = new File(RecordingFile);
        if (file.exists()) {
            file.delete();
        }

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        setContentView(R.layout.activity_main);
        initializeUI();
        initializeSeekbar();
        initializePlaybackController();
        Log.d(TAG, "onCreate: finished");
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mPlayerAdapter.loadMedia(MEDIA_RES_ID);
        Log.d(TAG, "onStart: create MediaPlayer");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (isChangingConfigurations() && mPlayerAdapter.isPlaying()) {
            Log.d(TAG, "onStop: don't release MediaPlayer as screen is rotating & playing");
        } else {
            mPlayerAdapter.release();
            Log.d(TAG, "onStop: release MediaPlayer");
        }
    }

    private void initializeUI()
    {
        // Record button.
        recordButton = (Button) findViewById(R.id.button_record);
        int buttonWidth = (int)((float)Resources.getSystem().getDisplayMetrics().widthPixels * 0.6f);
        recordButton.setWidth(buttonWidth);
        recordButton.setOnClickListener(new View.OnClickListener()
                                      {
                                          static final int StartColor = Color.BLACK;
                                          static final int StopColor = Color.RED;
                                          boolean mStartRecording = true;
                                          MediaRecorder mRecorder;

                                          @Override
                                          public void onClick(View v)
                                          {
                                              mPlayerAdapter.reset();
                                              if (mStartRecording)
                                              {
                                                  mRecorder = new MediaRecorder();
                                                  mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                                                  mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                                                  mRecorder.setOutputFile(RecordingFile);
                                                  mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                                                  try {
                                                      mRecorder.prepare();
                                                  }
                                                  catch (IOException e) {
                                                      Log.e(TAG, "prepare() failed");
                                                  }

                                                  mRecorder.start();
                                                  recordButton.setTextColor(StopColor);
                                                  recordButton.setText("Stop recording");
                                              }
                                              else
                                              {
                                                  mRecorder.stop();
                                                  mRecorder.release();
                                                  mRecorder = null;
                                                  recordButton.setTextColor(StartColor);
                                                  recordButton.setText("Start recording");
                                                  mPlayerAdapter.setDataSource(RecordingFile);
                                              }

                                              mStartRecording = !mStartRecording;
                                          }
                                      }
        );

        // Save.
        Button saveButton = (Button) findViewById(R.id.button_save);
        saveButton.setWidth(buttonWidth);
        saveButton.setOnClickListener(new View.OnClickListener()
                                      {
                                          String m_chosen;

                                          @Override
                                          public void onClick(View v)
                                          {
                                              mPlayerAdapter.reset();
                                              if (new File(RecordingFile).exists())
                                              {
                                                  DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy z hh:mm:ss aa");
                                                  String fileString = dateFormat.format(new Date()).toString() + ".3gp";
                                                  RecordManager.Default_File_Name = fileString;
                                                  RecordManager recordSaver = new RecordManager(MainActivity.this, "Save",
                                                          new RecordManager.Listener() {
                                                              @Override
                                                              public void onChosenDir(String chosenDir) {
                                                                  m_chosen = chosenDir;
                                                              }
                                                          }
                                                  );
                                                  recordSaver.chooseFile_or_Dir();
                                              } else {
                                                  Toast toast = Toast.makeText(MainActivity.this, "No recording to save", Toast.LENGTH_LONG);
                                                  toast.setGravity(Gravity.CENTER, 0, 0);
                                                  toast.show();
                                              }
                                          }
                                      }
        );

        // Browse.
        Button browseButton = (Button) findViewById(R.id.button_browse);
        browseButton.setWidth(buttonWidth);
        browseButton.setOnClickListener(new View.OnClickListener()
                                        {
                                            String m_chosen;

                                            @Override
                                            public void onClick(View v)
                                            {
                                                mPlayerAdapter.reset();
                                                RecordManager.Default_File_Name = "";
                                                RecordManager recordBrowser = new RecordManager(MainActivity.this, "Browse",
                                                        new RecordManager.Listener()
                                                        {
                                                            @Override
                                                            public void onChosenDir(String chosenDir)
                                                            {
                                                                m_chosen = chosenDir;
                                                            }
                                                        }
                                                );
                                                recordBrowser.chooseFile_or_Dir();
                                            }
                                        }
        );

        mTextDebug = (TextView) findViewById(R.id.text_debug);
        Button mPlayButton = (Button) findViewById(R.id.button_play);
        Button mPauseButton = (Button) findViewById(R.id.button_pause);
        Button mResetButton = (Button) findViewById(R.id.button_reset);
        mSeekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        mScrollContainer = (ScrollView) findViewById(R.id.scroll_container);

        mPauseButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.pause();
                    }
                });
        mPlayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.play();
                    }
                });
        mResetButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.reset();
                    }
                });
    }

    private void initializePlaybackController()
    {
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder(this);
        Log.d(TAG, "initializePlaybackController: created MediaPlayerHolder");
        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;
        Log.d(TAG, "initializePlaybackController: MediaPlayerHolder progress callback set");
    }

    private void initializeSeekbar()
    {
        mSeekbarAudio.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = false;
                        mPlayerAdapter.seekTo(userSelectedPosition);
                    }
                });
    }

    public class PlaybackListener extends PlaybackInfoListener
    {
        @Override
        public void onDurationChanged(int duration)
        {
            mSeekbarAudio.setMax(duration);
            Log.d(TAG, String.format("setPlaybackDuration: setMax(%d)", duration));
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onPositionChanged(int position)
        {
            if (!mUserIsSeeking) {
                mSeekbarAudio.setProgress(position, true);
                Log.d(TAG, String.format("setPlaybackPosition: setProgress(%d)", position));
            }
        }

        @Override
        public void onStateChanged(@State int state)
        {
            String stateToString = PlaybackInfoListener.convertStateToString(state);
            onLogUpdated(String.format("onStateChanged(%s)", stateToString));
        }

        @Override
        public void onPlaybackCompleted()
        {
        }

        @Override
        public void onLogUpdated(String message)
        {
            if (mTextDebug != null) {
                mTextDebug.append(message);
                mTextDebug.append("\n");
                // Moves the scrollContainer focus to the end.
                mScrollContainer.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                mScrollContainer.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
            }
        }
    }

    // Requesting permission to RECORD_AUDIO
    private static final int    REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private         String [] permissions = { Manifest.permission.RECORD_AUDIO };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) { finish(); }
    }
}