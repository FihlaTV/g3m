package org.glob3.mobile.generated;import java.util.*;

public class TransparentShapeWrapper extends OrderedRenderable
{
  private Shape _shape;
  private final double _squaredDistanceFromEye;
  private GLState _parentGLState;
  private final boolean _renderNotReadyShapes;
  public TransparentShapeWrapper(Shape shape, double squaredDistanceFromEye, GLState parentGLState, boolean renderNotReadyShapes)
  {
	  _shape = shape;
	  _squaredDistanceFromEye = squaredDistanceFromEye;
	  _parentGLState = parentGLState;
	  _renderNotReadyShapes = renderNotReadyShapes;
  }

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: double squaredDistanceFromEye() const
  public final double squaredDistanceFromEye()
  {
	return _squaredDistanceFromEye;
  }

  public final void render(G3MRenderContext rc)
  {
	_shape.render(rc, _parentGLState, _renderNotReadyShapes);
  }
}
