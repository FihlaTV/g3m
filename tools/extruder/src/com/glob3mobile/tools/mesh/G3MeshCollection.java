

package com.glob3mobile.tools.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glob3.mobile.generated.JSONArray;
import org.glob3.mobile.generated.JSONObject;


public class G3MeshCollection {

   private final List<G3Mesh> _meshes;


   public G3MeshCollection(final List<G3Mesh> meshes) {
      _meshes = Collections.unmodifiableList(new ArrayList<>(meshes));
   }


   private void validateMeshes() {
      for (final G3Mesh mesh : _meshes) {
         mesh.validate();
      }
   }


   public JSONObject toJSON() {
      validateMeshes();

      final JSONObject result = new JSONObject();
      result.put("materials", materialsJSON(_meshes));
      result.put("meshes", meshesJSON(_meshes));
      return result;
   }


   private static JSONArray meshesJSON(final List<G3Mesh> meshes) {
      final JSONArray result = new JSONArray();
      for (final G3Mesh mesh : meshes) {
         result.add(mesh.toJSON());
      }
      return result;
   }


   private static JSONArray materialsJSON(final List<G3Mesh> meshes) {
      final JSONArray result = new JSONArray();
      final Map<String, G3MeshMaterial> consolidatedMaterials = consolidateMaterials(meshes);
      for (final G3MeshMaterial material : consolidatedMaterials.values()) {
         result.add(material.toJSON());
      }
      return result;
   }


   private static Map<String, G3MeshMaterial> consolidateMaterials(final List<G3Mesh> meshes) {
      final Map<String, G3MeshMaterial> result = new HashMap<>();

      for (final G3Mesh mesh : meshes) {
         final G3MeshMaterial material = mesh.getMaterial();
         final String materialID = material.getID();
         if (!result.containsKey(materialID)) {
            result.put(materialID, material);
         }
      }

      return result;
   }


   //   public void add(final G3Mesh mesh) {
   //      _meshes.add(mesh);
   //   }


}
