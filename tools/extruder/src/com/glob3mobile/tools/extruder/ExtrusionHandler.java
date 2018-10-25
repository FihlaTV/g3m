

package com.glob3mobile.tools.extruder;

import java.util.List;

import org.glob3.mobile.generated.GEOFeature;
import org.glob3.mobile.generated.GEOObject;

import com.glob3mobile.tools.mesh.G3MeshCollection;
import com.glob3mobile.tools.mesh.G3MeshMaterial;


public interface ExtrusionHandler {


   void onRootGEOObject(GEOObject geoObject);


   boolean extrudes(GEOFeature geoFeature);


   G3MeshMaterial getMaterialFor(GEOFeature geoFeature);


   Heigths getHeightsFor(GEOFeature geoFeature);


   void processTriangulationError(GEOFeature geoFeature);


   void onBuildings(List<Building> buildings);


   void onPolygons(List<ExtruderPolygon> polygons);


   void onMeshCollection(G3MeshCollection meshes);

}
