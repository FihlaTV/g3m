package org.glob3.mobile.generated;import java.util.*;

public class TrailsRenderer extends DefaultRenderer
{
  private java.util.ArrayList<Trail> _trails = new java.util.ArrayList<Trail>();


  private GLState _glState;

  private void updateGLState(Camera camera)
  {
	if (_projection == null)
	{
	  _projection = new ProjectionGLFeature(camera.getProjectionMatrix44D());
	  _glState.addGLFeature(_projection, true);
	}
	else
	{
	  _projection.setMatrix(camera.getProjectionMatrix44D());
	}
  
	if (_model == null)
	{
	  _model = new ModelGLFeature(camera.getModelMatrix44D());
	  _glState.addGLFeature(_model, true);
	}
	else
	{
	  _model.setMatrix(camera.getModelMatrix44D());
	}
  }
  private ProjectionGLFeature _projection;
  private ModelGLFeature _model;

  public TrailsRenderer()
  {
	  _projection = null;
	  _model = null;
	  _glState = new GLState();
  }

  public final void addTrail(Trail trail)
  {
	if (trail != null)
	{
	  _trails.add(trail);
	}
  }

  public final void removeTrail(Trail trail)
  {
	  removeTrail(trail, true);
  }
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void removeTrail(Trail* trail, boolean deleteTrail = true)
  public final void removeTrail(Trail trail, boolean deleteTrail)
  {
	final int trailsCount = _trails.size();
	int foundIndex = -1;
	for (int i = 0; i < trailsCount; i++)
	{
	  Trail each = _trails.get(i);
	  if (trail == each)
	  {
		foundIndex = i;
		break;
	  }
	}
	if (foundIndex >= 0)
	{
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if C_CODE
//C++ TO JAVA CONVERTER TODO TASK: There is no direct equivalent to the STL vector 'erase' method in Java:
	  _trails.erase(_trails.iterator() + foundIndex);
//#endif
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JAVA_CODE
	  _trails.remove(foundIndex);
//#endif
	  if (deleteTrail)
	  {
		if (trail != null)
			trail.dispose();
	  }
	}
  }

  public final void removeAllTrails()
  {
	  removeAllTrails(true);
  }
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: void removeAllTrails(boolean deleteTrails = true)
  public final void removeAllTrails(boolean deleteTrails)
  {
	if (deleteTrails)
	{
	  final int trailsCount = _trails.size();
	  for (int i = 0; i < trailsCount; i++)
	  {
		Trail trail = _trails.get(i);
		if (trail != null)
			trail.dispose();
	  }
	}
	_trails.clear();
  }

  public void dispose()
  {
	final int trailsCount = _trails.size();
	for (int i = 0; i < trailsCount; i++)
	{
	  Trail trail = _trails.get(i);
	  if (trail != null)
		  trail.dispose();
	}
	_trails.clear();
  
	_glState._release();
  }

  public final void onResizeViewportEvent(G3MEventContext ec, int width, int height)
  {

  }

  public final void render(G3MRenderContext rc, GLState glState)
  {
	final int trailsCount = _trails.size();
	if (trailsCount > 0)
	{
	  final Camera camera = rc.getCurrentCamera();
  
	  updateGLState(camera);
  
	  final Frustum frustum = camera.getFrustumInModelCoordinates();
	  for (int i = 0; i < trailsCount; i++)
	  {
		Trail trail = _trails.get(i);
		if (trail != null)
		{
		  trail.render(rc, frustum, _glState);
		}
	  }
	}
  }

}
//#define MAX_POSITIONS_PER_SEGMENT 128

//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#pragma mark TrailsRenderer
