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

  // ---------------------------------------------------------------------------
  // References Errors (000-009)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_REFERENCE_NOT_A_TYPE(0,
      "Invalid reference: %s does not refer to a type definition.",
      "<adl name>"),

  /** */
  INVALID_REFERENCE_NOT_A_COMPOSITE(1,
      "Invalid reference: %s does not refer to a composite.", "<adl name>"),

  /** */
  INVALID_REFERENCE_NOT_A_PRIMITIVE(2,
      "Invalid reference: %s does not refer to a primitive.", "<adl name>"),

  /** */
  INVALID_REFERENCE_FOR_SUB_COMPONENT(3,
      "Invalid reference: %s refer to a type or an abstract definition.",
      "<adl name>"),

  // ---------------------------------------------------------------------------
  // Sub component errors (010-019)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_SUB_COMPONENT(10, "Invalid sub-component."),

  /** */
  INVALID_SUB_COMPONENT_DUPLICATE_SINGLETON(
      11,
      "Invalid sub-component \"%s\" : duplicates singleton definition \"%s\". Previous use of singleton definition at %s",
      "<sub-comp>", "<singleton def>", "<previous location>"),

  /** */
  INVALID_CONTENT_CONTROLLER_NOT_A_COMPOSITE(
      12,
      "Invalid @controller.ContentController annotation, definition is not a composite"),

  /** */
  INVALID_CONTENT_CONTROLLER_MISSING_COMPONENT_CONTROLLER_ON_SUB_COMPONENT(
      13,
      "Invalid @controller.ContentController annotation, sub-component %s must have a \"component\" controller interface",
      "<sub comp name>"),

  /** */
  INVALID_CONTENT_CONTROLLER_MISSING_BINDING_CONTROLLER_ON_SUB_COMPONENT(
      14,
      "Invalid @controller.ContentController annotation, sub-component %s must have a \"bindingController\" interface",
      "<sub comp name>"),

  /** */
  WARNING_SINGLETON_SUB_COMPONENT(
      15,
      "WARNINIG : sub-component \"%s\" is singleton. The \"@Singleton\" annotation should be"
          + " added on definition", "<subCompName>"),

  // ---------------------------------------------------------------------------
  // template errors (020-049)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_REFERENCE_MISSING_TEMPLATE_VALUE(20,
      "Invalid reference: missing type arguments."),

  /** */
  INVALID_REFERENCE_TOO_MANY_TEMPLATE_VALUE(21,
      "Invalid reference: too many type arguments."),

  /** */
  INVALID_REFERENCE_ANY_TEMPLATE_VALUE(22,
      "Invalid reference: \"any\" type argument is not allowed here."),

  /** */
  INVALID_ANY_TEMPLATE_VALUE(
      23,
      "Invalid reference: sub-component \"%s\" must be overridden or \"any\" type argument is not allowed here.",
      "<sub-comp name>"),

  /** */
  INVALID_REFERENCE_NO_SUCH_TEMPLATE_VARIABLE(24,
      "Invalid reference: no such type parameter \"%s\".", "<var name>"),

  /** */
  INVALID_REFERENCE_NO_TEMPLATE_VARIABLE(25,
      "Invalid reference: %s has no type parameter.", "<def name>"),

  /** */
  UNDEFINED_TEMPALTE_VARIABLE(26,
      "Type parameter \"%s\" is not defined in current definition",
      "<var name>"),

  /** */
  DUPLICATED_TEMPALTE_VARIABLE_NAME(27, "Duplicated type parameter \"%s\"",
      "<name>"),

  /** */
  INVALID_TEMPLATE_VALUE_TYPE_DEFINITON(28,
      "Invalid type argument: %s is a type or abstract definition.",
      "<template value>"),

  /** */
  INVALID_TEMPLATE_VALUE_MISSING_SERVER_INTERFACE(29,
      "Invalid type argument: %s must provide a \"%s\" interface.",
      "<template value>", "<server itf name>"),

  /** */
  INVALID_TEMPLATE_VALUE_MISSING_CLIENT_INTERFACE(30,
      "Invalid type argument: %s must require a \"%s\" interface.",
      "<template value>", "<client itf name>"),

  /** */
  INVALID_TEMPLATE_VALUE_CLIENT_INTERFACE_MUST_BE_OPTIONAL(
      31,
      "Invalid type argument: required interface \"%s\" must be optional. Interface declared at %s",
      "<client itf name>", "<client itf locator>"),

  /** */
  WARNING_TEMPLATE_VARIABLE_HIDE(32,
      "WARNING : template variable %s hides import at %s",
      "<template variable>", "<import location>"),

  // ---------------------------------------------------------------------------
  // Inheritance errors (50-59)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_EXTENDS_TYPE_EXTENDS_PRIMITIVE(50,
      "Type definition cannot extends the primitive defintion \"%s\"",
      "<primitive def>"),

  /** */
  INVALID_EXTENDS_TYPE_EXTENDS_COMPOSITE(51,
      "Type definition cannot extends the composite defintion \"%s\"",
      "<composite def>"),

  /** */
  INVALID_EXTENDS_PRIMITIVE_EXTENDS_COMPOSITE(52,
      "Primitive definition cannot extends the composite defintion \"%s\"",
      "<composite def>"),

  /** */
  INVALID_EXTENDS_COMPOSITE_EXTENDS_PRIMITIVE(53,
      "Composite definition cannot extends the primitive defintion \"%s\"",
      "<primitive def>"),

  /** */
  DO_NOT_OVERRIDE(54, "Declaration does not override an inherited declaration."),

  /** */
  INVALID_INTERFACE_NAME_OVERRIDE_INHERITED_INTERFACE(
      55,
      "Invalid interface name, an interface with the same name already exist in inherited definition at %s",
      "<location>"),

  /** */
  INVALID_ATTRIBUTE_OVERRIDE_INHERITED_ATTRIBUTE_TYPE(
      56,
      "Invalid attribute type, an attribute with the same name already exist in inherited definition with a different type at %s",
      "<location>"),

  // ---------------------------------------------------------------------------
  // Attribute errors (60-64)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_ATTRIBUTE_MISSING_TYPE(60, "Invalid attribute, missing type"),

  /** */
  INVALID_ATTRIBUTE_VALUE_INCOMPATIBLE_TYPE(61,
      "Invalid attribute, incompatible type"),

  /** */
  DUPLICATED_ATTRIBUTE_NAME(62,
      "Redefinition of attribute \"%s\" (previously defined at \"%s\").",
      "<name>", "<location>"),

  /** */
  INVALID_ATTRIBUTE_CONTROLLER_NO_ATTRIBUTE(
      63,
      "Invalid @controller.AttributeController annotation, definition has no attribute"),

  /** */
  WARNING_ATTRIBUTE_UNSIGNED_ASSIGNED_TO_NEGATIVE(64,
      "WARNING : Initialize unsigned attribute with negative value"),

  // ---------------------------------------------------------------------------
  // Implementation errors (65-69)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_PATH(65, "Invalid Path \"%s\"", "<path>"),

  /** */
  SOURCE_NOT_FOUND(66, "Can't find source file \"%s\"", "<path>"),

  /** */
  MISSING_SOURCE(67, "Primitive component must have source"),

  /** */
  MULTIPLE_DATA(68, "\"data\" declaration can only appear at most once."),

  // ---------------------------------------------------------------------------
  // Parameter errors (70-89)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_REFERENCE_NO_PARAMETER(70,
      "Invalid reference: referenced definition has no parameter."),

  /** */
  INVALID_REFERENCE_MISSING_ARGUMENT(71, "Invalid reference: missing argument."),

  /** */
  INVALID_REFERENCE_TOO_MANY_ARGUMENT(72,
      "Invalid reference: too many argument."),

  /** */
  INVALID_REFERENCE_NO_SUCH_PARAMETER(73,
      "Invalid reference: no such parameter \"%s\".", "<param name>"),

  /** */
  UNDEFINED_PARAMETER(74,
      "Parameter \"%s\" is not defined in current definition.", "<var name>"),

  /** */
  DUPLICATED_ARGUMENT_VARIABLE_NAME(75, "Duplicated argument \"%s\"", "<name>"),

  /** */
  INCOMPATIBLE_ARGUMENT_TYPE(76, "Incompatible type for argument \"%s\".",
      "<var name>"),

  /** */
  INCOMPATIBLE_ARGUMENT_VALUE(77, "Incompatible type for argument \"%s\".",
      "<var name>"),

  /** */
  INCOMPATIBLE_ATTRIBUTE_VALUE(78,
      "Incompatible type value for attribute \"%s\".", "<attr name>"),

  // ---------------------------------------------------------------------------
  // Factory errors (90-99)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_FACTORY_OF_SINGLETON(90,
      "Cannot make a factory of a singleton definition"),

  /** */
  INVALID_FACTORY_OF_ABSTRACT(91,
      "Cannot make a factory of an abstract definition"),

  /** */
  INVALID_FACTORY_OF_REFERENCED_SINGLETON(
      92,
      "Cannot make a factory of this definition. Definition references the singleton definition \"%s\"",
      "<singleton-def>"),

  /** */
  SINGLETON_WITH_DIFFERENT_NAME(93,
      "Singleton definition must always be instantiated with the same name"),

  // ---------------------------------------------------------------------------
  // Graph errors (100-109)
  // ---------------------------------------------------------------------------

  /** */
  INSTANTIATE_TYPE_DEFINIITON(100, "Can't instantiate type definition \"%s\".",
      "<def name>"),

  /** */
  INSTANTIATE_TEMPLATE_DEFINIITON(
      101,
      "Can't instantiate definition \"%s\", definition contains template variables.",
      "<def name>"),

  /** */
  INSTANTIATE_ARGUMENT_DEFINIITON(102,
      "Can't instantiate definition \"%s\", definition contains arguments.",
      "<def name>"),

  // ---------------------------------------------------------------------------
  // Import errors (110-119)
  // ---------------------------------------------------------------------------

  /** */
  UNKNOWN_IMPORT(110, "Unknown import."),

  // ---------------------------------------------------------------------------
  // Bindings errors (120-149)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_BINDING_CARDINALITY(120,
      "Invalid binding between a singleton interface and a collection interface."),

  /** */
  INVALID_BINDING_COLLECTION_SIZE(
      121,
      "Invalid binding, incompatible collection sizes. Size of \"%s\" (%d) is greater that size of \"%s\" (%d). Change size of \"%1$s\" or mark it optional.",
      "<fromName>", /* fromSize */1, "<toName>", /* toSize */2),

  /** */
  INVALID_BINDING_CONTROLLER_NO_BINDING(
      122,
      "Invalid @controller.BindingController annotation, definition has no client interface. You should set the 'allowNoRequiredItf' annotation field to true"),

  // ---------------------------------------------------------------------------
  // Membrane errors (150-159)
  // ---------------------------------------------------------------------------

  /** */
  INVALID_CONTROLLER_INTERFACE_NO_SUCH_INTERFACE(150,
      "Invalid controller interface \"%s\", no such interface", "<itfName>"),

  /** */
  INVALID_MEMBRANE_MISSING_CONTROLLER(151,
      "Invalid membrane. Missing controller for interfaces : %s", "<itfs list>"),

  /** */
  INVALID_MEMBRANE_UNIMPLEMENTED_INTERFACE(
      152,
      "Invalid membrane. interfaces %s are not implemented. Add a source file or a controller.",
      "<itfs list>"),

  // ---------------------------------------------------------------------------
  // Interface errors (160-169)
  // ---------------------------------------------------------------------------

  /** */
  // Temporary error (i.e. unsupported feature)
  MISSING_COLLECTION_SIZE(
      169,
      "Unbounded collection interface is not yet supported. The interface %s must have a fixed size.",
      "<itf name>");

  /** The groupId of ErrorTemplates defined in this enumeration. */
  public static final String GROUP_ID = "MADL";

  private int                id;
  private String             format;

  private ADLErrors(final int id, final String format, final Object... args) {
    this.id = id;
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
