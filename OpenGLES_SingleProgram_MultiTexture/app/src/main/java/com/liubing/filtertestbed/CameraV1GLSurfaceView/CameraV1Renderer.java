package com.liubing.filtertestbed.CameraV1GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.liubing.filtertestbed.CameraV1;
import com.liubing.filtertestbed.FilterEngine;
import com.liubing.filtertestbed.Utils;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindFramebuffer;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * Created by lb6905 on 2017/6/12.
 */

public class CameraV1Renderer implements GLSurfaceView.Renderer {

    private static final String TAG = "Filter_MyRenderer";
    private Context mContext;
    private int mOESTextureId = -1;
    private SurfaceTexture mSurfaceTexture;
    private float[] transformMatrix = new float[16];
    private CameraV1GLSurfaceView mGLSurfaceView;
    private CameraV1 mCamera;
    private boolean bIsPreviewStarted;
    private FilterEngine mFilterEngine;
    private FloatBuffer mDataBuffer;
    private int mShaderProgram = -1;
    private int aPositionLocation = -1;
    private int aTextureCoordLocation = -1;
    private int uTextureMatrixLocation = -1;
    private int uTextureSamplerLocation = -1;
    private int uTextureCoordLocation = -1;
    private int[] mFBOIds = new int[1];

    private int mOESTextureId2 = -1;
    private FloatBuffer mFilterFaceBuffer;
    public void init(CameraV1GLSurfaceView glSurfaceView, CameraV1 camera, boolean isPreviewStarted, Context context) {
        mContext = context;
        mGLSurfaceView = glSurfaceView;
        mCamera = camera;
        bIsPreviewStarted = isPreviewStarted;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOESTextureId = Utils.createOESTextureObject();
        mFilterEngine = new FilterEngine(mOESTextureId, mContext);
        mDataBuffer = mFilterEngine.getBuffer();
//        获取着色器程序ID
        mShaderProgram = mFilterEngine.getShaderProgram();
        glBindFramebuffer(GL_FRAMEBUFFER, mFBOIds[0]);
        Log.i("lb6905", "onSurfaceCreated: mFBOId: " + mFBOIds[0]);

        mOESTextureId2 = Utils.loadTexture(mContext);
        mFilterFaceBuffer = mDataBuffer;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Long t1 = System.currentTimeMillis();
        if (mSurfaceTexture != null) {
            //从图像流更新纹理图像到最新帧。这可能只是调用拥有纹理的OpenGL ES上下文是调用线程上的当前调用。它将隐式地将其纹理绑定到GLXTraceURIONEXALALIONS纹理目标
            mSurfaceTexture.updateTexImage();
            //检索与纹理图像集相关的4x4纹理坐标变换矩阵
            //获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转等问题
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        if (!bIsPreviewStarted) {
            bIsPreviewStarted = initSurfaceTexture();
            bIsPreviewStarted = true;
            return;
        }

        //glClear(GL_COLOR_BUFFER_BIT);
        glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glUseProgram(mShaderProgram);
        //glGetAttribLocation() 来获取顶点着色器中,指定attribute名的index。以后就可以通过这个index向顶点着色器中传递数据。获取失败则返回-1
        aPositionLocation = glGetAttribLocation(mShaderProgram, FilterEngine.POSITION_ATTRIBUTE);
        aTextureCoordLocation = glGetAttribLocation(mShaderProgram, FilterEngine.TEXTURE_COORD_ATTRIBUTE);
        uTextureMatrixLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MATRIX_UNIFORM);
        uTextureSamplerLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_SAMPLER_UNIFORM);


        //激活纹理单元0
        glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定外部纹理到纹理单元0
        //从一个流中绑定纹理都要绑定到GL_TEXTURE_EXTERNAL_OES，而不是GL_TEXTURE_2D
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOESTextureId);
        //将此纹理单元床位片段着色器的uTextureSampler外部纹理采样器
        //为当前程序对象指定统一变量或数组的值
        glUniform1i(uTextureSamplerLocation, 0);
        //为当前程序对象指定统一变量4×4矩阵的值。
        glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0);

        //将顶点和纹理坐标传给顶点着色器
        if (mDataBuffer != null) {
            //顶点坐标从位置0开始读取
            mDataBuffer.position(0);
            //使能顶点属性
            glEnableVertexAttribArray(aPositionLocation);
            //顶点坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mDataBuffer);

            //纹理坐标从位置2开始读取
            mDataBuffer.position(2);
            glEnableVertexAttribArray(aTextureCoordLocation);
            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mDataBuffer);
        }

        //glDrawElements(GL_TRIANGLE_FAN, 6,GL_UNSIGNED_INT, 0);
        //glDrawArrays(GL_TRIANGLE_FAN, 0 , 6);
        ////绘制两个三角形（6个顶点）
        glDrawArrays(GL_TRIANGLES, 0, 6);
        //glDrawArrays(GL_TRIANGLES, 3, 3);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        long t2 = System.currentTimeMillis();
        long t = t2 - t1;
        Log.i("lb6905", "onDrawFrame: time: " + t);

        drawTexture2D();
    }



    private void drawTexture2D() {
        uTextureCoordLocation = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_TEXCOORD_UNIFORM);
        int leftBottomUniform = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_LEFTBOTTOM_UNIFORM);
        int rightTopUniform = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_RIGHTTOP_UNIFORM);
        int textureUniform = glGetUniformLocation(mShaderProgram, FilterEngine.TEXTURE_MYTEXTURE1_UNIFORM);

        glActiveTexture(GLES20.GL_TEXTURE1);
        glBindTexture(GLES20.GL_TEXTURE_2D, mOESTextureId2);
        glUniform1i(textureUniform, 1);
        //可以控制图片的左下大小
        GLES20.glUniform2f(leftBottomUniform, -0.5f, -0.5f);
        //可以控制图片的右上大小
        GLES20.glUniform2f(rightTopUniform, 0.50f, 0.50f);

        if (mFilterFaceBuffer != null) {
            //顶点坐标从位置0开始读取
            mFilterFaceBuffer.position(0);
            glEnableVertexAttribArray(aPositionLocation);
            glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 16, mFilterFaceBuffer);

            //纹理坐标从位置2开始读取
            mFilterFaceBuffer.position(2);
            glEnableVertexAttribArray(aTextureCoordLocation);
            glVertexAttribPointer(aTextureCoordLocation, 2, GL_FLOAT, false, 16, mFilterFaceBuffer);
        }

        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

    }

    //在onDrawFrame方法中调用此方法
    public boolean initSurfaceTexture() {
        if (mCamera == null || mGLSurfaceView == null) {
            Log.i(TAG, "mCamera or mGLSurfaceView is null!");
            return false;
        }
        //根据外部纹理ID创建SurfaceTexture
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                //每获取到一帧数据时请求OpenGL ES进行渲染
                mGLSurfaceView.requestRender();
            }
        });
        //讲此SurfaceTexture作为相机预览输出
        mCamera.setPreviewTexture(mSurfaceTexture);
        //开启预览
        mCamera.startPreview();
        return true;
    }

    public void deinit() {
        if (mFilterEngine != null) {
            mFilterEngine = null;
        }
        mDataBuffer = null;
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mCamera = null;
        mOESTextureId = -1;
        bIsPreviewStarted = false;
    }
}
