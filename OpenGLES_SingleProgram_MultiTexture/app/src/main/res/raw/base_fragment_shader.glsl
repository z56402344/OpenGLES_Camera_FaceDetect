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
   if (varyOtherPostion.x >= leftBottom.x && varyOtherPostion.y >= leftBottom.y && varyOtherPostion.x <= rightTop.x && varyOtherPostion.y <= rightTop.y) {
      lowp vec2 test = vec2((varyOtherPostion.x - leftBottom.x) / (rightTop.x - leftBottom.x), 1.0 -  (varyOtherPostion.y - leftBottom.y) / (rightTop.y - leftBottom.y));
      lowp vec4 otherColor = texture2D(myTexture1, test);
      otherColor.a = 0.8;
      gl_FragColor = otherColor * otherColor.a + texture2D(myTexture0, vTextureCoord) * (1.0 - otherColor.a);
  }else {
      gl_FragColor = texture2D(myTexture0, vTextureCoord);
  }
}
