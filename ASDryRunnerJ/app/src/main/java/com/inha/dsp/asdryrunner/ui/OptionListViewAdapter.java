package com.inha.dsp.asdryrunner.ui;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.inha.dsp.asdryrunner.R;
import com.inha.dsp.causality.type.SerialNumber;

import java.util.ArrayList;

public class OptionListViewAdapter extends BaseAdapter {
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<OptionListItem> optionItemList = new ArrayList<OptionListItem>() ;

    public OptionListViewAdapter(){}

    @Override
    public int getCount() {
        return optionItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return optionItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_option_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView tvRadix = (TextView) convertView.findViewById(R.id.tv_lv_item_radix) ;
        TextView tvOption = (TextView) convertView.findViewById(R.id.tv_lv_item_option) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        OptionListItem optionListItem = optionItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        tvRadix.setText(optionListItem.getRadix());
        tvOption.setText(optionListItem.getOption());

        return convertView;
    }

    public void addItem(int radix, String option, SerialNumber actionNumber)
    {
        optionItemList.add(new OptionListItem(Integer.toString(radix), option, actionNumber));
    }
}
