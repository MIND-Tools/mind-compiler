
package org.ow2.mind.adl.ast;

import org.objectweb.fractal.adl.Node;

public interface DataField extends Node {

  String getName();

  void setName(String name);

  String getIdt();

  void setIdt(String idt);

  String getType();

  void setType(String type);
}
