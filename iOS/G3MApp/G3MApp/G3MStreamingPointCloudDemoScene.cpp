//
//  G3MStreamingPointCloudDemoScene.cpp
//  G3MApp
//
//  Created by Diego Gomez Deck on 8/19/14.
//  Copyright (c) 2014 Igo Software SL. All rights reserved.
//

#include "G3MStreamingPointCloudDemoScene.hpp"
#include <G3MiOSSDK/MapBoxLayer.hpp>
#include <G3MiOSSDK/LayerSet.hpp>
#include <G3MiOSSDK/G3MWidget.hpp>
#include <G3MiOSSDK/PointCloudsRenderer.hpp>
#include <G3MiOSSDK/URL.hpp>
#include <G3MiOSSDK/DownloadPriority.hpp>

#include "G3MDemoModel.hpp"


class G3MStreamingPointCloudDemoScene_PointCloudMetadataListener : public PointCloudsRenderer::PointCloudMetadataListener {
private:
  G3MWidget* _g3mWidget;
public:
  G3MStreamingPointCloudDemoScene_PointCloudMetadataListener(G3MWidget* g3mWidget) :
  _g3mWidget(g3mWidget)
  {
  }

  void onMetadata(long long pointsCount,
                  const Sector& sector,
                  double minHeight,
                  double maxHeight) {
    _g3mWidget->setAnimatedCameraPosition(Geodetic3D(sector._center, 26000),
                                          Angle::zero(),
                                          Angle::fromDegrees(-25));
//    _g3mWidget->setAnimatedCameraPosition( Geodetic3D(sector._center, 5000) );
  }

};


void G3MStreamingPointCloudDemoScene::rawActivate(const G3MContext *context) {
  G3MDemoModel* model     = getModel();
  G3MWidget*    g3mWidget = model->getG3MWidget();

  MapBoxLayer* layer = new MapBoxLayer("examples.map-m0t0lrpu",
                                       TimeInterval::fromDays(30),
                                       true,
                                       2);
  model->getLayerSet()->addLayer(layer);

#warning TODO cache
  model->getPointCloudsRenderer()->addPointCloud(URL("http://192.168.1.6:8080"),
                                                 "Loudoun-VA_simplified_LOD",
                                                 DownloadPriority::LOWER,
                                                 TimeInterval::zero(),
                                                 false,
                                                 new G3MStreamingPointCloudDemoScene_PointCloudMetadataListener(g3mWidget),
                                                 true);

//  g3mWidget->setAnimatedCameraPosition( Geodetic3D::fromDegrees(39.23148585629368057, -77.641587694782629114, 5000) );

}
