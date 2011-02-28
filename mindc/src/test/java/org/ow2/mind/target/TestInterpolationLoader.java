/**
 * Author: Matthieu Leclercq
 */

package org.ow2.mind.target;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.ow2.mind.CommonFrontendModule;
import org.ow2.mind.inject.AbstractMindModule;
import org.ow2.mind.plugin.PluginLoaderModule;
import org.ow2.mind.target.ast.CFlag;
import org.ow2.mind.target.ast.Compiler;
import org.ow2.mind.target.ast.Linker;
import org.ow2.mind.target.ast.Target;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestInterpolationLoader {

  protected TargetDescriptorLoader loader;
  protected Map<Object, Object>    context;

  @BeforeMethod(alwaysRun = true)
  public void setUp() {

    final Injector injector = Guice.createInjector(new PluginLoaderModule(),
        new CommonFrontendModule(), new AbstractMindModule() {
          protected void configureTargetDescriptorLoader() {
            bind(TargetDescriptorLoader.class)
                .toChainStartingWith(InterpolationLoader.class)
                .followedBy(ExtensionLoader.class)
                .endingWith(BasicTargetDescriptorLoader.class);
          }
        });

    loader = injector.getInstance(TargetDescriptorLoader.class);

    context = new HashMap<Object, Object>();
    context.put("classloader", this.getClass().getClassLoader());
  }

  @Test(groups = {"functional", "checkin"})
  public void test1() throws Exception {

    final String prevValue = System.setProperty("TOOL_DIR", "foo");
    final Target target = loader.load("descriptor3", context);
    if (prevValue != null) System.setProperty("TOOL_DIR", prevValue);

    assertNotNull(loader);

    assertEquals("descriptor3", target.getName());

    final Compiler compiler = target.getCompiler();
    assertNotNull(compiler);
    assertEquals("foo/gcc", compiler.getPath());

    final Linker linker = target.getLinker();
    assertNotNull(linker);
    assertEquals("foo/gcc", linker.getPath());

    final CFlag[] cFlags = target.getCFlags();
    assertEquals(1, cFlags.length);

    assertNull(cFlags[0].getId());
    assertEquals("-Ifoo/lib", cFlags[0].getValue());
  }
}
