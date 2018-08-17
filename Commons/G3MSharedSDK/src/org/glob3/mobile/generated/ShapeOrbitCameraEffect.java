package org.glob3.mobile.generated;import java.util.*;

//
//  ShapeOrbitCameraEffect.cpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 1/31/13.
//
//

//
//  ShapeOrbitCameraEffect.hpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 1/31/13.
//
//



//C++ TO JAVA CONVERTER NOTE: Java has no need of forward class declarations:
//class Shape;

public class ShapeOrbitCameraEffect extends EffectWithDuration
{
  private Shape _shape;

  private final double _fromDistance;
  private final double _toDistance;

  private final double _fromAzimuthInRadians;
  private final double _toAzimuthInRadians;

  private final double _fromAltitudeInRadians;
  private final double _toAltitudeInRadians;


  public ShapeOrbitCameraEffect(TimeInterval duration, Shape shape, double fromDistance, double toDistance, Angle fromAzimuth, Angle toAzimuth, Angle fromAltitude, Angle toAltitude)
  {
	  this(duration, shape, fromDistance, toDistance, fromAzimuth, toAzimuth, fromAltitude, toAltitude, false);
  }
//C++ TO JAVA CONVERTER NOTE: Java does not allow default values for parameters. Overloaded methods are inserted above.
//ORIGINAL LINE: ShapeOrbitCameraEffect(const TimeInterval& duration, Shape* shape, double fromDistance, double toDistance, const Angle& fromAzimuth, const Angle& toAzimuth, const Angle& fromAltitude, const Angle& toAltitude, const boolean linearTiming=false) : EffectWithDuration(duration, linearTiming), _shape(shape), _fromDistance(fromDistance), _toDistance(toDistance), _fromAzimuthInRadians(fromAzimuth._radians), _toAzimuthInRadians(toAzimuth._radians), _fromAltitudeInRadians(fromAltitude._radians), _toAltitudeInRadians(toAltitude._radians)
  public ShapeOrbitCameraEffect(TimeInterval duration, Shape shape, double fromDistance, double toDistance, Angle fromAzimuth, Angle toAzimuth, Angle fromAltitude, Angle toAltitude, boolean linearTiming)
  {
	  super(duration, linearTiming);
	  _shape = shape;
	  _fromDistance = fromDistance;
	  _toDistance = toDistance;
	  _fromAzimuthInRadians = fromAzimuth._radians;
	  _toAzimuthInRadians = toAzimuth._radians;
	  _fromAltitudeInRadians = fromAltitude._radians;
	  _toAltitudeInRadians = toAltitude._radians;

  }

  public final void doStep(G3MRenderContext rc, TimeInterval when)
  {
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: const double alpha = getAlpha(when);
	final double alpha = getAlpha(new TimeInterval(when));
  
	final IMathUtils mu = IMathUtils.instance();
	final double distance = mu.linearInterpolation(_fromDistance, _toDistance, alpha);
	final double azimuthInRadians = mu.linearInterpolation(_fromAzimuthInRadians, _toAzimuthInRadians, alpha);
	final double altitudeInRadians = mu.linearInterpolation(_fromAltitudeInRadians, _toAltitudeInRadians, alpha);
  
	rc.getNextCamera().setPointOfView(_shape.getPosition(), distance, Angle.fromRadians(azimuthInRadians), Angle.fromRadians(altitudeInRadians));
  }

  public final void cancel(TimeInterval when)
  {
	// do nothing
  }

  public final void stop(G3MRenderContext rc, TimeInterval when)
  {
	rc.getNextCamera().setPointOfView(_shape.getPosition(), _toDistance, Angle.fromRadians(_toAzimuthInRadians), Angle.fromRadians(_toAltitudeInRadians));
  
  }

}
