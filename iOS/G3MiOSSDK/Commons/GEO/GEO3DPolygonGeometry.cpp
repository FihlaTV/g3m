//
//  GEO3DPolygonGeometry.cpp
//  G3MiOSSDK
//
//  Created by DIEGO RAMIRO GOMEZ-DECK on 10/22/18.
//

#include "GEO3DPolygonGeometry.hpp"

#include "GEOSymbolizer.hpp"
#include "GEO3DPolygonData.hpp"
#include "GEORasterSymbolizer.hpp"

const std::vector<Geodetic3D*>* GEO3DPolygonGeometry::getCoordinates() const {
  return _polygonData->getCoordinates();
}

const std::vector<std::vector<Geodetic3D*>*>* GEO3DPolygonGeometry::getHolesCoordinatesArray() const {
  return _polygonData->getHolesCoordinatesArray();
}

GEO3DPolygonGeometry::~GEO3DPolygonGeometry() {
  if (_polygonData) {
    _polygonData->_release();
  }
#ifdef JAVA_CODE
  super.dispose();
#endif
}

std::vector<GEOSymbol*>* GEO3DPolygonGeometry::createSymbols(const GEOSymbolizer* symbolizer) const {
  return symbolizer->createSymbols(this);
}

std::vector<GEORasterSymbol*>* GEO3DPolygonGeometry::createRasterSymbols(const GEORasterSymbolizer* symbolizer) const {
  return symbolizer->createSymbols(this);
}

long long GEO3DPolygonGeometry::getCoordinatesCount() const {
  return (_polygonData == NULL) ? 0 : _polygonData->getCoordinatesCount();
}

GEO3DPolygonGeometry* GEO3DPolygonGeometry::deepCopy() const {
  if (_polygonData != NULL) {
    _polygonData->_retain();
  }
  return new GEO3DPolygonGeometry(_polygonData);
}

bool GEO3DPolygonGeometry::contain(const Geodetic3D& point) const {
  if (_polygonData) {
    return _polygonData->contains(point);
  }
  return false;
}

const Sector* GEO3DPolygonGeometry::calculateSector() const {
  return (_polygonData == NULL) ? NULL : _polygonData->getSector();
}
