package com.inha.dsp.asdryrunner.driver;

import androidx.annotation.NonNull;

import com.inha.dsp.asdryrunner.StudentInfo;
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

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CausalityData implements Serializable {
    public static CausalityData CurrentData;
    public static final ArrayList<CausalityData> DefaultDatas = new ArrayList<>();
    // TODO: 진행중이거나 중간에 하다말거나 완료한 학습 데이터는 아래 UserDatas에 넣어지도록 하기
    public static final ArrayList<CausalityData> UserDatas = new ArrayList<>();

    public ArrayList<Action> Actions = null;
    public ArrayList<CustomAction> CustomActions = null;
    public ArrayList<Caption> Captions  = null;
    public ArrayList<Cause> Causes  = null;
    public ArrayList<Context> Contexts  = null;
    public ArrayList<Option> Options  = null;
    public ArrayList<Perceptron> Perceptrons  = null;
    public ArrayList<Role> Roles  = null;
    public ArrayList<Scenario> Scenarios  = null;
    public ArrayList<Scene> Scenes  = null;
    public ArrayList<Media> Medias = null;

    public Metadata Metadata  = null;

    public File dataPath = null;
    public StudentInfo studentInfo = null;

    public CausalityData(ArrayList<Action> actions, ArrayList<Caption> captions,
                         ArrayList<Cause> causes, ArrayList<Context> contexts, ArrayList<Option> options,
                         ArrayList<Perceptron> perceptrons, ArrayList<Role> roles,
                         ArrayList<Scene> scenes, ArrayList<Scenario> scenarios,
                         ArrayList<CustomAction> customActions, ArrayList<Media> medias,
                         Metadata metadata)
    {
        Actions = actions;
        Captions = captions;
        Causes = causes;
        Contexts = contexts;
        Options = options;
        Perceptrons = perceptrons;
        Roles = roles;
        Scenes = scenes;
        Scenarios = scenarios;
        CustomActions = customActions;
        Medias = medias;
        Metadata = metadata;

        DefaultDatas.add(this);
    }

    private CausalityData() { }


}
