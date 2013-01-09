//
//  GEO2DLineStringGeometry.cpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 11/30/12.
//
//

#include "GEO2DLineStringGeometry.hpp"

#include "Geodetic2D.hpp"

GEO2DLineStringGeometry::~GEO2DLineStringGeometry() {
  const int coordinatesCount = _coordinates->size();
  for (int i = 0; i < coordinatesCount; i++) {
    Geodetic2D* coordinate = _coordinates->at(i);
    delete coordinate;
  }
  delete _coordinates;
}

Mesh* GEO2DLineStringGeometry::createMesh(const G3MRenderContext* rc) {

  return create2DBoundaryMesh(_coordinates, _color, _lineWidth, rc);
}
