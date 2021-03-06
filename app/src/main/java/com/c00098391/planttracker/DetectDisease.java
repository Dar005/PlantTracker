package com.c00098391.planttracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DetectDisease extends AppCompatActivity {
    // Over lay grid image
    private ImageView gridImage;
    private static final String TAG = "CaptureText";
    private Button btnDetectDisease;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest; // DON'T THINK THIS IS USED.....
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file; // DON'T THINK THIS IS USED....

    // Location variables
    String lat = "";
    String lon = "";

    // Weather variables
    private static final String APP_ID = "b11dc521fd3aecc6374e2e331dc090e3";
    String weather = "";
    String units = "metric";
    String url = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units="+units+"&appid="+APP_ID;




    protected String fileLoc;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported; // DON'T THINK THIS IS USED
    private Handler mBackGroundHandler;
    private HandlerThread mBackgroundThread;


    // variables for experiment info and user
    String expt;
    String treatment;
    String rep;
    String username;
    String userId;
    String expId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_disease);

        gridImage = findViewById(R.id.gridImage);
        textureView = findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        btnDetectDisease = findViewById(R.id.btnDetectDisease);
        assert btnDetectDisease != null;


        // Get user info
        username = getIntent().getStringExtra("username");
        userId = getIntent().getStringExtra("userid");
        rep = getIntent().getStringExtra("rep");
        expt = getIntent().getStringExtra("expt");
        treatment = getIntent().getStringExtra("treatment");
        expId = getIntent().getStringExtra("expid");



        // Start Location service and get lat and lon
        startService(new Intent(DetectDisease.this,
                com.c00098391.planttracker.GPS.class));
        final GPS gps = new GPS(DetectDisease.this);
        lat = Double.toString(gps.getLatitude());
        lon = Double.toString(gps.getLongitude());


        // Continue experiment.....
        if(rep!=null){
            // need to get EXPT, TREATMENT, rep from the database baseed on the info provided by the user
            Toast.makeText(DetectDisease.this, rep + "\n " + expt + "\n" +treatment + " found", Toast.LENGTH_LONG).show();


        }else{
            Toast.makeText(DetectDisease.this, "EXP not found ", Toast.LENGTH_LONG).show();
        }



        btnDetectDisease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectDisease();
            }
        });



    }// END OF ON CREATE

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "In onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null; // not used
        }
    };

    final CameraCaptureSession.CaptureCallback captureCallBackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result){
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(DetectDisease.this, "Saved: " + file, Toast.LENGTH_LONG).show();
            createCameraPreview();
        }
    };

    protected void startBackgroundThread(){
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackGroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread(){
        mBackgroundThread.quitSafely();
        try{
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackGroundHandler = null;
        }catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    protected void detectDisease(){
        if (null == cameraDevice){
            Log.e(TAG, "Camera Device is null");
            return;
        }

        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try{
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null){
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 640;
            if(jpegSizes != null && 0 < jpegSizes.length){
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder
                    = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            /**
             *
             */

            String outPic = Constants.SCAN_IMAGE_LOCATION
                    + File.separator + Utilities.generateFilename();
            FolderUtil.createDefaultFolder(Constants.SCAN_IMAGE_LOCATION);
            fileLoc = outPic;

            //  byte [] arr;

            final File file = new File(outPic);

            String fn = file.toString();
            Log.i("FILE TEST", "File Path after create.... " + fn);

            // Get user info
            //   Bundle bundle = getIntent().getExtras();
            //   assert bundle != null;
//            final String username = bundle.getString("username");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener(){
                @Override
                public void onImageAvailable(ImageReader reader){
                    Image image = null;

                    try {

//                        // Get weather
                        String units = "metric";
                        String url = "http://api.openweathermap.org/data/2.5/weather?lat="+lat+"&lon="+lon+"&units="+units+"&appid="+APP_ID;
                        String weatherData =  new GetWeatherTask(weather).execute(url).get();

                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);


                        BitmapFactory.Options options = new BitmapFactory.Options();
                        // options.inJustDecodeBounds = true;
                        // Size is 3 as 4 meant onTouch on analysis page could be outside the bounds of the image...
                        options.inSampleSize = 3;
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,  options);
                        int imageHeight = options.outHeight;
                        int imageWidth = options.outWidth;
                        String imageType = options.outMimeType;
                        Log.i("DETECT DISEASE", "Bitmap Info :" +  " Height: " + imageHeight + " Width: " + imageWidth + "Type: " + imageType + "----------------------------------------------------------------");



                       // Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                        byte[] byteOut = stream.toByteArray();


                        /**
                         *
                         *
                         *
                         */

                        String encodedImg = Base64.encodeToString(byteOut, Base64.DEFAULT);

                        Intent intent = new Intent(DetectDisease.this,
                                DiseaseAnalysis.class);
                        intent.putExtra("username", username);
                        intent.putExtra("image", byteOut);
                        intent.putExtra("weather", weatherData);
                        intent.putExtra("lat", lat);
                        intent.putExtra("lon", lon);
                        intent.putExtra("userid", userId);
                        intent.putExtra("rep", rep);
                        intent.putExtra("treatment", treatment);
                        intent.putExtra("expt", expt);
                        intent.putExtra("expid", expId);
                        startActivity(intent);

                        // save(byteOut);
                        // save(bytes); TEST IF THIS WORKS WITHOUT THE ADDED STEPS!!!!
                        // }catch(IOException e){
                        //      e.printStackTrace();;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } finally{
                        if(image != null){
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try{
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    }finally{
                        if(null != output){
                            output.close();
                        }
                    }
                }
            };

            reader.setOnImageAvailableListener(readerListener, mBackGroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener
                    = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);


                }
            };

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(CameraCaptureSession session){
                    try{
                        session.capture(captureBuilder.build(), captureListener, mBackGroundHandler);
                    }catch(CameraAccessException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession){

                }
            }, mBackGroundHandler);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    protected void createCameraPreview(){
        try{
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight()-100);
            Surface surface = new Surface(texture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession){
                    if(null == cameraDevice){
                        return;
                    }

                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession){
                    Toast.makeText(DetectDisease.this, "Configuration changed",
                            Toast.LENGTH_SHORT).show();
                }
            }, null);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void openCamera(){
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "in openCamera()");
        try{
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map
                    = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(DetectDisease.this, new String[]{
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
        Log.e(TAG, "in Open Camera (permissions");
    }

    protected void updatePreview(){
        if(null == cameraDevice){
            Log.e(TAG, "Update Preview Error..");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try{
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(),
                    null, mBackGroundHandler);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    private void closeCamera(){
        if(null != cameraDevice){
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader){
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults){
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // Close app
                Toast.makeText(DetectDisease.this,
                        "Sorry, you cannot use this app without granting permission", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e(TAG, "in onResume");
        startBackgroundThread();
        if(textureView.isAvailable()){
            openCamera();
        }else{
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause(){
        Log.e(TAG, "in OnPause");
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

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
