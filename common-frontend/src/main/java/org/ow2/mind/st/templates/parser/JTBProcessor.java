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
 * Authors: Ali Erdem Ozcan
 * Contributors: 
 */

package org.ow2.mind.st.templates.parser;

import static org.objectweb.fractal.adl.NodeUtil.castNodeError;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.GenericErrors;
import org.objectweb.fractal.adl.xml.XMLNodeFactory;
import org.ow2.mind.st.templates.ast.BoundInterface;
import org.ow2.mind.st.templates.ast.PluginInterface;
import org.ow2.mind.st.templates.ast.ServerInterface;
import org.ow2.mind.st.templates.ast.SuperTemplate;
import org.ow2.mind.st.templates.ast.TemplateComponent;
import org.ow2.mind.st.templates.jtb.syntaxtree.FullyQualifiedName;
import org.ow2.mind.st.templates.jtb.syntaxtree.ImplementsDefinitions;
import org.ow2.mind.st.templates.jtb.syntaxtree.NodeChoice;
import org.ow2.mind.st.templates.jtb.syntaxtree.NodeSequence;
import org.ow2.mind.st.templates.jtb.syntaxtree.NodeToken;
import org.ow2.mind.st.templates.jtb.syntaxtree.RequiresDefinitions;
import org.ow2.mind.st.templates.jtb.syntaxtree.SuperTemplateDefinition;
import org.ow2.mind.st.templates.jtb.syntaxtree.TemplateComponentDefinition;
import org.ow2.mind.st.templates.jtb.visitor.GJDepthFirst;
import org.xml.sax.SAXException;

/**
 * Translates the JTB AST of a template component file into a semantic-based
 * AST.
 */
public class JTBProcessor extends GJDepthFirst<Node, Node> {

  private final String         filename;
  private final XMLNodeFactory nodeFactory;
  private final String         adlDtd;

  /**
   * @param nodeFactory The node factory to be used for instantiating AST nodes.
   * @param adlDtd The grammar definition for ADL nodes.
   * @param filename The name of the parsed file.
   */
  public JTBProcessor(final XMLNodeFactory nodeFactory, final String adlDtd,
      final String filename) {
    this.nodeFactory = nodeFactory;
    this.adlDtd = adlDtd;
    this.filename = filename;
    try {
      nodeFactory.checkDTD(adlDtd);
    } catch (final SAXException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Error in dtd file '" + adlDtd + "'");
    }
  }

  // TODO
  public TemplateComponent toTemplateComponent(
      final TemplateComponentDefinition tcd) {
    return (TemplateComponent) visit(tcd, null);
  }

  private Node newNode(final String name, final NodeToken source) {
    Node node;
    try {
      node = nodeFactory.newXMLNode(adlDtd, name);
    } catch (final SAXException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Unable to create node");
    }
    setSource(node, source);

    return node;
  }

  private void setSource(final Node node, final NodeToken source) {
    if (source == null)
      node.astSetSource(filename);
    else
      node.astSetSource(filename + ":" + source.beginLine + "-"
          + source.beginColumn);
  }

  // ---------------------------------------------------------------------------
  // File level grammar
  // ---------------------------------------------------------------------------

  @Override
  public Node visit(final TemplateComponentDefinition n, final Node argu) {
    final TemplateComponent template = (TemplateComponent) newNode("template",
        n.f0);
    template.setName(fullyQualifiedName(n.f1));
    n.f2.accept(this, template);
    n.f3.accept(this, template);
    n.f5.accept(this, template);
    template.setContent(n.f6.tokenImage);
    return template;
  }

  @Override
  public Node visit(final SuperTemplateDefinition n, final Node argu) {
    assert argu != null;
    final TemplateComponent template = castNodeError(argu,
        TemplateComponent.class);
    final SuperTemplate superTemplate = (SuperTemplate) newNode(
        "superTemplate", n.f0);
    superTemplate.setName(fullyQualifiedName(n.f1));
    template.setSuperTemplate(superTemplate);
    return template;
  }

  @Override
  public Node visit(final ImplementsDefinitions n, final Node argu) {
    assert argu != null;
    final TemplateComponent template = castNodeError(argu,
        TemplateComponent.class);
    final ServerInterface si = (ServerInterface) newNode("serverInterface",
        n.f0);
    si.setSignature(fullyQualifiedName(n.f1));
    template.addServerInterface(si);
    for (final org.ow2.mind.st.templates.jtb.syntaxtree.Node node : n.f2.nodes) {
      final ServerInterface sitf = (ServerInterface) newNode("serverInterface",
          n.f0);
      sitf
          .setSignature(fullyQualifiedName((FullyQualifiedName) ((NodeSequence) node)
              .elementAt(1)));
      template.addServerInterface(sitf);
    }
    return template;
  }

  @Override
  public Node visit(final RequiresDefinitions n, final Node argu) {
    assert argu != null;
    final TemplateComponent template = castNodeError(argu,
        TemplateComponent.class);
    for (final org.ow2.mind.st.templates.jtb.syntaxtree.Node node : n.f0.nodes) {
      final NodeSequence sequence = (NodeSequence) node;
      final String signature = fullyQualifiedName((FullyQualifiedName) sequence
          .elementAt(0));
      final String name = ((NodeToken) sequence.elementAt(1)).tokenImage;
      final NodeChoice itfChoice = (NodeChoice) sequence.elementAt(3);
      if (itfChoice.which == 0) {
        // PluginInterface
        final PluginInterface itf = (PluginInterface) newNode(
            "pluginInterface", (NodeToken) sequence.elementAt(1));
        itf.setSignature(signature);
        itf.setName(name);
        final NodeSequence s = (NodeSequence) itfChoice.choice;
        itf.setRepository(fullyQualifiedName((FullyQualifiedName) s
            .elementAt(2)));
        template.addPluginInterface(itf);
      } else {
        // BoundInterface
        final BoundInterface itf = (BoundInterface) newNode("boundInterface",
            (NodeToken) sequence.elementAt(1));
        itf.setSignature(signature);
        itf.setName(name);
        itf
            .setBoundTo(fullyQualifiedName((FullyQualifiedName) itfChoice.choice));
        template.addBoundInterface(itf);
      }
    }
    return template;
  }

  private String fullyQualifiedName(final FullyQualifiedName n) {
    String name = n.f0.tokenImage;
    for (final org.ow2.mind.st.templates.jtb.syntaxtree.Node node : n.f1.nodes) {
      name += "." + ((NodeToken) ((NodeSequence) node).elementAt(1)).tokenImage;
    }
    return name;
  }

}
