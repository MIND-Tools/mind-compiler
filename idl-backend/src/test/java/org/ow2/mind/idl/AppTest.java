
package org.ow2.mind.idl;

import java.util.HashMap;

import junit.framework.TestCase;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.objectweb.fractal.adl.xml.XMLNodeFactoryImpl;
import org.ow2.mind.idl.IDLLoader;
import org.ow2.mind.idl.IDLLoaderChainFactory;
import org.ow2.mind.idl.ast.ArrayOf;
import org.ow2.mind.idl.ast.EnumDefinition;
import org.ow2.mind.idl.ast.EnumReference;
import org.ow2.mind.idl.ast.IDL;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.PointerOf;
import org.ow2.mind.idl.ast.PrimitiveType;
import org.ow2.mind.idl.ast.StructDefinition;
import org.ow2.mind.idl.ast.StructReference;
import org.ow2.mind.idl.ast.TypeDefReference;
import org.ow2.mind.idl.ast.TypeDefinition;
import org.ow2.mind.idl.ast.UnionDefinition;
import org.ow2.mind.idl.ast.UnionReference;
import org.ow2.mind.st.BasicASTTransformer;
import org.ow2.mind.st.STLoaderFactory;
import org.ow2.mind.st.STNodeFactoryImpl;
import org.ow2.mind.st.StringTemplateASTTransformer;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

  IDLLoader                    loader;
  StringTemplateASTTransformer astTransformer;

  StringTemplateGroupLoader    stcLoader;
  final XMLNodeFactory         nodeFactory = new XMLNodeFactoryImpl();

  @Override
  protected void setUp() throws Exception {
    loader = IDLLoaderChainFactory.newLoader();

    final BasicASTTransformer basicASTTransformer = new BasicASTTransformer();
    basicASTTransformer.nodeFactoryItf = new STNodeFactoryImpl();
    astTransformer = basicASTTransformer;

    stcLoader = STLoaderFactory.newSTLoader();
  }

  public void test1() throws Exception {
    final StringTemplate t = getTemplate("test1");

    System.out.println(t.toString());
  }

  public void test2() throws Exception {
    final StringTemplate t = getTemplate("test2");

    System.out.println(t.toString());
  }

  public void testFoo2() throws Exception {
    final StringTemplate t = getTemplate("/foo/test2.idt");

    System.out.println(t.toString());
  }

  private StringTemplate getTemplate(final String testName) throws ADLException {
    final IDL idl = astTransformer.toStringTemplateAST(loader.load(testName,
        new HashMap<Object, Object>()));
    addDecoration(idl);

    final StringTemplateGroup group = stcLoader
        .loadGroup("st.interfaces.IDL2C");
    // group.registerRenderer(String.class, new StringFormatRenderer());

    final StringTemplate t = group.getInstanceOf("idlFile");
    t.setAttribute("idl", idl);
    return t;
  }

  protected void addDecoration(final Node node) {
    if (node instanceof InterfaceDefinition)
      node.astSetDecoration("kind", "interface");
    else if (node instanceof EnumDefinition)
      node.astSetDecoration("kind", "enum");
    else if (node instanceof EnumReference)
      node.astSetDecoration("kind", "enumRef");
    else if (node instanceof StructDefinition)
      node.astSetDecoration("kind", "struct");
    else if (node instanceof StructReference)
      node.astSetDecoration("kind", "structRef");
    else if (node instanceof UnionDefinition)
      node.astSetDecoration("kind", "union");
    else if (node instanceof UnionReference)
      node.astSetDecoration("kind", "unionRef");
    else if (node instanceof TypeDefinition)
      node.astSetDecoration("kind", "typedef");
    else if (node instanceof TypeDefReference)
      node.astSetDecoration("kind", "typedefRef");
    else if (node instanceof PrimitiveType)
      node.astSetDecoration("kind", "primitiveType");
    else if (node instanceof ArrayOf)
      node.astSetDecoration("kind", "arrayOf");
    else if (node instanceof PointerOf)
      node.astSetDecoration("kind", "pointerOf");

    for (final String type : node.astGetNodeTypes()) {
      for (final Node n : node.astGetNodes(type)) {
        if (n != null) addDecoration(n);
      }
    }
  }
}
