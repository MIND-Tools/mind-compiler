
package org.ow2.mind.value.ast;

public interface CompoundValue extends Value {

  CompoundValueField[] getCompoundValueFields();

  void addCompoundValueField(CompoundValueField field);

  void removeCompoundValueField(CompoundValueField field);
}
