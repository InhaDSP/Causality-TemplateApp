package com.inha.dsp.asdryrunner;

import com.inha.dsp.asdryrunner.driver.CausalityReport;

import java.io.Serializable;

public class StudentInfo implements Serializable {
    private String _name;
    public String getName() { return _name; }

    private String _currentLecture;
    public String getCurrentLecture() { return _currentLecture; }
    public void setCurrentLecture(String lectureName) { _currentLecture = lectureName; }

    private String _currentScene;
    public String getCurrentScene() { return _currentScene; }
    public void setCurrentScene(String sceneName) { _currentScene = sceneName; }

    private CausalityReport causalityReport;
    public CausalityReport getCausalityReport() { return  causalityReport; }
    public void setCausalityReport(CausalityReport _report) {
        causalityReport = _report;
    }

    private static StudentInfo _current;
    private StudentInfo() {}
    public StudentInfo(String name)
    {
        _name = name;
        _current = this;
    }

    public static StudentInfo CurrentStudent() {
        return _current;
    }
}
