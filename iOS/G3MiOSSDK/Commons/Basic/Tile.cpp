//
//  Tile.cpp
//  G3MiOSSDK
//
//  Created by Agustin Trujillo Pino on 12/06/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#include "Tile.hpp"

#include "TileRenderingListener.hpp"
#include "Mesh.hpp"
#include "ITexturizerData.hpp"
#include "ElevationData.hpp"
#include "TileElevationDataRequest.hpp"
#include "PlanetTileTessellator.hpp"
#include "TileTexturizer.hpp"
#include "LayerTilesRenderParameters.hpp"
#include "TilesRenderParameters.hpp"
#include "MeshHolder.hpp"
#include "PlanetRenderer.hpp"
#include "FlatColorMesh.hpp"
#include "MercatorUtils.hpp"
#include "DecimatedSubviewElevationData.hpp"

std::string Tile::createTileId(int level,
                               int row,
                               int column) {
#ifdef C_CODE
  IStringBuilder* isb = IStringBuilder::newStringBuilder();
  isb->addInt(level);
  isb->addString("/");
  isb->addInt(row);
  isb->addString("/");
  isb->addInt(column);
  std::string s = isb->getString();
  delete isb;
  return s;
#endif
#ifdef JAVA_CODE
  return level + "/" + row + "/" + column;
#endif
}

Tile::Tile(TileTexturizer* texturizer,
           Tile* parent,
           const Sector& sector,
           const bool mercator,
           int level,
           int row,
           int column,
           const PlanetRenderer* planetRenderer,
           TileCache* tileCache,
           bool deleteTextureWhenNotVisible):
_texturizer(texturizer),
_parent(parent),
_sector(sector),
_mercator(mercator),
_level(level),
_row(row),
_column(column),
_tessellatorMesh(NULL),
_debugMesh(NULL),
_flatColorMesh(NULL),
_texturizedMesh(NULL),
_textureSolved(false),
_texturizerDirty(true),
_subtiles(NULL),
_justCreatedSubtiles(false),
_isVisible(false),
_texturizerData(NULL),
_elevationData(NULL),
_elevationDataLevel(-1),
_elevationDataRequest(NULL),
_verticalExaggeration(0),
_mustActualizeMeshDueToNewElevationData(false),
_lastTileMeshResolutionX(-1),
_lastTileMeshResolutionY(-1),
_lastMeetsRenderCriteriaTimeInMS(0),
_planetRenderer(planetRenderer),
_tessellatorData(NULL),
_northWestPoint(NULL),
_northEastPoint(NULL),
_southWestPoint(NULL),
_southEastPoint(NULL),
_northArcSegmentRatioSquared(0),
_southArcSegmentRatioSquared(0),
_eastArcSegmentRatioSquared(0),
_westArcSegmentRatioSquared(0),
_rendered(false),
_tileRenderingListener(NULL),
_id( createTileId(level, row, column) ),
_tileCache(tileCache),
_deleteTextureWhenNotVisible(deleteTextureWhenNotVisible)
{
  //  int __remove_tile_print;
  //  printf("Created tile=%s\n deltaLat=%s deltaLon=%s\n",
  //         getKey().description().c_str(),
  //         _sector._deltaLatitude.description().c_str(),
  //         _sector._deltaLongitude.description().c_str()
  //         );
}

Tile::~Tile() {
  prune(NULL, NULL);
  
  if (_tileRenderingListener != NULL) {
    if (_rendered) {
      _tileRenderingListener->stopRendering(this);
    }
  }
  
  delete _debugMesh;
  _debugMesh = NULL;
  
  delete _flatColorMesh;
  _flatColorMesh = NULL;
  
  delete _tessellatorMesh;
  _tessellatorMesh = NULL;
  
  delete _texturizerData;
  _texturizerData = NULL;
  
  delete _texturizedMesh;
  _texturizedMesh = NULL;
  
  delete _elevationData;
  _elevationData = NULL;
  
  if (_elevationDataRequest != NULL) {
    _elevationDataRequest->cancelRequest(); //The listener will auto delete
    delete _elevationDataRequest;
    _elevationDataRequest = NULL;
  }
  
  delete _tessellatorData;
  
  delete _northWestPoint;
  delete _northEastPoint;
  delete _southWestPoint;
  delete _southEastPoint;
}

void Tile::setTexturizerData(ITexturizerData* texturizerData) {
  if (texturizerData != _texturizerData) {
    delete _texturizerData;
    _texturizerData = texturizerData;
  }
}


void Tile::ancestorTexturedSolvedChanged(Tile* ancestor,
                                         bool textureSolved) {
  if (textureSolved && isTextureSolved()) {
    return;
  }
  
  if (_texturizer != NULL) {
    _texturizer->ancestorTexturedSolvedChanged(this, ancestor, textureSolved);
  }
  
  if (_subtiles != NULL) {
    const int subtilesSize = _subtiles->size();
    for (int i = 0; i < subtilesSize; i++) {
      Tile* subtile = _subtiles->at(i);
      subtile->ancestorTexturedSolvedChanged(ancestor, textureSolved);
    }
  }
}

void Tile::setTextureSolved(bool textureSolved) {
  if (textureSolved != _textureSolved) {
    _textureSolved = textureSolved;
    
    if (_textureSolved) {
      delete _texturizerData;
      _texturizerData = NULL;
    }
    
    if (_debugMesh != NULL){
      delete _debugMesh;
      _debugMesh = NULL;
    }

    if (_subtiles != NULL) {
      const int subtilesSize = _subtiles->size();
      for (int i = 0; i < subtilesSize; i++) {
        Tile* subtile = _subtiles->at(i);
        subtile->ancestorTexturedSolvedChanged(this, _textureSolved);
      }
    }
  }
}

Mesh* Tile::getTessellatorMesh(const G3MRenderContext* rc,
                               ElevationDataProvider* elevationDataProvider,
                               const TileTessellator* tessellator,
                               const LayerTilesRenderParameters* layerTilesRenderParameters,
                               const TilesRenderParameters* tilesRenderParameters) {
  
  
  if ( (_elevationData == NULL) && (elevationDataProvider != NULL) && (elevationDataProvider->isEnabled()) ) {
    initializeElevationData(elevationDataProvider,
                            tessellator,
                            layerTilesRenderParameters->_tileMeshResolution,
                            rc->getPlanet(),
                            tilesRenderParameters->_renderDebug);
  }
  
  if ( (_tessellatorMesh == NULL) || _mustActualizeMeshDueToNewElevationData ) {
    _mustActualizeMeshDueToNewElevationData = false;
    
    if (elevationDataProvider == NULL) {
      // no elevation data provider, just create a simple mesh without elevation
      _tessellatorMesh = tessellator->createTileMesh(rc->getPlanet(),
                                                     layerTilesRenderParameters->_tileMeshResolution,
                                                     this,
                                                     NULL,
                                                     _verticalExaggeration,
                                                     tilesRenderParameters->_renderDebug,
                                                     _tileTessellatorMeshData);
      
      computeTileCorners(rc->getPlanet());
    }
    else {
      Mesh* tessellatorMesh = tessellator->createTileMesh(rc->getPlanet(),
                                                          layerTilesRenderParameters->_tileMeshResolution,
                                                          this,
                                                          _elevationData,
                                                          _verticalExaggeration,
                                                          tilesRenderParameters->_renderDebug,
                                                          _tileTessellatorMeshData);
      
      MeshHolder* meshHolder = (MeshHolder*) _tessellatorMesh;
      if (meshHolder == NULL) {
        meshHolder = new MeshHolder(tessellatorMesh);
        _tessellatorMesh = meshHolder;
      }
      else {
        meshHolder->setMesh(tessellatorMesh);
      }
      
      computeTileCorners(rc->getPlanet());
    }
    
    //Notifying when the tile is first created and every time the elevation data changes
    _planetRenderer->sectorElevationChanged(_elevationData);
  }
  
  return _tessellatorMesh;
}

Mesh* Tile::getDebugMesh(const G3MRenderContext* rc,
                         const TileTessellator* tessellator,
                         const LayerTilesRenderParameters* layerTilesRenderParameters) {
  if (_debugMesh == NULL) {
    const Vector2I tileMeshResolution(layerTilesRenderParameters->_tileMeshResolution);
    Color color = isTextureSolved()? Color::blue() : Color::red();

    //TODO: CHECK
    _debugMesh = tessellator->createTileDebugMesh(rc->getPlanet(), tileMeshResolution, this, color);
  }
  return _debugMesh;
}


const BoundingVolume* Tile::getBoundingVolume(const G3MRenderContext* rc,
                                              ElevationDataProvider* elevationDataProvider,
                                              const TileTessellator* tessellator,
                                              const LayerTilesRenderParameters* layerTilesRenderParameters,
                                              const TilesRenderParameters* tilesRenderParameters) {

    Mesh* mesh = getTessellatorMesh(rc,
                                    elevationDataProvider,
                                    tessellator,
                                    layerTilesRenderParameters,
                                    tilesRenderParameters);
    if (mesh != NULL) {
      return mesh->getBoundingVolume();
    } else{
      return NULL;
    }
}

bool Tile::isVisible(const G3MRenderContext* rc,
                     const Planet* planet,
                     const Vector3D& cameraNormalizedPosition,
                     double cameraAngle2HorizonInRadians,
                     const Frustum* cameraFrustumInModelCoordinates,
                     const Frustum* cameraWiderFrustumInModelCoordinates,
                     ElevationDataProvider* elevationDataProvider,
                     const Sector* renderedSector,
                     const TileTessellator* tessellator,
                     const LayerTilesRenderParameters* layerTilesRenderParameters,
                     const TilesRenderParameters* tilesRenderParameters) {
  
  if (renderedSector != NULL && !renderedSector->touchesWith(_sector)) { //Incomplete world
    return false;
  }
  
  const BoundingVolume* boundingVolume = getBoundingVolume(rc,
                                                           elevationDataProvider,
                                                           tessellator,
                                                           layerTilesRenderParameters,
                                                           tilesRenderParameters);
    return ((boundingVolume != NULL)  &&
          boundingVolume->touchesFrustum(cameraWiderFrustumInModelCoordinates));
}

bool Tile::meetsRenderCriteria(const G3MRenderContext* rc,
                               const LayerTilesRenderParameters* layerTilesRenderParameters,
                               TileTexturizer* texturizer,
                               const TilesRenderParameters* tilesRenderParameters,
                               const TilesStatistics* tilesStatistics,
                               const ITimer* lastSplitTimer,
                               double texWidthSquared,
                               double texHeightSquared,
                               double nowInMS) {
  
  if ((_level >= layerTilesRenderParameters->_maxLevelForPoles) &&
      (_sector.touchesPoles())) {
    return true;
  }
  
  if (_level >= layerTilesRenderParameters->_maxLevel) {
    return true;
  }
  
  if (texturizer != NULL) {
    if (texturizer->tileMeetsRenderCriteria(this)) {
      return true;
    }
  }
  
  if (_lastMeetsRenderCriteriaTimeInMS != 0 &&
      (nowInMS - _lastMeetsRenderCriteriaTimeInMS) < 250 /*500*/ ) {
    return _lastMeetsRenderCriteriaResult;
  }
  
  if (tilesRenderParameters->_useTilesSplitBudget) {
    if (_subtiles == NULL && (_tileCache == NULL || !_tileCache->has4SubTilesCached(this))) { // the tile needs to create the subtiles
      if (lastSplitTimer->elapsedTimeInMilliseconds() < 67) {
        // there are not more time-budget to spend
        return true;
      }
    }
  }
  
  _lastMeetsRenderCriteriaTimeInMS = nowInMS; //Storing time of result
  
  
  if ((_northArcSegmentRatioSquared == 0) ||
      (_southArcSegmentRatioSquared == 0) ||
      (_eastArcSegmentRatioSquared  == 0) ||
      (_westArcSegmentRatioSquared  == 0)) {
    prepareTestLODData( rc->getPlanet() );
  }
  
  const Camera* camera = rc->getCurrentCamera();
  
  const double distanceInPixelsNorth = camera->getEstimatedPixelDistance(*_northWestPoint, *_northEastPoint);
  const double distanceInPixelsSouth = camera->getEstimatedPixelDistance(*_southWestPoint, *_southEastPoint);
  const double distanceInPixelsWest  = camera->getEstimatedPixelDistance(*_northWestPoint, *_southWestPoint);
  const double distanceInPixelsEast  = camera->getEstimatedPixelDistance(*_northEastPoint, *_southEastPoint);
  
  const double distanceInPixelsSquaredArcNorth = (distanceInPixelsNorth * distanceInPixelsNorth) * _northArcSegmentRatioSquared;
  const double distanceInPixelsSquaredArcSouth = (distanceInPixelsSouth * distanceInPixelsSouth) * _southArcSegmentRatioSquared;
  const double distanceInPixelsSquaredArcWest  = (distanceInPixelsWest  * distanceInPixelsWest)  * _westArcSegmentRatioSquared;
  const double distanceInPixelsSquaredArcEast  = (distanceInPixelsEast  * distanceInPixelsEast)  * _eastArcSegmentRatioSquared;
  
  
#warning ÑAPA
  double augmentedFactor = 1.0;
  for (int i = 0; i < _lODAugmentedSectors.size(); i++) {
    Sector* sector = _lODAugmentedSectors[i]._sector;
    if (_sector.touchesWith(*sector)){
      augmentedFactor = _lODAugmentedSectors[i]._lodFactor;
    }
  }
  
  if (augmentedFactor != 1.0){
    augmentedFactor *= augmentedFactor;
    texHeightSquared /= augmentedFactor ;
    texWidthSquared /= augmentedFactor;
  }
  
  
//  Sector bandamaSector = Sector::fromDegrees(28.0184736385041, -15.4667703990175,
//                                             28.0501939378331, -15.447509868283);
//  
//  if (_sector.touchesWith(bandamaSector)){
//    texHeightSquared /= 16.0;
//    texWidthSquared /= 16.0;
//  }
  
  _lastMeetsRenderCriteriaResult = ((distanceInPixelsSquaredArcNorth <= texHeightSquared) &&
                                    (distanceInPixelsSquaredArcSouth <= texHeightSquared) &&
                                    (distanceInPixelsSquaredArcWest  <= texWidthSquared ) &&
                                    (distanceInPixelsSquaredArcEast  <= texWidthSquared ));
  
  return _lastMeetsRenderCriteriaResult;
}

void Tile::prepareForFullRendering(const G3MRenderContext* rc,
                                   TileTexturizer* texturizer,
                                   ElevationDataProvider* elevationDataProvider,
                                   const TileTessellator* tessellator,
                                   TileRasterizer* tileRasterizer,
                                   const LayerTilesRenderParameters* layerTilesRenderParameters,
                                   const LayerSet* layerSet,
                                   const TilesRenderParameters* tilesRenderParameters,
                                   bool forceFullRender,
                                   long long tileDownloadPriority,
                                   float verticalExaggeration,
                                   bool logTilesPetitions) {
  
  //You have to set _verticalExaggertion
  if (verticalExaggeration != _verticalExaggeration) {
    // TODO: verticalExaggeration changed, invalidate tileExtent, Mesh, etc.
    _verticalExaggeration = verticalExaggeration;
  }
  
  
  Mesh* tessellatorMesh = getTessellatorMesh(rc,
                                             elevationDataProvider,
                                             tessellator,
                                             layerTilesRenderParameters,
                                             tilesRenderParameters);
  if (tessellatorMesh == NULL) {
    return;
  }
  
  if (texturizer != NULL) {
    const bool needsToCallTexturizer = (_texturizedMesh == NULL) || isTexturizerDirty();
    
    if (needsToCallTexturizer) {
      _texturizedMesh = texturizer->texturize(rc,
                                              tessellator,
                                              tileRasterizer,
                                              layerTilesRenderParameters,
                                              layerSet,
                                              forceFullRender,
                                              tileDownloadPriority,
                                              this,
                                              tessellatorMesh,
                                              _texturizedMesh,
                                              logTilesPetitions);
    }
  }
}

int MAX_LOD = -1;

void Tile::rawRender(const G3MRenderContext* rc,
                     const GLState* glState,
                     TileTexturizer* texturizer,
                     ElevationDataProvider* elevationDataProvider,
                     const TileTessellator* tessellator,
                     TileRasterizer* tileRasterizer,
                     const LayerTilesRenderParameters* layerTilesRenderParameters,
                     const LayerSet* layerSet,
                     const TilesRenderParameters* tilesRenderParameters,
                     bool forceFullRender,
                     long long tileDownloadPriority,
                     bool logTilesPetitions) {
  
  Mesh* tessellatorMesh = getTessellatorMesh(rc,
                                             elevationDataProvider,
                                             tessellator,
                                             layerTilesRenderParameters,
                                             tilesRenderParameters);
  if (tessellatorMesh == NULL) {
    return;
  }
  
  if (texturizer == NULL) {
    tessellatorMesh->render(rc, glState);
  }
  else {
    const bool needsToCallTexturizer = (_texturizedMesh == NULL) || isTexturizerDirty();
    
    if (needsToCallTexturizer) {
      _texturizedMesh = texturizer->texturize(rc,
                                              tessellator,
                                              tileRasterizer,
                                              layerTilesRenderParameters,
                                              layerSet,
                                              forceFullRender,
                                              tileDownloadPriority,
                                              this,
                                              tessellatorMesh,
                                              _texturizedMesh,
                                              logTilesPetitions);
    }
    
    if (_texturizedMesh != NULL) {
      _texturizedMesh->render(rc, glState);
    }
    else {
      //Adding flat color if no texture set on the mesh
      if (_flatColorMesh == NULL) {
        _flatColorMesh = new FlatColorMesh(tessellatorMesh, false,
                                           Color::newFromRGBA((float) 1.0, (float) 1.0, (float) 1.0, (float) 1.0), true);
      }
      _flatColorMesh->render(rc, glState);
    }
  }

  if (_level > MAX_LOD){
    MAX_LOD = _level;
    ILogger::instance()->logInfo("MAXLOD = %d\n", _level);
  }

  //  const BoundingVolume* boundingVolume = getBoundingVolume(rc, trc);
  //  boundingVolume->render(rc, parentState);
}

void Tile::debugRender(const G3MRenderContext* rc,
                       const GLState* glState,
                       const TileTessellator* tessellator,
                       const LayerTilesRenderParameters* layerTilesRenderParameters) {
  Mesh* debugMesh = getDebugMesh(rc, tessellator, layerTilesRenderParameters);
  if (debugMesh != NULL) {
    debugMesh->render(rc, glState);
  }
}


std::vector<Tile*>* Tile::getSubTiles() {
  if (_subtiles != NULL) {
    // quick check to avoid splitLongitude/splitLatitude calculation
    return _subtiles;
  }
  
  const Geodetic2D lower = _sector._lower;
  const Geodetic2D upper = _sector._upper;
  
  const Angle splitLongitude = Angle::midAngle(lower._longitude,
                                               upper._longitude);
  
  
  const Angle splitLatitude = _mercator
  /*                               */ ? MercatorUtils::calculateSplitLatitude(lower._latitude,
                                                                              upper._latitude)
  /*                               */ : Angle::midAngle(lower._latitude,
                                                        upper._latitude);
  
  return getSubTiles(splitLatitude, splitLongitude);
}


std::vector<Tile*>* Tile::getSubTiles(const Angle& splitLatitude,
                                      const Angle& splitLongitude) {
  if (_subtiles == NULL) {
    //Checking if subtiles are gonna be created or fetched from cache
    _justCreatedSubtiles = (_tileCache == NULL || !_tileCache->has4SubTilesCached(this));
    _subtiles = createSubTiles(splitLatitude, splitLongitude, true);
  }
  return _subtiles;
}

void Tile::toBeDeleted(TileTexturizer*        texturizer,
                       ElevationDataProvider* elevationDataProvider) {
  if (texturizer != NULL) {
    texturizer->tileToBeDeleted(this, _texturizedMesh);
  }
  
  if (elevationDataProvider != NULL) {
    if (_elevationDataRequest != NULL) {
      _elevationDataRequest->cancelRequest();
    }
  }
}

void Tile::prune(TileTexturizer*        texturizer,
                 ElevationDataProvider* elevationDataProvider) {
  if (_subtiles != NULL) {
    //Notifying elevation event when LOD decreases
    _planetRenderer->sectorElevationChanged(_elevationData);
    
    const int subtilesSize = _subtiles->size();
    for (int i = 0; i < subtilesSize; i++) {
      Tile* subtile = _subtiles->at(i);
      
      subtile->setIsVisible(false, texturizer);
      
      subtile->prune(texturizer, elevationDataProvider);
      
      if (_tileCache == NULL){
        
        if (texturizer != NULL) {
          texturizer->tileToBeDeleted(subtile, subtile->_texturizedMesh);
        }
        
        delete subtile;
      } else{
        _tileCache->clearTile(subtile);
      }
    }
    
    delete _subtiles;
    _subtiles = NULL;
  }
}

void Tile::setIsVisible(bool isVisible,
                        TileTexturizer* texturizer) {
  if (_isVisible != isVisible) {
    _isVisible = isVisible;
    
    if (_deleteTextureWhenNotVisible && !_isVisible) {
      deleteTexturizedMesh(texturizer);
    }
  }
}

void Tile::deleteTexturizedMesh(TileTexturizer* texturizer) {
  // check for (_parent != NULL) to avoid deleting the firstLevel tiles.
  // in this case, the mesh is always loaded (as well as its texture) to be the last option
  // falback texture for any tile
  if ((_parent != NULL) && (_texturizedMesh != NULL)) {
    
    if (texturizer != NULL) {
      texturizer->tileMeshToBeDeleted(this, _texturizedMesh);
    }
    
    delete _texturizedMesh;
    _texturizedMesh = NULL;
    
    delete _texturizerData;
    _texturizerData = NULL;
    
    setTexturizerDirty(true);
    setTextureSolved(false);
  }
}

void Tile::setRendered(const bool rendered, TileRenderingListener* tileRenderingListener){
  if (_rendered != rendered) {
    _rendered = rendered;

    if (tileRenderingListener != NULL) {
      if (_rendered) {
        tileRenderingListener->startRendering(this);
      }
      else {
        tileRenderingListener->stopRendering(this);
      }
    }
  }
}


bool Tile::render(const G3MRenderContext* rc,
                  const GLState& parentState,
                  std::list<Tile*>* toVisitInNextIteration,
                  const Planet* planet,
                  const Vector3D& cameraNormalizedPosition,
                  double cameraAngle2HorizonInRadians,
                  const Frustum* cameraFrustumInModelCoordinates,
                  const Frustum* cameraWiderFrustumInModelCoordinates,
                  TilesStatistics* tilesStatistics,
                  const float verticalExaggeration,
                  const LayerTilesRenderParameters* layerTilesRenderParameters,
                  TileTexturizer* texturizer,
                  const TilesRenderParameters* tilesRenderParameters,
                  ITimer* lastSplitTimer,
                  ElevationDataProvider* elevationDataProvider,
                  const TileTessellator* tessellator,
                  TileRasterizer* tileRasterizer,
                  const LayerSet* layerSet,
                  const Sector* renderedSector,
                  bool forceFullRender,
                  long long tileDownloadPriority,
                  double texWidthSquared,
                  double texHeightSquared,
                  double nowInMS,
                  const bool renderTileMeshes,
                  bool logTilesPetitions,
                  TileRenderingListener* tileRenderingListener) {
  
  tilesStatistics->computeTileProcessed(this);
  
  if (verticalExaggeration != _verticalExaggeration) {
    // TODO: verticalExaggeration changed, invalidate tileExtent, Mesh, etc.
    _verticalExaggeration = verticalExaggeration;
  }
  
  bool rendered = false;
  
  if (isVisible(rc,
                planet,
                cameraNormalizedPosition,
                cameraAngle2HorizonInRadians,
                cameraFrustumInModelCoordinates,
                cameraWiderFrustumInModelCoordinates,
                elevationDataProvider,
                renderedSector,
                tessellator,
                layerTilesRenderParameters,
                tilesRenderParameters)) {
    setIsVisible(true, texturizer);
    
    tilesStatistics->computeVisibleTile(this);
    
    const bool isRawRender = (
                              (toVisitInNextIteration == NULL) ||
                              meetsRenderCriteria(rc,
                                                  layerTilesRenderParameters,
                                                  texturizer,
                                                  tilesRenderParameters,
                                                  tilesStatistics,
                                                  lastSplitTimer,
                                                  texWidthSquared,
                                                  texHeightSquared,
                                                  nowInMS) ||
                              (tilesRenderParameters->_incrementalTileQuality && !_textureSolved)
                              );
    
    if (isRawRender) {
      
      const long long tileTexturePriority = (tilesRenderParameters->_incrementalTileQuality
                                             ? tileDownloadPriority + layerTilesRenderParameters->_maxLevel - _level
                                             : tileDownloadPriority + _level);
      
      rendered = true;
      if (renderTileMeshes) {
        rawRender(rc,
                  &parentState,
                  texturizer,
                  elevationDataProvider,
                  tessellator,
                  tileRasterizer,
                  layerTilesRenderParameters,
                  layerSet,
                  tilesRenderParameters,
                  forceFullRender,
                  tileTexturePriority,
                  logTilesPetitions);
      }
      if (tilesRenderParameters->_renderDebug) {
        debugRender(rc, &parentState, tessellator, layerTilesRenderParameters);
      }
      
      tilesStatistics->computeTileRenderered(this);
      
      prune(texturizer, elevationDataProvider);
      //TODO: AVISAR CAMBIO DE TERRENO
    }
    else {
      std::vector<Tile*>* subTiles = getSubTiles();
      if (_justCreatedSubtiles) {
        lastSplitTimer->start();
        _justCreatedSubtiles = false;
      }
      
      const int subTilesSize = (int)subTiles->size();
      for (int i = 0; i < subTilesSize; i++) {
        Tile* subTile = subTiles->at(i);
        toVisitInNextIteration->push_back(subTile);
      }
    }
    
    setRendered(rendered, tileRenderingListener);
    
    return isRawRender; //RETURN ISRAWRENDER
  }

  //Not rendering, prunning will be performed
  
  setRendered(rendered, tileRenderingListener);
  setIsVisible(false, texturizer);
  prune(texturizer, elevationDataProvider); //TODO: AVISAR CAMBIO DE TERRENO
  return false;

}

Tile* Tile::createSubTile(const Angle& lowerLat, const Angle& lowerLon,
                          const Angle& upperLat, const Angle& upperLon,
                          const int level,
                          const int row, const int column,
                          bool setParent) {
  Tile* parent = setParent ? this : NULL;
  return new Tile(_texturizer,
                  parent,
                  Sector(Geodetic2D(lowerLat, lowerLon), Geodetic2D(upperLat, upperLon)),
                  _mercator,
                  level,
                  row, column,
                  _planetRenderer,
                  _tileCache,
                  _deleteTextureWhenNotVisible);
}

std::vector<Tile*>* Tile::createSubTiles(const Angle& splitLatitude,
                                         const Angle& splitLongitude,
                                         bool setParent) {
  const Geodetic2D lower = _sector._lower;
  const Geodetic2D upper = _sector._upper;
  
  const int nextLevel = _level + 1;
  
  const int row2    = 2 * _row;
  const int column2 = 2 * _column;
  
  std::vector<Tile*>* subTiles = new std::vector<Tile*>();
  
  const Sector* renderedSector = _planetRenderer->getRenderedSector();
  
  Sector s1(Geodetic2D(lower._latitude, lower._longitude), Geodetic2D(splitLatitude, splitLongitude));
  if (renderedSector == NULL || renderedSector->touchesWith(s1)) {
    Tile* tile = _tileCache == NULL? NULL : _tileCache->getSubTileFromCache(nextLevel, row2, column2);
    
    if (tile == NULL){
      tile = createSubTile(lower._latitude, lower._longitude,
                           splitLatitude, splitLongitude,
                           nextLevel,
                           row2,
                           column2,
                           setParent);
    }
    
    subTiles->push_back( tile);
  }
  
  Sector s2(Geodetic2D(lower._latitude, splitLongitude), Geodetic2D(splitLatitude, upper._longitude));
  if (renderedSector == NULL || renderedSector->touchesWith(s2)) {
    Tile* tile = _tileCache == NULL? NULL : _tileCache->getSubTileFromCache(nextLevel, row2, column2 + 1);
    
    if (tile == NULL){
      tile = createSubTile(lower._latitude, splitLongitude,
                           splitLatitude, upper._longitude,
                           nextLevel,
                           row2,
                           column2 + 1,
                           setParent);
    }
    
    subTiles->push_back( tile);
  }
  
  Sector s3(Geodetic2D(splitLatitude, lower._longitude), Geodetic2D(upper._latitude, splitLongitude));
  if (renderedSector == NULL || renderedSector->touchesWith(s3)) {
    Tile* tile = _tileCache == NULL? NULL : _tileCache->getSubTileFromCache(nextLevel, row2 + 1, column2);
    
    if (tile == NULL){
      tile = createSubTile(splitLatitude, lower._longitude,
                           upper._latitude, splitLongitude,
                           nextLevel,
                           row2 + 1,
                           column2,
                           setParent);
      
    }
    
    subTiles->push_back( tile);
  }
  
  Sector s4(Geodetic2D(splitLatitude, splitLongitude), Geodetic2D(upper._latitude, upper._longitude));
  if (renderedSector == NULL || renderedSector->touchesWith(s4)) {
    
    Tile* tile = _tileCache == NULL? NULL : _tileCache->getSubTileFromCache(nextLevel, row2 + 1, column2 + 1);
    
    if (tile == NULL){
      tile = createSubTile(splitLatitude, splitLongitude,
                           upper._latitude, upper._longitude,
                           nextLevel,
                           row2 + 1,
                           column2 + 1,
                           setParent);
      
      
    }
    
    subTiles->push_back( tile);
  }
  
  return subTiles;
}

//const TileKey Tile::getKey() const {
//  return TileKey(_level, _row, _column);
//}

const Tile* Tile::getDeepestTileContaining(const Geodetic3D& position) const {
  if (_sector.contains(position._latitude, position._longitude)) {
    if (_subtiles == NULL) {
      return this;
    }
    
    for (int i = 0; i < _subtiles->size(); i++) {
      const Tile* subtile = _subtiles->at(i);
      const Tile* subtileResult = subtile->getDeepestTileContaining(position);
      if (subtileResult != NULL) {
        return subtileResult;
      }
    }
  }
  
  return NULL;
}

const std::string Tile::description() const {
  IStringBuilder* isb = IStringBuilder::newStringBuilder();
  isb->addString("(Tile");
  isb->addString(" level=");
  isb->addInt(_level);
  isb->addString(", row=");
  isb->addInt(_row);
  isb->addString(", column=");
  isb->addInt(_column);
  isb->addString(", sector=");
  isb->addString(_sector.description());
  isb->addString(")");
  const std::string s = isb->getString();
  delete isb;
  return s;
}

#pragma mark ElevationData methods

void Tile::setElevationData(ElevationData* ed, int level) {
  if (_elevationDataLevel < level) {
    
    if (_elevationData != NULL) {
      delete _elevationData;
    }
    
    _elevationData = ed;
    _elevationDataLevel = level;
    _mustActualizeMeshDueToNewElevationData = true;
    
    //If the elevation belongs to tile's level, we notify the sub-tree
    if (isElevationDataSolved()) {
      if (_subtiles != NULL) {
        const int subtilesSize = _subtiles->size();
        for (int i = 0; i < subtilesSize; i++) {
          Tile* subtile = _subtiles->at(i);
          subtile->ancestorChangedElevationData(this);
        }
      }
    }
    
  }
}

void Tile::getElevationDataFromAncestor(const Vector2I& extent) {
  if (_elevationData == NULL) {
    Tile* ancestor = getParent();
    while ((ancestor != NULL) &&
           !ancestor->isElevationDataSolved()) {
      ancestor = ancestor->getParent();
    }
    
    if (ancestor != NULL) {
      ElevationData* subView = createElevationDataSubviewFromAncestor(ancestor);
      setElevationData(subView, ancestor->_level);
    }
  }
  else {
    printf("break point on me\n");
  }
}

void Tile::initializeElevationData(ElevationDataProvider* elevationDataProvider,
                                   const TileTessellator* tessellator,
                                   const Vector2I& tileMeshResolution,
                                   const Planet* planet,
                                   bool renderDebug) {
  //Storing for subviewing
  _lastElevationDataProvider = elevationDataProvider;
  _lastTileMeshResolutionX = tileMeshResolution._x;
  _lastTileMeshResolutionY = tileMeshResolution._y;
  if (_elevationDataRequest == NULL) {
    
    const Vector2I res = tessellator->getTileMeshResolution(planet,
                                                            tileMeshResolution,
                                                            this,
                                                            renderDebug);
    _elevationDataRequest = new TileElevationDataRequest(this, res, elevationDataProvider);
    _elevationDataRequest->sendRequest();
  }
  
  //If after petition we still have no data we request from ancestor (provider asynchronous)
  if (_elevationData == NULL) {
    getElevationDataFromAncestor(tileMeshResolution);
  }
  
}

void Tile::ancestorChangedElevationData(Tile* ancestor) {
  
  if (ancestor->_level > _elevationDataLevel) {
    ElevationData* subView = createElevationDataSubviewFromAncestor(ancestor);
    if (subView != NULL) {
      setElevationData(subView, ancestor->_level);
    }
  }
  
  if (_subtiles != NULL) {
    const int subtilesSize = _subtiles->size();
    for (int i = 0; i < subtilesSize; i++) {
      Tile* subtile = _subtiles->at(i);
      subtile->ancestorChangedElevationData(this);
    }
  }
}

ElevationData* Tile::createElevationDataSubviewFromAncestor(Tile* ancestor) const{
  ElevationData* ed = ancestor->getElevationData();
  
  if (ed == NULL) {
    ILogger::instance()->logError("Ancestor can't have undefined Elevation Data.");
    return NULL;
  }
  
  if (ed->getExtentWidth() < 1 || ed->getExtentHeight() < 1) {
    ILogger::instance()->logWarning("Tile too small for ancestor elevation data.");
    return NULL;
  }
  
  if ((_lastElevationDataProvider != NULL) &&
      (_lastTileMeshResolutionX > 0) &&
      (_lastTileMeshResolutionY > 0)) {
    return new DecimatedSubviewElevationData(ed,
                                             _sector,
                                             Vector2I(_lastTileMeshResolutionX, _lastTileMeshResolutionY));
  }
  
  ILogger::instance()->logError("Can't create subview of elevation data from ancestor");
  return NULL;
  
}

void Tile::setTessellatorData(PlanetTileTessellatorData* tessellatorData) {
  if (tessellatorData != _tessellatorData) {
    delete _tessellatorData;
    _tessellatorData = tessellatorData;
  }
}

void Tile::performRawRender(const G3MRenderContext* rc,
                            const GLState* glState,
                            TileTexturizer* texturizer,
                            ElevationDataProvider* elevationDataProvider,
                            const TileTessellator* tessellator,
                            TileRasterizer* tileRasterizer,
                            const LayerTilesRenderParameters* layerTilesRenderParameters,
                            const LayerSet* layerSet,
                            const TilesRenderParameters* tilesRenderParameters,
                            bool isForcedFullRender,
                            long long texturePriority,
                            TilesStatistics* tilesStatistics,
                            bool logTilesPetitions){

  rawRender(rc,
            glState,
            texturizer,
            elevationDataProvider,
            tessellator,
            tileRasterizer,
            layerTilesRenderParameters,
            layerSet,
            tilesRenderParameters,
            isForcedFullRender,
            texturePriority,
            logTilesPetitions);
  if (tilesRenderParameters->_renderDebug) { //TO RAW RENDER
    debugRender(rc, glState, tessellator, layerTilesRenderParameters);
  }

  tilesStatistics->computeTileRenderered(this);

  //TODO: AVISAR CAMBIO DE TERRENO
}

void Tile::actualizeQuadTree(const G3MRenderContext* rc,
                             std::list<Tile*>& renderedTiles,
                             const Planet* planet,
                             const Vector3D& cameraNormalizedPosition,
                             double cameraAngle2HorizonInRadians,
                             const Frustum* cameraFrustumInModelCoordinates,
                             const Frustum* cameraWiderFrustumInModelCoordinates,
                             TilesStatistics* tilesStatistics,
                             const float verticalExaggeration,
                             const LayerTilesRenderParameters* layerTilesRenderParameters,
                             TileTexturizer* texturizer,
                             const TilesRenderParameters* tilesRenderParameters,
                             ITimer* lastSplitTimer,
                             ElevationDataProvider* elevationDataProvider,
                             const TileTessellator* tessellator,
                             TileRasterizer* tileRasterizer,
                             const LayerSet* layerSet,
                             const Sector* renderedSector,
                             bool isForcedFullRender,
                             long long texturePriority,
                             double texWidthSquared,
                             double texHeightSquared,
                             double nowInMS) {

  tilesStatistics->computeTileProcessed(this);

  if (verticalExaggeration != _verticalExaggeration) {
    // TODO: verticalExaggeration changed, invalidate tileExtent, Mesh, etc.
    _verticalExaggeration = verticalExaggeration;
  }


  if (isVisible(rc,
                planet,
                cameraNormalizedPosition,
                cameraAngle2HorizonInRadians,
                cameraFrustumInModelCoordinates,
                cameraWiderFrustumInModelCoordinates,
                elevationDataProvider,
                renderedSector,
                tessellator,
                layerTilesRenderParameters,
                tilesRenderParameters)) {
    setIsVisible(true, texturizer);

    tilesStatistics->computeVisibleTile(this);

    const bool isRawRender = (
                              meetsRenderCriteria(rc,
                                                  layerTilesRenderParameters,
                                                  texturizer,
                                                  tilesRenderParameters,
                                                  tilesStatistics,
                                                  lastSplitTimer,
                                                  texWidthSquared,
                                                  texHeightSquared,
                                                  nowInMS) ||
                              (tilesRenderParameters->_incrementalTileQuality && !_textureSolved)
                              );

    if (isRawRender) {

      renderedTiles.push_back(this);

      prune(texturizer, elevationDataProvider);
      //TODO: AVISAR CAMBIO DE TERRENO

    }
    else {
      const Geodetic2D lower = _sector._lower;
      const Geodetic2D upper = _sector._upper;

      const Angle splitLongitude = Angle::midAngle(lower._longitude,
                                                   upper._longitude);

      const Angle splitLatitude = layerTilesRenderParameters->_mercator
      /*                               */ ? MercatorUtils::calculateSplitLatitude(lower._latitude,
                                                                                  upper._latitude)
      /*                               */ : Angle::midAngle(lower._latitude,
                                                            upper._latitude);

      std::vector<Tile*>* subTiles = getSubTiles(splitLatitude, splitLongitude);
      if (_justCreatedSubtiles) {
        lastSplitTimer->start();
        tilesStatistics->computeBuilderStartInFrame();
        _justCreatedSubtiles = false;
      }

      const int subTilesSize = subTiles->size();
      for (int i = 0; i < subTilesSize; i++) {
        Tile* subTile = subTiles->at(i);

        subTile->actualizeQuadTree(rc,/* parentState,*/ renderedTiles, planet,
                                   cameraNormalizedPosition, cameraAngle2HorizonInRadians, cameraFrustumInModelCoordinates, cameraWiderFrustumInModelCoordinates,
                                   tilesStatistics, verticalExaggeration, layerTilesRenderParameters, texturizer, tilesRenderParameters, lastSplitTimer, elevationDataProvider, tessellator, tileRasterizer, layerSet, renderedSector, isForcedFullRender, texturePriority, texWidthSquared,
                                   texHeightSquared,
                                   nowInMS);
      }
    }
  }
  else {
    setIsVisible(false, texturizer);

    prune(texturizer, elevationDataProvider);
    //TODO: AVISAR CAMBIO DE TERRENO
  }

}


void Tile::zRender(const G3MRenderContext* rc,
                   const GLState& parentState) {

  if (_tessellatorMesh == NULL) {
    ILogger::instance()->logError("Calling ZRender for Tile withouth any valid mesh.");
    return;
  } else{
    _tessellatorMesh->zRender(rc, &parentState);
  }
}

void Tile::prepareTestLODData(const Planet* planet){

  if ((_northWestPoint == NULL) ||
      (_northEastPoint == NULL) ||
      (_southWestPoint == NULL) ||
      (_southEastPoint == NULL)) {
    ILogger::instance()->logError("Error in Tile::prepareTestLODData");
    return;
  }
  
  const Vector3D normalNW = planet->centricSurfaceNormal(*_northWestPoint);
  const Vector3D normalNE = planet->centricSurfaceNormal(*_northEastPoint);
  const Vector3D normalSW = planet->centricSurfaceNormal(*_southWestPoint);
  const Vector3D normalSE = planet->centricSurfaceNormal(*_southEastPoint);
  
  _northArcSegmentRatioSquared = getSquaredArcSegmentRatio(normalNW, normalNE);
  _southArcSegmentRatioSquared = getSquaredArcSegmentRatio(normalSW, normalSE);
  _eastArcSegmentRatioSquared  = getSquaredArcSegmentRatio(normalNE, normalSE);
  _westArcSegmentRatioSquared  = getSquaredArcSegmentRatio(normalNW, normalSW);
}

double Tile::getSquaredArcSegmentRatio(const Vector3D& a,
                                       const Vector3D& b) {
  /*
   Arco = ang * Cuerda / (2 * sen(ang/2))
   */
  
  const double angleInRadians = a.angleInRadiansBetween(b);
  const double halfAngleSin = SIN(angleInRadians / 2);
  const double arcSegmentRatio = (halfAngleSin == 0) ? 1 : angleInRadians / (2 * halfAngleSin);
  return (arcSegmentRatio * arcSegmentRatio);
}

void Tile::computeTileCorners(const Planet* planet) {
  
  if (_tessellatorMesh == NULL) {
    ILogger::instance()->logError("Error in Tile::computeTileCorners");
    return;
  }
  
  //  delete _middleWestPoint;
  //  delete _middleEastPoint;
  //  delete _middleNorthPoint;
  //  delete _middleSouthPoint;
  delete _northWestPoint;
  delete _northEastPoint;
  delete _southWestPoint;
  delete _southEastPoint;
  
  
  const double mediumHeight = _tileTessellatorMeshData._averageHeight;
  
  //  const Geodetic2D center = _sector.getCenter();
  //  const Geodetic3D gN( Geodetic2D(_sector.getNorth(), center._longitude), mediumHeight);
  //  const Geodetic3D gS( Geodetic2D(_sector.getSouth(), center._longitude), mediumHeight);
  //  const Geodetic3D gW( Geodetic2D(center._latitude, _sector.getWest()), mediumHeight);
  //  const Geodetic3D gE( Geodetic2D(center._latitude, _sector.getEast()), mediumHeight);
  //
  //  _middleNorthPoint = new Vector3D(planet->toCartesian(gN));
  //  _middleSouthPoint = new Vector3D(planet->toCartesian(gS));
  //  _middleEastPoint = new Vector3D(planet->toCartesian(gE));
  //  _middleWestPoint = new Vector3D(planet->toCartesian(gW));
  
  _northWestPoint = new Vector3D( planet->toCartesian( _sector.getNW(), mediumHeight ) );
  _northEastPoint = new Vector3D( planet->toCartesian( _sector.getNE(), mediumHeight ) );
  _southWestPoint = new Vector3D( planet->toCartesian( _sector.getSW(), mediumHeight ) );
  _southEastPoint = new Vector3D( planet->toCartesian( _sector.getSE(), mediumHeight ) );
}

Vector2I Tile::getNormalizedPixelsFromPosition(const Geodetic2D& position2D,
                                               const Vector2I& tileDimension) const{
  const IMathUtils* math = IMathUtils::instance();
  const Vector2D uv = _sector.getUVCoordinates(position2D);
  return Vector2I(math->toInt(tileDimension._x * uv._x), math->toInt(tileDimension._y * uv._y));
}

#pragma mark TileCache

Tile* TileCache::getSubTileFromCache(int level, int row, int column){
  
  for (std::vector<Tile*>::iterator it = _tileCache.begin();
       it != _tileCache.end();
       it++){
    Tile* tile = *it;
    if (tile->_level == level &&
        tile->_row == row &&
        tile->_column == column){
#ifdef C_CODE
      _tileCache.erase(it);
#endif
#ifdef JAVA_CODE
      _tileCache.remove(tile);
#endif
      return tile;
    }
  }
  
  return NULL;
}

void TileCache::clearTile(Tile* tile){
  
  _tileCache.push_back(tile);
  cropTileCache(_maxSize);
}

void TileCache::cropTileCache(int size){
  while (_tileCache.size() > size){
#ifdef C_CODE
    Tile* t = *(_tileCache.begin());
    _tileCache.erase(_tileCache.begin());
#endif
#ifdef JAVA_CODE
    Tile t = _tileCache.get(0);
    _tileCache.remove(0);
#endif
    
    TileTexturizer* texturizer = t->getTexturizer();
    if (texturizer != NULL) {
      texturizer->tileToBeDeleted(t, t->getTexturizedMesh());
      
      t->deleteTexturizedMesh(texturizer);
    }
    
#ifdef JAVA_CODE
    t.dispose();
#endif
    
    delete t;
    
  }
}

bool TileCache::has4SubTilesCached(const Tile* tile) {
  
  const int nextLevel = tile->_level + 1;
  
  const int row2    = 2 * tile->_row;
  const int column2 = 2 * tile->_column;
  
  int nSubtiles = 0;
  
  for (std::vector<Tile*>::iterator it = _tileCache.begin();
       it != _tileCache.end();
       it++){
    Tile* t = *it;
    if (t->_level == nextLevel){
      if ((t->_row == row2 && t->_column == column2) ||
          (t->_row == row2+1 && t->_column == column2) ||
          (t->_row == row2 && t->_column == column2+1) ||
          (t->_row == row2+1 && t->_column == column2+1)){
        nSubtiles++;
        if (nSubtiles == 4){
          return true;
        }
      }
    }
  }
  
  return false;
}

#warning ÑAPA
std::vector<LODAugmentedSector> Tile::_lODAugmentedSectors;
LODAugmentedSector::LODAugmentedSector(const Sector& sector, double factor):_sector( new Sector(sector)), _lodFactor(factor){}

void Tile::addLODAugmentedForSector(const Sector& sector, double factor){
  _lODAugmentedSectors.push_back(LODAugmentedSector(sector, factor));
}
