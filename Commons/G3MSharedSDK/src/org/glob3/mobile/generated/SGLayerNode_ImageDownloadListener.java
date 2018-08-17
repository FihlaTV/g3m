package org.glob3.mobile.generated;import java.util.*;

///#define TEXTURES_DOWNLOAD_PRIORITY 1000000


public class SGLayerNode_ImageDownloadListener implements IImageDownloadListener
{
  private SGLayerNode _layerNode;

  public SGLayerNode_ImageDownloadListener(SGLayerNode layerNode)
  {
	  _layerNode = layerNode;

  }

  public final void onDownload(URL url, IImage image, boolean expired)
  {
	_layerNode.onImageDownload(image);
  }

  public final void onError(URL url)
  {
	ILogger.instance().logWarning("Can't download texture \"%s\"", url._path.c_str());
  }

  public final void onCancel(URL url)
  {

  }

  public final void onCanceledDownload(URL url, IImage image, boolean expired)
  {

  }
}
