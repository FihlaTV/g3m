//
//  GPUUniformKey.hpp
//  G3MiOSSDK
//
//  Created by DIEGO RAMIRO GOMEZ-DECK on 1/4/19.
//

#ifndef GPUUniformKey_hpp
#define GPUUniformKey_hpp

enum GPUUniformKey {
  UNRECOGNIZED_UNIFORM = -1,
  FLAT_COLOR = 0,
  MODELVIEW = 1,
  TEXTURE_EXTENT = 2,
  VIEWPORT_EXTENT = 3,
  TRANSLATION_TEXTURE_COORDS = 4,
  SCALE_TEXTURE_COORDS = 5,
  POINT_SIZE= 6,
  AMBIENT_LIGHT_COLOR = 7,
  DIFFUSE_LIGHT_DIRECTION = 8,
  DIFFUSE_LIGHT_COLOR = 9,
  PROJECTION = 10,
  CAMERA_MODEL = 11,
  MODEL = 12,
  POINT_LIGHT_POSITION= 13,
  POINT_LIGHT_COLOR= 14,
  BILLBOARD_POSITION = 15,
  ROTATION_CENTER_TEXTURE_COORDS = 16,
  ROTATION_ANGLE_TEXTURE_COORDS = 17,
  SAMPLER = 18,
  SAMPLER2 = 19,
  SAMPLER3 = 20,
  TRANSLATION_2D = 21,
  BILLBOARD_ANCHOR = 22,
  CAMERA_POSITION = 23
};

#endif
