package com.c00098391.planttracker;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.AdapterView.OnItemSelectedListener;

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
import java.util.List;
import java.util.Map;

public class ContinueExperiment extends AppCompatActivity {

    Button btnContinue;
    Spinner spnExpList;
    TextView tvJson;
    ListView lvExps;

    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";

    ArrayList<String> exps = new ArrayList<>();
    ArrayList<String> expIds = new ArrayList<>();
    Map<String, String> all = new HashMap<>();
    String username;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_continue_experiment);

        //btnContinue = findViewById(R.id.btnContinue);
        tvJson = findViewById(R.id.tvJson);
        lvExps = findViewById(R.id.lvExps);

        username = getIntent().getStringExtra("username");
        userId = getIntent().getStringExtra("userid");
        exps = getIntent().getStringArrayListExtra("exps");
        expIds = getIntent().getStringArrayListExtra("expids");

        String[] user = new String[1];
        user[0] = username;


        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1
                , exps);
        lvExps.setAdapter(adapter);

        lvExps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String value = adapter.getItem(i);

                String expId = expIds.get(i);

                String[] parts = value.split(" ");
                String rep = parts[0];
                String treatment = parts[1];
                String expt = parts[2] + " " + parts[3];
                //Toast.makeText(getApplicationContext(), value, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(ContinueExperiment.this,
                        com.c00098391.planttracker.DetectDisease.class);
                intent.putExtra("rep", rep);
                intent.putExtra("expt", expt);
                intent.putExtra("treatment", treatment);
                intent.putExtra("username", username);
                intent.putExtra("userid", userId);
                intent.putExtra("expid", expId);
                startActivity(intent);

            }
        });


    } //  end on create


    // Async task for sending data i.e. image date and time....
//    public class UploadData extends AsyncTask<String, Void, JSONObject> {
//
//        public DataResponse response = null;
//
//        @Override
//        protected JSONObject doInBackground(String... args){
//
//            try{
//                URL url = new URL("http://www.c0009839.candept.com/API/getExperimentDetails.php");
//
//
//                // put params in a JSON Object
//                JSONObject dataParams = new JSONObject();
//                dataParams.put("username", args[0]);
//
//                // Set up connection
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setReadTimeout(15000);
//                conn.setConnectTimeout(15000);
//                conn.setRequestMethod("POST");
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//
//                //send data
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(
//                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
//                writer.write(getPostDateString(dataParams));
//
//                writer.flush();
//                writer.close();
//                os.close();
//
//                // Get Response
//                int responseCode = conn.getResponseCode();
//                error = String.valueOf(conn.getResponseCode());
//
//                if (responseCode == HttpURLConnection.HTTP_OK){
//                    inputStream = conn.getInputStream();
//                    BufferedReader in  = new BufferedReader(new InputStreamReader(inputStream));
//                    StringBuilder sb = new StringBuilder();
//                    String line;
//
//                    while(null!= (line = in.readLine())){
//                        sb.append(line).append("\n");
//                    }
//                    in.close();
//                    inputStream.close();
//                    json = sb.toString();
//                    Log.i("API Camera: ", json);
//                }
//                else{
//                    Log.e("Buffer Error", "Error Getting Result " +responseCode);
//                }
//                try{
//                    jObj = new JSONObject(json);
//                    jObj.put("error_code", error);
//                }catch(JSONException e){
//                    Log.e("JSON Parser", "Error Parsing Data " + e.toString());
//                }
//            }catch(Exception e){
//                Log.e("Exception: ", "Overall Try Block " + e.toString());
//            }
//            return jObj;
//        }// end of doInBackground
//
//        @Override
//        protected void onPostExecute(JSONObject result){
//
//            try {
//
//                if (result != null){
//
//                    String dataFound = result.getString("message");
//                    if (dataFound.equals("Data found")){
//                        // use the data that was found....
//                     //   JSONArray jArray = JSONObject.getJSONArray(result);
//                        Toast.makeText(getApplicationContext(), result.getString(
//                                "message"), Toast.LENGTH_LONG).show();
//                        // response is a json object
//                        //response.processFinish(result);
//                        String exp = result.getString("exp");
//                       // Toast.makeText(getApplicationContext(), exp, Toast.LENGTH_LONG).show();
//
//
//
//                        //JSONArray results = result.getJSONArray("exp");
//                        //Iterator<String> x = results.keys();
//                        JSONArray jsonArray = result.getJSONArray("exp");
//
////                        while(x.hasNext()){
////                            String key = x.next();
////                            jsonArray.put(results.get(key));
////                        }
//
//                        String test ="";
//                        Map<String, String> list = new HashMap<>();
//                        for (int i = 0; i <jsonArray.length();i++){
//
//                            String type = jsonArray.getJSONObject(i).getString("expname");
//                            String value = jsonArray.getJSONObject(i).getString("id");
//                           // list.put(""+type ," "+value);
//                            //all.put(""+type ," "+value);
//                            exps.add(type);
//                        }
////                         if (list.size() > 0){
////                             Toast.makeText(getApplicationContext(), "List not empty", Toast.LENGTH_LONG).show();
////                         }else{
////                             Toast.makeText(getApplicationContext(), "FUCK THIS", Toast.LENGTH_LONG).show();
////                         }
//
//
//
//
//                        if (jsonArray != null){
//                            for (int i = 0; i <jsonArray.length();i++){
//
////                                String d = jsonArray.getString(i).replace
//                               // exps.add(jsonArray.getString(i).replaceAll("[^a-zA-Z0-9:,]", " "));
//                            }
//                        }else{
//                           exps.add("NOT WORKING");
//                        }
//
//                       // tvJson.setText(list.toString());
//
//                    }else{
//                        Toast.makeText(getApplicationContext(), result.getString(
//                                "message"), Toast.LENGTH_LONG).show();
//                    }
//                }else{
//                    Toast.makeText(getApplicationContext(),
//                            "Unable to retrieve data from the server", Toast.LENGTH_LONG).show();
//                }
//            }catch(JSONException e){
//                e.printStackTrace();
//            }
//        }
//    }
//
//    // Turn json object to string for post
//    public String getPostDateString(JSONObject params) throws Exception{
//
//        StringBuilder result = new StringBuilder();
//        boolean first = true;
//        Iterator<String> itr = params.keys();
//
//        while(itr.hasNext()){
//            String key = itr.next();
//            Object value = params.get(key);
//            if(first){
//                first = false;
//            }else{
//                result.append("&");
//            }
//            result.append(URLEncoder.encode(key, "UTF-8"));
//            result.append("=");
//            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
//        }
//        return result.toString();
//    }
}
