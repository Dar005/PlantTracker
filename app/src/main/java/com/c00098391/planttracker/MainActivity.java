package com.c00098391.planttracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button btnStartExp, btnContinue, btnPreviousAnalysis;

    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";
    ArrayList<String> exps = new ArrayList<>();
    ArrayList<String> expIds = new ArrayList<>();


    String userId;
    String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStartExp = findViewById(R.id.btnStartExp);
        btnContinue = findViewById(R.id.btnContinue);
        btnPreviousAnalysis = findViewById(R.id.btnPreviousAnalysis);



        final String username = getIntent().getStringExtra("username");

        String[] user = new String[1];
        user[0] = username;

        GetUserExperimentData userData = new GetUserExperimentData();
        userData.execute(user);


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
                intent.putExtra("userid", userId);
                startActivity(intent);
            }
        });

        // Button should allow the user to add to an already ongoing experiment
        // When selected the user should be given a list of experiments they have
        // contributed to which holds all the information fot the experiment i.e. the
        // information captured in the Capture Text stage.
        // So this button will move a user to a new activity to allow them to select what
        // the information they want to use

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,
                        com.c00098391.planttracker.ContinueExperiment.class);
                intent.putExtra("username", username);
                intent.putExtra("exps", exps);
                intent.putExtra("userid", userId);
                intent.putExtra("expids", expIds);
                startActivity(intent);
            }
        });

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


    // Async task for sending data i.e. image date and time....
    public class GetUserExperimentData extends AsyncTask<String, Void, JSONObject> {


        @Override
        protected JSONObject doInBackground(String... args){

            try{
                URL url = new URL("http://www.c0009839.candept.com/API/getExperimentDetails.php");


                // put params in a JSON Object
                JSONObject dataParams = new JSONObject();
                dataParams.put("username", args[0]);

                // Set up connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                //send data
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

                    String dataFound = result.getString("message");
                    if (dataFound.equals("Data found")){
                        //Toast.makeText(getApplicationContext(), result.getString(
                             //   "message"), Toast.LENGTH_LONG).show();
                        String exp = result.getString("exp");

                        userId = result.getString("userid");
                        username = result.getString("username");

                        Toast.makeText(getApplicationContext(), userId, Toast.LENGTH_LONG).show();

                        JSONArray jsonArray = result.getJSONArray("exp");

                        String test ="";
                        Map<String, String> list = new HashMap<>();
                        for (int i = 0; i <jsonArray.length();i++){
                            String rep = jsonArray.getJSONObject(i).getString("replicant");
                            String expt = jsonArray.getJSONObject(i).getString("expt");
                            String treatment = jsonArray.getJSONObject(i).getString("treatment");
                            String expId = jsonArray.getJSONObject(i).getString("id");
                            // list.put(""+type ," "+value);
                            //all.put(""+type ," "+value);
                            exps.add(rep +  " " + treatment + " " + expt);
                            expIds.add(expId);
                        }


//                        if (jsonArray != null){
//                            for (int i = 0; i <jsonArray.length();i++){
//
//                                String d = jsonArray.getString(i).replace
//                                // exps.add(jsonArray.getString(i).replaceAll("[^a-zA-Z0-9:,]", " "));
//                            }
//                        }else{
//                            exps.add("NOT WORKING");
//                        }

                        // tvJson.setText(list.toString());

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


    // Static block to load the OpenCv lib...
    static {
        System.loadLibrary("opencv_java3");
        // System.loadLibrary("jniLibs");
    }
}
