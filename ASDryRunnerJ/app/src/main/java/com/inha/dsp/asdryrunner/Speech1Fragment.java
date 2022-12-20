package com.inha.dsp.asdryrunner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.google.gson.Gson;
import com.inha.dsp.asdryrunner.driver.CausalityData;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Speech1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Speech1Fragment extends Fragment {
    private File recordedFile;
    private MediaRecorder recorder;
    private MediaPlayer player;
    private int playPosition = 0;
    private String recPath;

    private String asrText = "직접 입력해 주세요.";

    private boolean isConverterDisabled = false;
    private Button btPlay;
    private Button btRecord;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Speech1Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Speech1Fragment.
     */
    public static Speech1Fragment newInstance(String param1, String param2) {
        Speech1Fragment fragment = new Speech1Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_speech1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btNext = view.findViewById(R.id.button_sp1_next);
        btNext.setEnabled(false);
        btNext.setOnClickListener(v -> {
            ((LectureType3Fragment)getParentFragment()).setCustomActionText("");
//                ((LectureType3Fragment)getParentFragment()).replaceFragment(Speech2Fragment.newInstance("/dummy/path", asrText));
        });

        btRecord = view.findViewById(R.id.button_sp1_record);
        btRecord.setOnClickListener(v -> {
            if(btRecord.getText().equals("녹음하기")) {
                btRecord.setText("녹음 끝내기");
                btRecord.setEnabled(false);
                AsyncTask.execute(() -> {
                    // Stop narration
                    ((LectureType3Fragment)getParentFragment()).stopAudio();
                });
                // Record video
                ((LectureType3Fragment)getParentFragment()).ToggleRecorder();

                new Handler().postDelayed(() -> {
                    //Do your work
                    btRecord.setEnabled(true);
                },2000);

                // Start camera
//                    ((LectureType3Fragment)getParentFragment()).myCameraPreview.resumePreview();

                /* Make filename with current date and create file */
//                    String captionSerial = CausalityData.CurrentData.Contexts.get(0).CurrentCaption.toString();
//                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss", java.util.Locale.getDefault());
//                    Date date = new Date();
//                    String now = dateFormat.format(date);
//                    String filename = String.format("%s (%s).mp4", now, captionSerial);
//                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//                    StrictMode.setVmPolicy(builder.build());
//                    recordedFile = new File(CausalityData.CurrentData.dataPath, filename);
//                    recPath = recordedFile.getAbsolutePath();
//                    recordAudio();
            } else {
                btRecord.setText("녹음하기");
                ((LectureType3Fragment)getParentFragment()).ToggleRecorder();
//                    stopRecording();
//                    ((LectureType3Fragment)getParentFragment()).myCameraPreview.stopPreview();
                btNext.setEnabled(true);
            }
        });

        btPlay = view.findViewById(R.id.button_sp1_play);
        btPlay.setEnabled(false);
        btPlay.setOnClickListener(v -> {
            if(btPlay.getText().equals("들어보기")) {
                btPlay.setText("그만 듣기");
                playAudio();
            } else {
                btPlay.setText("들어보기");
                stopAudio();
            }
        });
    }

    private void recordAudio(){
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setAudioChannels(1);
//        recorder.setAudioSamplingRate(16000);
//        recorder.setAudioEncodingBitRate(16);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(recPath);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording(){
        if(recorder != null){
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    private void playAudio(){
        try {
            stopAudio();

            player = new MediaPlayer();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    btPlay.setText("들어보기");
                }
            });
            player.setDataSource(recPath);
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAudio(){
        if(player != null){
            player.release();
            player = null;
        }
    }
}