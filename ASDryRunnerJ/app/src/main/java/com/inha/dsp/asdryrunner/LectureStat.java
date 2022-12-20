package com.inha.dsp.asdryrunner;

import java.util.LinkedHashMap;
import java.util.Map;

public class LectureStat {
    private String lectureName;
    public String getLectureName() { return lectureName; }
    public void setLectureName(String n) { lectureName = n; }

    private String sceneName;
    public String getSceneName() { return sceneName; }
    public void setSceneName(String n) { sceneName = n; }

    private String studentName;
    public String getStudentName() { return studentName; }
    public void setStudentName(String n) { studentName = n; }


    public final Map<String, Integer> sceneCountMap = new LinkedHashMap<>();

    public LectureStat(String lname, String scname, String stname) {
        setLectureName(lname);
        setSceneName(scname);
        setStudentName(stname);
    }
    private LectureStat(){};
}
