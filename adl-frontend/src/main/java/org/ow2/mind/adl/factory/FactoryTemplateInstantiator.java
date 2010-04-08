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

package org.ow2.mind.adl.factory;

import static org.ow2.mind.BindingControllerImplHelper.checkItfName;
import static org.ow2.mind.BindingControllerImplHelper.listFcHelper;
import static org.ow2.mind.adl.ast.ASTHelper.getResolvedComponentDefinition;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.getTemplateName;
import static org.ow2.mind.adl.generic.ast.GenericASTHelper.setPartiallyInstiantedTemplate;
import static org.ow2.mind.adl.implementation.SharedImplementationDecorationHelper.getSharedImplementation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.ow2.mind.adl.ADLErrors;
import org.ow2.mind.adl.DefinitionReferenceResolver;
import org.ow2.mind.adl.annotation.predefined.Singleton;
import org.ow2.mind.adl.ast.ASTHelper;
import org.ow2.mind.adl.ast.Component;
import org.ow2.mind.adl.ast.ComponentContainer;
import org.ow2.mind.adl.ast.DefinitionReference;
import org.ow2.mind.adl.generic.TemplateInstantiator;
import org.ow2.mind.adl.generic.ast.FormalTypeParameter;
import org.ow2.mind.adl.generic.ast.TypeArgument;
import org.ow2.mind.adl.implementation.SharedImplementationDecorationHelper;
import org.ow2.mind.annotation.AnnotationHelper;

public class FactoryTemplateInstantiator
    implements
      TemplateInstantiator,
      BindingController {

  // ---------------------------------------------------------------------------
  // Client interfaces
  // ---------------------------------------------------------------------------

  /** The name of the {@link #clientInstantiatorItf} client interface. */
  public static final String         CLIENT_INSTANTIATOR_ITF_NAME = "client-instantiator";

  /** The client {@link TemplateInstantiator} interface. */
  public TemplateInstantiator        clientInstantiatorItf;

  /** The interface used to resolve referenced definitions. */
  public DefinitionReferenceResolver definitionReferenceResolverItf;

  /** The name of the {@link #loaderItf} client interface. */
  public static final String         LOADER_ITF_NAME              = "loader";

  /** The Loader interface used to load referenced definitions. */
  public Loader                      loaderItf;

  // ---------------------------------------------------------------------------
  // Implementation of the TemplateInstantiator interface
  // ---------------------------------------------------------------------------

  public Definition instantiateTemplate(final Definition genericDefinition,
      final Map<String, Object> typeArgumentValues,
      final Map<Object, Object> context) throws ADLException {
    final Definition instantiatedTemplate = clientInstantiatorItf
        .instantiateTemplate(genericDefinition, typeArgumentValues, context);

    // Try to get the template name from the 'templateName' decoration'. This is
    // useful if the template to instantiate is a partially instantiated
    // template.
    String name = getTemplateName(genericDefinition);
    if (name == null) name = genericDefinition.getName();

    if (FactoryLoader.FACTORY_DEFINITION_NAME.equals(name)
        || FactoryLoader.FACTORY_CONTROLLED_DEFINITION_NAME.equals(name)) {
      final Object typeArgumentValue = typeArgumentValues
          .get(FactoryLoader.FORMAL_TYPE_PARAMETER_NAME);
      assert typeArgumentValue != null;
      if (typeArgumentValue instanceof TypeArgument) {
        final TypeArgument typeArgument = (TypeArgument) typeArgumentValue;
        final DefinitionReference defRef = typeArgument
            .getDefinitionReference();
        if (defRef == null) {
          // The value of the TypeArgument is ANY.
          throw new ADLException(
              ADLErrors.INVALID_REFERENCE_ANY_TEMPLATE_VALUE, typeArgument);
        } else {
          final Definition instantiatedDef = definitionReferenceResolverItf
              .resolve(defRef, null, context);
          checkInstantiatedDef(instantiatedDef, defRef, context);
          ASTHelper.setFactoryInstantiatedDefinition(instantiatedTemplate,
              instantiatedDef);

          // propagate the "shared-implementations"
          final Set<String> sharedImpls = new HashSet<String>();
          findSharedImplementations(instantiatedDef, sharedImpls, context);
          for (final String sharedImpl : sharedImpls) {
            SharedImplementationDecorationHelper.addSharedImplementation(
                instantiatedTemplate, sharedImpl);
          }
        }
        // un-set 'PartiallyInstiantedTemplate' decoration. Will be re-set if
        // needed
        setPartiallyInstiantedTemplate(instantiatedTemplate, false);
      } else {
        assert typeArgumentValue instanceof FormalTypeParameter;
        // the value of the formal type parameter references another formal
        // type parameter
        setPartiallyInstiantedTemplate(instantiatedTemplate, true);
      }
    }
    return instantiatedTemplate;
  }

  protected void checkInstantiatedDef(final Definition instantiatedDef,
      final DefinitionReference defRef, final Map<Object, Object> context)
      throws ADLException {
    if (AnnotationHelper.getAnnotation(instantiatedDef, Singleton.class) != null) {
      // definition is a singleton, cannot make a factory of that kind of
      // definition.
      throw new ADLException(ADLErrors.INVALID_FACTORY_OF_SINGLETON, defRef);
    }
    if (ASTHelper.isAbstract(instantiatedDef)) {
      // definition is abstract, cannot make a factory of that kind of
      // definition.
      throw new ADLException(ADLErrors.INVALID_FACTORY_OF_ABSTRACT, defRef);
    }

    checkReferencedInstantiatedDef(instantiatedDef, defRef, context);
  }

  protected void checkReferencedInstantiatedDef(
      final Definition instantiatedDef, final DefinitionReference defRef,
      final Map<Object, Object> context) throws ADLException {
    if (instantiatedDef instanceof ComponentContainer) {
      for (final Component subComp : ((ComponentContainer) instantiatedDef)
          .getComponents()) {
        if (subComp.getDefinitionReference() == null) continue;
        final Definition subCompDef = definitionReferenceResolverItf.resolve(
            subComp.getDefinitionReference(), instantiatedDef, context);

        if (AnnotationHelper.getAnnotation(subCompDef, Singleton.class) != null) {
          // definition is a singleton, cannot make a factory of that kind of
          // definition.
          throw new ADLException(
              ADLErrors.INVALID_FACTORY_OF_REFERENCED_SINGLETON, defRef,
              subCompDef.getName());
        }

        checkReferencedInstantiatedDef(subCompDef, defRef, context);
      }
    }
  }

  protected void findSharedImplementations(final Definition instantiatedDef,
      final Set<String> sharedImpls, final Map<Object, Object> context)
      throws ADLException {
    sharedImpls.addAll(getSharedImplementation(instantiatedDef));

    if (instantiatedDef instanceof ComponentContainer) {
      for (final Component cmp : ((ComponentContainer) instantiatedDef)
          .getComponents()) {
        findSharedImplementations(getResolvedComponentDefinition(cmp,
            loaderItf, context), sharedImpls, context);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Implementation of the BindingController interface
  // ---------------------------------------------------------------------------

  public void bindFc(final String itfName, final Object value)
      throws NoSuchInterfaceException, IllegalBindingException {
    checkItfName(itfName);

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = (DefinitionReferenceResolver) value;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = (TemplateInstantiator) value;
    } else if (LOADER_ITF_NAME.equals(itfName)) {
      loaderItf = (Loader) value;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "' for binding the interface");
    }
  }

  public String[] listFc() {
    return listFcHelper(DefinitionReferenceResolver.ITF_NAME,
        CLIENT_INSTANTIATOR_ITF_NAME, LOADER_ITF_NAME);
  }

  public Object lookupFc(final String itfName) throws NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      return definitionReferenceResolverItf;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      return clientInstantiatorItf;
    } else if (LOADER_ITF_NAME.equals(itfName)) {
      return loaderItf;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

  public void unbindFc(final String itfName) throws IllegalBindingException,
      NoSuchInterfaceException {
    checkItfName(itfName);

    if (itfName.equals(DefinitionReferenceResolver.ITF_NAME)) {
      definitionReferenceResolverItf = null;
    } else if (itfName.equals(CLIENT_INSTANTIATOR_ITF_NAME)) {
      clientInstantiatorItf = null;
    } else if (LOADER_ITF_NAME.equals(itfName)) {
      loaderItf = null;
    } else {
      throw new NoSuchInterfaceException("No client interface named '"
          + itfName + "'");
    }
  }

}
