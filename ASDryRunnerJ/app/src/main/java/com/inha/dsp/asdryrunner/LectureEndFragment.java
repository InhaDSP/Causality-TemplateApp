package com.inha.dsp.asdryrunner;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;

import com.inha.dsp.asdryrunner.driver.CausalityData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LectureEndFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public LectureEndFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LectureEndFragment.
     */
    public static LectureEndFragment newInstance(String param1, String param2) {
        LectureEndFragment fragment = new LectureEndFragment();
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
        return inflater.inflate(R.layout.fragment_lecture_end, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        expandFragment(view);

        saveCurrentLecture();

        Button btSelectLecture = view.findViewById(R.id.button_lend_go_lecture_select);
        btSelectLecture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LearnActivity)getActivity()).replaceFragment(LectureSelectFragment.newInstance());
            }
        });

        Button btExit = view.findViewById(R.id.button_lend_exit);
        btExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LearnActivity)getActivity()).replaceFragment(LectureResultFragment.newInstance("",""));
            }
        });
    }

    private void saveCurrentLecture() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss", java.util.Locale.getDefault());
        Date date = new Date();
        String now = dateFormat.format(date);
        String filename = String.format("%s (%s,%s,%s).obj",
                now, StudentInfo.CurrentStudent().getName(),
                StudentInfo.CurrentStudent().getCurrentLecture(),
                StudentInfo.CurrentStudent().getCurrentScene());
        File dataFile = new File(CausalityData.CurrentData.dataPath, filename);

        try {
            FileOutputStream fos = new FileOutputStream(dataFile);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(CausalityData.CurrentData);
            os.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        recordedFile = new File(sdcard, filename);
//        recPath = recordedFile.getAbsolutePath();
    }

    private void expandFragment(@NonNull View view) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Log.i( " Pixel(Width) "  , width  + "px" );
        Log.i( " Pixel(Height) " , height + "px Z");

        GridLayout layout = view.findViewById(R.id.grid_learn_end);
        ViewGroup.LayoutParams params = layout.getLayoutParams();
        params.width = width;
        params.height = height;
        layout.setLayoutParams(params);
    }
}