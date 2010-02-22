/**
 * Copyright (C) 2009 STMicroelectronics
 *
 * This file is part of "Mind Compiler" is free software: you can redistribute 
 * it and/or modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact: mind@ow2.org
 *
 * Authors: Matthieu Leclercq
 * Contributors: 
 */

package org.ow2.mind.adl;

import static org.objectweb.fractal.adl.error.ErrorTemplateValidator.validErrorTemplate;

import org.objectweb.fractal.adl.error.ErrorTemplate;

/**
 * Enumeration of Errors that can be raised by the ADL-front-end.
 */
public enum ADLErrors implements ErrorTemplate {

  /** */
  INVALID_DEFINITION_NAME("\"%s\" is not a valid definition name",
      "<definition name>"),

  /** */
  INVALID_REFERENCE_NOT_A_TYPE(
      "Invalid reference: %s does not refer to a type definition.",
      "<adl name>"),

  /** */
  INVALID_REFERENCE_NOT_A_COMPOSITE(
      "Invalid reference: %s does not refer to a composite.", "<adl name>"),

  /** */
  INVALID_REFERENCE_NOT_A_PRIMITIVE(
      "Invalid reference: %s does not refer to a primitive.", "<adl name>"),

  /** */
  INVALID_REFERENCE_FOR_SUB_COMPONENT(
      "Invalid reference: %s refer to a type or an abstract definition.",
      "<adl name>"),

  /** */
  INVALID_SUB_COMPONENT("Invalid sub-component."),

  /** */
  INVALID_REFERENCE_MISSING_TEMPLATE_VALUE(
      "Invalid reference: missing type arguments."),

  /** */
  INVALID_REFERENCE_TOO_MANY_TEMPLATE_VALUE(
      "Invalid reference: too many type arguments."),

  /** */
  INVALID_REFERENCE_ANY_TEMPLATE_VALUE(
      "Invalid reference: \"any\" type argument is not allowed here."),

  /** */
  INVALID_ANY_TEMPLATE_VALUE(
      "Invalid reference: sub-component \"%s\" must be overridden or \"any\" type argument is not allowed here.",
      "<sub-comp name>"),

  /** */
  INVALID_REFERENCE_NO_SUCH_TEMPLATE_VARIABLE(
      "Invalid reference: no such type parameter \"%s\".", "<var name>"),

  /** */
  INVALID_REFERENCE_NO_TEMPLATE_VARIABLE(
      "Invalid reference: %s has no type parameter.", "<def name>"),

  /** */
  UNDEFINED_TEMPALTE_VARIABLE(
      "Type parameter \"%s\" is not defined in current definition",
      "<var name>"),

  /** */
  DUPLICATED_TEMPALTE_VARIABLE_NAME("Duplicated type parameter \"%s\"",
      "<name>"),

  /** */
  INVALID_TEMPLATE_VALUE_TYPE_DEFINITON(
      "Invalid type argument: %s is a type or abstract definition.",
      "<template value>"),

  /** */
  INVALID_TEMPLATE_VALUE_MISSING_SERVER_INTERFACE(
      "Invalid type argument: %s must provide a \"%s\" interface.",
      "<template value>", "<server itf name>"),

  /** */
  INVALID_TEMPLATE_VALUE_MISSING_CLIENT_INTERFACE(
      "Invalid type argument: %s must require a \"%s\" interface.",
      "<template value>", "<client itf name>"),

  /** */
  INVALID_TEMPLATE_VALUE_CLIENT_INTERFACE_MUST_BE_OPTIONAL(
      "Invalid type argument: required interface \"%s\" must be optional. Interface declared at %s",
      "<client itf name>", "<client itf locator>"),

  /** */
  DO_NOT_OVERRIDE("Declaration does not override an inherited declaration."),

  /** */
  INVALID_INTERFACE_NAME_OVERRIDE_INHERITED_INTERFACE(
      "Invalid interface name, an interface with the same name already exist in inherited definition at %s",
      "<location>"),

  /** */
  INVALID_ATTRIBUTE_OVERRIDE_INHERITED_ATTRIBUTE_TYPE(
      "Invalid attribute type, an attribute with the same name already exist in inherited definition with a different type at %s",
      "<location>"),

  /** */
  INVALID_ATTRIBUTE_MISSING_TYPE("Invalid attribute, missing type"),

  /** */
  INVALID_ATTRIBUTE_VALUE_INCOMPATIBLE_TYPE(
      "Invalid attribute, incompatible type"),

  /** */
  DUPLICATED_ATTRIBUTE_NAME(
      "Redefinition of attribute \"%s\" (previously defined at \"%s\").",
      "<name>", "<location>"),

  /** */
  INVALID_PATH("Invalid Path \"%s\"", "<path>"),

  /** */
  SOURCE_NOT_FOUND("Can't find source file \"%s\"", "<path>"),

  /** */
  MISSING_SOURCE("Primitive component must have source"),

  /** */
  INVALID_REFERENCE_NO_PARAMETER(
      "Invalid reference: referenced definition has no parameter."),

  /** */
  INVALID_REFERENCE_MISSING_ARGUMENT("Invalid reference: missing argument."),

  /** */
  INVALID_REFERENCE_TOO_MANY_ARGUMENT("Invalid reference: too many argument."),

  /** */
  INVALID_REFERENCE_NO_SUCH_PARAMETER(
      "Invalid reference: no such parameter \"%s\".", "<param name>"),

  /** */
  UNDEFINED_PARAMETER("Parameter \"%s\" is not defined in current definition.",
      "<var name>"),

  /** */
  DUPLICATED_ARGUMENT_VARIABLE_NAME("Duplicated argument \"%s\"", "<name>"),

  /** */
  INCOMPATIBLE_ARGUMENT_TYPE("Incompatible type for argument \"%s\".",
      "<var name>"),

  /** */
  INCOMPATIBLE_ARGUMENT_VALUE("Incompatible type for argument \"%s\".",
      "<var name>"),

  /** */
  INCOMPATIBLE_ATTRIBUTE_VALUE("Incompatible type value for attribute \"%s\".",
      "<attr name>"),

  /** */
  INVALID_EXTENDS_TYPE_EXTENDS_PRIMITIVE(
      "Type definition cannot extends the primitive defintion \"%s\"",
      "<primitive def>"),

  /** */
  INVALID_EXTENDS_TYPE_EXTENDS_COMPOSITE(
      "Type definition cannot extends the composite defintion \"%s\"",
      "<composite def>"),

  /** */
  INVALID_EXTENDS_PRIMITIVE_EXTENDS_COMPOSITE(
      "Primitive definition cannot extends the composite defintion \"%s\"",
      "<composite def>"),

  /** */
  INVALID_EXTENDS_COMPOSITE_EXTENDS_PRIMITIVE(
      "Composite definition cannot extends the primitive defintion \"%s\"",
      "<primitive def>"),

  /** */
  INVALID_FACTORY_OF_SINGLETON(
      "Cannot make a factory of a singleton definition"),

  /** */
  INVALID_FACTORY_OF_ABSTRACT("Cannot make a factory of an abstract definition"),

  /** */
  INVALID_FACTORY_OF_REFERENCED_SINGLETON(
      "Cannot make a factory of this definition. Definition references the singleton definition \"%s\"",
      "<singleton-def>"),

  /** */
  SINGLETON_WITH_DIFFERENT_NAME(
      "Singleton definition must always be instantiated with the same name"),

  /** */
  INSTANTIATE_TYPE_DEFINIITON("Can't instantiate type definition \"%s\".",
      "<def name>"),

  /** */
  INSTANTIATE_TEMPLATE_DEFINIITON(
      "Can't instantiate definition \"%s\", definition contains template variables.",
      "<def name>"),

  /** */
  INSTANTIATE_ARGUMENT_DEFINIITON(
      "Can't instantiate definition \"%s\", definition contains arguments.",
      "<def name>"),

  /** */
  UNKNOWN_IMPORT("Unknown import."),

  ;

  /** The groupId of ErrorTemplates defined in this enumeration. */
  public static final String GROUP_ID = "MADL";

  private int                id;
  private String             format;

  private ADLErrors(final String format, final Object... args) {
    this.id = ordinal();
    this.format = format;

    assert validErrorTemplate(this, args);
  }

  public int getErrorId() {
    return id;
  }

  public String getGroupId() {
    return GROUP_ID;
  }

  public String getFormatedMessage(final Object... args) {
    return String.format(format, args);
  }

  public String getFormat() {
    return format;
  }

}
