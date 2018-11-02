package com.liubing.filtertestbed.CameraV1GLSurfaceView2;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.liubing.filtertestbed.CameraV1;

/**
 * Created by lb6905 on 2017/6/12.
 */

public class CameraV1GLSurfaceView2 extends GLSurfaceView {

    private CameraV1Renderer2 mRenderer;
    private int textureId = -1;

    public CameraV1GLSurfaceView2(Context context) {
        super(context);
    }

    public void init(CameraV1 camera, boolean isPreviewStarted, Context context) {
        setEGLContextClientVersion(2);
        mRenderer = new CameraV1Renderer2();
        mRenderer.init(this, camera, isPreviewStarted, context);
        setRenderer(mRenderer);
    }

    public void deinit() {
        if (mRenderer != null) {
            mRenderer.deinit();
            mRenderer = null;
            textureId = -1;
        }
    }
}
