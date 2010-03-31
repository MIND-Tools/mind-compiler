
package org.ow2.mind.preproc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InvocationTargetException e) {
      e.printStackTrace();
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
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InvocationTargetException e) {
      throw (T) e.getTargetException();
    }
    return res;
  }
}
