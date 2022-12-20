package com.inha.dsp.asdryrunner.driver;

import java.io.Serializable;

public class Metadata implements Cloneable, Serializable {
    public String Name;
    public String Layout;

    public Metadata() { }

    public Object clone() {
        Metadata data = null;
        try {
            data = (Metadata) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        data.Name = Name;
        data.Layout = Layout;
        return data;
    }
}
