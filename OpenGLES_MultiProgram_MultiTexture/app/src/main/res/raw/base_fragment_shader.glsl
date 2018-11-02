#extension GL_OES_EGL_image_external : require
precision mediump float;
uniform samplerExternalOES myTexture0;
varying vec2 vTextureCoord;

uniform sampler2D myTexture1;

varying lowp vec2 varyOtherPostion;

uniform lowp vec2 leftBottom;
uniform lowp vec2 rightTop;
void main()
{
      gl_FragColor = texture2D(myTexture0, vTextureCoord);
}
