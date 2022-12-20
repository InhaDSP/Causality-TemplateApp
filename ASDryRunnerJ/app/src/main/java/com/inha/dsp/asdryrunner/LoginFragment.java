package com.inha.dsp.asdryrunner;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private String[] students = {"민준", "지후", "준서",
            "우진", "서현",
            "지윤", "수민", "예은",
            "도현", "건우",
            "서영", "태인"
    };

    private String[] passwords = { "000102", "011005", "000921",
            "980614", "990225",
            "000503", "990415", "990702",
            "000312", "010624",
            "961107", "941111"
    };

    private Map<String,String> account;
    private String input_pw = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAccountMap();

        TableLayout idTable = view.findViewById(R.id.login_table);
        final int COL = 3;
        TableRow tr = null;
        android.widget.TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(64, 24, 64, 24);
        final int targetDP = 30;
        final int targetPX = targetDP * getResources().getDisplayMetrics().densityDpi;
        int width = (getResources().getDisplayMetrics().widthPixels - targetPX) / 4;
        int height = (getResources().getDisplayMetrics().heightPixels) / 5;

        for(int i = 0; i < students.length; i++) {
            if(i % COL == 0) {
                if(tr != null) {
                    idTable.addView(tr);
                }
                tr = new TableRow(getActivity());
                tr.setPadding(0,24,0,24);
            }

            MaterialButton btID = new MaterialButton(getActivity());
            btID.setText(students[i]);
            btID.setWidth(width);
            btID.setHeight(height);
            btID.setTextSize(20);
            btID.setOnClickListener(this::onClick);
            tr.addView(btID, layoutParams);
        }
        idTable.addView(tr);
    }

    private void initAccountMap() {
        account = new HashMap<>();
        account.put("민준", "000102");
        account.put("지후", "011005");
        account.put("준서", "000921");
        account.put("우진", "980614");
        account.put("서현", "990225");
        account.put("지윤", "000503");
        account.put("수민", "990415");
        account.put("예은", "990702");
        account.put("도현", "000312");
        account.put("건우", "010624");
        account.put("서영", "991107");
        account.put("태인", "941111");
    }

    @Override
    public void onClick(View v) {
        Button bt = (Button)v;
        String name = bt.getText().toString();
        String password = account.get(name);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("비밀번호 입력");

        // Set up the input
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        InputFilter[] inputFilters = new InputFilter[1];
        inputFilters[0] = new InputFilter.LengthFilter(6);
        input.setFilters(inputFilters);
//        input.setText("941111");
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("확인", (dialog, which) -> {
            input_pw = input.getText().toString();
            if(checkPassword(name, password)) {
                StudentInfo student = new StudentInfo(name);
                ((LearnActivity)getActivity()).replaceFragment(LectureSelectFragment.newInstance());
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private boolean checkPassword(String name, String pw) {
        if(input_pw.equals(pw)) {
            Toast.makeText(getContext(), MessageFormat.format("{0}님, 안녕하세요.", name), Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(getContext(), "비밀번호를 틀렸어요.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}