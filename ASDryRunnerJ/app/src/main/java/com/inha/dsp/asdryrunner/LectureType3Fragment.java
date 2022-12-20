package com.inha.dsp.asdryrunner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.inha.dsp.asdryrunner.driver.CausalityData;
import com.inha.dsp.causality.type.Action;
import com.inha.dsp.causality.type.Caption;
import com.inha.dsp.causality.type.Cause;
import com.inha.dsp.causality.type.Context;
import com.inha.dsp.causality.type.CustomAction;
import com.inha.dsp.causality.type.Media;
import com.inha.dsp.causality.type.Option;
import com.inha.dsp.causality.type.Perceptron;
import com.inha.dsp.causality.type.Scene;
import com.inha.dsp.causality.type.SerialNumber;
import com.inha.dsp.causality.type.TypeEnum;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LectureType3Fragment extends Fragment implements SurfaceHolder.Callback {
    private String customActionText = "";
    public void setCustomActionText(String text) {
        customActionText = text;
        btNext.setEnabled(true);
    }
    private Button btNext;

    private MediaPlayer audioPlayer = null;
    private int audioPos = 0;

    private MediaRecorder videoRecorder;
    private SurfaceHolder surfaceHolder;
    private CamcorderProfile camcorderProfile;
    private Camera camera;
    private Camera.CameraInfo cameraInfo;
    boolean recording = false;
    boolean usecamera = true;
    boolean previewRunning = false;
    private int displayOrientation;

//    public MyCameraPreview myCameraPreview = null;
    private static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private void Logd(String message) {
//        Class thisClass = this.getClass();
//        String className = thisClass.getEnclosingClass().getSimpleName();
//        String methodName = thisClass.getEnclosingMethod().getName();
        Log.d("ASDryRunner", String.format("%s:%s", "LectureType3Fragment", message));
    }

    public LectureType3Fragment() {
        // Required empty public constructor
    }

    public static LectureType3Fragment newInstance() {
        return new LectureType3Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragment();
        initCamcorder();
        displayOrientation = (getActivity()).getWindowManager().getDefaultDisplay().getRotation();
    }

    private void initCameraView(View view) {
        SurfaceView cameraView = (SurfaceView) view.findViewById(R.id.sv_camera);
        surfaceHolder = cameraView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void initCamcorder() {
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        } else {
            Logd("480p Camcorder is unavailable. Setting it as Lowest quality");
            camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lecture_type3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandFragment(view);
//        initCamera();
        initCameraView(view);

        Button btExit = view.findViewById(R.id.button_lt3_exit);
        btExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudio();
                Intent intent = new Intent(getContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Context 데이터에 기반해서 현재 보여줘야 할 내용을 보여줌
        updateContent(view);

        btNext = view.findViewById(R.id.button_lt3_next);
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 재생중이던 소리가 있으면 중지
                if(audioPlayer != null) {
                    if(audioPlayer.isPlaying()) {
                        audioPlayer.stop();
                    }
                    audioPlayer.release();
                    audioPlayer = null;
                }
                /* 다음 장면에 해당되는 데이터를 준비 */
                // 현재 장면의 데이터 수집
                CausalityData content = CausalityData.CurrentData;
                Context context = content.Contexts.get(0);
                SerialNumber currentCaptionSerial = context.CurrentCaption;
                ArrayList<Caption> captions = content.Captions;
                Caption currentCaption = null;
                for(Caption caption : captions)
                {
                    if(caption.getSerialNumber().equals(currentCaptionSerial)) {
                        currentCaption = caption;
                        break;
                    }
                }

                // 주관식 대답을 저장
                if(currentCaption.getOption() != null) {
                    Option currentOption = null;
                    for(Option option : content.Options)
                    {
                        if(option.getSerialNumber().equals(currentCaption.getOption()))
                        {
                            currentOption = option;
                            break;
                        }
                    }
                    SerialNumber[] actions = currentOption.Actions;
                    ArrayList<CustomAction> customActions = content.CustomActions;
                    for (SerialNumber actionSerial : actions)
                    {
                        if(actionSerial.getType() == TypeEnum.CustomAction) {
                            for(CustomAction customAction : customActions)
                            {
                                if(customAction.getSerialNumber().equals(actionSerial)) {
                                    customAction.setCustomValue(customActionText);
                                    customAction.IsPerformed = true;
                                    break;
                                }
                            }
                        }
                    }
                    // 다음 버튼과 주관식 대답을 저장하는 로컬변수를 초기화
                    btNext.setEnabled(false);
                    customActionText = "";
                    // 음성인식/주관식 작성용 하위 Fragment도 초기화
                    Fragment speech1Fragment = new Speech1Fragment();
                    replaceFragment(speech1Fragment);
                }

                // 캡션이 보여졌음
                currentCaption.Displayed = true;

                // 보고서 갱신
                StudentInfo.CurrentStudent().getCausalityReport().SaveReport();

                // 현재 Caption이 Scene의 마지막 Caption이면
                if(currentCaption.getNextCaption().getType() == TypeEnum.End) {
                    // 종료 화면으로 넘어감
                    ((LearnActivity)getActivity()).replaceFragment(LectureEndFragment.newInstance("aa","bb"));
                } else {
                    // 동적 UI 요소들 초기화
                    ImageView ivCamera = view.findViewById(R.id.iv_pictures);
                    ivCamera.setImageResource(R.drawable.icon_smile);

                    /* 다음 내용으로 Context 업데이트 */
                    Caption nextCaption = null;
                    for(Caption caption : captions)
                    {
                        if(caption.getSerialNumber().equals(currentCaption.getNextCaption()))
                        {
                            nextCaption = caption;
                            break;
                        }
                    }

                    // 일반적인 경우엔 getNextCaption()이 가리키는 곳이 맞음
                    context.CurrentCaption = nextCaption.getSerialNumber();
                    // 하지만 해당 Caption에 Cause가 있다면 가야하는 곳이 다를 수 있음
                    if(nextCaption.hasCause())
                    {
                        SerialNumber currentCauseSerial = nextCaption.getCause();
                        Cause currentCause = null;
                        for(Cause cause : content.Causes)
                        {
                            if(cause.getSerialNumber().equals(currentCauseSerial)) {
                                currentCause = cause;
                                break;
                            }
                        }

                        ArrayList<Caption> nextCaptions = new ArrayList<>();
                        for(SerialNumber nextSerial : currentCause.Next)
                        {
                            for(Caption caption : captions)
                            {
                                if(caption.getSerialNumber().equals(nextSerial)) {
                                    nextCaptions.add(caption);
                                }
                            }
                        }

                        SerialNumber currentPerceptronSerial = currentCause.getPerceptron();
                        Perceptron currentPerceptron = null;
                        for(Perceptron perceptron : content.Perceptrons)
                        {
                            if(perceptron.getSerialNumber().equals(currentPerceptronSerial)) {
                                currentPerceptron = perceptron;
                                break;
                            }
                        }

                        ArrayList<Action> inputs = new ArrayList<>();
                        for (SerialNumber inputSerial : currentPerceptron.Inputs)
                        {
                            if(inputSerial.getType() == TypeEnum.Action) {
                                Action inputAction = null;
                                for(Action action : content.Actions) {
                                    if(action.getSerialNumber().equals(inputSerial)) {
                                        inputAction = action;
                                        break;
                                    }
                                }
                                inputs.add(inputAction);
                            }
                        }

                        // Bias와 perform된 Action들의 Weight를 더한다
                        double y = currentPerceptron.getBias();
                        for (int i = 0; i < inputs.size(); i++)
                        {
                            if(inputs.get(i).IsPerformed) {
                                y += currentPerceptron.Weights[i];
                            }
                        }

                        /*
                         * 이제 약간의 꼼수를 이용해 어떤 캡션으로 가야할지 계산한다.
                         * int idx = -1 이고 갈 수 있는 캡션이 2개(CAP003, CAP009)라 하자.
                         * 퍼셉트론에서 값이 증가했을수록 Threshold를 넘기기 쉬워지고,
                         * 그 말은 곧 뒷쪽의 캡션으로 가야한다는 말이라고 생각해라.
                         * 그래서 만약 위에서 계산한 y가 0.9고 Threshold가 0.8이었으면
                         * CAP003이 아니라 CAP009로 가야한다는 이야기다.
                         * 그런데 만약 캡션이 3개(CAP003,006,009)라 하자.
                         * 그럼 Threshold는 3-1, 곧 2(0.3, 0.5)개이다.
                         * 만약 y가 0.6이었으면 얘보다 작은건 0.3,0.5 2개니까 idx++를 두번해라.
                         * 그럼 idx는 0에서 2로 바뀌게 되고,
                         * 갈 수 있는 캡션이 담긴 배열에 접근할 인덱스로 그대로 쓸 수 있게된다.
                         * >>   nextCaptions[idx]    <<
                         */
                        int nextIdx = 0;
                        for(double threshold : currentCause.Thresholds)
                        {
                            if(y > threshold) {
                                nextIdx++;
                            }
                        }
                        nextCaption = nextCaptions.get(nextIdx);
                        // 마지막으로 context에 있는 CurrentCaption을 덮어씌워주면 끝
                        context.CurrentCaption = nextCaption.getSerialNumber();
                    }


                    /* Context는 다음 장면으로 업데이트 했으므로 UI내용도 갱신 */
                    // TODO: 필요하다면 Type 2를 만들고 전환까지 구현해야함
                    // 다음 장면 캡션에 객관식/주관식(Options)이 있는지 확인
                    SerialNumber nextOptionSerial = nextCaption.getOption();
                    if(nextOptionSerial != null)
                    {// 일단 객관식이나 주관식이 있네
                        Option nextOption = null;
                        for(Option option : content.Options)
                        {
                            if(option.getSerialNumber().equals(nextOptionSerial))
                            {
                                nextOption = option;
                                break;
                            }
                        }
                        // Action들의 타입을 검사해서 CustomAction이 "없는지" 확인
                        boolean hasCustomAction = false;
                        for(SerialNumber sn : nextOption.Actions)
                        {
                            if(sn.getType() == TypeEnum.CustomAction) {
                                hasCustomAction = true;
                                btNext.setEnabled(false);
                                break;
                            }// 주관식이 있는 상황
                        }
                        if(!hasCustomAction) { // "Type 1으로 넘어가기"
                            ((LearnActivity)getActivity()).replaceFragment(LectureType1Fragment.newInstance());
                            return;
                        }
                    }

                    // 객관식이 없는 장면이면 Type1에서 그대로 화면 업데이트
                    updateContent(view);
                }
            }
        });
    }

    private void updateContent(@NonNull View view) {
        ImageView ivPictures = view.findViewById(R.id.iv_pictures);

        // 최초 진행일수도, 중단 후 재시작일수도 있으니 Context를 조사하기
        CausalityData content = CausalityData.CurrentData;
        Context context = content.Contexts.get(0);
        int progress = context.CurrentScenario.getIndex() + context.CurrentScene.getIndex() + context.CurrentCaption.getIndex();
        // progress가 0이면 최초 진행

        SerialNumber currentCaptionSerial = context.CurrentCaption;
        ArrayList<Caption> captions = content.Captions;
        Caption currentCaption = null;
        for(Caption caption : captions)
        {
            if(caption.getSerialNumber().equals(currentCaptionSerial)) {
                currentCaption = caption;
                break;
            }
        }
        // 현재 캡션에 주관식이 있는지
        if(currentCaption.getOption() != null)
        {// 이미 이전 화면에서 넘어올때 주관식만 있는 것이 확정되었으므로 객관식 생각할 필요 없음
            FrameLayout frameLayout = view.findViewById(R.id.speech_fragment_container);
            frameLayout.setVisibility(View.VISIBLE);
            // 일단 다음 버튼을 비활성화하고, 선택지가 한번이라도 선택되면 OnClickListener로 활성화함
            Option currentOption = null;
            ArrayList<Option> options = content.Options;
            for(Option opt : options)
            {
                if(opt.getSerialNumber().equals(currentCaption.getOption()))
                {
                    currentOption = opt;
                    break;
                }
            }
            ivPictures.setVisibility(View.INVISIBLE);
//            myCameraPreview.resumePreview();
        } else {
            // 오른쪽 요소들 안 보이게?
            FrameLayout frameLayout = view.findViewById(R.id.speech_fragment_container);
            frameLayout.setVisibility(View.INVISIBLE);
            //
            btNext.setEnabled(true);
            ivPictures.setVisibility(View.VISIBLE);
//            myCameraPreview.stopPreview();
        }

        String dialogue = currentCaption.getDialogue();
        String speaker = currentCaption.getSpeakerName();
        currentCaption.Displayed = true;

        // 카메라 위치에 이미지/동영상이 표시되어야 하는지 알아내고 표시
        for(Media media : content.Medias)
        {
            if (media.getTargetCaption().equals(currentCaptionSerial)) {
                if(media.getMediaType().equals("image")) {
                    ivPictures.setVisibility(View.VISIBLE);
                    String uri = String.format("@drawable/%s",media.getFileName());
                    int imageResource = getResources().getIdentifier(uri, null, getActivity().getPackageName());
                    ivPictures.setImageResource(imageResource);
                } else if(media.getMediaType().equals("video/weblink")) {
                    ivPictures.setVisibility(View.VISIBLE);
                    SurfaceView svCamera = view.findViewById(R.id.sv_camera);
                    svCamera.setVisibility(View.INVISIBLE);
                    String thumbnailUri = String.format("@drawable/%s",media.getThumbnail());
                    int imageResource = getResources().getIdentifier(thumbnailUri, null, getActivity().getPackageName());
                    ivPictures.setImageResource(imageResource);

                    ivPictures.setOnClickListener(v -> {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addCategory(Intent.CATEGORY_BROWSABLE);
                        final int id = getResources().getIdentifier(media.getFileName(), "string", getActivity().getPackageName());
                        int id2 = R.string.link_video_1_1;
                        intent.setData(Uri.parse(getResources().getString(id)));
                        startActivity(intent);
                    });
                } else if(media.getMediaType().equals("audio")) {
                    String filename = media.getFileName();
                    int id = getResources().getIdentifier(filename, "raw", getActivity().getPackageName());
                    audioPlayer = MediaPlayer.create(getActivity(), id);
                }
                break;
            }
        }

        if(currentCaption.getNextCaption().getType() == TypeEnum.End) {
            Button btExit = view.findViewById(R.id.button_lt3_exit);
            btExit.setEnabled(false);
        }
        TextView tvDial = view.findViewById(R.id.tv_lt3_dialogue);
        tvDial.setText(dialogue);
        TextView tvSpeaker = view.findViewById(R.id.tv_lt3_speakername);
        tvSpeaker.setText(speaker);
        if(audioPlayer != null) {
            audioPlayer.start();
        }
    }

    public void stopAudio() {
        if(audioPlayer != null) {
            if(audioPlayer.isPlaying()) {
                audioPlayer.stop();
            }
        }
    }

    public void pauseAudio() {
        if(audioPlayer != null) {
            if(audioPlayer.isPlaying()) {
                audioPlayer.pause();
                audioPos = audioPlayer.getCurrentPosition();
            }
        }
    }

    public void resumeAudio() {
        if(audioPlayer != null) {
            if(!audioPlayer.isPlaying()) {
                audioPlayer.seekTo(audioPos);
                audioPlayer.start();
            }
        }
    }

//    private void initCamera()
//    {
//        myCameraPreview = new MyCameraPreview(getContext(), CAMERA_FACING);
//
//        FrameLayout fl = getView().findViewById(R.id.fl_camera);
//        fl.addView(myCameraPreview);
//        myCameraPreview.stopPreview();
//    }

    public void ToggleRecorder() {
        SurfaceView cameraView = getView().findViewById(R.id.sv_camera);
        if (recording) {
            videoRecorder.stop();
            if (usecamera) {
                try {
                    camera.reconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // recorder.release();
            recording = false;
            camera.stopPreview();
            cameraView.setVisibility(View.INVISIBLE);
            Logd("Recording Stopped");
        } else {
            cameraView.setVisibility(View.VISIBLE);
            // Let's prepareRecorder so we can record again
            prepareRecorder();
            recording = true;
            videoRecorder.start();
            Logd("Recording Started");
        }
    }

    private void prepareRecorder() {
        videoRecorder = new MediaRecorder();
        videoRecorder.setPreviewDisplay(surfaceHolder.getSurface());

        if (usecamera) {
            camera.unlock();
            videoRecorder.setCamera(camera);
        }


        videoRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        videoRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        videoRecorder.setProfile(camcorderProfile);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss", java.util.Locale.getDefault());
        Date date = new Date();
        String now = dateFormat.format(date);
        String captionSerial = CausalityData.CurrentData.Contexts.get(0).CurrentCaption.toString();

        String filename = String.format("%s (%s,%s,%s,%s)",
                now, captionSerial,
                StudentInfo.CurrentStudent().getName(),
                StudentInfo.CurrentStudent().getCurrentLecture(),
                StudentInfo.CurrentStudent().getCurrentScene());
        // This is all very sloppy
        if (camcorderProfile.fileFormat == MediaRecorder.OutputFormat.THREE_GPP) {
            String fullname = String.format("%s.3gp", filename);
            File dataFile = new File(CausalityData.CurrentData.dataPath, fullname);
            videoRecorder.setOutputFile(dataFile.getAbsolutePath());
        } else if (camcorderProfile.fileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            String fullname = String.format("%s.mp4", filename);
            File dataFile = new File(CausalityData.CurrentData.dataPath, fullname);
            videoRecorder.setOutputFile(dataFile.getAbsolutePath());
        } else {
            String fullname = String.format("%s.mp4", filename);
            File dataFile = new File(CausalityData.CurrentData.dataPath, fullname);
            videoRecorder.setOutputFile(dataFile.getAbsolutePath());
        }
        //recorder.setMaxDuration(50000); // 50 seconds
        //recorder.setMaxFileSize(5000000); // Approximately 5 megabytes

        try {
            videoRecorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFragment() {
        Speech1Fragment speech1Fragment = new Speech1Fragment();
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.speech_fragment_container, speech1Fragment);
        fragmentTransaction.commit();
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.speech_fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void expandFragment(@NonNull View view) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.i( " Pixel(Width) "  , width  + "px" );
        Log.i( " Pixel(Height) " , height + "px Z");

        GridLayout layout = view.findViewById(R.id.grid_learn_type3);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.width = width;
        params.height = height;
        layout.setLayoutParams(params);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logd("surfaceCreated");

        if (usecamera) {
            cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            if(cameraCount == 1) {
                Camera.getCameraInfo(0, cameraInfo);
                camera = Camera.open(0);
            } else {
                for (int i = 0; i < cameraCount; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        try {
                            Camera.getCameraInfo(i, cameraInfo);
//                        int orientation = calculatePreviewOrientation(cameraInfo, displayOrientation);
//                        camera.setDisplayOrientation(orientation);
//                        camera.getParameters().setRotation(orientation);
                            camera = Camera.open(i);
                        } catch (RuntimeException e) {
                            Logd("Failed to open camera: " + e.getLocalizedMessage());
                        }
                    }
                }
            }

            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {

                    }
                });
                previewRunning = true;
            }
            catch (IOException e) {
                Logd(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logd("surfaceChanged");
        int orientation = calculatePreviewOrientation(cameraInfo, displayOrientation);
        camera.setDisplayOrientation(orientation);
        camera.getParameters().setRotation(orientation);

        if (!recording && usecamera) {
            if (previewRunning){
                camera.stopPreview();
            }

            try {
                Camera.Parameters p = camera.getParameters();
                if (p.getSupportedFocusModes().contains(
                        Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    p.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                camera.setParameters(p);

                p.setPreviewSize(camcorderProfile.videoFrameWidth, camcorderProfile.videoFrameHeight);
                p.setPreviewFrameRate(camcorderProfile.videoFrameRate);

                camera.setParameters(p);

                camera.setPreviewDisplay(holder);
                camera.startPreview();
                previewRunning = true;
            }
            catch (IOException e) {
                Logd(e.getMessage());
                e.printStackTrace();
            }

//            prepareRecorder();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logd("surfaceDestroyed");
        if (recording) {
            videoRecorder.stop();
            recording = false;
        }
        if(videoRecorder != null) {
            videoRecorder.release();
        }
        if (usecamera) {
            previewRunning = false;
            //camera.lock();
            camera.release();
        }
    }

    /**
     * 안드로이드 디바이스 방향에 맞는 카메라 프리뷰를 화면에 보여주기 위해 계산합니다.
     */
    public int calculatePreviewOrientation(Camera.CameraInfo info, int rotation) {
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

}