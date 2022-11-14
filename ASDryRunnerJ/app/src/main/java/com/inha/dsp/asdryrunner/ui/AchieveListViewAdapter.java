package com.inha.dsp.asdryrunner.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.inha.dsp.asdryrunner.AchievementActivity;
import com.inha.dsp.asdryrunner.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class AchieveListViewAdapter extends BaseAdapter {
    private AchieveListViewAdapter() {}

    public AchieveListViewAdapter(Context context, AchievementActivity achievementActivity) {
        this.context = context;
        this.achievementActivity = achievementActivity;
    }
    private Context context;
    private AchievementActivity achievementActivity;
    private final ArrayList<AchieveListItem> achieveListItems = new ArrayList<>() ;

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return achieveListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return achieveListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(String lectureName, int progress)
    {
        achieveListItems.add(new AchieveListItem(lectureName, progress));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_achievement_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView tvLectureName = (TextView) convertView.findViewById(R.id.tv_lv_ach_lecture);
        ProgressBar progressBar = (ProgressBar) convertView.findViewById(R.id.pb_lv_ach_progressbar);
        TextView tvProgress = (TextView) convertView.findViewById(R.id.tv_lv_ach_progress);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        AchieveListItem achieveListItem = achieveListItems.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        tvLectureName.setText(achieveListItem.getLectureName());
        int progress = achieveListItem.getProgressPercent();
        progressBar.setProgress(progress);
        tvProgress.setText(String.format(Locale.getDefault(), "%d%%", progress));

        return convertView;
    }
}
