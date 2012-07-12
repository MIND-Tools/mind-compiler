
package org.ow2.mind.value;

import java.util.Map;

import org.objectweb.fractal.adl.ADLException;
import org.ow2.mind.value.ast.Array;
import org.ow2.mind.value.ast.BooleanLiteral;
import org.ow2.mind.value.ast.CompoundValue;
import org.ow2.mind.value.ast.CompoundValueField;
import org.ow2.mind.value.ast.MultipleValueContainer;
import org.ow2.mind.value.ast.NullLiteral;
import org.ow2.mind.value.ast.NumberLiteral;
import org.ow2.mind.value.ast.PathLiteral;
import org.ow2.mind.value.ast.Reference;
import org.ow2.mind.value.ast.SingleValueContainer;
import org.ow2.mind.value.ast.StringLiteral;
import org.ow2.mind.value.ast.Value;

import com.google.inject.Inject;

public class BasicValueKindDecorator implements ValueKindDecorator {

  @Inject
  protected ValueKindDecorator recursiveValueKindDecorator;

  public void setValueKind(final Value value, final Map<Object, Object> context)
      throws ADLException {
    if (value instanceof Array) {
      value.astSetDecoration(KIND_DECORATION, "array");
    } else if (value instanceof BooleanLiteral) {
      value.astSetDecoration(KIND_DECORATION, "boolean");
    } else if (value instanceof CompoundValue) {
      value.astSetDecoration(KIND_DECORATION, "compound");
      for (final CompoundValueField field : ((CompoundValue) value)
          .getCompoundValueFields()) {
        recursiveValueKindDecorator.setValueKind(field.getValue(), context);
      }
    } else if (value instanceof NullLiteral) {
      value.astSetDecoration(KIND_DECORATION, "null");
    } else if (value instanceof NumberLiteral) {
      value.astSetDecoration(KIND_DECORATION, "number");
    } else if (value instanceof PathLiteral) {
      value.astSetDecoration(KIND_DECORATION, "path");
    } else if (value instanceof Reference) {
      value.astSetDecoration(KIND_DECORATION, "reference");
    } else if (value instanceof StringLiteral) {
      value.astSetDecoration(KIND_DECORATION, "string");
    }

    if (value instanceof SingleValueContainer) {
      recursiveValueKindDecorator.setValueKind(
          ((SingleValueContainer) value).getValue(), context);
    }
    if (value instanceof MultipleValueContainer) {
      for (final Value subValue : ((MultipleValueContainer) value).getValues()) {
        recursiveValueKindDecorator.setValueKind(subValue, context);
      }
    }
  }

}
