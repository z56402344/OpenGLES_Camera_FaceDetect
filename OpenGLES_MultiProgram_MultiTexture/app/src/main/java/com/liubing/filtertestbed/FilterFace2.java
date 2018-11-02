package com.liubing.filtertestbed;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by duguang on 18-10-25.
 */
public class FilterFace2 {private static final String TAG = "FACE";

    //顶点程序
    private static final String VERTEX_SHADER =
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_position;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = a_position;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";

    private static final String FRAGMENT_SHADER =
            "precision lowp float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D u_samplerTexture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(u_samplerTexture, v_texCoord);" +
                    "}";

    public static final String POSITION_ATTRIBUTE = "a_position";
    public static final String TEXTURE_COORD_ATTRIBUTE = "a_texCoord";
    public static final String TEXTURE_SAMPLER_UNIFORM = "u_samplerTexture";

    private float[] quadVertex = new float[]{
            -1.0f, 0.296f, 0.0f, // Position 0
            0, 0, // TexCoord 1
            -1f, -0.296f, 0.0f, // Position 1
            0, 1.0f, // TexCoord 0
            1f, -0.296f, 0.0f, // Position 2
            1.0f, 1.0f, // TexCoord 3
            1f, 0.296f, 0.0f, // Position 3
            1.0f, 0, // TexCoord 2
    };
    private short[] quadIndex = new short[]{
            0, 1, 2, // 0号点，1号点，2号点组成一个三角形
            0, 2, 3, // 0号点，2号点，3号点组成一个三角形
    };

    private float[] TEX_VERTEX = new float[]{
            0, 0, // TexCoord 1
            0, 1.0f, // TexCoord 0
            1.0f, 1.0f, // TexCoord 3
            1.0f, 0, // TexCoord 2
    };

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mIndexBuffer;
    private FloatBuffer mTexVertexBuffer;
    private final float[] mMVPMatrix = new float[16];

    private int mShaderProgram;
    private int attribPosition;
    private int mMatrixHandle;
    private int attribTexCoord;
    private int uniformTexture;
    private int mOESTextureId;
    private Camera.Face mFace;
    private int mW,mH;
    private Context mContext;

    private Matrix mMatrix = new Matrix();


    public FilterFace2(int OESTextureId, Context context) {
        super();
        mContext = context;
        mOESTextureId = OESTextureId;
//        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
          // Active the texture unit 0
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        loadVertex();
        initShader();
//        loadTexture();
    }

    public void setWH(int w,int h){
        mW = w;
        mH = h;
    }

    private void loadVertex() {
        // float size = 4
        this.mVertexBuffer = ByteBuffer.allocateDirect(quadVertex.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        this.mVertexBuffer.put(quadVertex).position(0);
        // short size = 2
        this.mIndexBuffer = ByteBuffer.allocateDirect(quadIndex.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();
        this.mIndexBuffer.put(quadIndex).position(0);

        this.mTexVertexBuffer = ByteBuffer.allocateDirect(TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        this.mTexVertexBuffer.put(TEX_VERTEX).position(0);
    }

    private void initShader() {

//        String vertexSource = Tools.readFromAssets("VertexShader.glsl");
//        String fragmentSource = Tools.readFromAssets("FragmentShader.glsl");
        // Load the shaders and get a linked program
        mShaderProgram = loadProgram(VERTEX_SHADER, FRAGMENT_SHADER);
        // Get the attribute locations
        attribPosition = GLES20.glGetAttribLocation(mShaderProgram, "a_position");
        attribTexCoord = GLES20.glGetAttribLocation(mShaderProgram, "a_texCoord");
        uniformTexture = GLES20.glGetUniformLocation(mShaderProgram, "u_samplerTexture");
        GLES20.glUseProgram(mShaderProgram);
        GLES20.glEnableVertexAttribArray(attribPosition);
        GLES20.glEnableVertexAttribArray(attribTexCoord);
        // Set the sampler to texture unit 0
        GLES20.glUniform1i(uniformTexture, 1);
    }

    public static int loadProgram(String vertexSource, String fragmentSource) {

        // Load the vertex shaders
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        // Load the fragment shaders
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        // Create the program object
        int program = GLES20.glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("Error create program.");
        }
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        // Link the program
        GLES20.glLinkProgram(program);
        int[] linked = new int[1];
        // Check the link status
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0);
        if (linked[0] == 0) {
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Error linking program: " +
                    GLES20.glGetProgramInfoLog(program));
        }
        // Free up no longer needed shader resources
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        return program;
    }

    public static int loadShader(int shaderType, String source) {
        // Create the shader object
        int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0) {
            throw new RuntimeException("Error create shader.");
        }
        int[] compiled = new int[1];
        // Load the shader source
        GLES20.glShaderSource(shader, source);
        // Compile the shader
        GLES20.glCompileShader(shader);
        // Check the compile status
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Error compile shader: " +
                    GLES20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    //绘制每一帧的时候回调
    public void onDrawFrame(GL10 unused) {
        // clear screen to black
//        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        mVertexBuffer.position(0);
        // load the position
        // 3(x , y , z)
        // (2 + 3 )* 4 (float size) = 20
        GLES20.glVertexAttribPointer(attribPosition,  3, GLES20.GL_FLOAT, false, 20, mVertexBuffer);
        mVertexBuffer.position(3);
        // load the texture coordinate
        GLES20.glVertexAttribPointer(attribTexCoord, 2, GLES20.GL_FLOAT, false, 20, mVertexBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndexBuffer);

    }

    private RectF mRect = new RectF();

    public void setFaces(Camera.Face face){
        mFace = face;
//        quadVertex;
        Log.i(TAG, "DrawTexture  mW=" +mW +", mH="+mH );

        prepareMatrix(mMatrix, true, 90, mW, mH);
        mMatrix.postRotate(0);
        mRect.set(face.rect);
        mMatrix.mapRect(mRect);//计算出在父布局的真是坐标
        //识别的Rect 系数，使装饰图片根据人脸与摄像头的距离放大或者缩小
        float dx = mRect.bottom - mRect.top;
        boolean isHeadShow = true;
//        mRect.left=463.32, mRect.top=983.94, mRect.right=847.26, mRect.bottom=1479.63
        Log.i(TAG, "DrawTexture  mRect.left=" +mRect.left +", mRect.top="+mRect.top +", mRect.right="+mRect.right +", mRect.bottom="+mRect.bottom );

        float[] lefttop = tranXY(mRect.left,mRect.top);
        float[] righttop = tranXY(mRect.right,mRect.top);
        float[] leftbottom = tranXY(mRect.left,mRect.bottom);
        float[] rightbottom = tranXY(mRect.right,mRect.bottom);
        quadVertex[0] = lefttop[0];
        quadVertex[1] = lefttop[1];
        quadVertex[15] = righttop[0];
        quadVertex[16] = righttop[1];
        quadVertex[5] = leftbottom[0];
        quadVertex[6] = leftbottom[1];
        quadVertex[10] = rightbottom[0];
        quadVertex[11] = rightbottom[1];
        Log.i(TAG, "DrawTexture  lefttop[0]=" +lefttop[0] +", lefttop[1]="+lefttop[1] +",\n " +
                "righttop[0]="+righttop[0] +", righttop[0]="+righttop[0]+",\n " +
                "leftbottom[0]="+leftbottom[0] +", leftbottom[1]="+leftbottom[1]+",\n " +
                "rightbottom[0]="+rightbottom[0] +", rightbottom[1]="+rightbottom[1] );
        //重新对装饰坐标进行读取
        loadVertex();
    }

    //从手机坐标系到-->GLSurfaceView 世界坐标的投影坐标
    public float[] tranXY(float x,float y){
        //GLSurfaceView 世界坐标的投影坐标
        float[] point = new float[2];
        int centerX = mW/2;
        int centerY = mH/2;
        if (x > centerX){
            point[0] = (x - centerX) / centerX;
        }else{
            point[0] = -(centerX - x) / centerX;
        }

        if (y > centerY){
            point[1] = -(y - centerY) / centerY;
        }else{
            point[1] = (centerY - y) / centerY;
        }
        return point;
    }

    public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
                                     int viewWidth, int viewHeight) {
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
    }

    public int getShaderProgram() {
        return mShaderProgram;
    }

    public FloatBuffer getBuffer() {
        return mVertexBuffer;
    }

    public ShortBuffer getIndexBuffer() {
        return mIndexBuffer;
    }
}
