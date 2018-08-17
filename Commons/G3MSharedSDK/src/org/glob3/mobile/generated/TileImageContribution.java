package org.glob3.mobile.generated;import java.util.*;

//
//  TileImageContribution.cpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 4/22/14.
//
//

//
//  TileImageContribution.hpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 4/22/14.
//
//



public class TileImageContribution extends RCObject
{
	private static final TileImageContribution FULL_COVERAGE_OPAQUE = new TileImageContribution(false, 1);

	private static TileImageContribution _lastFullCoverageTransparent = null;

	private final boolean _isFullCoverage;
	private final Sector _sector = new Sector(); //Sector within the tile where the contribution will be painted
	private final boolean _isTransparent;

	private TileImageContribution(Sector sector, boolean isTransparent, float alpha)
	{
		_isFullCoverage = false;
		_sector = new Sector(sector);
		_isTransparent = isTransparent;
		_alpha = alpha;
	}

//C++ TO JAVA CONVERTER TODO TASK: The implementation of the following method could not be found:
//	TileImageContribution operator =(TileImageContribution that);

	//  void _retain() const {
	//    RCObject::_retain();
	//  }
	//
	//  void _release() const {
	//    RCObject::_release();
	//  }


	protected TileImageContribution(boolean isTransparent, float alpha)
	{
		_isFullCoverage = true;
		_sector = new Sector(Sector.NAN_SECTOR);
		_isTransparent = isTransparent;
		_alpha = alpha;
	}

//  TileImageContribution(const Sector& sector,
//                        bool isFullCoverage,
//                        bool isTransparent,
//                        float alpha) :
//  _isFullCoverage(isFullCoverage),
//  _sector(sector),
//  _isTransparent(isTransparent),
//  _alpha(alpha)
//  {
//  }

	public void dispose()
	{
//C++ TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if JAVA_CODE
		super.dispose();
//#endif
	}

	protected TileImageContribution(TileImageContribution that)
	{
		_isFullCoverage = that._isFullCoverage;
		_sector = new Sector(that._sector);
		_isTransparent = that._isTransparent;
		_alpha = that._alpha;
	}



	public final float _alpha;

	public static TileImageContribution fullCoverageOpaque()
	{
	  FULL_COVERAGE_OPAQUE._retain();
	  return FULL_COVERAGE_OPAQUE;
    
	//  return new TileImageContribution(false, 1);
	}

	public static TileImageContribution fullCoverageTransparent(float alpha)
	{
	  if (alpha <= 0.01)
	  {
		return null;
	  }
    
	  // try to reuse the contribution between calls to avoid too much garbage. Android, in your face!
	  if ((_lastFullCoverageTransparent == null) || (_lastFullCoverageTransparent._alpha != alpha))
	  {
		if (_lastFullCoverageTransparent != null)
		{
		  _lastFullCoverageTransparent._release();
		}
    
		_lastFullCoverageTransparent = new TileImageContribution(true, alpha);
	  }
	  _lastFullCoverageTransparent._retain();
	  return _lastFullCoverageTransparent;
    
	//  return new TileImageContribution(true, alpha);
	}

	public static TileImageContribution partialCoverageOpaque(Sector sector)
	{
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: return new TileImageContribution(sector, false, 1);
	  return new TileImageContribution(new Sector(sector), false, 1);
	}

	public static TileImageContribution partialCoverageTransparent(Sector sector, float alpha)
	{
//C++ TO JAVA CONVERTER WARNING: The following line was determined to be a copy constructor call - this should be verified and a copy constructor should be created if it does not yet exist:
//ORIGINAL LINE: return (alpha <= 0.01) ? null : new TileImageContribution(sector, true, alpha);
	  return (alpha <= 0.01) ? null : new TileImageContribution(new Sector(sector), true, alpha);
	}

	public static void retainContribution(TileImageContribution contribution)
	{
	  if (contribution != null)
	  {
		contribution._retain();
	  }
	}
	public static void releaseContribution(TileImageContribution contribution)
	{
	  if (contribution != null)
	  {
		contribution._release();
	  }
	}

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean isFullCoverageAndOpaque() const
	public final boolean isFullCoverageAndOpaque()
	{
		return _isFullCoverage && !_isTransparent && isOpaque();
	}

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean isFullCoverage() const
	public final boolean isFullCoverage()
	{
		return _isFullCoverage;
	}

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: const Sector* getSector() const
	public final Sector getSector()
	{
		return _sector;
	}

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean isOpaque() const
	public final boolean isOpaque()
	{
		return (_alpha >= 0.99);
	}

//C++ TO JAVA CONVERTER WARNING: 'const' methods are not available in Java:
//ORIGINAL LINE: boolean isTransparent() const
	public final boolean isTransparent()
	{
		return _isTransparent;
	}

}
