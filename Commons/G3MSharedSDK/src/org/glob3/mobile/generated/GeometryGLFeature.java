package org.glob3.mobile.generated;import java.util.*;

/////////////////////////////////////////////////////////


public class GeometryGLFeature extends GLFeature
{
  //Position + cull + depth + polygonoffset + linewidth
  private GPUAttributeValueVec4Float _position;

  private final boolean _depthTestEnabled;
  private final boolean _cullFace;
  private final int _culledFace;
  private final boolean _polygonOffsetFill;
  private final float _polygonOffsetFactor;
  private final float _polygonOffsetUnits;
  private final float _lineWidth;

  public void dispose()
  {
	//  _position->_release();
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JAVA_CODE
	super.dispose();
//#endif
  }


  public GeometryGLFeature(IFloatBuffer buffer, int arrayElementSize, int index, boolean normalized, int stride, boolean depthTestEnabled, boolean cullFace, int culledFace, boolean polygonOffsetFill, float polygonOffsetFactor, float polygonOffsetUnits, float lineWidth, boolean needsPointSize, float pointSize)
  {
	  super(GLFeatureGroupName.NO_GROUP, GLFeatureID.GLF_GEOMETRY);
	  _depthTestEnabled = depthTestEnabled;
	  _cullFace = cullFace;
	  _culledFace = culledFace;
	  _polygonOffsetFill = polygonOffsetFill;
	  _polygonOffsetFactor = polygonOffsetFactor;
	  _polygonOffsetUnits = polygonOffsetUnits;
	  _lineWidth = lineWidth;
	_position = new GPUAttributeValueVec4Float(buffer, arrayElementSize, index, stride, normalized);
	_values.addAttributeValue(GPUAttributeKey.POSITION, _position, false);
  
	if (needsPointSize)
	{
	  _values.addUniformValue(GPUUniformKey.POINT_SIZE, new GPUUniformValueFloat(pointSize), false);
	}
  }


//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: void applyOnGlobalGLState(GLGlobalState* state) const
  public final void applyOnGlobalGLState(GLGlobalState state)
  {
	if (_depthTestEnabled)
	{
	  state.enableDepthTest();
	}
	else
	{
	  state.disableDepthTest();
	}
  
	if (_cullFace)
	{
	  state.enableCullFace(_culledFace);
	}
	else
	{
	  state.disableCullFace();
	}
  
	if (_polygonOffsetFill)
	{
	  state.enablePolygonOffsetFill(_polygonOffsetFactor, _polygonOffsetUnits);
	}
	else
	{
	  state.disablePolygonOffsetFill();
	}
  
	state.setLineWidth(_lineWidth);
  }

}
