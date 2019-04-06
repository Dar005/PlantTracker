package com.c00098391.planttracker;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
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

import com.google.android.gms.flags.Flag;

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

    ConstraintLayout cl;

    String username;
    String userId;
    String lat = "";
    String lon = "";
    String weather = "";
    String rep;
    String treatment;
    String expt;
    String expId;

    int originalImageHeight = 1280;
    int originalImageWidet = 720;

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
        expId = getIntent().getStringExtra("expid");

        final byte[] byteArray = getIntent().getByteArrayExtra("image");
        final Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imgView.setImageBitmap(bm);

        int IVwidth = imgView.getWidth();
        int IVHeight = imgView.getHeight();

        final float HeightRatio = (float)bm.getHeight() / (float)IVHeight;
        final float WidthRatio = (float)bm.getWidth() / (float)IVwidth;

        final int h = (int) HeightRatio;
        final int w = (int) WidthRatio;

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        float density  = getResources().getDisplayMetrics().density;
        final float screenWidth = dm.widthPixels / density;
        final float screenHeight = dm.heightPixels / density;

        Mat mat = new Mat();
        Utils.bitmapToMat(bm, mat);
        final Mat hsvMat = mat;

        Toast.makeText(DiseaseAnalysis.this, "width x height" + screenWidth + ", "
                + screenHeight, Toast.LENGTH_LONG).show();


        imgView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                int action = motionEvent.getAction();
                int x = (int) motionEvent.getX();
                int y = (int) motionEvent.getY();


                float xRatio = (float)bm.getWidth() / imgView.getWidth();
                float xPos = motionEvent.getX() * xRatio;
                float yRatio = (float)bm.getHeight() / imgView.getHeight();
                float yPos = motionEvent.getY() * yRatio;

                int xP = (int) xPos;
                int yP = (int) yPos;
//                int XonImage = x * w;
//                int YonImage = y * h;


           //     switch(action){
              //      case MotionEvent.ACTION_DOWN:
                      //  Toast.makeText(DiseaseAnalysis.this,
                        //        "ACTION_DOWN " + x + " : " + y, Toast.LENGTH_LONG).show();
              //          break;

//                    case MotionEvent.ACTION_MOVE:
//                        Toast.makeText(DiseaseAnalysis.this,
//                                "ACTION_MOVE " + x + " : " + y, Toast.LENGTH_LONG).show();
//                        break;
//
//                    case MotionEvent.ACTION_UP:
//                        Toast.makeText(DiseaseAnalysis.this,
//                                "ACTION_UP " + x + " : " + y, Toast.LENGTH_LONG).show();
//                        break;
     //           }

                int pixel = bm.getPixel(xP, yP);

                int redValue = Color.red(pixel);
                int blueValue = Color.blue(pixel);
                int greenValue = Color.green(pixel);

                int red;
                int green;
                int blue;

                int totalRed = 0;
                int totalGreen = 0;
                int totalBlue = 0;

                for (int i = yP-4; i <= yP + 4; i++){

                    for (int k = xP - 4; k <= xP + 4; k ++){

                        int f = bm.getPixel(i, k);
                        red = Color.red(f);
                        green = Color.green(f);
                        blue = Color.blue(f);

                        totalRed += red;
                        totalGreen += green;
                        totalBlue += blue;
                    }
                }

                int finalRed = totalRed / 81;
                int finalGreen = totalGreen / 81;
                int finalBlue = totalBlue / 81;

                //color

//                int[] viewCoords = new int[2];
//                imgView.getLocationOnScreen(viewCoords);


//                float x =   motionEvent.getX();
//                float y =  motionEvent.getY();
//
//                float XonImage = x * w;
//                float YonImage = y * h;
//
//                int finalX = (int) XonImage;
//                int finalY = (int) YonImage;

               // bitmap = imgView.getDrawingCache();

                //bitmap = bmp;
//                int pixel = bm.getPixel(finalX, finalY);
//
//                int touchX = (int) motionEvent.getX();
//                int touchY = (int) motionEvent.getY();
//
//                int imageX = touchX - viewCoords[0];
//                int imageY = touchY - viewCoords[1];
//
//                int p = bm.getPixel(imageX, imageY);

//                Log.v("X-COORD", imageX+"");
//                Log.v("Y-COORD", imageY+"");


//                int redValue = Color.red(pixel);
//                int blueValue = Color.blue(pixel);
//                int greenValue = Color.green(pixel);


                /**
                 * Get hsv double array of pixel at position of on touch
                 */

                Size sz = new Size(400 ,400);
                Imgproc.resize(hsvMat, hsvMat, sz);
                Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_RGB2BGR);
                Imgproc.cvtColor(hsvMat, hsvMat, Imgproc.COLOR_BGR2HLS);




                // array of the color for onTouch
                int [] hsl = new int[3];

                // Get hsl value from rgb values
                rgb2hsl(finalRed, finalGreen, finalBlue, hsl);
               // Color.RGBToHSV(5, 68, 15, hsv);

                Toast.makeText(DiseaseAnalysis.this, "RGB: " + redValue +
                                "," + greenValue +
                                ", " + blueValue+
                                "HSL = " + hsl[0] + ", " + hsl[1] + ", " + hsl[2],
                        Toast.LENGTH_LONG).show();




               // Toast.makeText(DiseaseAnalysis.this, "White Count = " + whiteCount +
//                        "\nGreen Count = " + greenCount + "\nRed Count = " + redCount +
//                        "\n Unknown = " + unknownCount, Toast.LENGTH_LONG).show();

//
                HashMap<String, Integer> colorList = new HashMap<String, Integer>();

                colorList.put("bad", 0);
//
                int hsvHeight = hsvMat.rows();
                int hsvWidth = hsvMat.cols();
                int total = hsvHeight * hsvWidth;

//                double [] onTouch = hsvMat.get(imageX, imageY);
//


                String color = "";
                int colorCount = 0;
                double H = hsl[0] / 2.0;

                double lowerH = H - 18.0;
                double upperH = H + 18.0;
                double lowerS = hsl[1] - 10.0;
                double upperS = hsl[1] + 10.0;
                double lowerL = hsl[2] - 10.0;
                double upperL = hsl[2] + 10.0;


                for (int i = 0; i < hsvHeight; i++){

                    for (int k = 0; k < hsvWidth; k++) {



                      double [] hslMat = hsvMat.get(i, k);

                        double h = hslMat[0]; // open cv uses 180 not 360 for memory reasons
                        double s = hslMat[1];
                        double l = hslMat[2];



                        if ((l >= 90) && (l <= 100)) {
                            color = "white";
                        }
                        else if ((h >= lowerH && h <= upperH) && (s >= lowerS && s <= upperS) && (l >= lowerL && l <= upperL)) {
                            color = "bad";
                        }

                        /**
                         * Have white and bad ....
                         */
                        if (colorList.containsKey(color)) {
                            colorCount = colorList.get(color);
                            colorCount++;
                            colorList.put(color, colorCount);

                        } else {
                            colorCount = 1;
                            colorList.put(color, colorCount);
                        }

                    }
                }

                        int whiteCount = colorList.get("white");
                        int badCount = colorList.get("bad");
                        double leaf = total - whiteCount;
                        double disease = (badCount / leaf) * 100.0;



                        Toast.makeText(DiseaseAnalysis.this, "White Count = " + whiteCount +
                         "\nBad Count = " + badCount + "\nleaf Count = " + leaf +
                            "\n disease = " + disease, Toast.LENGTH_LONG).show();


                        String analysis = String.format("%.2f", disease);
//
////                        else if(h <= touchHupper && h >= touchHlower
////                                && l <= touchLupper && l >= touchLlower
////                                && s <= touchSupper && s >= touchSlower) {
////                            color = "good";
////                        }
//
//                        if (l > 94){
//                            color = "white";
//                        }
//                        else if(h >= 80.0 && s >= 21.0 && l >= 20.0
//                                && h <= 140.0 && s <= 100.0 && l <= 75.0){
//                            color = "green";
//                        }
//
//                        else if (h >= 0.0 && s <= 100.0 && l >= 27.0
//                                && h >= 0.0 && s >= 100.0 && l >= 50.0){
//                            color = "red";
//                        }
//
//                        else{
//                            color = "unknown";
//                        }
//
//                        if (colorList.containsKey(color)){
//                            colorCount = colorList.get(color);
//                            colorCount++;
//                            colorList.put(color, colorCount);
//
//                        }else{
//                            colorCount = 1;
//                            colorList.put(color, colorCount);
//                        }
//                    }
//                }


//                Integer whiteCount = colorList.get("white");
//                Integer greenCount = colorList.get("green");
//                Integer unknownCount = colorList.get("unknown");
//                Integer redCount = colorList.get("red");

//                Toast.makeText(DiseaseAnalysis.this, "White Count = " + whiteCount +
//                        "\nGreen Count = " + greenCount + "\nRed Count = " + redCount +
//                        "\n Unknown = " + unknownCount, Toast.LENGTH_LONG).show();

//                Integer all = (greenCount + redCount + unknownCount);
//                Integer analysis = greenCount * (100/all);

//                tvAnalysis.setVisibility(View.VISIBLE);
//                btnEndExperiment.setVisibility(View.VISIBLE);
//                btnUploadAnalysis.setVisibility(View.VISIBLE);
//
//                tvAnalysis.setText(analysis + "%");

              //  String a = String.valueOf(analysis);

               // String b = "18%";




                Intent intent = new Intent(DiseaseAnalysis.this,
                        com.c00098391.planttracker.DiseaseResult.class);
                intent.putExtra("disease", analysis);
                intent.putExtra("image", byteArray);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("weather", weather);
                intent.putExtra("username", username);
                intent.putExtra("userid", userId);
                intent.putExtra("rep", rep);
                intent.putExtra("treatment", treatment);
                intent.putExtra("expt", expt);
                intent.putExtra("expid", expId);
                startActivity(intent);


                return false;

                // finish();

            }
        });

    }

//    private int getProjectedColor(ImageView iv, Bitmap bm, int x, int y) {
//        if (x < 0 || y < 0 || x > iv.getWidth() || y > iv.getHeight()) {
//            //outside ImageView
//            return Color.BLACK;
//        } else {
//            int projectedX = (int) ((double) x * ((double) bm.getWidth() / (double) iv.getWidth()));
//            int projectedY = (int) ((double) y * ((double) bm.getHeight() / (double) iv.getHeight()));
//
//            Toast.makeText(DiseaseAnalysis.this, x + ":" + y + "/" + iv.getWidth() + " : " + iv.getHeight() + "\n" +
//                            projectedX + " : " + projectedY + "/" + bm.getWidth() + " : " + bm.getHeight(),
//                    Toast.LENGTH_LONG).show();
//
//            return bm.getPixel(projectedX, projectedY);
//        }
//    }

    private void rgb2hsl(int r, int g, int b, int hsl[]) {

        float var_R = ( r / 255f );
        float var_G = ( g / 255f );
        float var_B = ( b / 255f );

        float var_Min;    //Min. value of RGB
        float var_Max;    //Max. value of RGB
        float del_Max;    //Delta RGB value

        if (var_R > var_G)
        { var_Min = var_G; var_Max = var_R; }
        else
        { var_Min = var_R; var_Max = var_G; }

        if (var_B > var_Max) var_Max = var_B;
        if (var_B < var_Min) var_Min = var_B;

        del_Max = var_Max - var_Min;

        float H = 0, S, L;
        L = ( var_Max + var_Min ) / 2f;

        if ( del_Max == 0 ) { H = 0; S = 0; } // gray
        else {                                //Chroma
            if ( L < 0.5 )
                S = del_Max / ( var_Max + var_Min );
            else
                S = del_Max / ( 2 - var_Max - var_Min );

            float del_R = ( ( ( var_Max - var_R ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
            float del_G = ( ( ( var_Max - var_G ) / 6f ) + ( del_Max / 2f ) ) / del_Max;
            float del_B = ( ( ( var_Max - var_B ) / 6f ) + ( del_Max / 2f ) ) / del_Max;

            if ( var_R == var_Max )
                H = del_B - del_G;
            else if ( var_G == var_Max )
                H = ( 1 / 3f ) + del_R - del_B;
            else if ( var_B == var_Max )
                H = ( 2 / 3f ) + del_G - del_R;
            if ( H < 0 ) H += 1;
            if ( H > 1 ) H -= 1;
        }
        hsl[0] = (int)(360*H);
        hsl[1] = (int)(S*100);
        hsl[2] = (int)(L*100);
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
