package org.glob3.mobile.generated;import java.util.*;

//
//  GPUProgramManager.cpp
//  G3MiOSSDK
//
//  Created by Jose Miguel SN on 02/04/13.
//
//

//
//  GPUProgramManager.hpp
//  G3MiOSSDK
//
//  Created by Jose Miguel SN on 02/04/13.
//
//




public class GPUProgramManager
{

  private java.util.HashMap<String, GPUProgram> _programs = new java.util.HashMap<String, GPUProgram>();

  private GPUProgramFactory _factory;

  private GPUProgram getCompiledProgram(String name)
  {
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if C_CODE
	  java.util.Iterator<String, GPUProgram> it = _programs.find(name);
	  if (it.hasNext())
	  {
		  return it.next().getValue();
	  }
	  else
	  {
		  return null;
	  }
//#endif
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JAVA_CODE
	  return _programs.get(name);
//#endif
  }

  private GPUProgram compileProgramWithName(GL gl, String name)
  {
  
	  GPUProgram prog = getCompiledProgram(name);
	  if (prog == null)
	  {
		  final GPUProgramSources ps = _factory.get(name);
  
		  //Compile new Program
		  if (ps != null)
		  {
			  prog = GPUProgram.createProgram(gl, ps._name, ps._vertexSource, ps._fragmentSource);
			  ///#warning DETECT COLISSION WITH COLLECTION OF GPUPROGRAM
			  if (prog == null)
			  {
				  ILogger.instance().logError("Problem at creating program named %s.", name.c_str());
				  return null;
			  }
  
			  _programs.put(name, prog);
		  }
		  else
		  {
			  ILogger.instance().logError("No shader sources for program named %s.", name.c_str());
		  }
  
	  }
	  return prog;
  }

  private GPUProgram getNewProgram(GL gl, int uniformsCode, int attributesCode)
  {
  
	  final boolean texture = GPUVariable.hasAttribute(attributesCode, GPUAttributeKey.TEXTURE_COORDS);
	  final boolean flatColor = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.FLAT_COLOR);
	  final boolean billboard = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.VIEWPORT_EXTENT);
	  final boolean color = GPUVariable.hasAttribute(attributesCode, GPUAttributeKey.COLOR);
	  final boolean transformTC = (GPUVariable.hasUniform(uniformsCode, GPUUniformKey.TRANSLATION_TEXTURE_COORDS) || GPUVariable.hasUniform(uniformsCode, GPUUniformKey.SCALE_TEXTURE_COORDS));
	  final boolean rotationTC = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.ROTATION_ANGLE_TEXTURE_COORDS);
	  final boolean hasLight = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.AMBIENT_LIGHT_COLOR);
  
	  final boolean hasTexture2 = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.SAMPLER2);
	  //  const bool hasTexture3 = GPUVariable::hasUniform(uniformsCode, SAMPLER3);
  
	  final boolean is2D = GPUVariable.hasAttribute(attributesCode, GPUAttributeKey.POSITION_2D);
  
	  final boolean isPoints = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.ROUNDED_POINT_BORDER_COLOR);
  
	  //  const bool bbAnchor = GPUVariable::hasUniform(uniformsCode,    BILLBOARD_ANCHOR);
  
	  final boolean isColorRange = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.COLORRANGE_COLOR_AT_0);
	  final boolean isColorRange3 = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.COLORRANGE_COLOR_AT_0_5);
	  final boolean isDynamic = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.TIME);
  
	  final boolean hasTranspDist = GPUVariable.hasUniform(uniformsCode, GPUUniformKey.TRANSPARENCY_DISTANCE_THRESLHOLD);
  
	  if (isColorRange)
	  {
  
		  if (isColorRange3)
		  {
			  if (isPoints)
			  {
				  return compileProgramWithName(gl, "RoundedColoredPoints_DynamicParametricColorRange3");
			  }
  
			  if (!isDynamic)
			  {
				  return compileProgramWithName(gl, "ParametricColorRange3Mesh");
			  }
			  else
			  {
				  if (hasLight && hasTranspDist)
				  {
					  return compileProgramWithName(gl, "DynamicParametricColor3Mesh_DirectionLight_DistanceTransparency");
				  }
  
				  return compileProgramWithName(gl, "DynamicParametricColorRange3Mesh");
			  }
		  }
  
		  if (isPoints)
		  {
			  return compileProgramWithName(gl, "RoundedColoredPoints_DynamicParametricColorRange");
		  }
  
		  if (!isDynamic)
		  {
			  return compileProgramWithName(gl, "ParametricColorRangeMesh");
		  }
		  else
		  {
			  if (hasLight && hasTranspDist)
			  {
				  return compileProgramWithName(gl, "DynamicParametricColorMesh_DirectionLight_DistanceTransparency");
			  }
  
  
			  return compileProgramWithName(gl, "DynamicParametricColorRangeMesh");
		  }
	  }
  
	  if (isColorRange3)
	  {
		  if (isPoints)
		  {
			  return compileProgramWithName(gl, "RoundedColoredPoints_DynamicParametricColorRange3");
		  }
  
		  if (!isDynamic)
		  {
			  return compileProgramWithName(gl, "ParametricColorRange3Mesh");
		  }
		  else
		  {
			  return compileProgramWithName(gl, "DynamicParametricColorRange3Mesh");
		  }
	  }
  
	  if (isPoints)
	  {
		  return compileProgramWithName(gl, "RoundedColoredPoints");
	  }
  
	  if (is2D)
	  {
		  if (flatColor)
		  {
			  return compileProgramWithName(gl, "FlatColor2DMesh");
		  }
		  return compileProgramWithName(gl, "Textured2DMesh");
	  }
  
	  if (billboard)
	  {
		  if (transformTC)
		  {
			  return compileProgramWithName(gl, "Billboard_TransformedTexCoor");
		  }
  
		  return compileProgramWithName(gl, "Billboard");
	  }
  
	  if (flatColor && !texture && !color)
	  {
		  if (hasLight)
		  {
			  return compileProgramWithName(gl, "FlatColorMesh_DirectionLight");
		  }
		  if (!hasTranspDist)
		  {
			  return compileProgramWithName(gl, "FlatColorMesh");
		  }
		  else
		  {
			  return compileProgramWithName(gl, "FlatColorMesh_DistanceTransparency");
		  }
	  }
  
	  if (!flatColor && texture && !color)
	  {
  
		  if (hasTexture2)
		  {
  
			  if (transformTC)
			  {
				  if (rotationTC)
				  {
					  return compileProgramWithName(gl, "FullTransformedTexCoorMultiTexturedMesh");
				  }
				  return compileProgramWithName(gl, "TransformedTexCoorMultiTexturedMesh");
			  }
  
			  return compileProgramWithName(gl, "MultiTexturedMesh");
		  }
  
		  if (hasLight)
		  {
			  if (transformTC)
			  {
				  //        if (rotationTC) {
				  //          return compileProgramWithName(gl, "TransformedTexCoorWithRotationTexturedMesh_DirectionLight");
				  //        }
				  return compileProgramWithName(gl, "TransformedTexCoorTexturedMesh_DirectionLight");
			  }
			  return compileProgramWithName(gl, "TexturedMesh_DirectionLight");
		  }
  
		  if (transformTC)
		  {
			  if (rotationTC)
			  {
				  return compileProgramWithName(gl, "FullTransformedTexCoorTexturedMesh");
			  }
			  return compileProgramWithName(gl, "TransformedTexCoorTexturedMesh");
		  }
  
		  return compileProgramWithName(gl, "TexturedMesh");
	  }
  
	  if (!flatColor && !texture && color)
	  {
		  if (hasLight)
		  {
			  if (hasTranspDist)
			  {
				  return compileProgramWithName(gl, "ColorMesh_DirectionLight_DistanceTransparency");
			  }
			  return compileProgramWithName(gl, "ColorMesh_DirectionLight");
		  }
  
		  return compileProgramWithName(gl, "ColorMesh");
	  }
  
	  if (!flatColor && !texture && !color)
	  {
		  return compileProgramWithName(gl, "NoColorMesh");
	  }
  
	  return null;
  }

  private GPUProgram getCompiledProgram(int uniformsCode, int attributesCode)
  {
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if C_CODE
	  for (java.util.Iterator<String, GPUProgram> it = _programs.iterator(); it.hasNext();)
	  {
		  ///#warning GPUProgram getUniformsCode avoid call
		  GPUProgram p = it.next().getValue();
		  if (p.getUniformsCode() == uniformsCode && p.getAttributesCode() == attributesCode)
		  {
			  return p;
		  }
	  }
//#endif
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JAVA_CODE
	  for (final GPUProgram p : _programs.values())
	  {
		  if ((p.getUniformsCode() == uniformsCode) && (p.getAttributesCode() == attributesCode))
		  {
			  return p;
		  }
	  }
//#endif
	  return null;
  }

  public GPUProgramManager(GPUProgramFactory factory)
  {
	  _factory = factory;
  }

  public void dispose()
  {
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if C_CODE
	  _factory = null;
	  for (java.util.Iterator<String, GPUProgram> it = _programs.iterator(); it.hasNext();)
	  {
		  it.next().getValue() = null;
	  }
//#endif
  }

  public final GPUProgram getProgram(GL gl, int uniformsCode, int attributesCode)
  {
	  GPUProgram p = getCompiledProgram(uniformsCode, attributesCode);
	  if (p == null)
	  {
		  p = getNewProgram(gl, uniformsCode, attributesCode);
		  if (p == null)
		  {
			  ILogger.instance().logError("Problem at compiling program.");
			  return null;
		  }
  
		  ///#warning AVOID getAttributesCode and getUniformsCode calls
		  if (p.getAttributesCode() != attributesCode || p.getUniformsCode() != uniformsCode)
		  {
			  ///#warning GIVE MORE DETAIL
			  ILogger.instance().logError("New compiled program does not match GL state.");
		  }
	  }
  
	  p.addReference();
  
	  return p;
  }

  public final void removeUnused()
  {
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if C_CODE
	  java.util.Iterator<String, GPUProgram> it = _programs.iterator();
	  while (it.hasNext())
	  {
		  if (it.second.getNReferences() == 0)
		  {
			  ILogger.instance().logInfo("Deleting program %s", it.second.getName().c_str());
			  it.next().getValue() = null;
			  _programs.remove(it++);
		  }
		  else
		  {
		  }
	  }
//#endif
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JAVA_CODE
	  final java.util.Iterator<java.util.Map.Entry<String, GPUProgram>> iterator = _programs.entrySet().iterator();
	  while (iterator.hasNext())
	  {
		  final java.util.Map.Entry<String, GPUProgram> entry = iterator.next();
		  final GPUProgram program = entry.getValue();
		  if (program.getNReferences() == 0)
		  {
			  ILogger.instance().logInfo("Deleting program %s", program.getName());
			  iterator.remove();
		  }
	  }
//#endif
  }
}
