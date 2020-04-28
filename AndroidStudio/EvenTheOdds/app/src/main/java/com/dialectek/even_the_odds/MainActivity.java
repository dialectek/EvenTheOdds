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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public final class MainActivity extends AppCompatActivity
{
    public static final String TAG = "MainActivity";

    public static String mRecordingFile;
    private ArrayList<String> mPlaylist;
    private int mPlaylistIndex;
    private String m_dataDirectory;
    private Button mRecordButton;
    private Button mPlayButton;
    private Button mPauseButton;
    private Button mNextButton;
    private TextView mTextLog;
    private SeekBar mSeekbarAudio;
    private ScrollView mScrollContainer;
    private PlayerAdapter mPlayerAdapter;
    private boolean mUserIsSeeking = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        String rootDir = getFilesDir().getAbsolutePath();
        try
        {
            rootDir = new File(rootDir).getCanonicalPath();
        }
        catch (IOException ioe)
        {
        }
        mRecordingFile  = rootDir + "/recording.3gp";
        File file = new File(mRecordingFile);
        if (file.exists()) {
            file.delete();
        }
        m_dataDirectory = rootDir + "/content";

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
        mRecordButton = (Button) findViewById(R.id.button_record);
        int buttonWidth = (int)((float)Resources.getSystem().getDisplayMetrics().widthPixels * 0.6f);
        mRecordButton.setWidth(buttonWidth);
        mRecordButton.setOnClickListener(new View.OnClickListener()
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
                                                  mRecorder.setOutputFile(mRecordingFile);
                                                  mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                                                  try {
                                                      mRecorder.prepare();
                                                  }
                                                  catch (IOException e) {
                                                      Log.e(TAG, "prepare() failed");
                                                  }

                                                  mRecorder.start();
                                                  mRecordButton.setTextColor(StopColor);
                                                  mRecordButton.setText("Stop recording");

                                                  Animation anim = new AlphaAnimation(0.35f, 1.0f);
                                                  anim.setDuration(500);
                                                  anim.setStartOffset(20);
                                                  anim.setRepeatMode(Animation.REVERSE);
                                                  anim.setRepeatCount(Animation.INFINITE);
                                                  mRecordButton.startAnimation(anim);
                                              }
                                              else
                                              {
                                                  mRecordButton.clearAnimation();
                                                  mRecorder.stop();
                                                  mRecorder.release();
                                                  mRecorder = null;
                                                  mRecordButton.setTextColor(StartColor);
                                                  mRecordButton.setText("Start recording");
                                                  mPlaylist = new ArrayList<String>();
                                                  mPlaylist.add(mRecordingFile);
                                                  mPlaylistIndex = 0;
                                                  mPlayButton.setEnabled(false);
                                                  mPauseButton.setEnabled(false);
                                                  mNextButton.setEnabled(false);
                                                  if (new File(mRecordingFile).exists()) {
                                                      if (mPlayerAdapter.setDataSource(mRecordingFile)) {
                                                          mPlayButton.setEnabled(true);
                                                          mPauseButton.setEnabled(true);
                                                          mNextButton.setEnabled(false);
                                                      } else {
                                                          Toast toast = Toast.makeText(MainActivity.this, "Cannot play recording", Toast.LENGTH_LONG);
                                                          toast.setGravity(Gravity.CENTER, 0, 0);
                                                          toast.show();
                                                      }
                                                  }
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
                                          String m_saved;

                                          @Override
                                          public void onClick(View v)
                                          {
                                              mPlayerAdapter.reset();
                                              if (new File(mRecordingFile).exists()) {
                                                  DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy z hh:mm:ss aa");
                                                  String fileString = dateFormat.format(new Date()).toString() + ".3gp";
                                                  RecordManager.Default_File_Name = fileString;
                                              } else {
                                                  RecordManager.Default_File_Name = "";
                                              }
                                              RecordManager recordSaver = new RecordManager(MainActivity.this, m_dataDirectory,"Save",
                                                      new RecordManager.Listener() {
                                                          @Override
                                                          public void onSave(String savedFile) {
                                                              m_saved = savedFile;
                                                              logMessage(savedFile.replace(m_dataDirectory, "") + " saved.");
                                                          }
                                                          @Override
                                                          public void onDelete(String deletedFile) {
                                                          }
                                                          @Override
                                                          public void onSelect(String selectedFile) {
                                                          }
                                                      }
                                              );
                                              recordSaver.chooseFile_or_Dir();
                                          }
                                      }
        );

        // Browse.
        Button browseButton = (Button) findViewById(R.id.button_browse);
        browseButton.setWidth(buttonWidth);
        browseButton.setOnClickListener(new View.OnClickListener()
                                        {
                                            String m_selected;

                                            @Override
                                            public void onClick(View v)
                                            {
                                                mPlayerAdapter.reset();
                                                RecordManager.Default_File_Name = "";
                                                RecordManager recordBrowser = new RecordManager(MainActivity.this, m_dataDirectory,"Browse",
                                                        new RecordManager.Listener() {
                                                            @Override
                                                            public void onSave(String savedFile) {
                                                            }
                                                            @Override
                                                            public void onDelete(String deletedFile) {
                                                           }
                                                            @Override
                                                            public void onSelect(String selectedFile) {
                                                                m_selected = selectedFile;
                                                                String displayFile = selectedFile.replace(m_dataDirectory, "");
                                                                logMessage(displayFile + " selected.");
                                                                File file = new File(selectedFile);
                                                                if (file.exists())
                                                                {
                                                                    if (file.isFile()) {
                                                                        mPlaylist = new ArrayList<String>();
                                                                        mPlaylist.add(selectedFile);
                                                                    } else {
                                                                        mPlaylist = listRecordings(selectedFile);
                                                                    }
                                                                    mPlaylistIndex = 0;
                                                                    if (mPlaylist != null) {
                                                                        selectedFile = mPlaylist.get(0);
                                                                        displayFile = selectedFile.replace(m_dataDirectory, "");
                                                                        mPlayButton.setEnabled(false);
                                                                        mPauseButton.setEnabled(false);
                                                                        if (mPlaylist.size() > 1) {
                                                                            mNextButton.setEnabled(true);
                                                                            logMessage(mPlaylist.size() + " recordings found in folder.");
                                                                        } else {
                                                                            mNextButton.setEnabled(false);
                                                                        }
                                                                        if (!copyFile(selectedFile, mRecordingFile)) {
                                                                            Toast toast = Toast.makeText(MainActivity.this, "Cannot access recording " + displayFile, Toast.LENGTH_LONG);
                                                                            toast.setGravity(Gravity.CENTER, 0, 0);
                                                                            toast.show();
                                                                        } else {
                                                                            if (mPlayerAdapter.setDataSource(mRecordingFile)) {
                                                                                mPlayButton.setEnabled(true);
                                                                                mPauseButton.setEnabled(true);
                                                                                logMessage("Ready to play " + displayFile + " (" + (mPlaylistIndex + 1) + "/" + mPlaylist.size() + ").");
                                                                            } else {
                                                                                Toast toast = Toast.makeText(MainActivity.this, "Cannot access recording " + displayFile, Toast.LENGTH_LONG);
                                                                                toast.setGravity(Gravity.CENTER, 0, 0);
                                                                                toast.show();
                                                                            }
                                                                        }
                                                                    } else {
                                                                        Toast toast = Toast.makeText(MainActivity.this, "No recordings in folder " + displayFile, Toast.LENGTH_LONG);
                                                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                                                        toast.show();
                                                                    }
                                                                } else {
                                                                    Toast toast = Toast.makeText(MainActivity.this, displayFile + " does not exist", Toast.LENGTH_LONG);
                                                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                                                    toast.show();
                                                                }
                                                            }
                                                        }
                                                );
                                                recordBrowser.chooseFile_or_Dir();
                                            }
                                        }
        );

        // Delete.
        Button deleteButton = (Button) findViewById(R.id.button_delete);
        deleteButton.setWidth(buttonWidth);
        deleteButton.setOnClickListener(new View.OnClickListener()
                                        {
                                            String m_deleted;

                                            @Override
                                            public void onClick(View v)
                                            {
                                                mPlayerAdapter.reset();
                                                RecordManager.Default_File_Name = "";
                                                RecordManager recordBrowser = new RecordManager(MainActivity.this, m_dataDirectory,"Delete",
                                                        new RecordManager.Listener() {
                                                            @Override
                                                            public void onSave(String savedFile) {
                                                            }
                                                            @Override
                                                            public void onDelete(String deletedFile) {
                                                                m_deleted = deletedFile;
                                                                logMessage(deletedFile.replace(m_dataDirectory, "") + " deleted.");
                                                            }
                                                            @Override
                                                            public void onSelect(String selectedFile) {
                                                             }
                                                        }
                                                );
                                                recordBrowser.chooseFile_or_Dir();
                                            }
                                        }
        );

        mTextLog = (TextView) findViewById(R.id.text_log);
        mPlayButton = (Button) findViewById(R.id.button_play);
        mPlayButton.setEnabled(false);
        mPauseButton = (Button) findViewById(R.id.button_pause);
        mPauseButton.setEnabled(false);
        mNextButton = (Button) findViewById(R.id.button_next);
        mNextButton.setEnabled(false);
        mSeekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        mScrollContainer = (ScrollView) findViewById(R.id.scroll_container);

        mPlayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.play();
                    }
                });
        mPauseButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.pause();
                    }
                });
        mNextButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mPlaylist != null && mPlaylistIndex < mPlaylist.size() - 1)
                        {
                            mPlaylistIndex++;
                            mPlayButton.setEnabled(false);
                            mPauseButton.setEnabled(false);
                            if (mPlaylistIndex < mPlaylist.size() - 1)
                            {
                                mNextButton.setEnabled(true);
                            } else {
                                mNextButton.setEnabled(false);
                            }
                            String selectedFile = mPlaylist.get(mPlaylistIndex);
                            String displayFile = selectedFile.replace(m_dataDirectory, "");
                            File file = new File(selectedFile);
                            if (file.exists()) {
                                if (!copyFile(selectedFile, mRecordingFile)) {
                                    Toast toast = Toast.makeText(MainActivity.this, "Cannot access recording " + displayFile, Toast.LENGTH_LONG);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                } else {
                                    if (mPlayerAdapter.setDataSource(mRecordingFile)) {
                                        mPlayButton.setEnabled(true);
                                        mPauseButton.setEnabled(true);
                                        logMessage("Ready to play " + displayFile + " (" + (mPlaylistIndex + 1) + "/" + mPlaylist.size() + ").");
                                    } else {
                                        Toast toast = Toast.makeText(MainActivity.this, "Cannot access recording " + displayFile, Toast.LENGTH_LONG);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();
                                    }
                                }
                            } else {
                                Toast toast = Toast.makeText(MainActivity.this, "Cannot access recording " + displayFile, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }
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
            //logMessage(message);
        }
    }

    // Log message.
    public void logMessage(String message)
    {
        if (mTextLog != null) {
            mTextLog.append(message);
            mTextLog.append("\n");
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

    // Copy file.
    public static boolean copyFile(String fromFile, String toFile)
    {
        File sourceLocation = new File(fromFile);
        File targetLocation = new File(toFile);

        InputStream in = null;
        OutputStream out = null;
        boolean result = false;
        try
        {
            in = new FileInputStream(sourceLocation);
            out = new FileOutputStream(targetLocation);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            result = true;
        } catch (Exception e) {
        } finally
        {
            try
            {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (Exception e) {
                result = false;
            }
        }

        return(result);
    }

    // Recursively list recordings in directory.
    public ArrayList<String> listRecordings(String dir) {
        ArrayList<String> results = new ArrayList<String>();
        File[] files = new File(dir).listFiles();
        for(File f:files) {
            String name = f.getName();
            if (f.isDirectory()) {
                ArrayList<String> subResults = listRecordings(f.getPath());
                if (subResults != null) {
                    for (String path : subResults)
                    {
                        results.add(path);
                    }
                }
            } else {
                try {
                    results.add(f.getCanonicalPath());
                } catch (Exception e) {
                    Toast toast = Toast.makeText(MainActivity.this, "Cannot get path", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return null;
                }
            }
        }
        if (results.size() > 0)
        {
            return results;
        } else {
            return null;
        }
    }
}