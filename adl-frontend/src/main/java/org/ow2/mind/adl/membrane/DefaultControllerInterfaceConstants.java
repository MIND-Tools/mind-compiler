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

package org.ow2.mind.adl.membrane;

public interface DefaultControllerInterfaceConstants {

  /** The name of the component identity controller interface. */
  public static final String COMPONENT                      = "component";
  /** A shortcut to the {@link #COMPONENT} constant. */
  public static final String CI                             = COMPONENT;
  /** The signature of the {@link #COMPONENT} interface. */
  public static final String COMPONENT_SIGNATURE            = "fractal.api.Component";

  /** The name of the binding controller interface. */
  public static final String BINDING_CONTROLLER             = "bindingController";
  /** A shortcut to the {@link #BINDING_CONTROLLER} constant. */
  public static final String BC                             = BINDING_CONTROLLER;
  /** The signature of the {@link #BINDING_CONTROLLER} interface. */
  public static final String BINDING_CONTROLLER_SIGNATURE   = "fractal.api.BindingController";

  /** The name of the attribute controller interface. */
  public static final String ATTRIBUTE_CONTROLLER           = "attributeController";
  /** A shortcut to the {@link #ATTRIBUTE_CONTROLLER} constant. */
  public static final String AC                             = ATTRIBUTE_CONTROLLER;
  /** The signature of the {@link #ATTRIBUTE_CONTROLLER} interface. */
  public static final String ATTRIBUTE_CONTROLLER_SIGNATURE = "fractal.api.AttributeController";

  /** The name of the life cycle controller interface. */
  public static final String LIFECYCLE_CONTROLLER           = "lifeCycleController";
  /** A shortcut to the {@link #LIFECYCLE_CONTROLLER} constant. */
  public static final String LCC                            = LIFECYCLE_CONTROLLER;
  /** The signature of the {@link #LIFECYCLE_CONTROLLER} interface. */
  public static final String LIFECYCLE_CONTROLLER_SIGNATURE = "fractal.api.LifeCycleController";

  /** The name of the content controller interface. */
  public static final String CONTENT_CONTROLLER             = "contentController";
  /** A shortcut to the {@link #CONTENT_CONTROLLER} constant. */
  public static final String CC                             = CONTENT_CONTROLLER;
  /** The signature of the {@link #CONTENT_CONTROLLER} interface. */
  public static final String CONTENT_CONTROLLER_SIGNATURE   = "fractal.api.ContentController";

  /** The name of the factory interface provided by cloneable component. */
  public static final String FACTORY                        = "factory";
  /** The signature of the {@link #FACTORY} interface. */
  public static final String FACTORY_SIGNATURE              = "fractal.api.Factory";

}
