package org.glob3.mobile.generated;//
//  CanvasImageBuilder.cpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 1/10/14.
//
//

//
//  CanvasImageBuilder.hpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 1/10/14.
//
//


//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class ICanvas;

public abstract class CanvasImageBuilder extends AbstractImageBuilder
{
  private ICanvas _canvas;
  private int _canvasWidth;
  private int _canvasHeight;

  private ICanvas getCanvas(G3MContext context)
  {
	if ((_canvas == null) || (_canvasWidth != _width) || (_canvasHeight != _height))
	{
	  if (_canvas != null)
		  _canvas.dispose();
  
	  final IFactory factory = context.getFactory();
  
	  _canvas = factory.createCanvas(_retina);
	  _canvas.initialize(_width, _height);
	  _canvasWidth = _width;
	  _canvasHeight = _height;
	}
	else
	{
	  _canvas.setFillColor(Color.transparent());
	  _canvas.fillRectangle(0, 0, _width, _height);
	}
  
	return _canvas;
  }

  protected final int _width;
  protected final int _height;
  protected final boolean _retina;

  protected CanvasImageBuilder(int width, int height, boolean retina)
  {
	  _width = width;
	  _height = height;
	  _retina = retina;
	  _canvas = null;
	  _canvasWidth = 0;
	  _canvasHeight = 0;
  }


  ///#include "IStringUtils.hpp"
  
  public void dispose()
  {
	if (_canvas != null)
		_canvas.dispose();
  
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JAVA_CODE
	super.dispose();
//#endif
  }

  protected abstract void buildOnCanvas(G3MContext context, ICanvas canvas);

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: virtual String getImageName(const G3MContext* context) const = 0;
  protected abstract String getImageName(G3MContext context);

  public final void build(G3MContext context, IImageBuilderListener listener, boolean deleteListener)
  {
	ICanvas canvas = getCanvas(context);
  
	buildOnCanvas(context, canvas);
  
	canvas.createImage(new CanvasImageBuilder_ImageListener(getImageName(context), listener, deleteListener), true);
  }

}
