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

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.NodeFactory;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.merger.NodeMerger;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.cecilia.adl.plugin.JavaPluginManager;
import org.objectweb.fractal.cecilia.adl.plugin.PluginManager;
import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.InputResourceLocator;
import org.ow2.mind.annotation.AnnotationChainFactory;
import org.ow2.mind.idl.annotation.AnnotationLoader;
import org.ow2.mind.idl.annotation.AnnotationProcessorLoader;
import org.ow2.mind.idl.annotation.IDLLoaderPhase;
import org.ow2.mind.idl.parser.IDLFileLoader;
import org.ow2.mind.plugin.SimpleClassPluginFactory;
import org.ow2.mind.st.STLoaderFactory;
import org.ow2.mind.st.STNodeFactoryImpl;
import org.ow2.mind.st.STNodeMergerImpl;
import org.ow2.mind.st.XMLSTNodeFactoryImpl;

public final class IDLLoaderChainFactory {

  private IDLLoaderChainFactory() {
  }

  public static final class IDLFrontend {
    public final IDLLocator locator;
    public final IDLCache   cache;
    public final IDLLoader  loader;

    public IDLFrontend(final IDLLocator locator, final IDLCache cache,
        final IDLLoader loader) {
      this.locator = locator;
      this.cache = cache;
      this.loader = loader;
    }
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

  public static IDLFrontend newLoader() {
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();

    final org.objectweb.fractal.adl.Factory pluginFactory;
    final SimpleClassPluginFactory scpf = new SimpleClassPluginFactory();

    // Configuration of plugin factory components
    pluginFactory = scpf;

    return newLoader(newIDLLocator(inputResourceLocator), inputResourceLocator,
        pluginFactory);
  }

  public static IDLFrontend newLoader(final IDLLocator idlLocator,
      final InputResourceLocator inputResourceLocator,
      final org.objectweb.fractal.adl.Factory pluginFactory) {

    // plugin manager components
    PluginManager pluginManager;
    final JavaPluginManager jpm = new JavaPluginManager();
    jpm.pluginFactoryItf = pluginFactory;
    pluginManager = jpm;

    // Loader chain components
    IDLLoader idlLoader;
    IDLCache idlCache;
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
    idlCache = cil;
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
    apl1.pluginManagerItf = pluginManager;
    apl2.pluginManagerItf = pluginManager;

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
    ihr.idlLocatorItf = idlLocator;
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
    final STNodeMergerImpl nodeMerger = new STNodeMergerImpl();
    nodeMerger.setClassLoader(IDLLoaderChainFactory.class.getClassLoader());
    ifl.nodeFactoryItf = xnf;
    hl.nodeFactoryItf = nf;
    ihr.nodeFactoryItf = nf;

    bil.inputResourceLocatorItf = inputResourceLocator;

    // configuration of plugin-manager
    try {
      ((BindingController) pluginManager).bindFc(NodeFactory.ITF_NAME, nf);
      ((BindingController) pluginManager).bindFc(NodeMerger.ITF_NAME,
          nodeMerger);
      ((BindingController) pluginManager).bindFc(IDLCache.ITF_NAME, idlCache);
      ((BindingController) pluginManager).bindFc(IDLLoader.ITF_NAME, idlLoader);
      ((BindingController) pluginManager).bindFc("template-loader",
          STLoaderFactory.newSTLoader());
    } catch (final Exception e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "adl-frontend instantiation error");
    }

    return new IDLFrontend(idlLocator, idlCache, idlLoader);
  }
}
