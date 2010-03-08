
package org.ow2.mind.preproc;

import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.ow2.mind.adl.ast.Attribute;
import org.ow2.mind.adl.ast.AttributeContainer;
import org.ow2.mind.adl.idl.InterfaceDefinitionDecorationHelper;
import org.ow2.mind.idl.ast.InterfaceDefinition;
import org.ow2.mind.idl.ast.Method;

public class CPLChecker {
  protected final Definition definition;

  public CPLChecker(final Definition definition) {
    this.definition = definition;
  }

  public void serverMethDef(final String itfName, final String methName)
      throws Exception {

    // Add this condition so that the testNG will
    // not throw exceptions (stand-alone node)
    if (this.definition != null) {
      boolean foundItf = false;
      boolean isServer = false;
      boolean foundMeth = false;

      for (final Interface itf : ((InterfaceContainer) definition)
          .getInterfaces()) {
        if (itf.getName().equals(itfName)) {

          if (((TypeInterface) itf).getRole() == "server") {
            isServer = true;

            final InterfaceDefinition itfDef = ((InterfaceDefinitionDecorationHelper.InterfaceDefinitionDecoration) itf
                .astGetDecoration("resolved-interface-definition"))
                .getInterfaceDefinition();
            for (final Method meth : itfDef.getMethods()) {

              // TODO This code sample should replace the next test
              // which accept an "_override" suffix.
              // The replacement should be done when the Jira track
              // concerning abusive used of the METH macro is resolved.
              // if (meth.getName().equals(methName)) {
              // foundMeth = true;
              // break;
              // }
              if (meth.getName().equals(methName)
                  || meth.getName().concat("_override").equals(methName)) {
                foundMeth = true;
                break;
              }
              // TODO End of the part to remove

            }
          }
          foundItf = true;
          break;
        }
      }

      if (!foundItf) {

        throw new Exception(" Unknown interface \"" + itfName + "\" ");
      }
      if (isServer) {
        if (!foundMeth) {
          throw new Exception(" In interface \"" + itfName
              + "\" unknown method \"" + methName + "\" ");
        }
      } else {
        // TODO The following test should be removed end directly throw the
        // exception when the Jira track concerning abusive used of the METH
        // macro is resolved
        if (definition.astGetType().compareTo("composite") == 0) {
          throw new Exception(" Interface \"" + itfName
              + "\" is a client interface, method \"" + methName
              + "\" cannot be defined here");
        }
      }
    }
  }

  public void attAccess(final String attributeName) throws Exception {
    if (this.definition != null) { // add this condition so that the testNG will
      // not throw exceptions
      boolean foundAtt = false;

      for (final Attribute att : ((AttributeContainer) definition)
          .getAttributes()) {
        if (att.getName().equals(attributeName)) {
          foundAtt = true;
          break;
        }
      }

      if (!foundAtt) {
        throw new Exception(" Unknown attribute \"" + attributeName + "\" ");
      }
    }
  }
}