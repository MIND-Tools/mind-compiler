
package org.ow2.mind.adl.annotations;

import static org.objectweb.fractal.adl.NodeUtil.castNodeError;
import static org.ow2.mind.BCImplChecker.checkBCImplementation;
import static org.testng.Assert.assertEquals;

import java.util.HashMap;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.BasicInputResourceLocator;
import org.ow2.mind.adl.ADLLocator;
import org.ow2.mind.adl.Factory;
import org.ow2.mind.adl.annotation.AnnotationLoader;
import org.ow2.mind.adl.ast.ImplementationContainer;
import org.ow2.mind.adl.ast.Source;
import org.ow2.mind.adl.implementation.ImplementationLocator;
import org.ow2.mind.annotation.AnnotationHelper;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.IDLLoaderChainFactory.IDLFrontend;
import org.ow2.mind.idl.IDLLocator;
import org.ow2.mind.plugin.SimpleClassPluginFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestAnnotations {
  private Loader loader;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {
    // input locators
    final BasicInputResourceLocator inputResourceLocator = new BasicInputResourceLocator();
    final IDLLocator idlLocator = IDLLoaderChainFactory
        .newIDLLocator(inputResourceLocator);
    final ADLLocator adlLocator = Factory.newADLLocator(inputResourceLocator);
    final ImplementationLocator implementationLocator = Factory
        .newImplementationLocator(inputResourceLocator);

    // Plugin Manager Components
    final org.objectweb.fractal.adl.Factory pluginFactory = new SimpleClassPluginFactory();

    // loader chains
    final IDLFrontend idlFrontend = IDLLoaderChainFactory.newLoader(idlLocator,
        inputResourceLocator, pluginFactory);
    final Loader adlLoader = Factory.newLoader(inputResourceLocator,
        adlLocator, idlLocator, implementationLocator, idlFrontend.cache,
        idlFrontend.loader, pluginFactory);
    loader = adlLoader;
  }

  @Test(groups = {"functional", "checkin"})
  public void testAnnotationLoaderBC() throws Exception {
    checkBCImplementation(new AnnotationLoader());
  }

  @Test(groups = {"functional"})
  public void test1() throws Exception {
    final Definition definition = loader.load("pkg1.annotations.SourceFlag",
        new HashMap<Object, Object>());
    final Source[] sources = castNodeError(definition,
        ImplementationContainer.class).getSources();
    for (final Source source : sources) {
      final Flag flag = AnnotationHelper.getAnnotation(source, Flag.class);
      assertEquals(flag.value, "-DFOO");
    }
  }
}
