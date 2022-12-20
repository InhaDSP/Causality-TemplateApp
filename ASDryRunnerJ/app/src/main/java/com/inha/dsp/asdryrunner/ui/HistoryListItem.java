package com.inha.dsp.asdryrunner.ui;

import java.io.File;

public class HistoryListItem {
    private String lecture;
    private boolean prepared;
    private File zipFile;

    public void setLecture(String lecture) { this.lecture = lecture; }
    public void setPrepared(boolean prepared) { this.prepared = prepared; }
    public void setZipFile(File file) { this.zipFile = file; }

    public String getLecture() { return this.lecture; }
    public boolean isPrepared() { return this.prepared; }
    public File getZipFile() { return  this.zipFile; }

    public HistoryListItem(String lecture, File zipFile, boolean prepared)
    {
        setLecture(lecture);
        setPrepared(prepared);
        setZipFile(zipFile);
    }

    public HistoryListItem(String lecture, File zipFile)
    {
        setLecture(lecture);
        setZipFile(zipFile);
        setPrepared(false);
    }
}
