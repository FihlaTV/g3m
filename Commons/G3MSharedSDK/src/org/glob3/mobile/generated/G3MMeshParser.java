package org.glob3.mobile.generated;//
//  G3MMeshParser.cpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 1/29/14.
//
//

//
//  G3MMeshParser.h
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 1/29/14.
//
//


//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class Mesh;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class JSONObject;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class JSONArray;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class JSONString;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class G3MMeshMaterial;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class Color;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class URL;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class Vector3F;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class IFloatBuffer;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class IShortBuffer;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class Planet;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class Geodetic3D;
//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class Vector3D;

public class G3MMeshParser
{
  private static Color parseColor(JSONArray jsonColor)
  {
	if (jsonColor == null)
	{
	  return null;
	}
	final float r = (float) jsonColor.getAsNumber(0, 0);
	final float g = (float) jsonColor.getAsNumber(1, 0);
	final float b = (float) jsonColor.getAsNumber(2, 0);
	final float a = (float) jsonColor.getAsNumber(3, 0);
	return Color.newFromRGBA(r, g, b, a);
  }
  private static URL parseURL(JSONString jsonURL)
  {
	if (jsonURL == null)
	{
	  return null;
	}
	return new URL(jsonURL.value());
  }

  private static java.util.HashMap<String, G3MMeshMaterial> parseMaterials(JSONArray jsonMaterials)
  {
	java.util.HashMap<String, G3MMeshMaterial> result = new java.util.HashMap<String, G3MMeshMaterial>();
  
	if (jsonMaterials != null)
	{
	  final int materialsSize = jsonMaterials.size();
	  for (int i = 0; i < materialsSize; i++)
	  {
		final JSONObject jsonMaterial = jsonMaterials.getAsObject(i);
  
		final String id = jsonMaterial.getAsString("id").value();
		final JSONArray jsonColor = jsonMaterial.getAsArray("color");
		final JSONString jsonTextureURL = jsonMaterial.getAsString("textureURL");
  
		result.put(id, new G3MMeshMaterial(id, parseColor(jsonColor), parseURL(jsonTextureURL)));
	  }
	}
  
	return result;
  }

  private static java.util.ArrayList<Mesh> parseMeshes(tangible.RefObject<java.util.HashMap<String, G3MMeshMaterial>> materials, JSONArray jsonMeshes, Planet planet)
  {
	java.util.ArrayList<Mesh> result = new java.util.ArrayList<Mesh>();
	if (jsonMeshes != null)
	{
	  final int jsonMeshesSize = jsonMeshes.size();
	  for (int i = 0; i < jsonMeshesSize; i++)
	  {
		Mesh mesh = parseMesh(materials, jsonMeshes.getAsObject(i), planet);
		if (mesh != null)
		{
		  result.add(mesh);
		}
	  }
	}
	return result;
  }

  private static Mesh parseMesh(tangible.RefObject<java.util.HashMap<String, G3MMeshMaterial>> materials, JSONObject jsonMesh, Planet planet)
  {
	if (jsonMesh == null)
	{
	  return null;
	}
  
	final String materialID = jsonMesh.getAsString("material", "");
	if ( ! materials.argvalue.containsKey(materialID))
	{
	  ILogger.instance().logError("Can't find material \"%s\"", materialID.c_str());
	  return null;
	}
	G3MMeshMaterial material = materials.argvalue.get(materialID);
  
	final String primitive = jsonMesh.getAsString("primitive", "Triangles");
	final float pointSize = (float) jsonMesh.getAsNumber("pointSize", 1);
	final float lineWidth = (float) jsonMesh.getAsNumber("lineWidth", 1);
	final boolean depthTest = jsonMesh.getAsBoolean("depthTest", true);
  
	final String verticesFormat = jsonMesh.getAsString("verticesFormat", "Cartesian");
	final boolean isGeodetic = (verticesFormat.equals("Geodetic"));
  
	double centerX;
	double centerY;
	double centerZ;
	IFloatBuffer vertices;
	if (isGeodetic)
	{
	  final Geodetic3D geodeticCenter = parseGeodetic3D(jsonMesh.getAsArray("center"));
  
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: const Vector3D cartesian = planet->toCartesian(geodeticCenter);
	  final Vector3D cartesian = planet.toCartesian(new Geodetic3D(geodeticCenter));
  
	  centerX = cartesian._x;
	  centerY = cartesian._y;
	  centerZ = cartesian._z;
  
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: vertices = parseGeodeticFloatBuffer(jsonMesh->getAsArray("vertices"), geodeticCenter, Vector3D(centerX, centerY, centerZ), planet);
	  vertices = parseGeodeticFloatBuffer(jsonMesh.getAsArray("vertices"), new Geodetic3D(geodeticCenter), new Vector3D(centerX, centerY, centerZ), planet);
	}
	else
	{
	  final Vector3F center = parseVector3F(jsonMesh.getAsArray("center"));
	  centerX = center._x;
	  centerY = center._y;
	  centerZ = center._z;
  
	  vertices = parseFloatBuffer(jsonMesh.getAsArray("vertices"));
	}
  
  //  const Vector3F    center         = parseVector3F(jsonMesh->getAsArray("center"));
  
  //  IFloatBuffer* vertices  = parseFloatBuffer( jsonMesh->getAsArray("vertices") );
  
	IFloatBuffer normals = parseFloatBuffer(jsonMesh.getAsArray("normals"));
	IFloatBuffer colors = parseFloatBuffer(jsonMesh.getAsArray("colors"));
  // #warning TODO texCoords
	//IFloatBuffer* texCoords = parseFloatBuffer( jsonMesh->getAsArray("texCoords") );
  
	IShortBuffer indices = parseShortBuffer(jsonMesh.getAsArray("indices"));
  
	Mesh mesh;
	if (indices == null)
	{
	  mesh = new DirectMesh(toGLPrimitive(primitive), true, new Vector3D(centerX, centerY, centerZ), vertices, lineWidth, pointSize, material._color, colors, 0, depthTest, normals); // colorsIntensity -  flatColor -  owner
	}
	else
	{
	  mesh = new IndexedMesh(toGLPrimitive(primitive), new Vector3D(centerX, centerY, centerZ), vertices, true, indices, true, lineWidth, pointSize, material._color, colors, 0, depthTest, normals); // colorsIntensity -  flatColor
	}
	return mesh;
  }

  private static Vector3F parseVector3F(JSONArray jsonArray)
  {
	if (jsonArray == null)
	{
	  return Vector3F.zero();
	}
	if (jsonArray.size() < 3)
	{
	  ILogger.instance().logError("Vector3F invalid format");
	  return Vector3F.zero();
	}
  
	final float x = (float) jsonArray.getAsNumber(0, 0);
	final float y = (float) jsonArray.getAsNumber(1, 0);
	final float z = (float) jsonArray.getAsNumber(2, 0);
	return new Vector3F(x, y, z);
  }

  private static Geodetic3D parseGeodetic3D(JSONArray jsonArray)
  {
	if (jsonArray == null)
	{
	  return Geodetic3D.zero();
	}
	final double longitudeInDegrees = jsonArray.getAsNumber(0, 0);
	final double latitudeInDegrees = jsonArray.getAsNumber(1, 0);
	final double height = jsonArray.getAsNumber(2, 0);
	return Geodetic3D.fromDegrees(latitudeInDegrees, longitudeInDegrees, height);
  }

  private static IFloatBuffer parseFloatBuffer(JSONArray jsonArray)
  {
	if (jsonArray == null)
	{
	  return null;
	}
	final int size = jsonArray.size();
	IFloatBuffer result = IFactory.instance().createFloatBuffer(size);
	for (int i = 0; i < size; i++)
	{
	  final float value = (float) jsonArray.getAsNumber(i, 0);
	  result.rawPut(i, value);
	}
	return result;
  }
  private static IShortBuffer parseShortBuffer(JSONArray jsonArray)
  {
	if (jsonArray == null)
	{
	  return null;
	}
	final int size = jsonArray.size();
	IShortBuffer result = IFactory.instance().createShortBuffer(size);
	for (int i = 0; i < size; i++)
	{
	  final short value = (short) jsonArray.getAsNumber(i, 0);
	  result.rawPut(i, value);
	}
	return result;
  
  }

  private static IFloatBuffer parseGeodeticFloatBuffer(JSONArray jsonArray, Geodetic3D geodeticCenter, Vector3D center, Planet planet)
  {
	if (jsonArray == null)
	{
	  return null;
	}
	final int size = jsonArray.size();
	IFloatBuffer result = IFactory.instance().createFloatBuffer(size);
	for (int i = 0; i < size; i += 3)
	{
	  final double longitudeInDegrees = jsonArray.getAsNumber(i, 0) + geodeticCenter._longitude._degrees;
	  final double latitudeInDegrees = jsonArray.getAsNumber(i+1, 0) + geodeticCenter._latitude._degrees;
	  final double height = jsonArray.getAsNumber(i+2, 0) + geodeticCenter._height;
  
	  final Vector3D cartesian = planet.toCartesian(Angle.fromDegrees(latitudeInDegrees), Angle.fromDegrees(longitudeInDegrees), height);
  
	  result.rawPut(i, (float)(cartesian._x - center._x));
	  result.rawPut(i+1, (float)(cartesian._y - center._y));
	  result.rawPut(i+2, (float)(cartesian._z - center._z));
	}
	return result;
  }

  private static int toGLPrimitive(String primitive)
  {
	if (primitive.equals("Triangles"))
	{
	  return GLPrimitive.triangles();
	}
	else if (primitive.equals("TriangleStrip"))
	{
	  return GLPrimitive.triangleStrip();
	}
	else if (primitive.equals("TriangleFan"))
	{
	  return GLPrimitive.triangleFan();
	}
	else if (primitive.equals("Lines"))
	{
	  return GLPrimitive.lines();
	}
	else if (primitive.equals("LineStrip"))
	{
	  return GLPrimitive.lineStrip();
	}
	else if (primitive.equals("LineLoop"))
	{
	  return GLPrimitive.lineLoop();
	}
	else if (primitive.equals("Points"))
	{
	  return GLPrimitive.points();
	}
	else
	{
	  ILogger.instance().logError("Invalid primitive named \"%s\"", primitive.c_str());
	  return GLPrimitive.triangles();
	}
  }

  public static java.util.ArrayList<Mesh> parse(JSONObject jsonObject, Planet planet)
  {
	if (jsonObject == null)
	{
	  return new java.util.ArrayList<Mesh*>();
	}
  
	java.util.HashMap<String, G3MMeshMaterial> materials = parseMaterials(jsonObject.getAsArray("materials"));
  
	tangible.RefObject<java.util.HashMap<String, G3MMeshMaterial>> tempRef_materials = new tangible.RefObject<java.util.HashMap<String, G3MMeshMaterial>>(materials);
	java.util.ArrayList<Mesh> meshes = parseMeshes(tempRef_materials, jsonObject.getAsArray("meshes"), planet);
	materials = tempRef_materials.argvalue;
  
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if C_CODE
	for (java.util.Iterator<String, G3MMeshMaterial> it = materials.iterator(); it.hasNext();)
	{
	  it.next().getValue() = null;
	}
//#endif
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JAVA_CODE
	for (final G3MMeshMaterial material : materials.values())
	{
	  material.dispose();
	}
//#endif
  
	return meshes;
  }
}
