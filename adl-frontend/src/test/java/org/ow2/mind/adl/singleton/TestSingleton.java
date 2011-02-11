
package org.ow2.mind.adl.singleton;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.adl.ADLFrontendModule;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.idl.IDLFrontendModule;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

public class TestSingleton {

  Loader              loader;
  ASTChecker          checker;

  Map<Object, Object> context;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    final Injector injector = Guice.createInjector(new CommonFrontendModule(),
        new PluginLoaderModule(), new IDLFrontendModule(),
        new ADLFrontendModule() {
          protected void configureErrorLoader() {
            bind(Loader.class).annotatedWith(Names.named("ErrorLoader"))
                .toChainStartingWith(ErrorLoader.class)
                .endingWith(Loader.class);
          }
        });
    loader = injector.getInstance(Key.get(Loader.class,
        Names.named("ErrorLoader")));

    checker = new ASTChecker();
    context = new HashMap<Object, Object>();
  }

  @Test(groups = {"functional", "checkin"})
  public void test1() throws Exception {
    final Definition def = loader.load("pkg1.singleton.SingletonPrimitive",
        context);
    checker.assertDefinition(def).isSingleton();
  }

  @Test(groups = {"functional", "checkin"})
  public void test2() throws Exception {
    final Definition def = loader.load("pkg1.singleton.Composite1", context);
    checker.assertDefinition(def).isSingleton().containsComponent("subComp1")
        .isAnInstanceOf("pkg1.singleton.SingletonPrimitive").isSingleton();
  }

  @Test(groups = {"functional", "checkin"})
  public void test3() throws Exception {
    final Definition def1 = loader.load("pkg1.generic.Generic1", context);
    checker.assertDefinition(def1).isMultiton();

    final Definition def = loader.load("pkg1.singleton.Composite2", context);
    checker
        .assertDefinition(def)
        .isSingleton()
        .containsComponent("subComp1")
        .isAnInstanceOf(
            "pkg1.generic.Generic1<pkg1.singleton.SingletonPrimitive>")
        .isSingleton().containsComponent("subComp1")
        .isAnInstanceOf("pkg1.singleton.SingletonPrimitive").isSingleton();
    checker.assertDefinition(def1).isMultiton();
  }

  @Test(groups = {"functional", "checkin"})
  public void test4() throws Exception {
    final Definition def1 = loader.load("pkg1.generic.Generic1", context);
    checker.assertDefinition(def1).isMultiton();
    final Definition def2 = loader.load("pkg1.generic.Generic2", context);
    checker.assertDefinition(def2).isMultiton();

    final Definition def = loader.load("pkg1.singleton.Composite3", context);
    checker
        .assertDefinition(def)
        .isSingleton()
        .containsComponent("subComp1")
        .isAnInstanceOf(
            "pkg1.generic.Generic2<pkg1.generic.Generic1<pkg1.singleton.SingletonPrimitive>,pkg1.generic.Generic1<pkg1.pkg2.Primitive1>>")
        .isSingleton().containsComponents("c1", "c2").whereFirst()
        .isAnInstanceOfSingleton().andNext().isAnInstanceOfMultiton();

    checker.assertDefinition(def1).isMultiton();
    checker.assertDefinition(def2).isMultiton();
  }
}
