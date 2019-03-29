package com.c00098391.planttracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DiseaseResult extends AppCompatActivity {

    ImageView imgView;
    Button btnUploadAnalysis, btnEndExperiment;
    TextView tvAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_result);

        imgView = findViewById(R.id.ivAnalysis);
        btnUploadAnalysis = findViewById(R.id.btnUploadAnalysis);
        btnEndExperiment = findViewById(R.id.btnEndExperiment);
        tvAnalysis = findViewById(R.id.tvAnalysis);

        String analysis = getIntent().getStringExtra("analysis");
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        tvAnalysis.setText(analysis +"%");

        imgView.setImageBitmap(bm);

    }
}
