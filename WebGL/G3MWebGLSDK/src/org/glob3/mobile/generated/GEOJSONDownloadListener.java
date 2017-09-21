

package org.glob3.mobile.generated;

//
//  SceneParserDownloadListener.cpp
//  G3MiOSSDK
//
//  Created by Eduardo de la Montaña on 26/10/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//


//
//  SceneParserDownloadListener.h
//  G3MiOSSDK
//
//  Created by Eduardo de la Montaña on 26/10/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//


//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class MarksRenderer;
public class GEOJSONDownloadListener
         extends
            IBufferDownloadListener {

   private static final String FEATURES     = "features";
   private static final String GEOMETRY     = "geometry";
   private static final String TYPE         = "type";
   private static final String COORDINATES  = "coordinates";
   private static final String PROPERTIES   = "properties";
   private static final String DENOMINATION = "DENOMINACI";
   private static final String CLASE        = "CLASE";


   private final MarksRenderer _marksRenderer;
   private final String        _icon;


   public GEOJSONDownloadListener(final MarksRenderer marksRenderer,
                                  final String icon) {
      _marksRenderer = marksRenderer;
      _icon = icon;
   }


   @Override
   public final void onError(final URL url) {
      ILogger.instance().logError("The requested geojson file could not be found!");
   }


   @Override
   public final void onCancel(final URL url) {
   }


   @Override
   public void dispose() {
   }


   private void parseGEOJSON(final JSONObject geojson) {
      final JSONArray jsonFeatures = geojson.get(FEATURES).asArray();
      for (int i = 0; i < jsonFeatures.size(); i++) {
         final JSONObject jsonFeature = jsonFeatures.getAsObject(i);
         final JSONObject jsonGeometry = jsonFeature.getAsObject(GEOMETRY);
         final String jsonType = jsonGeometry.getAsString(TYPE).value();
         if (jsonType.equals("Point")) {
            parsePointObject(jsonFeature);
         }
      }
   }


   private void parsePointObject(final JSONObject point) {
      final JSONObject jsonProperties = point.getAsObject(PROPERTIES);
      final JSONObject jsonGeometry = point.getAsObject(GEOMETRY);
      final JSONArray jsonCoordinates = jsonGeometry.getAsArray(COORDINATES);

      final JSONBaseObject denominaci = jsonProperties.get(DENOMINATION);
      final JSONBaseObject clase = jsonProperties.get(CLASE);

      if ((denominaci != null) && (clase != null)) {

         final IStringBuilder iconUrl = IStringBuilder.newStringBuilder();
         iconUrl.addString("http://glob3m.glob3mobile.com/icons/markers/ayto/");
         iconUrl.addString(_icon);
         iconUrl.addString(".png");

         final IStringBuilder name = IStringBuilder.newStringBuilder();
         name.addString(clase.asString().value());
         name.addString(" ");
         name.addString(denominaci.asString().value());

         final Angle latitude = Angle.fromDegrees(jsonCoordinates.getAsNumber(1).value());
         final Angle longitude = Angle.fromDegrees(jsonCoordinates.getAsNumber(0).value());

         final Mark mark = new Mark(name.getString(), new URL(iconUrl.getString(), false),
                  new Geodetic3D(latitude, longitude, 0), null, 10000);

         _marksRenderer.addMark(mark);
      }
   }


   @Override
   public void onDownload(final URL url,
                          final IByteBuffer buffer,
                          final boolean expired) {
      final String String = buffer.getAsString();
      final JSONObject json = IJSONParser.instance().parse(String).asObject();
      ILogger.instance().logInfo(url.getPath());
      parseGEOJSON(json);
   }


   @Override
   public void onCanceledDownload(final URL url,
                                  final IByteBuffer buffer,
                                  final boolean expired) {
      // TODO Auto-generated method stub

   }

}
