
package org.ow2.mind.value;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.value.ast.Value;

public interface ValueKindDecorator {

  String KIND_DECORATION = "kind";

  void setValueKind(Value value, Map<Object, Object> context)
      throws ADLException;
}
