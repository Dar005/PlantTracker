package com.c00098391.planttracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

public class DiseaseAnalysis extends AppCompatActivity {

    ImageView imgView;
    TextView tvAnalysis;
    Button btnUploadAnalysis, btnEndExperiment;

    String username;
    String userId;
    String lat = "";
    String lon = "";
    String weather = "";
    String rep;
    String treatment;
    String expt;

    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";

  // String exp;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_analysis);

        btnUploadAnalysis = findViewById(R.id.btnUploadAnalysis);
        btnEndExperiment = findViewById(R.id.btnEndExperiment);
        tvAnalysis = findViewById(R.id.tvAnalysis);

        imgView = findViewById(R.id.imgView);

        imgView.setDrawingCacheEnabled(true);
        imgView.buildDrawingCache(true);


        expt = getIntent().getStringExtra("expt");

        username = getIntent().getStringExtra("username");
        userId = getIntent().getStringExtra("userid");
        weather = getIntent().getStringExtra("weather");
        lat = getIntent().getStringExtra("lat");
        lon = getIntent().getStringExtra("lon");
        rep = getIntent().getStringExtra("rep");
        treatment = getIntent().getStringExtra("treatment");
        expt = getIntent().getStringExtra("expt");

        final byte[] byteArray = getIntent().getByteArrayExtra("image");
        final Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imgView.setImageBitmap(bm);



        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        final int screenWidth = dm.widthPixels;
        final int screenHeight = dm.heightPixels;

        Mat mat = new Mat();
        Utils.bitmapToMat(bm, mat);
        final Mat hsvMat = mat;

        Toast.makeText(DiseaseAnalysis.this, "width x height" + screenWidth + ", "
                + screenHeight, Toast.LENGTH_LONG).show();


        imgView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int[] viewCoords = new int[2];
                imgView.getLocationOnScreen(viewCoords);


               // bitmap = imgView.getDrawingCache();

                //bitmap = bmp;
                int pixel = bm.getPixel((int)motionEvent.getX(), (int)motionEvent.getY());

                int touchX = (int) motionEvent.getX();
                int touchY = (int) motionEvent.getY();

                int imageX = touchX - viewCoords[0];
                int imageY = touchY - viewCoords[1];



                Log.v("X-COORD", imageX+"");
                Log.v("Y-COORD", imageY+"");





                /**
                 * Get hsv double array of pixel at position of on touch
                 */

                Size sz = new Size(screenWidth, screenHeight);
                Imgproc.resize(hsvMat, hsvMat, sz);
//                Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_RGB2BGR);
                Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_BGR2HLS);

                HashMap<String, Integer> colorList = new HashMap<String, Integer>();

                int hsvHeight = hsvMat.rows();
                int hsvWidth = hsvMat.cols();
                int total = hsvHeight * hsvWidth;

                double [] onTouch = hsvMat.get(imageX, imageY);



                String color = "";
                int colorCount = 0;

                for (int i = 0; i < hsvHeight; i++){

                    for (int k = 0; k < hsvWidth; k++){




                        double [] hsl = hsvMat.get(i, k);

                        double h = hsl[0];
                        double s = hsl[1];
                        double l = hsl[2];

//                        if ((h >= 65) && (h <= 100)) {
//                            color = "white";
//                        }

//                        else if(h <= touchHupper && h >= touchHlower
//                                && l <= touchLupper && l >= touchLlower
//                                && s <= touchSupper && s >= touchSlower) {
//                            color = "good";
//                        }

                        if (l > 94){
                            color = "white";
                        }
                        else if(h >= 80.0 && s >= 21.0 && l >= 20.0
                                && h <= 140.0 && s <= 100.0 && l <= 75.0){
                            color = "green";
                        }

                        else if (h >= 0.0 && s <= 100.0 && l >= 27.0
                                && h >= 0.0 && s >= 100.0 && l >= 50.0){
                            color = "red";
                        }

                        else{
                            color = "unknown";
                        }

                        if (colorList.containsKey(color)){
                            colorCount = colorList.get(color);
                            colorCount++;
                            colorList.put(color, colorCount);

                        }else{
                            colorCount = 1;
                            colorList.put(color, colorCount);
                        }
                    }
                }


                Integer whiteCount = colorList.get("white");
                Integer greenCount = colorList.get("green");
                Integer unknownCount = colorList.get("unknown");
                Integer redCount = colorList.get("red");

//                Toast.makeText(DiseaseAnalysis.this, "White Count = " + whiteCount +
//                        "\nGreen Count = " + greenCount + "\nRed Count = " + redCount +
//                        "\n Unknown = " + unknownCount, Toast.LENGTH_LONG).show();

                Integer all = (greenCount + redCount + unknownCount);
                Integer analysis = greenCount * (100/all);

//                tvAnalysis.setVisibility(View.VISIBLE);
//                btnEndExperiment.setVisibility(View.VISIBLE);
//                btnUploadAnalysis.setVisibility(View.VISIBLE);
//
//                tvAnalysis.setText(analysis + "%");

                String a = String.valueOf(analysis);

                String b = "18%";




                Intent intent = new Intent(DiseaseAnalysis.this,
                        com.c00098391.planttracker.DiseaseResult.class);
                intent.putExtra("analysis", a);
                intent.putExtra("image", byteArray);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("weather", weather);
                intent.putExtra("username", username);
                intent.putExtra("userid", userId);
                intent.putExtra("rep", rep);
                intent.putExtra("treatment", treatment);
                intent.putExtra("expt", expt);
                startActivity(intent);


                return true;

                // finish();

            }
        });

    }

    // Async task for sending data i.e. image date and time....
    public class UploadData extends AsyncTask<String, Void, JSONObject> {



        @Override
        protected JSONObject doInBackground(String... args){

            try{
                URL url = new URL("http://www.c0009839.candept.com/API/CreateExperiment.php");

                // need user name and user id.....


                // DATE TIME IMAGE
                // put params in a JSON Object
                JSONObject dataParams = new JSONObject();
                dataParams.put("date", args[0]);
                dataParams.put("time", args[1]);
                dataParams.put("image", args[2]);
                dataParams.put("username", args[3]);
                dataParams.put("rep",args[4]);
                dataParams.put("exp", args[5]);

                Log.i("DATAPARAS", dataParams.toString());

                // Set up connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                //send date
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(getPostDateString(dataParams));

                writer.flush();
                writer.close();
                os.close();

                // Get Response
                int responseCode = conn.getResponseCode();
                error = String.valueOf(conn.getResponseCode());

                if (responseCode == HttpURLConnection.HTTP_OK){
                    inputStream = conn.getInputStream();
                    BufferedReader in  = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while(null!= (line = in.readLine())){
                        sb.append(line).append("\n");
                    }
                    in.close();
                    inputStream.close();
                    json = sb.toString();
                    Log.i("API Camera: ", json);
                }
                else{
                    Log.e("Buffer Error", "Error Getting Result " +responseCode);
                }
                try{
                    jObj = new JSONObject(json);
                    jObj.put("error_code", error);
                }catch(JSONException e){
                    Log.e("JSON Parser", "Error Parsing Data " + e.toString());
                }
            }catch(Exception e){
                Log.e("Exception: ", "Overall Try Block " + e.toString());
            }
            return jObj;
        }// end of doInBackground

        @Override
        protected void onPostExecute(JSONObject result){

            try {

                if (result != null){

                    String uploadSuccess = result.getString("message");
                    if (uploadSuccess.equals("Successfully uploaded analysis")){
                        Toast.makeText(getApplicationContext(), result.getString(
                                "message"), Toast.LENGTH_LONG).show();

                        String username = result.getString("username");
                        String userId = result.getString("userid");
                        String expId = result.getString("expid");


                        Intent intent = new Intent(DiseaseAnalysis.this,
                                com.c00098391.planttracker.DetectDisease.class);
                        intent.putExtra("username", username);
                        intent.putExtra("userid", userId);
                        intent.putExtra("expid", expId);
                        startActivity(intent);


                    }else{
                        Toast.makeText(getApplicationContext(), result.getString(
                                "message"), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Unable to retrieve data from the server", Toast.LENGTH_LONG).show();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    // Turn json object to string for post
    public String getPostDateString(JSONObject params) throws Exception{

        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();

        while(itr.hasNext()){
            String key = itr.next();
            Object value = params.get(key);
            if(first){
                first = false;
            }else{
                result.append("&");
            }
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }
}
