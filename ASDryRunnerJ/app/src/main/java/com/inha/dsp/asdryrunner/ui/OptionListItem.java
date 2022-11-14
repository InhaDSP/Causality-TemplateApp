package com.inha.dsp.asdryrunner.ui;

import com.inha.dsp.causality.type.SerialNumber;

public class OptionListItem {
    private String _radix;
    private String _option;
    private SerialNumber _actionNumber;

    public void setRadix(String _radix) {
        this._radix = _radix;
    }
    public void setOption(String option) {
        this._option = option ;
    }
    public void setActionNumber(SerialNumber sn) { this._actionNumber = sn; }

    public String getRadix() {
        return this._radix;
    }
    public String getOption() {
        return this._option;
    }
    public SerialNumber getActionNumber() { return this._actionNumber; }

    public OptionListItem(String radix, String optionText, SerialNumber actionNumber)
    {
        _radix = radix;
        _option = optionText;
        _actionNumber = actionNumber;
    }

    public OptionListItem(int radix, String optionText, SerialNumber actionNumber)
    {
        _radix = Integer.toString(radix);
        _option = optionText;
        _actionNumber = actionNumber;
    }
}
