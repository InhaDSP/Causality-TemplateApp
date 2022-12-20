package com.inha.dsp.asdryrunner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.inha.dsp.asdryrunner.ui.HistoryListItem;
import com.inha.dsp.asdryrunner.ui.HistoryListViewAdapter;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HistoryActivity extends AppCompatActivity {
    private Map<String, File> archives = new HashMap<>();
    private Map<String, File> lectureHistoryMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        loadLectureHistory();
        HistoryListViewAdapter adapter = new HistoryListViewAdapter(getApplicationContext(), HistoryActivity.this);
        loadHistoryListItems(adapter);
        if(adapter.getCount() > 0) {
            ListView lvHistory = findViewById(R.id.lv_history);
            lvHistory.setAdapter(adapter);
        }

        Button btExit = findViewById(R.id.bt_his_exit);
        btExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadHistoryListItems(HistoryListViewAdapter adapter) {
        for(String key : lectureHistoryMap.keySet()) {
            File archive = lectureHistoryMap.get(key);
            if(archive == null) { // zip 준비안된 학습데이터
                adapter.addItem(key, null, false);
            } else { // zip 준비된 학습데이터
                adapter.addItem(key, archive, true);
            }
        }
    }

    private void loadLectureHistory() {
        final String regex = "\\d+차시 \\(.+\\)";

        String rootDir = getResources().getString(R.string.directory_name_root);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        File rootFolder = new File(Environment.getExternalStorageDirectory(), rootDir);
        if(!rootFolder.exists()) {
            rootFolder.mkdirs();
        } else {
            File[] files = rootFolder.listFiles();
            for(File file : files)
            {
                if(file.isDirectory()) {
                    // 차시 데이터를 담은 디렉토리가 아니면 스킵
                    String dirName = FilenameUtils.getBaseName(file.getAbsolutePath());
                    if(!dirName.matches(regex)) {
                        continue;
                    }

                    // 내부가 비어있는지 확인
                    if (file.list().length != 0) { // 안 비었음
                        // 학습 데이터가 없는지 확인
//                        File[] subFiles = file.listFiles(new FilenameFilter() {
//                            @Override
//                            public boolean accept(File dir, String name) {
//                                return name.endsWith("xml");
//                            }
//                        });
//                        if (subFiles.length != 0) {
                            String basename = FilenameUtils.getBaseName(file.getAbsolutePath());
                            lectureHistoryMap.put(basename, null);
//                        }
                    }
                } else { // is File
                    String basename = FilenameUtils.getBaseName(file.getAbsolutePath());
                    String extension = FilenameUtils.getExtension(file.getAbsolutePath());
                    if(extension.equals("zip")) {
                        archives.put(basename, file);
                    }
                }
            }

            // zip 파일이 존재하는 학습내역이면 존재한다고 갱신
            for(String filename : archives.keySet()) {
                File archive = archives.get(filename);
                lectureHistoryMap.put(filename, archive);
            }
        }
    }
}