package com.inha.dsp.asdryrunner;

import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.inha.dsp.asdryrunner.driver.CausalityData;
import com.inha.dsp.asdryrunner.driver.Metadata;
import com.inha.dsp.causality.type.Action;
import com.inha.dsp.causality.type.Caption;
import com.inha.dsp.causality.type.Cause;
import com.inha.dsp.causality.type.Context;
import com.inha.dsp.causality.type.CustomAction;
import com.inha.dsp.causality.type.Media;
import com.inha.dsp.causality.type.Option;
import com.inha.dsp.causality.type.Perceptron;
import com.inha.dsp.causality.type.Role;
import com.inha.dsp.causality.type.Scenario;
import com.inha.dsp.causality.type.Scene;
import com.inha.dsp.causality.util.XmlLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LectureSelectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LectureSelectFragment extends Fragment implements View.OnClickListener {
    private String[] lectureNames;
    private Map<String, Date> lectureDateMap;

    public LectureSelectFragment() {
        // Required empty public constructor
    }

    public static LectureSelectFragment newInstance() {
        return new LectureSelectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lecture_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lectureNames = new String[]{
                getResources().getString(R.string.title_lecture_pre), getResources().getString(R.string.title_lecture_1),
                getResources().getString(R.string.title_lecture_2), getResources().getString(R.string.title_lecture_3),
                getResources().getString(R.string.title_lecture_4), getResources().getString(R.string.title_lecture_5)
        };
        try {
            initLectureDateMap();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        createLectureButtons(view);
    }

    private void initLectureDateMap() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm", java.util.Locale.getDefault());
        lectureDateMap = new HashMap<>();
        lectureDateMap.put(getResources().getString(R.string.title_lecture_pre), dateFormat.parse("2022-02-23-00-00"));
        lectureDateMap.put(getResources().getString(R.string.title_lecture_1), dateFormat.parse("2021-03-06-14-00"));
        lectureDateMap.put(getResources().getString(R.string.title_lecture_2), dateFormat.parse("2021-03-07-01-00"));
//        lectureDateMap.put(getResources().getString(R.string.title_lecture_3), dateFormat.parse("2021-03-14-01-00"));
        lectureDateMap.put(getResources().getString(R.string.title_lecture_3), dateFormat.parse("2021-03-14-14-00"));
//        lectureDateMap.put(getResources().getString(R.string.title_lecture_4), dateFormat.parse("2021-03-20-01-00"));
        lectureDateMap.put(getResources().getString(R.string.title_lecture_4), dateFormat.parse("2021-03-21-14-00"));
//        lectureDateMap.put(getResources().getString(R.string.title_lecture_5), dateFormat.parse("2021-03-20-01-00"));
        lectureDateMap.put(getResources().getString(R.string.title_lecture_5), dateFormat.parse("2021-03-28-14-00"));
    }

    private void createLectureButtons(@NonNull View view) {
        TableLayout lectureTable = view.findViewById(R.id.lecture_table);
        final int COL = 3;
        TableRow tr = null;
        android.widget.TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(64, 24, 64, 24);
        final int targetDP = 30;
        final int targetPX = targetDP * getResources().getDisplayMetrics().densityDpi;
        int width = (getResources().getDisplayMetrics().widthPixels - targetPX) / 4;
        int height = getResources().getDisplayMetrics().heightPixels / 5;

        for(int i = 0; i < lectureNames.length; i++) {
            if(i % COL == 0) {
                if(tr != null) {
                    lectureTable.addView(tr);
                }
                tr = new TableRow(getActivity());
                tr.setPadding(0,24,0,24);

            }
            MaterialButton btID = new MaterialButton(getActivity());
            btID.setText(lectureNames[i]);
            btID.setWidth(width);
            btID.setHeight(height);
            btID.setTextSize(20);
            btID.setOnClickListener(this::onClick);

            // 오늘 못 하는 수업의 버튼은 비활성화
            Date todayDate = Calendar.getInstance().getTime();
            Date lectureDate = lectureDateMap.get(lectureNames[i]);
            boolean available = todayDate.after(lectureDate);
            if(!available) {
                btID.setEnabled(false);
            }

            tr.addView(btID,layoutParams);
        }
        lectureTable.addView(tr);
    }

    @Override
    public void onClick(View v) {
        Button bt = (Button)v;
        String lectureName = bt.getText().toString();
        StudentInfo.CurrentStudent().setCurrentLecture(lectureName);
        ((LearnActivity)getActivity()).replaceFragment(SceneSelectFragment.newInstance());
    }
}