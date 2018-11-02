package com.liubing.filtertestbed;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

/**
 * Created by lb6905 on 2017/6/27.
 */

public class CameraV1 {
    private Activity mActivity;
    private int mCameraId;
    private Camera mCamera;
    private FaceDetectionListener mFaceDetectionListener = new FaceDetectionListener();
    private Handler mHandler;

    public CameraV1(Activity activity) {
        mActivity = activity;
    }

    public boolean openCamera(int screenWidth, int screenHeight, int cameraId, Handler handler) {
        try {
            mHandler = handler;
            mCameraId = cameraId;
            mCamera = Camera.open(mCameraId);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.set("orientation", "portrait");
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setPreviewSize(1280, 720);
            setCameraDisplayOrientation(mActivity, mCameraId, mCamera);
            mCamera.setParameters(parameters);
            Log.i("lb6905", "open camera");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

	//开启人脸识别监听
    public void startFaceDetection(){
        if (mCamera != null) {
            mCamera.setFaceDetectionListener(mFaceDetectionListener);
            mCamera.startFaceDetection();
        }
    }
	
	//人脸识别监听回调类
    class  FaceDetectionListener implements Camera.FaceDetectionListener{

        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            Message m = mHandler.obtainMessage();
            if (null == faces || faces.length == 0) {
                m.obj = null;
                Log.d("face", "onFaceDetection : There is no face found.");
            } else {
                Log.d("face", "onFaceDetection : face found.");
                m.obj = faces;
                for (int i = 0; i < faces.length; i++) {
                    Camera.Face face = faces[i];
                    if (face == null)return;
                    Rect rect = face.rect;
                    Log.i("face","face.score="+face.score);
                    Log.i("face","rect.left="+rect.left+"\nrect.top="+rect.top+"\nrect.right="+rect.right+"\nrect.bottom="+rect.bottom);
                    Log.i("face","id="+face.id+" \nface.leftEye.x="+face.leftEye.x+" \nface.leftEye.y"+face.leftEye.y+" \nface.mouth.x="+face.mouth.x+" \nface.mouth.y="+face.mouth.y);
                }
            }
            m.sendToTarget();
        }
    }
}
