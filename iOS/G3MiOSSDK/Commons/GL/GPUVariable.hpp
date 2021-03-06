//
//  GPUVariable.hpp
//  G3MiOSSDK
//
//  Created by Jose Miguel SN on 20/06/13.
//
//

#ifndef __G3MiOSSDK__GPUVariable__
#define __G3MiOSSDK__GPUVariable__

#include <string>

#include "GPUUniformKey.hpp"
#include "GPUAttributeKey.hpp"
#include "GPUVariableType.hpp"


class GPUVariable {
public:

  static GPUUniformKey getUniformKey(const std::string& name);
  static GPUAttributeKey getAttributeKey(const std::string& name);

  static int getUniformCode(GPUUniformKey u);
  static int getAttributeCode(GPUAttributeKey a);

  static int getUniformCode(int u);
  static int getAttributeCode(int a);

  static bool hasUniform(int code, int u);
  static bool hasAttribute(int code, int a);

  static bool hasUniform(int code, GPUUniformKey u);
  static bool hasAttribute(int code, GPUAttributeKey a);

  virtual ~GPUVariable() {}

  const GPUVariableType _variableType;
  const std::string     _name;

  GPUVariable(const std::string& name,
              GPUVariableType type) :
  _name(name),
  _variableType(type)
  {
  }

};

#endif
