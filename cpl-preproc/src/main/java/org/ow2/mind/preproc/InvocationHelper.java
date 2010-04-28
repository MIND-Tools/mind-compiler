
package org.ow2.mind.preproc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.objectweb.fractal.adl.CompilerError;
import org.objectweb.fractal.adl.error.GenericErrors;

public final class InvocationHelper {

  private InvocationHelper() {
  }

  public static Object invokeMethod(final Object object,
      final String methodName, final Class<?>[] argClasses, final Object[] args) {
    Object res = null;
    try {
      final Method method = object.getClass().getMethod(methodName, argClasses);
      res = method.invoke(object, args);
    } catch (final SecurityException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Cannot invoke MPP");
    } catch (final NoSuchMethodException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Cannot invoke MPP");
    } catch (final IllegalArgumentException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Cannot invoke MPP");
    } catch (final IllegalAccessException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Cannot invoke MPP");
    } catch (final InvocationTargetException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Cannot invoke MPP");
    }
    return res;
  }

  public static <T extends Throwable> Object invokeMethod(final Object object,
      final String methodName, final Class<?>[] argClasses,
      final Object[] args, final Class<T> ex) throws T {
    Object res = null;
    try {
      final Method method = object.getClass().getMethod(methodName, argClasses);
      res = method.invoke(object, args);
    } catch (final SecurityException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Cannot invoke MPP");
    } catch (final NoSuchMethodException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Cannot invoke MPP");
    } catch (final IllegalArgumentException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Cannot invoke MPP");
    } catch (final IllegalAccessException e) {
      throw new CompilerError(GenericErrors.INTERNAL_ERROR, e,
          "Cannot invoke MPP");
    } catch (final InvocationTargetException e) {
      throw ex.cast(e.getTargetException());
    }
    return res;
  }
}
