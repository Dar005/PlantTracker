package com.c00098391.planttracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Iterator;

public class LoginActivity extends AppCompatActivity {

    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";
    Button btnSignIn;
    EditText etName, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Edit Texts
        etName = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        // Buttons
        btnSignIn = findViewById(R.id.btnSignIn);

        // onClick listener
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (TextUtils.isEmpty(etName.getText()) && TextUtils.isEmpty(etPassword.getText())){
                    Toast.makeText(getApplicationContext(),
                            "Please enter a username and password", Toast.LENGTH_LONG).show();
                }
                else if (TextUtils.isEmpty((etName.getText()))){
                    Toast.makeText(getApplicationContext(),
                            "Please enter a username", Toast.LENGTH_LONG).show();
                }
                else if (TextUtils.isEmpty((etPassword.getText()))){
                    Toast.makeText(getApplicationContext(),
                            "Please enter a password", Toast.LENGTH_LONG).show();
                }else {
                    String[] loginInfo = new String[2];
                    loginInfo[0] = etName.getText().toString();
                    loginInfo[1] = etPassword.getText().toString();
                    LoginRequest lr = new LoginRequest();
                    lr.execute(loginInfo);
                }

            }
        });// sign in on click

    }

    // Async task to post login data
    @SuppressLint("StaticFieldLeak")
    public class LoginRequest extends AsyncTask<String, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... args) {

            try{
                URL url = new URL("http://c0009839.candept.com/API/loginregister.php");

                // put params in a Json Object
                JSONObject loginParams = new JSONObject();
                loginParams.put("username", args[0]);
                loginParams.put("password", args[1]);
                Log.i("LOGINPARAMS", loginParams.toString());

                // Set up connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Send data
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, StandardCharsets.UTF_8));
                writer.write(getPostDataString(loginParams));

                writer.flush();
                writer.close();
                os.close();

                // Get response
                int responseCode = conn.getResponseCode();
                error = String.valueOf(conn.getResponseCode());

                if (responseCode == HttpURLConnection.HTTP_OK){
                    inputStream = conn.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while (null != (line = in.readLine())){
                        sb.append(line).append("\n");
                    }
                    in.close();
                    inputStream.close();
                    json = sb.toString();
                    Log.i("APT: ", json);

                }else{
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
        }// end doInBackground

        // Interact with returned json object
        @Override
        protected void onPostExecute(JSONObject result){

            try{
                if(result != null){
                    String isAuthUser = result.getString("message");
                    if (isAuthUser.equals("Successfully logged in")){
                        Toast.makeText(getApplicationContext(), result.getString(
                                "message"), Toast.LENGTH_LONG).show();
                     Intent intent = new Intent(LoginActivity.this,
                             com.c00098391.planttracker.MainActivity.class);
                     startActivity(intent);
                    }else{
                        Toast.makeText(getApplicationContext(), result.getString(
                                "message"), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Unable to retrieve data from the server",
                            Toast.LENGTH_LONG).show();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

    // Turn json object to string for post
    public String getPostDataString(JSONObject params) throws Exception {

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
