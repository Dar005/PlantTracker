package com.c00098391.planttracker;

import android.annotation.SuppressLint;
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
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class TextResults extends AppCompatActivity {

    ImageView ivText;
    TextView tvText;
    Button btnShowText;


    static InputStream inputStream = null;
    static String json;
    static JSONObject jObj = null;
    static String error = "";

    private Bitmap bitmap;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_results);

        ivText = findViewById(R.id.ivText);
        tvText = findViewById(R.id.tvText);
        btnShowText = findViewById(R.id.btnShowText);




        // ivText.setDrawingCacheEnabled(true);
        //ivText.buildDrawingCache(true);

        Intent intent = getIntent();
        final String name = intent.getStringExtra("file");
        String username = intent.getStringExtra("username");


        byte[] by = new byte[(int) name.length()];
        Bitmap bmp = BitmapFactory.decodeFile(name);
        final Bitmap b = BitmapFactory.decodeFile(name);
        runTextRecognition(b);

        try {
            FileInputStream fi = new FileInputStream(name);
            fi.read(by);
            fi.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


        String textResults = tvText.getText().toString();
        String [] arrSplit = textResults.split("\\s");

        for (int i = 0; i < arrSplit.length; i++){

            Log.i("Text Results: ", arrSplit[i]);
        }
       // String rep = arrSplit[2];
      //  String expName = arrSplit[4] + " " + arrSplit[5];




        String encodedImg = Base64.encodeToString(by, Base64.DEFAULT);

        // Set format for time and date
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat tf = new SimpleDateFormat("hh:mm:ss");

        // Create strings for time and date
        String date = df.format(Calendar.getInstance().getTime());
        String time = tf.format(Calendar.getInstance().getTime());

        final String [] expDetails = new String[8];
        expDetails[0] = date;
        expDetails[1] = time;
        expDetails[2] = encodedImg;
       // expDetails[3] = rep;
     //   expDetails[4] = expName;
        expDetails[6] = username;

        ivText.setImageBitmap(bmp);

        btnShowText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                UploadData ud = new UploadData();
                ud.execute(expDetails);
            }
        });

    } // end of on create


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

    private String processTextRecognitionResult(FirebaseVisionText texts){
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if(blocks.size() == 0){
            Toast.makeText(TextResults.this, "No text found", Toast.LENGTH_LONG).show();
            return "";
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

                    s = s + ", " + elements.get(k).getText();
                }
            }
        }

        tvText.setText(sb);
        return s;
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
              //  dataParams.put("image", args[2]);
              //  dataParams.put("rep", args[3]);
              //  dataParams.put("expt", args[4]);
              //  dataParams.put("username", args[6]);
                Log.i("DATAPARAS", dataParams.toString());

                // Set up connection
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(50000);
                conn.setConnectTimeout(50000);
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
                    if (uploadSuccess.equals("Successfully uploaded data")){
                        Toast.makeText(getApplicationContext(), result.getString(
                                "message"), Toast.LENGTH_LONG).show();
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
