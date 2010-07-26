
package org.ow2.mind.adl.singleton;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.ow2.mind.adl.ASTChecker;
import org.ow2.mind.adl.ErrorLoader;
import org.ow2.mind.adl.Factory;
import org.ow2.mind.adl.annotation.predefined.Singleton;
import org.ow2.mind.annotation.AnnotationLocatorHelper;
import org.ow2.mind.error.ErrorManager;
import org.ow2.mind.error.ErrorManagerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestSingleton {

  Loader              loader;
  ASTChecker          checker;

  Map<Object, Object> context;

  @BeforeMethod(alwaysRun = true)
  protected void setUp() throws Exception {
    final ErrorManager errorManager = ErrorManagerFactory
        .newSimpleErrorManager();

    final ErrorLoader errL = new ErrorLoader();
    errL.errorManagerItf = errorManager;
    errL.clientLoader = Factory.newLoader(errorManager);
    loader = errL;

    checker = new ASTChecker();
    context = new HashMap<Object, Object>();
    AnnotationLocatorHelper.addDefaultAnnotationPackage(Singleton.class
        .getPackage().getName(), context);
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
