package com.inha.dsp.asdryrunner.ui;

public class AchieveListItem {
    private String lectureName;
    private int progressPercent;

    public void setLectureName(String lecture) { this.lectureName = lecture; }
    public void setProgressPercent(int progress) { this.progressPercent = progress; }

    public String getLectureName() { return this.lectureName; }
    public int getProgressPercent() { return this.progressPercent; }

    public AchieveListItem(String lecture, int progress)
    {
        setLectureName(lecture);
        setProgressPercent(progress);
    }
}
