package com.inha.dsp.asdryrunner;

import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.inha.dsp.asdryrunner.driver.CausalityData;
import com.inha.dsp.asdryrunner.ui.OptionListItem;
import com.inha.dsp.asdryrunner.ui.OptionListViewAdapter;
import com.inha.dsp.causality.type.Action;
import com.inha.dsp.causality.type.Caption;
import com.inha.dsp.causality.type.Cause;
import com.inha.dsp.causality.type.Context;
import com.inha.dsp.causality.type.Media;
import com.inha.dsp.causality.type.Option;
import com.inha.dsp.causality.type.Perceptron;
import com.inha.dsp.causality.type.Scene;
import com.inha.dsp.causality.type.SerialNumber;
import com.inha.dsp.causality.type.TypeEnum;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LectureType1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LectureType1Fragment extends Fragment {
    private Button next;
    private MediaPlayer audioPlayer = null;
    private int audioPos = 0;

    public LectureType1Fragment() {
        // Required empty public constructor
    }

    public static LectureType1Fragment newInstance() {
        return new LectureType1Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lecture_type1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandFragment(view);
        next = view.findViewById(R.id.button_lt1_next);
        ListView optListView = view.findViewById(R.id.listview_options);
        optListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                next.setEnabled(true);
            }
        });
        Button btExit = view.findViewById(R.id.button_lt1_exit);
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

        next.setOnClickListener(new View.OnClickListener() {
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

                // 객관식 선택지가 있었다면 선택을 저장
                if(currentCaption.getOption() != null) {
                    int pos = optListView.getCheckedItemPosition();
                    OptionListItem selectedItem = (OptionListItem) optListView.getAdapter().getItem(pos);
                    SerialNumber selectedSerial = selectedItem.getActionNumber();
                    ArrayList<Action> actions = content.Actions;
                    for(Action action : actions)
                    {
                        if(action.getSerialNumber().equals(selectedSerial)) {
                            action.IsPerformed = true;
                            break;
                        }
                    }
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
                    optListView.setAdapter(null);
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
                        // Action들의 타입을 검사해서 CustomAction이 있는지 확인
                        boolean hasCustomAction = false;
                        for(SerialNumber sn : nextOption.Actions)
                        {
                            if(sn.getType() == TypeEnum.CustomAction) {
                                hasCustomAction = true;
                                break;
                            }
                        }
                        if(hasCustomAction) { // Type 3으로 넘어가기
                            ((LearnActivity)getActivity()).replaceFragment(LectureType3Fragment.newInstance());
                            return;
                        }
                    }

                    // 주관식이 없는 장면이면 Type1에서 그대로 화면 업데이트
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
        // 현재 캡션에 선택지가 있는 경우 대응하기
         if(currentCaption.getOption() != null)
         {// 이미 이전 화면에서 넘어올때 객관식만 있는 것이 확정되었으므로 주관식 생각할 필요 없음
             // 일단 다음 버튼을 비활성화하고, 선택지가 한번이라도 선택되면 OnClickListener로 활성화함
             Button btNext = view.findViewById(R.id.button_lt1_next);
             btNext.setEnabled(false);

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
             ListView optionsView = view.findViewById(R.id.listview_options);
             OptionListViewAdapter adapter = new OptionListViewAdapter();
             optionsView.setAdapter(adapter);

             int radix = 1;
             for(SerialNumber actionSerial : currentOption.Actions)
             {
                 ArrayList<Action> actions = content.Actions;
                 for(Action action : actions)
                 {
                     if(action.getSerialNumber().equals(actionSerial))
                     {
                         adapter.addItem(radix, action.getDescription(), action.getSerialNumber());
                     }
                 }
                 radix++;
             }
         } else {
             TextView tvTitle = view.findViewById(R.id.tv_option_title);
             tvTitle.setVisibility(View.INVISIBLE);
         }

        String dialogue = currentCaption.getDialogue();
        String speaker = currentCaption.getSpeakerName();
        currentCaption.Displayed = true;

        // 카메라 위치에 이미지/동영상이 표시되어야 하는지 알아내고 표시
        for(Media media : content.Medias)
        {
            if (media.getTargetCaption().equals(currentCaptionSerial)) {
                if(media.getMediaType().equals("image")) {
                    String uri = String.format("@drawable/%s",media.getFileName());
                    int imageResource = getResources().getIdentifier(uri, null, getActivity().getPackageName());
//                    Drawable res = getResources().getDrawable(imageResource);
//                    ivCamera.setImageDrawable(R.drawable.avatar_juran);
                    ivPictures.setImageResource(imageResource);
                }  else if(media.getMediaType().equals("video/weblink")) {
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
            }
        }

        if(currentCaption.getNextCaption().getType() == TypeEnum.End) {
            Button btExit = view.findViewById(R.id.button_lt1_exit);
            btExit.setEnabled(false);
        }
        TextView tvDial = view.findViewById(R.id.tv_lt1_dialogue);
        tvDial.setText(dialogue);
        TextView tvSpeaker = view.findViewById(R.id.tv_lt1_speakername);
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

    private void expandFragment(@NonNull View view) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.i( " Pixel(Width) "  , width  + "px" );
        Log.i( " Pixel(Height) " , height + "px Z");

        GridLayout layout = view.findViewById(R.id.grid_learn_type1);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.width = width;
        params.height = height;
        layout.setLayoutParams(params);
    }
}