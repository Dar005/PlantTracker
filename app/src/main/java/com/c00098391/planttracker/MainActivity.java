package com.c00098391.planttracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }



    // Static block to load the OpenCv lib...
    static {
        System.loadLibrary("opencv_java3");
        // System.loadLibrary("jniLibs");
    }
}
