

package org.glob3.mobile.specific;

import java.util.ArrayList;
import java.util.Iterator;

import org.glob3.mobile.generated.IBufferDownloadListener;
import org.glob3.mobile.generated.IImageDownloadListener;
import org.glob3.mobile.generated.ILogger;
import org.glob3.mobile.generated.LogLevel;
import org.glob3.mobile.generated.URL;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;


public class Downloader_WebGL_Handler_DefaultImpl
         implements
            Downloader_WebGL_Handler {


   private final static String TAG = "Downloader_WebGL_HandlerImpl ";


   private long                     _priority;
   private URL                      _url;
   private ArrayList<ListenerEntry> _listeners;
   private boolean                  _requestingImage;

   private Downloader_WebGL _downloader;


   public Downloader_WebGL_Handler_DefaultImpl() {
   }


   @Override
   final public void init(final URL url,
                          final IBufferDownloadListener bufferListener,
                          final boolean deleteListener,
                          final long priority,
                          final long requestID,
                          final String tag) {
      _priority = priority;
      _url = url;
      _listeners = new ArrayList<ListenerEntry>();
      _listeners.add(new ListenerEntry(bufferListener, null, deleteListener, requestID, tag));
      _requestingImage = false;
   }


   @Override
   final public void init(final URL url,
                          final IImageDownloadListener imageListener,
                          final boolean deleteListener,
                          final long priority,
                          final long requestID,
                          final String tag) {
      _priority = priority;
      _url = url;
      _listeners = new ArrayList<ListenerEntry>();
      _listeners.add(new ListenerEntry(null, imageListener, deleteListener, requestID, tag));
      _requestingImage = true;
   }


   @Override
   final public boolean isRequestingImage() {
      return _requestingImage;
   }


   @Override
   final public void addListener(final IBufferDownloadListener listener,
                                 final boolean deleteListener,
                                 final long priority,
                                 final long requestID,
                                 final String tag) {
      _listeners.add(new ListenerEntry(listener, null, deleteListener, requestID, tag));
      if (priority > _priority) {
         _priority = priority;
      }
   }


   @Override
   final public void addListener(final IImageDownloadListener listener,
                                 final boolean deleteListener,
                                 final long priority,
                                 final long requestID,
                                 final String tag) {
      _listeners.add(new ListenerEntry(null, listener, deleteListener, requestID, tag));
      if (priority > _priority) {
         _priority = priority;
      }
   }


   @Override
   final public long getPriority() {
      return _priority;
   }


   @Override
   final public boolean cancelListenerForRequestId(final long requestID) {
      for (final ListenerEntry listener : _listeners) {
         if (listener.getRequestId() == requestID) {
            listener.cancel();
            return true;
         }
      }

      return false;
   }


   @Override
   public void cancelListenersTagged(final String tag) {
      for (final ListenerEntry listener : _listeners) {
         if (listener.getTag().equals(tag)) {
            listener.cancel();
         }
      }
   }


   @Override
   final public boolean removeListenerForRequestId(final long requestID) {
      final Iterator<ListenerEntry> iterator = _listeners.iterator();
      while (iterator.hasNext()) {
         final ListenerEntry listener = iterator.next();
         if (listener.getRequestId() == requestID) {
            listener.onCancel(_url);
            iterator.remove();
            return true;
         }
      }
      return false;
   }


   @Override
   final public boolean removeListenersTagged(final String tag) {
      boolean anyRemoved = false;

      final Iterator<ListenerEntry> iterator = _listeners.iterator();
      while (iterator.hasNext()) {
         final ListenerEntry listener = iterator.next();
         if (listener.getTag().equals(tag)) {
            listener.onCancel(_url);
            iterator.remove();
            anyRemoved = true;
         }
      }
      return anyRemoved;
   }


   @Override
   final public boolean hasListener() {
      return !_listeners.isEmpty();
   }


   @Override
   final public void runWithDownloader(final Downloader_WebGL downloader) {
      _downloader = downloader;

      jsRequest(_url._path);
   }


   @Override
   final public void removeFromDownloaderDownloadingHandlers() {
      _downloader.removeDownloadingHandlerForUrl(_url);
   }


   @Override
   final public void processResponse(final int statusCode,
                                     final JavaScriptObject data) {
      final boolean dataIsValid = (data != null) && (statusCode == 200);

      if (dataIsValid) {
         for (final ListenerEntry listener : _listeners) {
            if (listener.isCanceled()) {
               listener.onCanceledDownload(_url, data);
               listener.onCancel(_url);
            }
            else {
               listener.onDownload(_url, data);
            }
         }
      }
      else {
         if ((_downloader == null) || _downloader._verboseErrors) {
            log(LogLevel.ErrorLevel,
                     ": Error runWithDownloader: statusCode=" + statusCode + ", data=" + data + ", url=" + _url._path);
         }

         for (final ListenerEntry listener : _listeners) {
            listener.onError(_url);
         }
      }
   }


   @Override
   public native void jsRequest(String url) /*-{
		var that = this;
		var xhr = new XMLHttpRequest();
		xhr.open("GET", url, true);
		xhr.responseType = (that.@org.glob3.mobile.specific.Downloader_WebGL_Handler_DefaultImpl::_requestingImage) ? "blob"
				: "arraybuffer";
		xhr.onload = function() {
			if (xhr.readyState == 4) {
				// inform downloader to remove myself, to avoid adding new Listener
				that.@org.glob3.mobile.specific.Downloader_WebGL_Handler::removeFromDownloaderDownloadingHandlers()();
				if (xhr.status === 200) {
					if (that.@org.glob3.mobile.specific.Downloader_WebGL_Handler_DefaultImpl::_requestingImage) {
						that.@org.glob3.mobile.specific.Downloader_WebGL_Handler_DefaultImpl::jsCreateImageFromBlob(ILcom/google/gwt/core/client/JavaScriptObject;)(xhr.status, xhr.response);
					} else {
						that.@org.glob3.mobile.specific.Downloader_WebGL_Handler::processResponse(ILcom/google/gwt/core/client/JavaScriptObject;)(xhr.status, xhr.response);
					}
				} else {
					that.@org.glob3.mobile.specific.Downloader_WebGL_Handler::processResponse(ILcom/google/gwt/core/client/JavaScriptObject;)(xhr.status, null);
				}
			}
		};
		xhr.send();
   }-*/;


   private native void jsCreateImageFromBlob(final int xhrStatus,
                                             final JavaScriptObject blob) /*-{
		var that = this;

		var auxImg = new Image();
		var imgURL = $wnd.g3mURL.createObjectURL(blob);

		auxImg.onload = function() {
			that.@org.glob3.mobile.specific.Downloader_WebGL_Handler::processResponse(ILcom/google/gwt/core/client/JavaScriptObject;)(xhrStatus, auxImg);
			$wnd.g3mURL.revokeObjectURL(imgURL);
		};
		auxImg.onerror = function() {
			that.@org.glob3.mobile.specific.Downloader_WebGL_Handler::processResponse(ILcom/google/gwt/core/client/JavaScriptObject;)(xhrStatus, null);
			$wnd.g3mURL.revokeObjectURL(imgURL);
		};
		auxImg.onabort = function() {
			that.@org.glob3.mobile.specific.Downloader_WebGL_Handler::processResponse(ILcom/google/gwt/core/client/JavaScriptObject;)(xhrStatus, null);
			$wnd.g3mURL.revokeObjectURL(imgURL);
		};

		auxImg.src = imgURL;
   }-*/;


   public static void log(final LogLevel level,
                          final String msg) {
      final ILogger logger = ILogger.instance();
      if (logger == null) {
         GWT.log(TAG + msg);
      }
      else {
         switch (level) {
            case InfoLevel:
               logger.logInfo(TAG + msg);
               break;

            case WarningLevel:
               logger.logWarning(TAG + msg);
               break;

            case ErrorLevel:
               logger.logError(TAG + msg);
               break;

            default:
               break;
         }
      }
   }


}
