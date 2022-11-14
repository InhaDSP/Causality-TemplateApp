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
import com.inha.dsp.asdryrunner.driver.CausalityReport;
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
import com.inha.dsp.causality.type.SerialNumber;
import com.inha.dsp.causality.util.XmlLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class SceneSelectFragment extends Fragment implements View.OnClickListener {
    private String[] sceneNames;
    private final Map<String, Integer> learnedScenes = new LinkedHashMap<>();
    private final ArrayList<MaterialButton> sceneButtons = new ArrayList<>();

    private boolean pausedLectureExists;
    private int resumeOrOverwrite = 0;
    private final int CHOICE_RESUME = -1;
    private final int CHOICE_OVERWRITE = 1;
    private CausalityData previousData;
    private CausalityData defaultData;

    public SceneSelectFragment() {
        // Required empty public constructor
    }

    public static SceneSelectFragment newInstance() {
        return new SceneSelectFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scene_select, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Scene 이름들 수집
        String currentLecture = StudentInfo.CurrentStudent().getCurrentLecture();
        for (CausalityData data : CausalityData.DefaultDatas)
        {
            if(data.Metadata.Name.equals(currentLecture))
            {
                sceneNames = new String[data.Scenes.size()];
                for(int i = 0; i < data.Scenes.size(); i++) {
                    Scene scene = data.Scenes.get(i);
                    sceneNames[i] = scene.getDescription();
                }
            }
        }

        createSceneButtons(view);
        updateLearnedScene(view);
    }

    private void createSceneButtons(View view) {
        TableLayout sceneTable = view.findViewById(R.id.scene_table);
        final int COL = 3;
        TableRow tr = null;
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(64, 24, 64, 24);
        final int targetDP = 30;
        final int targetPX = targetDP * getResources().getDisplayMetrics().densityDpi;
        int width = (getResources().getDisplayMetrics().widthPixels - targetPX) / 4;
        int height = getResources().getDisplayMetrics().heightPixels / 5;

        for(int i = 0; i < sceneNames.length; i++) {
            if(i % COL == 0) {
                if(tr != null) {
                    sceneTable.addView(tr);
                }
                tr = new TableRow(getActivity());
                tr.setPadding(0,24,0,24);

            }
            MaterialButton btID = new MaterialButton(getActivity());
            btID.setText(sceneNames[i]);
            btID.setWidth(width);
            btID.setHeight(height);
            btID.setTextSize(20);
            btID.setOnClickListener(this::onClick);

            sceneButtons.add(btID);
            tr.addView(btID,layoutParams);
        }
        sceneTable.addView(tr);
    }

    @Override
    public void onClick(View v) {
        Button button = (Button)v;
        String sceneName = button.getText().toString();
//        Toast.makeText(getContext(), sceneName, Toast.LENGTH_SHORT).show();
        toLectureTypeNFragment(sceneName);
    }

    private void toLectureTypeNFragment(String sceneName) {
        CausalityData newData = loadNewData(StudentInfo.CurrentStudent().getCurrentLecture());

        newData.studentInfo = StudentInfo.CurrentStudent();
        for(Scene scene : newData.Scenes) {
            if(scene.getDescription().equals(sceneName)) {
                Context currentContext = newData.Contexts.get(0);
                currentContext.CurrentScene = scene.getSerialNumber();
                currentContext.CurrentCaption = scene.getCaption();

                StudentInfo.CurrentStudent().setCurrentScene(sceneName);
                break;
            }
        }
        CausalityData.UserDatas.add(newData);
        CausalityData.CurrentData = newData;

        newData.dataPath = createNewFolder();

        CausalityReport causalityReport = new CausalityReport(StudentInfo.CurrentStudent(), newData);
        StudentInfo.CurrentStudent().setCausalityReport(causalityReport);
        ((LearnActivity)getActivity()).replaceFragment(LectureType1Fragment.newInstance());
    }

    private File createNewFolder() { // 현재 차시 데이터의 폴더를 생성
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
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss", java.util.Locale.getDefault());
        Date date = new Date();
        String now = dateFormat.format(date);
        String filename = String.format("%s (%s,%s,%s)", now,
                StudentInfo.CurrentStudent().getName(),
                StudentInfo.CurrentStudent().getCurrentLecture(),
                StudentInfo.CurrentStudent().getCurrentScene());
        File currentFolder = new File(lectureFolder, filename);
        if(!currentFolder.exists()) {
            currentFolder.mkdirs();
        }

        /* 테스트용 코드 */
//        String zipFilename = String.format("%s.zip", filename);
//        File zipFile = new File(rootFolder, zipFilename);
//        try {
//            zipFile.createNewFile();
//            FileWriter fw = new FileWriter(zipFile);
//            fw.write("aaa");
//            fw.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return currentFolder;
    }

    public CausalityData loadNewData(String lectureName) {
        CausalityData data = null;
        Map<String, String> lecturePathMap = new HashMap<>();
        lecturePathMap.put("테스트 차시","lec0");
        lecturePathMap.put("1차시","lec1");
        lecturePathMap.put("2차시","lec2");
        lecturePathMap.put("3차시","lec3");
        lecturePathMap.put("4차시","lec4");
        lecturePathMap.put("5차시","lec5");
        String lecturePath = lecturePathMap.get(lectureName);

        String rootPath = "content/scenario";
        AssetManager assetManager = getResources().getAssets();
        String assets[] = null;
        try {
            assets = assetManager.list(rootPath);
            for(String subPath : assets) {
                // subdirectory in content/scenario
                if(subPath.equals(lecturePath)) {
                    data = loadContentXml(rootPath + "/" + subPath + "/");
                }
            }
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return data;
    }


    private void updateLearnedScene(View view) {
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
        }
        else {
            File[] files = lectureFolder.listFiles();
            for(File file : files)
            {
                if(file.isDirectory()) {
                    // 내부가 비어있는지 확인
                    if (file.list().length != 0) { // 안 비었음
                        // 학습 데이터가 없는지 확인
                        File[] subFiles = file.listFiles(new FilenameFilter() {
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
                                    if(learnedScenes.containsKey(sceneName)) {
                                        int learnCount = learnedScenes.get(sceneName);
                                        learnCount++;
                                        learnedScenes.put(sceneName, learnCount);
                                    } else {
                                        learnedScenes.put(sceneName, 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 학습이 완료된 장면들의 버튼 색을 다르게 바꿈
            for(String sceneName : learnedScenes.keySet()) {
                for (MaterialButton button : sceneButtons) {
                    if(button.getText().toString().equals(sceneName)) {
                        final int learnCount = learnedScenes.get(sceneName);
                        if(learnCount == 1) {
                            button.setBackgroundColor(getResources().getColor(R.color.primaryLightColor));
                        } else if (learnCount > 1) {
                            button.setBackgroundColor(getResources().getColor(R.color.buttonFinishedColor));
                        }
                    }
                }
            }
        }
    }

    private CausalityData loadContentXml(String rootPath) {
        XmlLoader loader = new XmlLoader();

        Document xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_role));
        ArrayList<Role> roles = loader.LoadRole(xmlDocument);
        for(Role role : roles) {
            String roleInfo = getString(R.string.format_role, role.getSerialNumber(),
                    role.getName(), role.getDescription(), role.getPosition());
            Log.d("SceneSelectFragment", roleInfo);
        }

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_action));
        ArrayList<Action> actions = loader.LoadAction(xmlDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_perceptron));
        ArrayList<Perceptron> perceptrons = loader.LoadPerceptron(xmlDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_cause));
        ArrayList<Cause> causes = loader.LoadCause(xmlDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_option));
        ArrayList<Option> options = loader.LoadOption(xmlDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_caption));
        ArrayList<Caption> captions = loader.LoadCaption(xmlDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_scene));
        ArrayList<Scene> scenes = loader.LoadScene(xmlDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_scenario));
        ArrayList<Scenario> scenarios = loader.LoadScenario(xmlDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_context));
        ArrayList<Context> contexts = loader.LoadContext(xmlDocument);

        xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_media));
        ArrayList<Media> medias = loader.LoadMedia(xmlDocument);

        Metadata metadata = LoadMetadata(rootPath + getResources().getString(R.string.filename_metadata));

        try
        {
            xmlDocument = loadXmlFromAsset(rootPath + getResources().getString(R.string.filename_customaction));
            ArrayList<CustomAction> customactions = loader.LoadCustomAction(xmlDocument);

            CausalityData data = new CausalityData(
                    actions, captions, causes, contexts, options,
                    perceptrons, roles, scenes, scenarios, customactions,
                    medias, metadata);
            return data;
        } catch (Exception e)
        {
            CausalityData data = new CausalityData(
                    actions, captions, causes, contexts, options,
                    perceptrons, roles, scenes, scenarios, null,
                    medias, metadata);
            return data;
        }
    }

    private Document loadXmlFromAsset(String filePath)
    {
        Document result = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(filePath);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringComments(true);
            dbFactory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder docBuild = dbFactory.newDocumentBuilder();
            result = docBuild.parse(is);
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

    private Metadata LoadMetadata(String filePath)
    {
        Node rootNode = loadXmlFromAsset(filePath).getLastChild();
        NodeList propertyNodes = rootNode.getChildNodes();
        Metadata metadata = new Metadata();
        for (int i = 0; i < propertyNodes.getLength(); i++)
        {
            Node propertyNode = propertyNodes.item(i);
            switch (propertyNode.getNodeName())
            {
                case "Name":
                    metadata.Name = propertyNode.getTextContent();
                    break;
                case "Layout":
                    metadata.Layout = propertyNode.getTextContent();
                    break;
            }
        }
        return metadata;
    }
}