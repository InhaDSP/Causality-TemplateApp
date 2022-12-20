package com.inha.dsp.asdryrunner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.FrameLayout;

public class LearnActivity extends AppCompatActivity {
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        initFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(currentFragment != null) {
            if(currentFragment instanceof LectureType3Fragment) {
                ((LectureType3Fragment)currentFragment).pauseAudio();
            } else if(currentFragment instanceof  LectureType1Fragment) {
                ((LectureType1Fragment)currentFragment).pauseAudio();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentFragment != null) {
            if(currentFragment instanceof LectureType3Fragment) {
                ((LectureType3Fragment)currentFragment).resumeAudio();
            } else if(currentFragment instanceof  LectureType1Fragment) {
                ((LectureType1Fragment)currentFragment).resumeAudio();
            }
        }
    }

    private void initFragment() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        currentFragment = loginFragment;
        fragmentTransaction.add(R.id.fragment_container, loginFragment);
        fragmentTransaction.commit();
    }

    public void replaceFragment(Fragment fragment) {
        currentFragment = fragment;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
    }

}