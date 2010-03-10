
package org.ow2.mind.idl;

import org.ow2.mind.AbstractVoidVisitorDispatcher;
import org.ow2.mind.VoidVisitor;
import org.ow2.mind.idl.ast.IDL;

public class IDLVisitorDispatcher extends AbstractVoidVisitorDispatcher<IDL>
    implements
      IDLVisitor {

  @Override
  protected VoidVisitor<IDL> castVisitorInterface(final Object serverItf) {
    return (IDLVisitor) serverItf;
  }

}
