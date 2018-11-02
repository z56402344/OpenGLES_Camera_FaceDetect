package com.liubing.filtertestbed.CameraV1TextureView;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;

import com.liubing.filtertestbed.CameraV1;
import com.liubing.filtertestbed.TextureUtils;

/**
 * Created by lb6905 on 2017/6/28.
 */

public class CameraV1TextureViewActivity extends Activity {
    private static final String TAG = "Filter_TVActivity";
    private TextureView mTextureView;
    private int mCameraId;
    private CameraV1 mCamera;
    private SurfaceTexture mOESSurfaceTexture;
    private int mOESTextureId = -1;
    private CameraV1GLRenderer mRenderer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置全屏无状态栏，并竖屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //6.0运行时摄像头权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.CAMERA}, 1);
            }
        }
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(mTextureListener);
        //设置隐藏虚拟按键
        //mTextureView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mRenderer = new CameraV1GLRenderer();
        setContentView(mTextureView);
    }

    public TextureView.SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mOESTextureId = TextureUtils.createOESTextureObject();
            mRenderer.init(mTextureView, mOESTextureId, CameraV1TextureViewActivity.this);
            mOESSurfaceTexture = mRenderer.initOESTexture();

//            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
            DisplayMetrics dm = new DisplayMetrics();
            mCamera = new CameraV1(CameraV1TextureViewActivity.this);
            if (!mCamera.openCamera(dm.widthPixels, dm.heightPixels, mCameraId,null)) {
                return;
            }

            mCamera.setPreviewTexture(mOESSurfaceTexture);
            mCamera.startPreview();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.releaseCamera();
                mCamera = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.releaseCamera();
            mCamera = null;
        }
    }
}
