package com.inha.dsp.asdryrunner;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inha.dsp.asdryrunner.driver.CausalityData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class LectureResultFragment extends Fragment {
    private final Map<String, Integer> sceneCountMap = new LinkedHashMap<>();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public LectureResultFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LectureResultFragment.
     */
    public static LectureResultFragment newInstance(String param1, String param2) {
        LectureResultFragment fragment = new LectureResultFragment();
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
        return inflater.inflate(R.layout.fragment_lecture_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvExit = view.findViewById(R.id.tv_lres_exit);
        tvExit.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        final int score = calculateScore(view);
        TextView tvScore = view.findViewById(R.id.tv_lres_score);
        tvScore.setText(String.format(Locale.getDefault(), "%d점 입니다!", score));
    }

    private int calculateScore(View view) {
        int totalScore = 0;

        String rootDir = getResources().getString(R.string.directory_name_root);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        File rootFolder = new File(Environment.getExternalStorageDirectory(), rootDir);
        if(!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        String lectureDir = String.format("%s (%s)",
                StudentInfo.CurrentStudent().getCurrentLecture(),
                StudentInfo.CurrentStudent().getName());
        File lectureFolder = new File(rootFolder, lectureDir);
        if(!lectureFolder.exists()) {
            lectureFolder.mkdirs();
        } else {
            File[] sceneFolders = lectureFolder.listFiles();
            for(File sceneFolder : sceneFolders)
            {
                if(sceneFolder.isDirectory()) {
                    // 내부가 비어있는지 확인
                    if (sceneFolder.list().length != 0) { // 안 비었음
                        // 학습 데이터가 없는지 확인
                        File[] subFiles = sceneFolder.listFiles(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith("xml");
                            }
                        });
                        if (subFiles.length != 0) {
                            String lectureName = "";
                            String sceneName = "";
                            boolean sceneFinished = false;
                            // Read xml and check whether lecture is finished
                            File reportFile = subFiles[0];
                            Document reportXml = loadXmlFromAsset(reportFile);

                            Node rootNode = reportXml.getLastChild();
                            NodeList childNodes = rootNode.getChildNodes();
                            for (int i = 0; i < childNodes.getLength(); i++) {
                                NodeList propertyNodes = rootNode.getChildNodes();
                                for (int j = 0; j < propertyNodes.getLength(); j++) {
                                    Node propertyNode = propertyNodes.item(j);
                                    switch (propertyNode.getNodeName())
                                    {
                                        case "LectureName":
                                            lectureName = propertyNode.getTextContent();
                                            break;
                                        case "SceneName":
                                            sceneName = propertyNode.getTextContent();
                                            break;
                                        case "SceneFinished":
                                            sceneFinished = Boolean.parseBoolean(propertyNode.getTextContent());
                                    }
                                }
                            }

                            if(StudentInfo.CurrentStudent().getCurrentLecture().equals(lectureName)) {
                                if(sceneFinished) {
                                    if(sceneCountMap.containsKey(sceneName)) {
                                        int lectureCount = sceneCountMap.get(sceneName);
                                        lectureCount++;
                                        sceneCountMap.put(sceneName, lectureCount);
                                    } else {
                                        sceneCountMap.put(sceneName, 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 집계가 끝난, 학습이 완료된 장면들로부터 점수를 계산함
            final int sceneSize = CausalityData.CurrentData.Scenes.size();
            final int unitScore = (100 / (sceneSize * 2));
            final int baseScore = 100 - ((unitScore * sceneSize) * 2);
            totalScore += baseScore;
            for(String key : sceneCountMap.keySet()) {
                Integer value = sceneCountMap.get(key);
                int score = value.intValue();
                if(score < 3) {
                    totalScore = totalScore + (score * unitScore);
                } else {
                    totalScore = totalScore + (2 * unitScore); // score > 2
                }
            }
        }
        return totalScore;
    }

    private Document loadXmlFromAsset(File file)
    {
        Document result = null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringComments(true);
            dbFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder docBuild = dbFactory.newDocumentBuilder();
            result = docBuild.parse(file);
            result.getDocumentElement().normalize();
//            byte[] buffer = new byte[is.available()];
//            is.read(buffer);
//            result = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return result;
    }
}