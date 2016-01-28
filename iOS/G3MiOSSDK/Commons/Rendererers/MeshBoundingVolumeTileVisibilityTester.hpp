//
//  MeshBoundingVolumeTileVisibilityTester.hpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 1/28/16.
//
//

#ifndef MeshBoundingVolumeTileVisibilityTester_hpp
#define MeshBoundingVolumeTileVisibilityTester_hpp

#include "TileVisibilityTester.hpp"


class MeshBoundingVolumeTileVisibilityTester : public TileVisibilityTester {
public:

  ~MeshBoundingVolumeTileVisibilityTester() {
#ifdef JAVA_CODE
    super.dispose();
#endif
  }

  bool isVisible(const Tile* tile,
                 const G3MRenderContext* rc) const;

  void onTileHasChangedMesh(const Tile* tile) const;

  void onLayerTilesRenderParametersChanged(const LayerTilesRenderParameters* ltrp);

};

#endif
