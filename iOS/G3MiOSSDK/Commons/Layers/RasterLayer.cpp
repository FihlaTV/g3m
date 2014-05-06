//
//  RasterLayer.cpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 4/22/14.
//
//

#include "RasterLayer.hpp"
#include "TimeInterval.hpp"
#include "RasterLayerTileImageProvider.hpp"
#include "LayerCondition.hpp"
#include "Context.hpp"
#include "IDownloader.hpp"
#include "URL.hpp"

RasterLayer::RasterLayer(const TimeInterval&               timeToCache,
                         const bool                        readExpired,
                         const LayerTilesRenderParameters* parameters,
                         const float                       transparency,
                         const LayerCondition*             condition) :
Layer(parameters,
      transparency,
      condition),
_timeToCacheMS(timeToCache._milliseconds),
_readExpired(readExpired)
{
}

const TimeInterval RasterLayer::getTimeToCache() const {
  return TimeInterval::fromMilliseconds(_timeToCacheMS);
}

bool RasterLayer::isEquals(const Layer* that) const {
  if (this == that) {
    return true;
  }

  if (that == NULL) {
    return false;
  }

  if (!Layer::isEquals(that)) {
    return false;
  }

  RasterLayer* rasterThat = (RasterLayer*) that;

  return ((_timeToCacheMS == rasterThat->_timeToCacheMS) &&
          (_readExpired   == rasterThat->_readExpired));
}

TileImageProvider* RasterLayer::createTileImageProvider(const G3MRenderContext* rc,
                                                        const LayerTilesRenderParameters* layerTilesRenderParameters) const {
  return new RasterLayerTileImageProvider(this, rc->getDownloader());
}

const TileImageContribution* RasterLayer::contribution(const Tile* tile) const {
  if ((_condition == NULL) || _condition->isAvailable(tile)) {
    return rawContribution(tile);
  }
  return NULL;
}

long long RasterLayer::requestImage(const Tile* tile,
                                    IDownloader* downloader,
                                    long long tileDownloadPriority,
                                    bool logDownloadActivity,
                                    IImageDownloadListener* listener,
                                    bool deleteListener) const {
  const URL url = createURL(tile);
  if (logDownloadActivity) {
    ILogger::instance()->logInfo("Downloading %s", url.getPath().c_str());
  }
  return downloader->requestImage(url,
                                  tileDownloadPriority,
                                  getTimeToCache(),
                                  _readExpired,
                                  listener,
                                  deleteListener);
}