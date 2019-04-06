package com.c00098391.planttracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class DiseaseResult extends AppCompatActivity {

    ImageView imgView;
    Button btnUploadAnalysis, btnEndExperiment;
    TextView tvAnalysis;


    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";

    /*
              intent.putExtra("analysis", a);
                intent.putExtra("image", byteArray);
                intent.putExtra("lat", lat);
                intent.putExtra("lon", lon);
                intent.putExtra("weather", weather);
                intent.putExtra("username", username);
                intent.putExtra("userid", userId);
                intent.putExtra("rep", rep);
                intent.putExtra("treatment", treatment);
                intent.putExtra("expt", expt);*/

    String username;
    String userId;
    String rep;
    String treatment;
    String expt;
    String expId;

    // Location variables
    String lat = "";
    String lon = "";

    // Weather variables
   // private static final String APP_ID = "b11dc521fd3aecc6374e2e331dc090e3";
    String weather = "";
   // String units = "metric";
    //String url = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon
     //       +"&units="+units+"&appid="+APP_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_result);

        imgView = findViewById(R.id.ivAnalysis);
        btnUploadAnalysis = findViewById(R.id.btnUploadAnalysis);
        btnEndExperiment = findViewById(R.id.btnEndExperiment);
        tvAnalysis = findViewById(R.id.tvAnalysis);

//        startService(new Intent(DiseaseResult.this,
//                com.c00098391.planttracker.GPS.class));
//        final GPS gps = new GPS(DiseaseResult.this);




        //String analysis = getIntent().getStringExtra("analysis");

        String analysis = "18";
        lat = getIntent().getStringExtra("lat");
        lon = getIntent().getStringExtra("lon");
        weather = getIntent().getStringExtra("weather");
        username = getIntent().getStringExtra("username");
        userId = getIntent().getStringExtra("userid");
        rep = getIntent().getStringExtra("rep");
        treatment = getIntent().getStringExtra("treatment");
        expt = getIntent().getStringExtra("expt");
        expId = getIntent().getStringExtra("expid");

        final byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        tvAnalysis.setText(analysis);

        imgView.setImageBitmap(bm);
//
//        lat = Double.toString(gps.getLatitude());
//        lon = Double.toString(gps.getLongitude());


        // Base64 encode image
        String encodedImg = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Set format for time and date
        final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        final SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss");

        // Create strings for time and date
        String date = df.format(Calendar.getInstance().getTime());
        String time = tf.format(Calendar.getInstance().getTime());

        final String [] analysisDetails = new String[13];
        analysisDetails[0] = date;
        analysisDetails[1] = time;
        analysisDetails[2] = encodedImg;
        analysisDetails[3] = lat;
        analysisDetails[4] = lon;
        analysisDetails[5] = weather;
        analysisDetails[6] = analysis;
        analysisDetails[7] = userId;
        analysisDetails[8] = username;
        analysisDetails[9] = rep;
        analysisDetails[10] = treatment;
        analysisDetails[11] = expt;
        analysisDetails[12] = expId;

        //final String [] up = imageDetails.clone();


        btnUploadAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                UploadData ud = new UploadData();
                ud.execute(analysisDetails);

                // create intent to move back to Detect Disease

            }
        });

    }


    // Async task for sending data i.e. image date and time....
    public class UploadData extends AsyncTask<String, Void, JSONObject> {


        @Override
        protected JSONObject doInBackground(String... args){

            try{
                URL url = new URL("http://www.c0009839.candept.com/API/AnalysisUpload.php");


                // DATE TIME IMAGE
                // put params in a JSON Object
                JSONObject dataParams = new JSONObject();
                dataParams.put("date", args[0]);
                dataParams.put("time", args[1]);
                dataParams.put("image", args[2]);
                dataParams.put("lat", args[3]);
                dataParams.put("lon", args[4]);
                dataParams.put("weather", args[5]);
                dataParams.put("analysis", args[6]);
                dataParams.put("userid", args[7]);
                dataParams.put("username", args[8]);
                dataParams.put("rep", args[9]);
                dataParams.put("treatment", args[10]);
                dataParams.put("expt", args[11]);
                dataParams.put("expid", args[12]);



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

                        Intent intent = new Intent(DiseaseResult.this,
                                com.c00098391.planttracker.DetectDisease.class);
                        intent.putExtra("username", username);
                        intent.putExtra("userid", userId);
                        intent.putExtra("rep", rep);
                        intent.putExtra("expt", expt);
                        intent.putExtra("treatment", treatment);
                        intent.putExtra("expid", expId);

                        startActivity(intent);

                    }else{
                        Toast.makeText(getApplicationContext(), result.getString(
                                "message"), Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Unable upload results", Toast.LENGTH_LONG).show();
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

    private class GetWeatherTask extends AsyncTask<String, Void, String>{

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
