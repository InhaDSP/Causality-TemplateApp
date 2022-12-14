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

        // Context ???????????? ???????????? ?????? ???????????? ??? ????????? ?????????
        updateContent(view);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // ??????????????? ????????? ????????? ??????
                if(audioPlayer != null) {
                    if(audioPlayer.isPlaying()) {
                        audioPlayer.stop();
                    }
                    audioPlayer.release();
                    audioPlayer = null;
                }
                
                /* ?????? ????????? ???????????? ???????????? ?????? */
                // ?????? ????????? ????????? ??????
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

                // ????????? ???????????? ???????????? ????????? ??????
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

                // ????????? ????????????
                currentCaption.Displayed = true;

                // ????????? ??????
                StudentInfo.CurrentStudent().getCausalityReport().SaveReport();

                // ?????? Caption??? Scene??? ????????? Caption??????
                if(currentCaption.getNextCaption().getType() == TypeEnum.End) {
                    // ?????? ???????????? ?????????
                    ((LearnActivity)getActivity()).replaceFragment(LectureEndFragment.newInstance("aa","bb"));
                } else {
                    // ?????? UI ????????? ?????????
                    optListView.setAdapter(null);
                    ImageView ivCamera = view.findViewById(R.id.iv_pictures);
                    ivCamera.setImageResource(R.drawable.icon_smile);

                    /* ?????? ???????????? Context ???????????? */
                    Caption nextCaption = null;
                    for(Caption caption : captions)
                    {
                        if(caption.getSerialNumber().equals(currentCaption.getNextCaption()))
                        {
                            nextCaption = caption;
                            break;
                        }
                    }

                    // ???????????? ????????? getNextCaption()??? ???????????? ?????? ??????
                    context.CurrentCaption = nextCaption.getSerialNumber();
                    // ????????? ?????? Caption??? Cause??? ????????? ???????????? ?????? ?????? ??? ??????
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

                        // Bias??? perform??? Action?????? Weight??? ?????????
                        double y = currentPerceptron.getBias();
                        for (int i = 0; i < inputs.size(); i++)
                        {
                            if(inputs.get(i).IsPerformed) {
                                y += currentPerceptron.Weights[i];
                            }
                        }

                        /*
                         * ?????? ????????? ????????? ????????? ?????? ???????????? ???????????? ????????????.
                         * int idx = -1 ?????? ??? ??? ?????? ????????? 2???(CAP003, CAP009)??? ??????.
                         * ?????????????????? ?????? ?????????????????? Threshold??? ????????? ????????????,
                         * ??? ?????? ??? ????????? ???????????? ??????????????? ???????????? ????????????.
                         * ????????? ?????? ????????? ????????? y??? 0.9??? Threshold??? 0.8????????????
                         * CAP003??? ????????? CAP009??? ??????????????? ????????????.
                         * ????????? ?????? ????????? 3???(CAP003,006,009)??? ??????.
                         * ?????? Threshold??? 3-1, ??? 2(0.3, 0.5)?????????.
                         * ?????? y??? 0.6???????????? ????????? ????????? 0.3,0.5 2????????? idx++??? ????????????.
                         * ?????? idx??? 0?????? 2??? ????????? ??????,
                         * ??? ??? ?????? ????????? ?????? ????????? ????????? ???????????? ????????? ??? ??? ????????????.
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
                        // ??????????????? context??? ?????? CurrentCaption??? ?????????????????? ???
                        context.CurrentCaption = nextCaption.getSerialNumber();
                    }


                    /* Context??? ?????? ???????????? ???????????? ???????????? UI????????? ?????? */
                    // TODO: ??????????????? Type 2??? ????????? ???????????? ???????????????
                    // ?????? ?????? ????????? ?????????/?????????(Options)??? ????????? ??????
                    SerialNumber nextOptionSerial = nextCaption.getOption();
                    if(nextOptionSerial != null)
                    {// ?????? ??????????????? ???????????? ??????
                        Option nextOption = null;
                        for(Option option : content.Options)
                        {
                            if(option.getSerialNumber().equals(nextOptionSerial))
                            {
                                nextOption = option;
                                break;
                            }
                        }
                        // Action?????? ????????? ???????????? CustomAction??? ????????? ??????
                        boolean hasCustomAction = false;
                        for(SerialNumber sn : nextOption.Actions)
                        {
                            if(sn.getType() == TypeEnum.CustomAction) {
                                hasCustomAction = true;
                                break;
                            }
                        }
                        if(hasCustomAction) { // Type 3?????? ????????????
                            ((LearnActivity)getActivity()).replaceFragment(LectureType3Fragment.newInstance());
                            return;
                        }
                    }

                    // ???????????? ?????? ???????????? Type1?????? ????????? ?????? ????????????
                    updateContent(view);
                }
            }
        });
    }

    private void updateContent(@NonNull View view) {
        ImageView ivPictures = view.findViewById(R.id.iv_pictures);

        // ?????? ???????????????, ?????? ??? ?????????????????? ????????? Context??? ????????????
        CausalityData content = CausalityData.CurrentData;
        Context context = content.Contexts.get(0);
        int progress = context.CurrentScenario.getIndex() + context.CurrentScene.getIndex() + context.CurrentCaption.getIndex();
        // progress??? 0?????? ?????? ??????

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
        // ?????? ????????? ???????????? ?????? ?????? ????????????
         if(currentCaption.getOption() != null)
         {// ?????? ?????? ???????????? ???????????? ???????????? ?????? ?????? ????????????????????? ????????? ????????? ?????? ??????
             // ?????? ?????? ????????? ??????????????????, ???????????? ??????????????? ???????????? OnClickListener??? ????????????
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

        // ????????? ????????? ?????????/???????????? ??????????????? ????????? ???????????? ??????
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