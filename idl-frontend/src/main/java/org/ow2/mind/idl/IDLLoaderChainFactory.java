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

package org.ow2.mind.idl;

import static org.ow2.mind.idl.IDLLocator.IDT_RESOURCE_KIND;
import static org.ow2.mind.idl.IDLLocator.ITF_RESOURCE_KIND;

import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.annotation.AnnotationChainFactory;
import org.ow2.mind.idl.annotation.AnnotationLoader;
import org.ow2.mind.idl.annotation.AnnotationProcessorLoader;
import org.ow2.mind.idl.annotation.IDLLoaderPhase;
import org.ow2.mind.idl.parser.IDLFileLoader;
import org.ow2.mind.st.STNodeFactoryImpl;
import org.ow2.mind.st.XMLSTNodeFactoryImpl;

public final class IDLLoaderChainFactory {

  private IDLLoaderChainFactory() {
  }

  public static IDLLocator newIDLLocator(
      final BasicInputResourceLocator inputResourceLocator) {
    final IDLLocator idlLocator = new BasicIDLLocator();
    inputResourceLocator.genericResourceLocators.put(IDT_RESOURCE_KIND,
        idlLocator);
    inputResourceLocator.genericResourceLocators.put(ITF_RESOURCE_KIND,
        idlLocator);
    return idlLocator;
  }

  public static IDLLoader newLoader() {
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();

    return newLoader(newIDLLocator(inputResourceLocator), inputResourceLocator);
  }

  public static IDLLoader newLoader(final IDLLocator idlLocator,
      final InputResourceLocator inputResourceLocator) {

    // Loader chain components
    IDLLoader idlLoader;
    final IDLFileLoader ifl = new IDLFileLoader();
    final AnnotationLoader al = new AnnotationLoader();
    final AnnotationProcessorLoader apl1 = new AnnotationProcessorLoader();
    final IncludeLoader uil = new IncludeLoader();
    final ExtendsInterfaceLoader eil = new ExtendsInterfaceLoader();
    final IDLTypeCheckerLoader tcl = new IDLTypeCheckerLoader();
    final KindDecorationLoader kdl = new KindDecorationLoader();
    final AnnotationProcessorLoader apl2 = new AnnotationProcessorLoader();
    final BinaryIDLLoader bil = new BinaryIDLLoader();
    final HeaderLoader hl = new HeaderLoader();
    final CacheIDLLoader cil = new CacheIDLLoader();

    idlLoader = cil;
    cil.clientIDLLoaderItf = hl;
    hl.clientIDLLoaderItf = bil;
    bil.clientIDLLoaderItf = apl2;
    apl2.clientIDLLoaderItf = kdl;
    kdl.clientIDLLoaderItf = tcl;
    tcl.clientIDLLoaderItf = uil;
    uil.clientIDLLoaderItf = eil;
    eil.clientIDLLoaderItf = apl1;
    apl1.clientIDLLoaderItf = al;
    al.clientIDLLoaderItf = ifl;

    apl1.setPhase(IDLLoaderPhase.AFTER_PARSING.name());
    apl2.setPhase(IDLLoaderPhase.AFTER_CHECKING.name());

    al.annotationCheckerItf = AnnotationChainFactory.newAnnotationChecker();

    // Recursive IDL Loader
    RecursiveIDLLoader recursiveIDLLoader;
    final RecursiveIDLLoaderImpl ril = new RecursiveIDLLoaderImpl();
    ril.clientIDLLoaderItf = idlLoader;
    recursiveIDLLoader = ril;

    // IncludeResolver sub-chain
    IncludeResolver includeResolver;
    final BasicIncludeResolver bir = new BasicIncludeResolver();
    final IncludeHeaderResolver ihr = new IncludeHeaderResolver();
    final InputResourcesIncludeResolver irir = new InputResourcesIncludeResolver();
    final CachingIncludeResolver cir = new CachingIncludeResolver();

    includeResolver = cir;
    cir.clientResolverItf = irir;
    irir.clientResolverItf = ihr;
    ihr.clientResolverItf = bir;

    bir.recursiveIdlLoaderItf = recursiveIDLLoader;
    bir.idlLocatorItf = idlLocator;
    cir.idlLoaderItf = idlLoader;

    uil.idlResolverItf = includeResolver;

    // Interface Reference Resolver
    InterfaceReferenceResolver interfaceReferenceResolver;
    final BasicInterfaceReferenceResolver birr = new BasicInterfaceReferenceResolver();
    final InputResourcesInterfaceReferenceResolver irirr = new InputResourcesInterfaceReferenceResolver();
    final ReferencedInterfaceResolver rir = new ReferencedInterfaceResolver();

    interfaceReferenceResolver = rir;
    rir.clientResolverItf = irirr;
    irirr.clientResolverItf = birr;
    birr.recursiveIdlLoaderItf = recursiveIDLLoader;

    eil.interfaceReferenceResolverItf = interfaceReferenceResolver;
    tcl.interfaceReferenceResolverItf = interfaceReferenceResolver;

    ifl.idlLocatorItf = idlLocator;
    bil.idlLocatorItf = idlLocator;

    // node factories
    final XMLSTNodeFactoryImpl xnf = new XMLSTNodeFactoryImpl();
    // set my class loader as classloader used by XMLNodeFactory
    xnf.setClassLoader(IDLLoaderChainFactory.class.getClassLoader());
    final STNodeFactoryImpl nf = new STNodeFactoryImpl();
    ifl.nodeFactoryItf = xnf;
    hl.nodeFactoryItf = nf;
    ihr.nodeFactoryItf = nf;

    bil.inputResourceLocatorItf = inputResourceLocator;

    return idlLoader;
  }
}
