package com.c00098391.planttracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TextResults extends AppCompatActivity {

    ImageView ivText;
    TextView tvText;
    Button btnShowText, btnInputdetailsMaually;
    String data;

    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";

    String username;
    String userId;
    String rep;
    String treatment;
    String expt;


    // Location variables
    String lat = "";
    String lon = "";

    // Weather variables
    private static final String APP_ID = "b11dc521fd3aecc6374e2e331dc090e3";
   // String weather = "";
    String units = "metric";
    String url;
    String weather = "";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_results);

        ivText = findViewById(R.id.ivText);
        tvText = findViewById(R.id.tvText);
        btnShowText = findViewById(R.id.btnShowText);
        btnInputdetailsMaually = findViewById(R.id.btnInputDetailsMaually);

        // Start Location service and get lat and lon
        startService(new Intent(TextResults.this,
                com.c00098391.planttracker.GPS.class));
        final GPS gps = new GPS(TextResults.this);
        lat = Double.toString(gps.getLatitude());
        lon = Double.toString(gps.getLongitude());

        url = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units="+units+"&appid="+APP_ID;

        username = getIntent().getStringExtra("username");
        userId = getIntent().getStringExtra("userid");

        final byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        runTextRecognition(bm);

        String encodedImg = Base64.encodeToString(byteArray, Base64.DEFAULT);



        // Set format for time and date
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss");

        // Create strings for time and date
        final String date = df.format(Calendar.getInstance().getTime());
        final String time = tf.format(Calendar.getInstance().getTime());

        String weatherData = null;
        try {
            weatherData = new TextResults.GetWeatherTask(weather).execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        // weather = weatherData;

        final String [] expDetails = new String[11];
        expDetails[0] = date;
        expDetails[1] = time;
        expDetails[2] = encodedImg;
        expDetails[3] = username;
        expDetails[4] = userId;
        expDetails[5] = weatherData;
        expDetails[6] = lat;
        expDetails[7] = lon;

        ivText.setImageBitmap(bm);

        btnShowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String textResults = getTextData();
               // Log.i("STRING", "ONE :" + tvText.getText().toString());
                //String [] split = textResults.split("\\s+");
                //String one = split[0];
               // Log.i("STRING", "ONE :" + split[0]);
              //  String rep = split[2];
               // String exp = split[4] + " " + split[5];

              //  String text = tvText.getText().toString();
                String [] parts = textResults.split(" ");
                rep = parts[1];
                treatment = parts[2];
                expt = parts[4] +" " + parts[5];

                expDetails[8] = rep;
                expDetails[9] = expt;
                expDetails[10] = treatment;

                UploadData ud = new UploadData();
                ud.execute(expDetails);
            }
        });

        btnInputdetailsMaually.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TextResults.this, com.c00098391.planttracker.InputDetails.class);
                intent.putExtra("image", byteArray);
                intent.putExtra("date", date);
                intent.putExtra("time", time);
                intent.putExtra("username", username);
                intent.putExtra("userid", userId);
                intent.putExtra("weather", weather);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);

                startActivity(intent);
            }
        });



    } // end of on create

    public void setTextData(String data){
        this.data = data;
    }
    public String getTextData(){
        return this.data;
    }


    private void runTextRecognition(Bitmap b){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(b);
        FirebaseVisionTextRecognizer recognizer = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();
        btnShowText.setEnabled(false);
        recognizer.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText texts) {
                        btnShowText.setEnabled(true);
                        processTextRecognitionResult(texts);

                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                btnShowText.setEnabled(true);
                                e.printStackTrace();
                            }
                        }
                );
    }

    private void processTextRecognitionResult(FirebaseVisionText texts){
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if(blocks.size() == 0){
            Toast.makeText(TextResults.this, "No text found", Toast.LENGTH_LONG).show();
            //return "";
        }

        StringBuilder sb = new StringBuilder();
        Boolean first = true;
        String s = "";

        for (int i = 0; i < blocks.size(); i++){
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++){
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
//                if (first){
//                    first = false;
//                }else {
//                    sb.append(" ");
//
//                }

                for (int k = 0; k< elements.size(); k++){
                    //  Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    // mGraphicOverlay.add(textGraphic);

                    sb.append(elements.get(k).getText()+ " ");

                    s = s + elements.get(k).getText() + " ";
                }
            }
        }

        tvText.setText(s);

        setTextData(s);
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
                dataParams.put("userid", args[4]);
                dataParams.put("weather", args[5]);
                dataParams.put("lat", args[6]);
                dataParams.put("lon", args[7]);
                dataParams.put("rep",args[8]);
                dataParams.put("expt", args[9]);
                dataParams.put("treatment", args[10]);


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
                    if (uploadSuccess.equals("Successfully created experiment")){
                        Toast.makeText(getApplicationContext(), result.getString(
                                "message"), Toast.LENGTH_LONG).show();

                       // String username = result.getString("username");
                      //  String userId = result.getString("userid");
                      //  String expId = result.getString("expid");
                     //   String rep = result.getString("rep");
                     //   String treatment = result.getString("treatment");
                      //  String expt = result.getString("expt");


                        Intent intent = new Intent(TextResults.this,
                                com.c00098391.planttracker.DetectDisease.class);
                        intent.putExtra("username", username);
                        intent.putExtra("userid", userId);
                      //  intent.putExtra("expid", expId);
                        intent.putExtra("rep", rep);
                        intent.putExtra("treatment", treatment);
                        intent.putExtra("expt", expt);
                        startActivity(intent);



                        //Intent intent = new Intent(TextResults.this,
                        //        com.c00098391.planttracker.DetectDisease.class);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                       // getApplicationContext().startActivity(intent);
                        //intent.putExtra("username", username);
                        //intent.putExtra("userid", userId);
                        //intent.putExtra("expid", expId);
                        //startActivity(intent);


                    }else{
                        Toast.makeText(getApplicationContext(),
                                "error", Toast.LENGTH_LONG).show();
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

    @SuppressLint("StaticFieldLeak")
    private class GetWeatherTask extends AsyncTask<String, Void, String> {

        private String weather;

        public GetWeatherTask(String weather){
            this.weather = weather;
        }

        @Override
        protected  String doInBackground(String... strings){
            String weather = "UNDEFINED";

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null){
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                JSONObject main = topLevel.getJSONObject("main");
                String temp = String.valueOf(main.getDouble("temp"));

                String overview = topLevel.getJSONArray("weather")
                        .getJSONObject(0).get("main").toString();
                String desc = topLevel.getJSONArray("weather")
                        .getJSONObject(0).get("description").toString();

                weather = temp + "C, " + overview + "(" + desc + ")";

                urlConnection.disconnect();
            }catch (IOException | JSONException e){
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(String temp) {
            weather = "Current Weather " + temp;
        }
    }

}
