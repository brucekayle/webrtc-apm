package com.bk.webrtc;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Apm apm = new Apm(false, true, true, false, false, false, false);
            apm.AECMSetSuppressionLevel(Apm.AECM_RoutingMode.LoudSpeakerphone);
            apm.AECM(true);
            apm.NSSetLevel(Apm.NS_Level.High);
            apm.NS(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}