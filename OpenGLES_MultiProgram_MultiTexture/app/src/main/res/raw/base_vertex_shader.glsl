attribute vec4 aPosition;
uniform mat4 uTextureMatrix;
attribute vec4 aTextureCoordinate;
varying vec2 vTextureCoord;

attribute vec2 textCoordinate;

varying lowp vec2 varyTextCoord;
varying lowp vec2 varyOtherPostion;

void main()
{
  vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;
  varyTextCoord = textCoordinate;
  varyOtherPostion = aPosition.xy;
  gl_Position = aPosition;
}