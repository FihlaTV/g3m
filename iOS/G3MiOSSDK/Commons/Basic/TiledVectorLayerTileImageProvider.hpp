//
//  TiledVectorLayerTileImageProvider.hpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 4/30/14.
//
//

#ifndef __G3MiOSSDK__TiledVectorLayerTileImageProvider__
#define __G3MiOSSDK__TiledVectorLayerTileImageProvider__

#include "TileImageProvider.hpp"

#include <map>
class TiledVectorLayer;
class IDownloader;
#include "IBufferDownloadListener.hpp"
#include "IThreadUtils.hpp"
class GEOObject;

class TiledVectorLayerTileImageProvider : public TileImageProvider {
private:

  class GEOJSONBufferParser : public GAsyncTask {
  private:
    IByteBuffer* _buffer;
    GEOObject*   _geoObject;

  public:
    GEOJSONBufferParser(IByteBuffer* buffer) :
    _buffer(buffer),
    _geoObject(NULL)
    {
    }

    ~GEOJSONBufferParser();

    void runInBackground(const G3MContext* context);

    void onPostExecute(const G3MContext* context);

  };

  
  class GEOJSONBufferDownloadListener : public IBufferDownloadListener {
  private:
    TiledVectorLayerTileImageProvider* _tiledVectorLayerTileImageProvider;
    const std::string                  _tileId;
    TileImageListener*                 _listener;
    const bool                         _deleteListener;
    const IThreadUtils*                _threadUtils;
#ifdef C_CODE
    const TileImageContribution*       _contribution;
#endif
#ifdef JAVA_CODE
    private TileImageContribution _contribution;
#endif

  public:
    GEOJSONBufferDownloadListener(TiledVectorLayerTileImageProvider* tiledVectorLayerTileImageProvider,
                                  const std::string&                 tileId,
                                  const TileImageContribution*       contribution,
                                  TileImageListener*                 listener,
                                  bool                               deleteListener,
                                  const IThreadUtils*                threadUtils) :
    _tiledVectorLayerTileImageProvider(tiledVectorLayerTileImageProvider),
    _tileId(tileId),
    _contribution(contribution),
    _listener(listener),
    _deleteListener(deleteListener),
    _threadUtils(threadUtils)
    {
    }

    ~GEOJSONBufferDownloadListener();

    void onDownload(const URL& url,
                    IByteBuffer* buffer,
                    bool expired);

    void onError(const URL& url);

    void onCancel(const URL& url);

    void onCanceledDownload(const URL& url,
                            IByteBuffer* buffer,
                            bool expired) {
      // do nothing
    }

  };

  const TiledVectorLayer* _layer;
  IDownloader*            _downloader;
  const IThreadUtils*     _threadUtils;

  std::map<const std::string, long long> _requestsIdsPerTile;

public:

  TiledVectorLayerTileImageProvider(const TiledVectorLayer* layer,
                                    IDownloader*            downloader,
                                    const IThreadUtils*     threadUtils) :
  _layer(layer),
  _downloader(downloader),
  _threadUtils(threadUtils)
  {
  }


  const TileImageContribution* contribution(const Tile* tile);

  void create(const Tile* tile,
              const TileImageContribution* contribution,
              const Vector2I& resolution,
              long long tileDownloadPriority,
              bool logDownloadActivity,
              TileImageListener* listener,
              bool deleteListener,
              FrameTasksExecutor* frameTasksExecutor);

  void cancel(const std::string& tileId);

  void requestFinish(const std::string& tileId);

};

#endif