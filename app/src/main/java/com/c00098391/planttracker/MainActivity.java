package com.c00098391.planttracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button btnStartExp, btnDetect, btnPreviousAnalysis;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartExp = findViewById(R.id.btnStartExp);
        btnDetect = findViewById(R.id.btnDetect);
        btnPreviousAnalysis = findViewById(R.id.btnPreviousAnalysis);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        final String username = bundle.getString("username");


        /**
         Need an activity that captures an image and allows the user to start an experiment the
         user the should be able to capture an image of a exp label and then be moved to the
         detection stage...
          */
        btnStartExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,
                        com.c00098391.planttracker.CaptureText.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        // Need to add an activity to allow user to detect individual disease
        // i.e. just check one leaf and no uplload...
//        btnDetect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this,
//                        com.c00098391.planttracker.DetectDisease.class);
//                intent.putExtra("username", username);
//                startActivity(intent);
//            }
//        });

        // Need to build avtivity to view chart based on analysis up to the current date...
//        btnPreviousAnalysis.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this,
//                        com.c00098391.planttracker.PreviousAnalysis.class);
//                intent.putExtra("username", username);
//                startActivity(intent);
//            }
//        });
    }



    // Static block to load the OpenCv lib...
    static {
        System.loadLibrary("opencv_java3");
        // System.loadLibrary("jniLibs");
    }
}
