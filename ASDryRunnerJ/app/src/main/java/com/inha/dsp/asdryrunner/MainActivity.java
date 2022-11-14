package com.inha.dsp.asdryrunner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
    }

    private void initUI() {
        Button btStart = findViewById(R.id.btMainStart);
        btStart.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LearnActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        Button btHistory = findViewById(R.id.btMainHistory);
        btHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        Button btAchievement = findViewById(R.id.btMainAchievement);
        btAchievement.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AchievementActivity.class);
            startActivity(intent);
        });
    }
}