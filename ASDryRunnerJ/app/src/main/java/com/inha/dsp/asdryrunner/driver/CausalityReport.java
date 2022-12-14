package com.inha.dsp.asdryrunner.driver;

import android.os.StrictMode;
import android.util.Log;
import android.util.Xml;

import com.inha.dsp.asdryrunner.LearnActivity;
import com.inha.dsp.asdryrunner.LectureType1Fragment;
import com.inha.dsp.asdryrunner.StudentInfo;
import com.inha.dsp.causality.type.Caption;
import com.inha.dsp.causality.type.Context;
import com.inha.dsp.causality.type.CustomAction;
import com.inha.dsp.causality.type.Option;
import com.inha.dsp.causality.type.Scene;
import com.inha.dsp.causality.type.SerialNumber;
import com.inha.dsp.causality.type.TypeEnum;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CausalityReport implements Serializable {
    private StudentInfo studentInfo;
    public StudentInfo getStudentInfo() {
        return studentInfo;
    }

    private boolean finished;
    public boolean isFinished() { return finished; }
    public void setFinished(boolean _finished) {
        finished = _finished;
    }

    private SerialNumber lastCaption;
    public SerialNumber getLastCaption() { return lastCaption; }

    private CausalityData causalityData;

    private CausalityReport() {}
    public CausalityReport(StudentInfo _studentInfo, CausalityData _causalityData) {
        studentInfo = _studentInfo;
        causalityData = _causalityData;
        SaveReport();
    }

    public void SaveReport() {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        // indentation as 3 spaces
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        StringWriter stringWriter = new StringWriter();
        try {
            xmlSerializer.setOutput(stringWriter);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag("", "CausalityReport");
        } catch (IOException e) {
            Logd(e.getLocalizedMessage());
        }

        String studentName = studentInfo.getName();
        String lectureName = studentInfo.getCurrentLecture();
        String sceneName = studentInfo.getCurrentScene();

        Context context = causalityData.Contexts.get(0);
        lastCaption = context.CurrentCaption;
        for(Caption caption : causalityData.Captions) {
            if(caption.getSerialNumber().equals(lastCaption)) {
                if(caption.getNextCaption().getType() == TypeEnum.End) {
                    finished = true;
                }
            }
        }

        Logd(String.format("%s is %s", "StudentName", studentName));
        Logd(String.format("%s is %s", "lectureName", lectureName));
        Logd(String.format("%s is %s", "sceneName", sceneName));
        Logd(String.format("%s is %s", "lastCaption", lastCaption.toString()));
        if(isFinished()) {
            Logd("Scene is finished");
        } else {
            Logd("Scene is not finished");
        }
        try {
            xmlSerializer.startTag("", "StudentName");
            xmlSerializer.text(studentName);
            xmlSerializer.endTag("", "StudentName");
            xmlSerializer.startTag("", "LectureName");
            xmlSerializer.text(lectureName);
            xmlSerializer.endTag("", "LectureName");
            xmlSerializer.startTag("", "SceneName");
            xmlSerializer.text(sceneName);
            xmlSerializer.endTag("", "SceneName");
            xmlSerializer.startTag("", "SceneFinished");
            xmlSerializer.text(Boolean.toString(isFinished()));
            xmlSerializer.endTag("", "SceneFinished");
        } catch (IOException e) {
            Logd(e.getLocalizedMessage());
        }

        /* ??? ???????????? ????????? ???????????? ??? ?????????????????? ????????? ???????????? ?????? ????????? ?????? ????????? */
        try {
            xmlSerializer.startTag("", "CustomActions");
        } catch (IOException e) {
            Logd(e.getLocalizedMessage());
        }

        reportCustomAction(context, xmlSerializer);

        try {
            xmlSerializer.endTag("", "CustomActions");
            xmlSerializer.endTag("", "CausalityReport");
            xmlSerializer.endDocument();
        } catch (IOException e) {
            Logd(e.getLocalizedMessage());
        }
        String xmlString = stringWriter.toString();
//        Logd(xmlString);
        SaveReportIntoFile(xmlString);
    }

    private void SaveReportIntoFile(String xmlString) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        String filename = String.format("report.xml");
        File dataFile = new File(CausalityData.CurrentData.dataPath, filename);

        try {
            FileOutputStream fos = new FileOutputStream(dataFile);
            fos.write(xmlString.getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            Logd(e.getLocalizedMessage());
        } catch (IOException e) {
            Logd(e.getLocalizedMessage());
        }
    }

    private void reportCustomAction(Context context, XmlSerializer xmlSerializer) {
        SerialNumber sceneSerial = context.CurrentScene;
        Scene currentScene = null;
        for(Scene scene : causalityData.Scenes) {
            if(scene.getSerialNumber().equals(sceneSerial)) {
                currentScene = scene;
                break;
            }
        }
        SerialNumber firstCaptionSerial = currentScene.getCaption();
        Caption firstCaption = null;
        for(Caption caption : causalityData.Captions) {
            if(caption.getSerialNumber().equals(firstCaptionSerial)) {
                firstCaption = caption;
                break;
            }
        }

        Caption currentCaption = firstCaption;
        while (currentCaption.getNextCaption().getType() != TypeEnum.End) {
            if(!currentCaption.Displayed) {
                break;
            }
            if (currentCaption.getOption() != null) {
                // ???????????? ????????????.
                SerialNumber currentOptionSerial = currentCaption.getOption();
                /* ???????????? ????????? ???????????? */
                Option currentOption = null;
                for (Option option : causalityData.Options) {
                    if (option.getSerialNumber().equals(currentOptionSerial)) {
                        currentOption = option;
                        break;
                    }
                }
                // Action?????? ????????? ???????????? CustomAction??? "?????????" ??????
                CustomAction customAction = null;
                for (SerialNumber sn : currentOption.Actions) {
                    if (sn.getType() == TypeEnum.CustomAction) {
                        for (CustomAction caction : causalityData.CustomActions) {
                            if (caction.getSerialNumber().equals(sn)) {
                                customAction = caction;
                                break;
                            }
                        }
                        break;
                    }
                }
                if (customAction != null) { // ???????????? ???????????? ????????????
                    String customValue = "";
                    if(customAction.getCustomValue() != null) {
                        customValue = customAction.getCustomValue();
                    }
                    Logd(String.format("%s: %s",
                            currentCaption.getSerialNumber().toString(), currentCaption.getDialogue()));
                    Logd(String.format("%s is %s", "customAction", customValue));
                    try {
                        xmlSerializer.startTag("", "CustomAction");
                        xmlSerializer.startTag("", "Caption");
                        xmlSerializer.text(currentCaption.getDialogue());
                        xmlSerializer.endTag("", "Caption");
                        xmlSerializer.startTag("", "Answer");
                        xmlSerializer.text(customValue);
                        xmlSerializer.endTag("", "Answer");
                        xmlSerializer.endTag("", "CustomAction");
                    } catch (IOException e) {
                        Logd(e.getLocalizedMessage());
                    }
                }
            }
            for (Caption caption : causalityData.Captions) {
                if (caption.getSerialNumber().equals(currentCaption.getNextCaption())) {
                    currentCaption = caption; // ?????? ????????????
                    break;
                }
            }
        }
    }

    private void Logd(String message) {
        Log.d("ASDryRunner", String.format("CausalityReport: %s", message));
    }
}
