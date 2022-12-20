package com.inha.dsp.asdryrunner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.inha.dsp.asdryrunner.driver.CausalityData;
import com.inha.dsp.asdryrunner.ui.AchieveListViewAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class AchievementActivity extends AppCompatActivity {
    private final ArrayList<LectureStat> lectureStats = new ArrayList<>();
    private final Map<String, Integer> lectureProgress = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        loadLectureStats();
        AchieveListViewAdapter adapter = new AchieveListViewAdapter(getApplicationContext(), AchievementActivity.this);
        loadAchieveListItems(adapter);
        if(adapter.getCount() > 0) {
            ListView lvAchievement = findViewById(R.id.lv_achievement);
            lvAchievement.setAdapter(adapter);
        }

        Button btExit = findViewById(R.id.bt_ach_exit);
        btExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadAchieveListItems(AchieveListViewAdapter adapter) {
        for(String lectureName : lectureProgress.keySet()) {
            int progress = lectureProgress.get(lectureName);
            adapter.addItem(lectureName, progress);
        }
    }

    private void loadLectureStats() {
        final String regex = "\\d+차시 \\(.+\\)";
        String rootDir = getResources().getString(R.string.directory_name_root);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        File rootFolder = new File(Environment.getExternalStorageDirectory(), rootDir);
        if(!rootFolder.exists()) {
            rootFolder.mkdirs();
        } else {
            File[] lectureFolders = rootFolder.listFiles();
            for(File lectureFolder : lectureFolders)
            {
                if(lectureFolder.isDirectory()) {
                    // 차시 데이터를 담은 디렉토리가 아니면 스킵
                    String dirName = FilenameUtils.getBaseName(lectureFolder.getAbsolutePath());
                    if(!dirName.matches(regex)) {
                        continue;
                    }

                    // 내부가 비어있는지 확인
                    if (lectureFolder.list().length != 0)
                    { // 안 비었음
                        String lectureFolderName = FilenameUtils.getBaseName(lectureFolder.getAbsolutePath());
                        String lectureName = "";
                        String studentName = "";
                        String sceneName = "";
                        Matcher matcher = Pattern.compile("\\((.*)\\)").matcher(lectureFolderName);
                        if( matcher.find()) {
                            studentName = matcher.group(1) ;
                        } else {
                            continue;
                        }
                        // 장면 데이터가 없는지 확인
                        File[] sceneFolders = lectureFolder.listFiles();
                        for(File sceneFolder : sceneFolders) {
                            File[] subFiles = sceneFolder.listFiles((dir, name) -> name.endsWith("xml"));
                            if (subFiles.length != 0) {
//                                String basename = FilenameUtils.getBaseName(sceneFolder.getAbsolutePath());
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

                                if(sceneFinished) {
                                    // lectureStats에 완료한 장면의 정보를 집어넣는다.
                                    String finalSceneName = sceneName;
                                    String finalLectureName = lectureName;
                                    Collection result = CollectionUtils.select(lectureStats,
                                            stat -> (stat.getSceneName().equals(finalSceneName))
                                                    &&(stat.getLectureName().equals(finalLectureName)));
                                    if(result.size() == 0) {
                                        // 새로 객체 만들고 리스트에 추가
                                        LectureStat stat = new LectureStat(lectureName, sceneName, studentName);
                                        stat.sceneCountMap.put(sceneName, 1);
                                        lectureStats.add(stat);
                                    } else {
                                        LectureStat[] stats = (LectureStat[]) result.toArray(new LectureStat[result.size()]);
                                        LectureStat stat = stats[0];
                                        if(stat.sceneCountMap.containsKey(sceneName)) {
                                            int lectureCount = stat.sceneCountMap.get(sceneName);
                                            lectureCount++;
                                            stat.sceneCountMap.put(sceneName, lectureCount);
                                        } else {
                                            stat.sceneCountMap.put(sceneName, 1);
                                        }
                                    }
                                }
                            }
                        }

                        // 집계가 끝난, 학습이 완료된 장면들로부터 점수를 계산함
                        String finalLectureName = lectureName;
                        Collection result = CollectionUtils.select(lectureStats,
                                stat -> (stat.getLectureName().equals(finalLectureName)));
                        LectureStat[] stats = (LectureStat[]) result.toArray(new LectureStat[result.size()]);
                        int totalScore = 0;
                        for(LectureStat stat : stats) {
                            int sceneSize = 0;
                            for (CausalityData causalityData : CausalityData.DefaultDatas)
                            {
                                if(causalityData.Metadata.Name.equals(lectureName)) {
                                    sceneSize = causalityData.Scenes.size();
                                }
                            }
                            final int unitScore = (100 / (sceneSize * 2));
                            final int baseScore = 100 - ((unitScore * sceneSize) * 2);
                            totalScore += baseScore;
                            for(String key : stat.sceneCountMap.keySet()) {
                                Integer value = stat.sceneCountMap.get(key);
                                int score = value;
                                if(score < 3) {
                                    totalScore = totalScore + (score * unitScore);
                                } else {
                                    totalScore = totalScore + (2 * unitScore); // score > 2
                                }
                            }
                            lectureProgress.put(stat.getLectureName(), totalScore);
                        }
                    }
                }
            }
        }
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