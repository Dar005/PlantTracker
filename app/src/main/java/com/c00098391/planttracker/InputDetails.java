package com.c00098391.planttracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class InputDetails extends AppCompatActivity {

    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";

    Button btnCreateExp;

    EditText etRep, etTreat, etExpt;

    String rep;
    String treatment;
    String expt;

    String userId;
    String username;
    String time;
    String date;

    // Location variables
    String lat = "";
    String lon = "";

    // Weather variables
    private static final String APP_ID = "b11dc521fd3aecc6374e2e331dc090e3";
    String weather = "";
    String units = "metric";
    String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_details);

        // Start Location service and get lat and lon
        startService(new Intent(InputDetails.this,
                com.c00098391.planttracker.GPS.class));
        final GPS gps = new GPS(InputDetails.this);
        lat = Double.toString(gps.getLatitude());
        lon = Double.toString(gps.getLongitude());
        url = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units="+units+"&appid="+APP_ID;

        btnCreateExp = findViewById(R.id.btnCreateExp);
        etRep = findViewById(R.id.etRep);
        etTreat = findViewById(R.id.etTreat);
        etExpt = findViewById(R.id.etExpt);

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        date = getIntent().getStringExtra("date");
        time = getIntent().getStringExtra("time");
        username = getIntent().getStringExtra("username");
        userId = getIntent().getStringExtra("userid");

        String weatherData = null;
        try {
            weatherData = new GetWeatherTask(weather).execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        String encodedImg = Base64.encodeToString(byteArray, Base64.DEFAULT);

        final String [] expDetails = new String[11];
        expDetails[0] = date;
        expDetails[1] = time;
        expDetails[2] = encodedImg;
        expDetails[3] = username;
        expDetails[4] = userId;
        expDetails[5] = weatherData;
        expDetails[6] = lat;
        expDetails[7] = lon;



        btnCreateExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                rep = etRep.getText().toString();
                expt = etExpt.getText().toString();
                treatment = etTreat.getText().toString();

                expDetails[8] = rep;
                expDetails[9] = expt;
                expDetails[10] = treatment;

                CreateExperiment ce = new CreateExperiment();
                ce.execute(expDetails);

            }
        });
    }

    // Async task for sending data i.e. image date and time....
    public class CreateExperiment extends AsyncTask<String, Void, JSONObject> {


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

                        //String username = result.getString("username");
                       // String userId = result.getString("userid");
                        String expId = result.getString("expid");


                        Intent intent = new Intent(InputDetails.this,
                                com.c00098391.planttracker.DetectDisease.class);
                        intent.putExtra("username", username);
                        intent.putExtra("userid", userId);
                        intent.putExtra("rep", rep);
                        intent.putExtra("expt", expt);
                        intent.putExtra("treatment", treatment);
                        intent.putExtra("expid", expId);

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
