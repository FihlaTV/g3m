

package org.glob3.mobile.specific;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.glob3.mobile.generated.G3MWidget;
import org.glob3.mobile.generated.ILogger;
import org.glob3.mobile.generated.Touch;
import org.glob3.mobile.generated.TouchEvent;
import org.glob3.mobile.generated.TouchEventType;
import org.glob3.mobile.generated.Vector2F;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;


public final class MotionEventProcessor {

   private static final String   TAG                    = "MotionEventProcessor";

   private static final Vector2F DELTA                  = new Vector2F(10, 0);


   private final G3MWidget       _widget;
   private final CanvasElement   _canvasElement;
   private boolean               _mouseDown             = false;
   private Vector2F              _previousMousePosition = null;


   public MotionEventProcessor(final G3MWidget widget,
                               final CanvasElement canvasElement) {
      _widget = widget;
      _canvasElement = canvasElement;

      jsAddMouseWheelListener();
   }


   private Vector2F createPosition(final Event event) {
	  float factor = G3MWidget_WebGL._downScaleFactor;
	  
      Vector2F v =   new Vector2F(//
               (event.getClientX() - _canvasElement.getAbsoluteLeft())/factor, //
               (event.getClientY() - _canvasElement.getAbsoluteTop())/factor);
      return v;
   }


   public void processEvent(final Event event) {

      TouchEvent touchEvent = null;

      switch (DOM.eventGetType(event)) {
         case Event.ONTOUCHSTART:
            event.preventDefault();
            ILogger.instance().logInfo("Touch thing START");
            touchEvent = processTouchStart(event);
            break;
         case Event.ONTOUCHEND:
            event.preventDefault();
            ILogger.instance().logInfo("Touch thing END");
            touchEvent = processTouchEnd(event);
            break;
         case Event.ONTOUCHMOVE:
            event.preventDefault();
            ILogger.instance().logInfo("Touch thing MOVE");
            touchEvent = processTouchMove(event);
            break;
         case Event.ONTOUCHCANCEL:
            event.preventDefault();
            ILogger.instance().logInfo("Touch thing CANCEL");
            touchEvent = processTouchCancel(event);
            break;

         case Event.ONMOUSEMOVE:
        	ILogger.instance().logInfo("Mouse thing MOVE");
            touchEvent = processMouseMove(event);
            break;
         case Event.ONMOUSEDOWN:
        	ILogger.instance().logInfo("Mouse thing DOWN");
            touchEvent = processMouseDown(event);
            break;
         case Event.ONMOUSEUP:
        	 ILogger.instance().logInfo("Mouse thing UP");
            touchEvent = processMouseUp(event);
            break;

         case Event.ONDBLCLICK:
        	 ILogger.instance().logInfo("Double clicl");
            touchEvent = processDoubleClick(event);
            break;

         case Event.ONCONTEXTMENU:
        	 ILogger.instance().logInfo("Context");
            event.preventDefault();
            touchEvent = processContextMenu(event);
            break;

         case Event.ONMOUSEWHEEL:
        	ILogger.instance().logInfo("Mouse wheel");
            event.preventDefault();
            break;

         default:
        	 ILogger.instance().logInfo("Something else");
            return;
      }

      if (touchEvent != null) {
         dispatchEvents(touchEvent);
      }
   }


   private Map<Integer, Vector2F> _previousTouchesPositions = new HashMap<>();


   private ArrayList<Touch> createTouches(final JsArray<com.google.gwt.dom.client.Touch> jsTouches) {
      final Map<Integer, Vector2F> currentTouchesPositions = new HashMap<>();
      final int jsTouchesSize = jsTouches.length();
      final ArrayList<Touch> touches = new ArrayList<>(jsTouchesSize);
      for (int i = 0; i < jsTouchesSize; i++) {
         final com.google.gwt.dom.client.Touch jsTouch = jsTouches.get(i);

         /*final Vector2F currentTouchPosition = new Vector2F( //
                  jsTouch.getRelativeX(_canvasElement), //
                  jsTouch.getRelativeY(_canvasElement) //
         );*/
         float factor = G3MWidget_WebGL._downScaleFactor;
         
         final Vector2F currentTouchPosition = new Vector2F( //
         	jsTouch.getRelativeX(_canvasElement)/factor, //
         	jsTouch.getRelativeY(_canvasElement)/factor//
		);
         
         ILogger.instance().logInfo("Created position: "+currentTouchPosition._x+"_"+currentTouchPosition._y);

         final Integer touchId = Integer.valueOf(jsTouch.getIdentifier());

         currentTouchesPositions.put(touchId, currentTouchPosition);


         Vector2F previousTouchPosition = _previousTouchesPositions.get(touchId);
         if (previousTouchPosition == null) {
            previousTouchPosition = currentTouchPosition;
         }

         touches.add(new Touch(currentTouchPosition, previousTouchPosition));
      }

      _previousTouchesPositions = currentTouchesPositions;

      return touches;
   }


   private TouchEvent processTouchStart(final Event event) {
      return TouchEvent.create(TouchEventType.Down, createTouches(event.getTouches()));
   }


   private TouchEvent processTouchMove(final Event event) {
      return TouchEvent.create(TouchEventType.Move, createTouches(event.getTouches()));
   }


   private TouchEvent processTouchEnd(final Event event) {
      return TouchEvent.create(TouchEventType.Up, createTouches(event.getChangedTouches()));
   }


   private TouchEvent processTouchCancel(@SuppressWarnings("unused")
   final Event event) {
      _previousTouchesPositions.clear();
      return null;
   }


   private void dispatchEvents(final TouchEvent... events) {
      if (events.length > 0) {
         final Scheduler scheduler = Scheduler.get();
         for (final TouchEvent event : events) {
            scheduler.scheduleDeferred( //
            new Command() {
               @Override
               public void execute() {
                  _widget.onTouchEvent(event);
               }
            });
         }
      }
   }


   private TouchEvent processMouseMove(final Event event) {
      if (!_mouseDown) {
         return null;
      }

      final Vector2F currentMousePosition = createPosition(event);
      final ArrayList<Touch> touches = new ArrayList<>();

      if (event.getShiftKey()) {
         touches.add(new Touch(currentMousePosition.sub(DELTA), _previousMousePosition.sub(DELTA)));
         touches.add(new Touch(currentMousePosition, _previousMousePosition));
         touches.add(new Touch(currentMousePosition.add(DELTA), _previousMousePosition.add(DELTA)));
      }
      else {
         touches.add(new Touch(currentMousePosition, _previousMousePosition));
      }

      _previousMousePosition = currentMousePosition;

      return TouchEvent.create(TouchEventType.Move, touches);
   }


   private TouchEvent processMouseDown(final Event event) {
      final Vector2F currentMousePosition = createPosition(event);
      final ArrayList<Touch> touches = new ArrayList<>();

      _mouseDown = true;
      if (event.getShiftKey()) {
         touches.add(new Touch(currentMousePosition.sub(DELTA), _previousMousePosition.sub(DELTA)));
         touches.add(new Touch(currentMousePosition, _previousMousePosition));
         touches.add(new Touch(currentMousePosition.add(DELTA), _previousMousePosition.add(DELTA)));
      }
      else {
         touches.add(new Touch(currentMousePosition, _previousMousePosition));
      }

      _previousMousePosition = currentMousePosition;

      return TouchEvent.create(TouchEventType.Down, touches);
   }


   private TouchEvent processMouseUp(final Event event) {
      final Vector2F currentMousePosition = createPosition(event);
      final ArrayList<Touch> touches = new ArrayList<>();

      final TouchEventType touchType;

      _mouseDown = false;
      if (event.getShiftKey()) {
         touches.add(new Touch(currentMousePosition.sub(DELTA), _previousMousePosition.sub(DELTA)));
         touches.add(new Touch(currentMousePosition, _previousMousePosition));
         touches.add(new Touch(currentMousePosition.add(DELTA), _previousMousePosition.add(DELTA)));

         touchType = TouchEventType.Up;
      }
      else {
         touches.add(new Touch(currentMousePosition, _previousMousePosition));
         touchType = (event.getCtrlKey() && (event.getButton() == NativeEvent.BUTTON_LEFT)) ? TouchEventType.LongPress
                                                                                           : TouchEventType.Up;
      }
      _previousMousePosition = currentMousePosition;

      return TouchEvent.create(touchType, touches);
   }


   private TouchEvent processDoubleClick(final Event event) {
      final Vector2F currentMousePosition = createPosition(event);
      final Touch touch = new Touch(currentMousePosition, currentMousePosition, (byte) 2);
      return TouchEvent.create(TouchEventType.Down, touch);
   }


   private TouchEvent processContextMenu(final Event event) {
      _mouseDown = false;

      final Vector2F currentMousePosition = createPosition(event);
      final Touch touch = new Touch(currentMousePosition, _previousMousePosition);
      _previousMousePosition = currentMousePosition;

      return TouchEvent.create(TouchEventType.LongPress, touch);
   }


   private void processMouseWheel(final int delta,
                                  final int x,
                                  final int y) {
	  float factor = G3MWidget_WebGL._downScaleFactor;
	  int x2 = (int) (x/factor);
	  int y2 = (int) (y/factor);
      final Vector2F beginFirstPosition = new Vector2F(x2 - 10, y2 - 10);
      final Vector2F beginSecondPosition = new Vector2F(x2 + 10, y2 + 10);

      final ArrayList<Touch> beginTouches = new ArrayList<>(2);
      beginTouches.add(new Touch(beginFirstPosition, beginFirstPosition));
      beginTouches.add(new Touch(beginSecondPosition, beginSecondPosition));


      final Vector2F endFirstPosition = new Vector2F(beginFirstPosition._x - delta, beginFirstPosition._y - delta);
      final Vector2F endSecondPosition = new Vector2F(beginSecondPosition._x + delta, beginSecondPosition._y + delta);

      final ArrayList<Touch> endTouches = new ArrayList<>(2);
      endTouches.add(new Touch(endFirstPosition, beginFirstPosition));
      endTouches.add(new Touch(endSecondPosition, beginSecondPosition));

      dispatchEvents( //
               TouchEvent.create(TouchEventType.Down, beginTouches), //
               TouchEvent.create(TouchEventType.Move, endTouches), //
               TouchEvent.create(TouchEventType.Up, endTouches) //
      );

      _previousMousePosition = new Vector2F(x2, y2);
   }


   private native void jsAddMouseWheelListener() /*-{
		var thisInstance = this;

		var canvas = this.@org.glob3.mobile.specific.MotionEventProcessor::_canvasElement;

		$wnd.g3mMouseWheelHandler = function(e) {
			// cross-browser wheel delta
			var e = $wnd.event || e; // old IE support
			var delta = (Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail))));
			thisInstance.@org.glob3.mobile.specific.MotionEventProcessor::processMouseWheel(III)(delta, e.clientX, e.clientY);
		};

		if (canvas) {
			if (canvas.addEventListener) {
				// IE9, Chrome, Safari, Opera
				canvas.addEventListener("mousewheel", $wnd.g3mMouseWheelHandler,
						false);
				// Firefox
				canvas.addEventListener("DOMMouseScroll",
						$wnd.g3mMouseWheelHandler, false);
			}
			// IE 6/7/8
			else {
				canvas.attachEvent("onmousewheel", $wnd.g3mMouseWheelHandler);
			}
		}

   }-*/;


}
