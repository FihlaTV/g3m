package org.glob3.mobile.generated;import java.util.*;

public class GLFeatureLightingGroup extends GLFeatureGroup
{
  public final void apply(GLFeatureSet features, tangible.RefObject<GPUVariableValueSet> vs, tangible.RefObject<GLGlobalState> state)
  {
  
	  final int size = features.size();
  
	  boolean normalsAvailable = false;
	  for(int i = 0; i < size; i++)
	  {
		  final GLFeature f = features.get(i);
		  if (f._id == GLFeatureID.GLF_VERTEX_NORMAL)
		  {
			  normalsAvailable = true;
			  break;
		  }
	  }
  
  
	  if (normalsAvailable)
	  {
  
		  for(int i = 0; i < size; i++)
		  {
			  final GLFeature f = features.get(i);
  
			  if (f._group == GLFeatureGroupName.LIGHTING_GROUP)
			  {
				  f.applyOnGlobalGLState(state.argvalue);
				  vs.argvalue.combineWith(f.getGPUVariableValueSet());
			  }
		  }
	  }
  }
}
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#pragma mark GLFeatureGroup
