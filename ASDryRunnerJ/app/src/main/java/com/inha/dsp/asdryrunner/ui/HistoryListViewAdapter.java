package com.inha.dsp.asdryrunner.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.inha.dsp.asdryrunner.HistoryActivity;
import com.inha.dsp.asdryrunner.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HistoryListViewAdapter extends BaseAdapter {
    private HistoryListViewAdapter() {}

    public HistoryListViewAdapter(Context context, HistoryActivity historyActivity) {
        this.context = context;
        this.historyActivity = historyActivity;
    }
    private Context context;
    private HistoryActivity historyActivity;

    private final ArrayList<HistoryListItem> historyListItems = new ArrayList<>() ;

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
        return historyListItems.size();
    }

    @Override
    public Object getItem(int position) {
        return historyListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(String lectureName, File zipFile, boolean isPrepared)
    {
        historyListItems.add(new HistoryListItem(lectureName,zipFile, isPrepared));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_lecture_history, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView tvLectureName = (TextView) convertView.findViewById(R.id.tv_lv_lhis_lecture_name);
        Button btPrepared = (Button) convertView.findViewById(R.id.bt_lv_lhis_prepare);
        Button btShare = (Button) convertView.findViewById(R.id.bt_lv_lhis_share);
        Button btReset = (Button) convertView.findViewById(R.id.bt_lv_lhis_reset);

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        HistoryListItem historyListItem = historyListItems.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        tvLectureName.setText(historyListItem.getLecture());
        btPrepared.setEnabled(!historyListItem.isPrepared());
        if(historyListItem.isPrepared()) {
            btPrepared.setText("완료");
            btPrepared.setEnabled(false);
            btReset.setEnabled(true);
        }
        btShare.setEnabled(historyListItem.isPrepared());

        btPrepared.setOnClickListener(v -> {
            if(btPrepared.getText().equals("시작")){
                // Zip 파일 준비
                Log.d("HistoryListViewAdapter", "Zip 파일 준비");
                String rootDir = historyActivity.getResources().getString(R.string.directory_name_root);
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                File rootFolder = new File(Environment.getExternalStorageDirectory(), rootDir);

                String dirPath = String.format("%s/%s",
                        rootFolder.getAbsolutePath(),
                        historyListItem.getLecture());
                String zipPath = String.format("%s.zip",dirPath);
                boolean success = zipFileAtPath(dirPath, zipPath);
                File zipFile = new File(zipPath);
                // zip 파일을 setZipFile()로 추가
                historyListItem.setZipFile(zipFile);
                
                historyListItem.setPrepared(true);
                btPrepared.setText("완료");
                btPrepared.setEnabled(false);
                btShare.setEnabled(true);
                btReset.setEnabled(true);
            }
        });

        btShare.setOnClickListener(v -> {
            Log.d("HistoryListViewAdapter", "공유 시작");
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("application/zip");
            Uri contentUri = FileProvider.getUriForFile(context,
                    context.getPackageName()+".fileprovider",
                    historyListItem.getZipFile());
            sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            historyActivity.startActivity(Intent.createChooser(sendIntent, "학습내역 공유"));
        });

        btReset.setOnClickListener(v -> {
            Log.d("HistoryListViewAdapter", "공유파일 삭제(초기화)");
            File zipFile = historyListItem.getZipFile();
            if(zipFile.delete()) {
                btReset.setEnabled(false);
                btPrepared.setText("시작");
                btPrepared.setEnabled(true);
            }
        });

        return convertView;
    }

    /*
     *
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     */

    public boolean zipFileAtPath(String sourcePath, String toLocation) {
        final int BUFFER = 2048;

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(toLocation);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                entry.setTime(sourceFile.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /*
     *
     * Zips a subfolder
     *
     */

    private void zipSubFolder(ZipOutputStream out, File folder,
                              int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    /*
     * gets the last path component
     *
     * Example: getLastPathComponent("downloads/example/fileToZip");
     * Result: "fileToZip"
     */
    public String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }
}
