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

package org.ow2.mind.adl;

import static org.ow2.mind.annotation.AnnotationHelper.getAnnotation;

import java.util.Map;

import org.objectweb.fractal.adl.Node;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.merger.MergeException;
import org.objectweb.fractal.adl.merger.NodeMergerImpl;
import org.ow2.mind.adl.ast.Binding;

/**
 * Node merger component that implements the node merging algorithm that
 * respects the STCF ADL language.
 */
// TODO check @Override annotations while merging nodes.
public class STCFNodeMerger extends NodeMergerImpl {

  @Override
  protected void computeInhertedSubNodeMergeInfos(final Node inheritedSubNode,
      final MergeInfo parentInfo, final String subNodeType,
      final Map<Node, MergeInfo> infos) throws MergeException {

    if (subNodeType.equals("import") || subNodeType.equals("formalParameter")
        || subNodeType.equals("formalTypeParameter")
        || subNodeType.equals("extends")) {
      // these elements are not inherited.
      return;
    } else {
      super.computeInhertedSubNodeMergeInfos(inheritedSubNode, parentInfo,
          subNodeType, infos);
    }
  }

  @Override
  protected void computeMergedSubNodesMergeInfos(final Node subNode,
      final Node inheritedSubNode, final MergeInfo parentInfo,
      final Map<Node, MergeInfo> infos, final Map<String, String> idAttributes,
      final String subNodeType) throws MergeException {

    if (subNodeType.equals("annotation")
        || subNodeType.equals("formalParameter")
        || subNodeType.equals("formalTypeParameter")
        || subNodeType.equals("value")) {
      // these elements are simply overridden (i.e. not merged).
      super.computeSubNodeMergeInfos(subNode, parentInfo, subNodeType, infos);

    } else if (subNodeType.equals("interface")) {
      // interface elements can't be merge, this is considered as an error
      throw new InvalidMergeException(
          ADLErrors.INVALID_INTERFACE_NAME_OVERRIDE_INHERITED_INTERFACE,
          subNode, new NodeErrorLocator(inheritedSubNode));

    } else if (subNodeType.equals("attribute")) {
      // Attribute can be overridden, but the type cannot be changed
      final String inheritedType = inheritedSubNode.astGetAttributes().get(
          "type");
      final String overridingType = subNode.astGetAttributes().get("type");
      if (overridingType != null && !overridingType.equals(inheritedType)) {
        throw new InvalidMergeException(
            ADLErrors.INVALID_ATTRIBUTE_OVERRIDE_INHERITED_ATTRIBUTE_TYPE,
            subNode, new NodeErrorLocator(inheritedSubNode));
      }
      super.computeMergedSubNodesMergeInfos(subNode, inheritedSubNode,
          parentInfo, infos, idAttributes, subNodeType);

    } else if (subNodeType.equals("component")) {

      super.computeSubNodeMergeInfos(subNode, parentInfo, subNodeType, infos);

    } else if (subNodeType.equals("binding")) {

      super.computeSubNodeMergeInfos(subNode, parentInfo, subNodeType, infos);

    } else {
      super.computeMergedSubNodesMergeInfos(subNode, inheritedSubNode,
          parentInfo, infos, idAttributes, subNodeType);
    }
  }

  @Override
  protected void computeSubNodeMergeInfos(final Node subNode,
      final MergeInfo parentInfo, final String subNodeType,
      final Map<Node, MergeInfo> infos) throws MergeException {
    if (getAnnotation(subNode,
        org.ow2.mind.adl.annotation.predefined.Override.class) != null) {
      // subNode is marked has overriding an inherited element but it doesn't.
      throw new InvalidMergeException(ADLErrors.DO_NOT_OVERRIDE, subNode);
    }
    super.computeSubNodeMergeInfos(subNode, parentInfo, subNodeType, infos);
  }

  @Override
  protected Node findOverridingNode(final Node superNode, final Node[] nodes,
      final String nodeType, final Map<String, String> idAttributes) {
    if (nodeType.equals("binding")) {

      final Binding superBinding = (Binding) superNode;
      final String superCompName = superBinding.getFromComponent();
      final String superItfName = superBinding.getFromInterface();

      for (final Node node : nodes) {
        final Binding binding = (Binding) node;
        if (superCompName.equals(binding.getFromComponent())
            && superItfName.equals(binding.getFromInterface())) {
          return node;
        }
      }
      return null;
    } else {
      return super.findOverridingNode(superNode, nodes, nodeType, idAttributes);
    }
  }

}
