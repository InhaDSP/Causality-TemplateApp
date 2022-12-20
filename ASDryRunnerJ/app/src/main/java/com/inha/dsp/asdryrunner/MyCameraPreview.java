package com.inha.dsp.asdryrunner;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.inha.dsp.asdryrunner.driver.CausalityData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyCameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {
    private boolean shouldSaveFrame = false;

    private final String TAG = "MyTag";

    private SurfaceHolder mHolder;

    private int mCameraID;

    public Camera mCamera;
    private Camera.CameraInfo mCameraInfo;

    private int mDisplayOrientation;

    byte[] callbackBuffer = null;

    public MyCameraPreview(Context context, int cameraId) {
        super(context);
        Log.d(TAG, "MyCameraPreview cameraId : " + cameraId);

        // 0    ->     CAMERA_FACING_BACK
        // 1    ->     CAMERA_FACING_FRONT
        mCameraID = cameraId;

        try {
            // attempt to get a Camera instance
            mCamera = Camera.open(mCameraID);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Camera is not available");
        }

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // get display orientation
        mDisplayOrientation = ((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");

        // retrieve camera's info.
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        mCameraInfo = cameraInfo;

        // The Surface has been created, now tell the camera where to draw the preview.
        try {
//            mCamera.addCallbackBuffer(callbackBuffer);
//            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.setPreviewDisplay(holder);
            mCamera.setPreviewCallback(this);
//            mCamera.startPreview();

        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.d(TAG, "surfaceChanged");
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            Log.e(TAG, "preview surface does not exist");
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
            Log.d(TAG, "Preview stopped.");
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }


        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
        mCamera.setDisplayOrientation(orientation);

        try {
//            mCamera.addCallbackBuffer(callbackBuffer);
//            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.setPreviewCallback(this);
//            mCamera.startPreview();
            Log.d(TAG, "Camera preview started.");
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }

    }

    /**
     * 안드로이드 디바이스 방향에 맞는 카메라 프리뷰를 화면에 보여주기 위해 계산합니다.
     */
    public int calculatePreviewOrientation(Camera.CameraInfo info, int rotation) {
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    public void stopPreview()
    {
        shouldSaveFrame = false;
        mCamera.stopPreview();
    }

    public void resumePreview()
    {
        shouldSaveFrame = true;
        mCamera.startPreview();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if(shouldSaveFrame) {
            Log.d("ASDryrunner", "onPreviewFrame()");
            Handler handler = new Handler();  //Optional. Define as a variable in your activity.

            Runnable r = () -> {
                // your code here
                //If you want to update the UI, queue the code on the UI thread
                handler.post(() -> saveFrame(data, camera));
            };

            Thread t = new Thread(r);
            t.start();
        }
    }

    private void saveFrame(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        ByteArrayOutputStream outstr = new ByteArrayOutputStream();
        Rect rect = new Rect(0, 0, width, height);
        YuvImage yuvimage=new YuvImage(data, ImageFormat.NV21,width,height,null);
        yuvimage.compressToJpeg(rect, 100, outstr);
        Bitmap bmp = BitmapFactory.decodeByteArray(outstr.toByteArray(), 0, outstr.size());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        String currentSerial = CausalityData.CurrentData.Contexts.get(0).CurrentCaption.toString();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss", java.util.Locale.getDefault());
        Date date = new Date();
        String now = dateFormat.format(date);
        String filename = String.format("%s (%s).jpg", now, currentSerial);
        File dataFile = new File(CausalityData.CurrentData.dataPath, filename);

        try {
            FileOutputStream fos = new FileOutputStream(dataFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
