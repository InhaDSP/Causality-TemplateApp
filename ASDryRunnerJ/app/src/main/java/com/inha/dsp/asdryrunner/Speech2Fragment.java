package com.inha.dsp.asdryrunner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class Speech2Fragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String _recordPath;
    private String _asrText = "";

    EditText et;

    public Speech2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param recordPath Parameter 1.
     * @param asrText Parameter 2.
     * @return A new instance of fragment Speech2Fragment.
     */
    public static Speech2Fragment newInstance(String recordPath, String asrText) {
        Speech2Fragment fragment = new Speech2Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, recordPath);
        args.putString(ARG_PARAM2, asrText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            _recordPath = getArguments().getString(ARG_PARAM1);
            _asrText = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_speech2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        et = view.findViewById(R.id.et_sp2_asrText);
        et.setText(_asrText);
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable et_) {
                String s = et_.toString();
                if(s.length() != 0){
                    et.removeTextChangedListener(this);
                    String text = s.toString();
                    _asrText = text;
                    et.addTextChangedListener(this);
                }

            }
        });

        Button btConfirm = view.findViewById(R.id.button_sp2_confirm);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = et.getText().toString();
                ((LectureType3Fragment)getParentFragment()).setCustomActionText(_asrText);
            }
        });
    }
}