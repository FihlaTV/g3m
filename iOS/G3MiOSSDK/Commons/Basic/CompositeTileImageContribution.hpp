//
//  CompositeTileImageContribution.hpp
//  G3MiOSSDK
//
//  Created by Diego Gomez Deck on 4/26/14.
//
//

#ifndef __G3MiOSSDK__CompositeTileImageContribution__
#define __G3MiOSSDK__CompositeTileImageContribution__

#include "TileImageContribution.hpp"
#include <vector>


class CompositeTileImageContribution : public TileImageContribution {
public:

  class ChildContribution {
  public:
    const int                    _childIndex;
    const TileImageContribution* _contribution;

    ChildContribution(const int                    childIndex,
                      const TileImageContribution* contribution);

    ~ChildContribution();
  };


  static const TileImageContribution* create(const std::vector<ChildContribution*>& contributions);


private:
#ifdef C_CODE
  const std::vector<ChildContribution*> _contributions;
#endif
#ifdef JAVA_CODE
  private final java.util.ArrayList<ChildContribution> _contributions;
#endif

  CompositeTileImageContribution(const std::vector<ChildContribution*>& contributions) :
  TileImageContribution(false, 1),
  _contributions(contributions)
  {
  }

public:
  ~CompositeTileImageContribution() {
    const int size = _contributions.size();
    for (int i = 0; i < size; i++) {
      delete _contributions[i];
    }
  }

  const int size() const {
    return _contributions.size();
  }

  const ChildContribution* get(int index) const {
    return _contributions[index];
  }
  
};

#endif