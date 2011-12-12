/**
 * Copyright (C) 2010 STMicroelectronics
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

package org.ow2.mind.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.spi.Message;
import com.google.inject.util.Providers;

/**
 * Extends default Google Guice {@link AbstractModule} and adds API to create
 * delegation chains. A delegation chain is a list of objects where each object
 * implements the same interface and may delegate its processing to the next
 * object in the chain. To do so, each object in the chain (expect the last one)
 * contains a field that is injected to the reference of the next object in the
 * chain. This field must be annotated with the {@link InjectDelegate}
 * annotation.<br>
 * This class provides an extension to the <em>Google Guide Binding EDSL</em> to
 * define such delegation chain. For example :
 * 
 * <pre>
 * bind(MyService.class).toChainStartingWith(MyFirstDelegate.class)
 *     .followedBy(MySecondDelegate.class).endingWith(MyServiceImpl.class);
 * </pre>
 * 
 * Moreover, this {@link AbstractModule} provides a standard implementation of
 * the {@link AbstractModule#configure()} method that calls (using reflection)
 * every non-static methods whose name starts with <code>configure</code> and
 * that has no parameter. So sub-classes can implement the configuration logic
 * in several <i>configure</i> methods which can then be overridden
 * independently.
 */
public abstract class AbstractMindModule extends AbstractModule {

  private static final String CONFIGURE_METHOD_PREFIX = "configure";

  @Override
  protected void configure() {
    Class<?> clazz = this.getClass();
    final Set<String> calledMeths = new HashSet<String>();
    do {
      for (final Method meth : clazz.getDeclaredMethods()) {
        if (meth.getName().startsWith(CONFIGURE_METHOD_PREFIX)
            && meth.getName().length() > CONFIGURE_METHOD_PREFIX.length()
            && meth.getParameterTypes().length == 0
            && calledMeths.add(meth.getName())) {
          try {
            meth.setAccessible(true);
            meth.invoke(this);
          } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
          } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
          } catch (final InvocationTargetException e) {
            throw new RuntimeException(e);
          }
        }
      }
    } while ((clazz = clazz.getSuperclass()) != null);
  }

  @Override
  protected <T> AnnotatedBindingChainBuilder<T> bind(final Class<T> clazz) {
    final Binder binder = binder().skipSources(AbstractMindModule.class);
    return new AnnotatedBindingChainBuilderImpl<T>(binder, clazz,
        binder.bind(clazz));
  }

  @Override
  protected <T> LinkedBindingChainBuilder<T> bind(final Key<T> key) {
    return new LinkedBindingChainBuilderImpl<T>(binder().skipSources(
        AbstractMindModule.class), key.getTypeLiteral().getRawType(),
        super.bind(key));
  }

  static class AnnotatedBindingChainBuilderImpl<T>
      extends
        LinkedBindingChainBuilderImpl<T>
      implements
        AnnotatedBindingChainBuilder<T> {

    final AnnotatedBindingBuilder<T> delegate;

    AnnotatedBindingChainBuilderImpl(final Binder binder,
        final Class<T> bindType, final AnnotatedBindingBuilder<T> delegate) {
      super(binder, bindType, delegate);
      this.delegate = delegate;
    }

    public LinkedBindingChainBuilder<T> annotatedWith(
        final Class<? extends Annotation> annotationType) {
      delegate.annotatedWith(annotationType);
      return this;
    }

    public LinkedBindingChainBuilder<T> annotatedWith(
        final Annotation annotation) {
      delegate.annotatedWith(annotation);
      return this;
    }
  }

  static class LinkedBindingChainBuilderImpl<T>
      implements
        LinkedBindingChainBuilder<T> {
    final Binder                  binder;
    final LinkedBindingBuilder<T> delegate;
    final Class<? super T>        bindType;

    LinkedBindingChainBuilderImpl(final Binder binder,
        final Class<? super T> bindType, final LinkedBindingBuilder<T> delegate) {
      this.binder = binder;
      this.bindType = bindType;
      this.delegate = delegate;
    }

    public ScopedBindingBuilder to(final Class<? extends T> implementation) {
      return delegate.to(implementation);
    }

    public ScopedBindingBuilder to(final TypeLiteral<? extends T> implementation) {
      return delegate.to(implementation);
    }

    public ScopedBindingBuilder to(final Key<? extends T> targetKey) {
      return delegate.to(targetKey);
    }

    public void toInstance(final T instance) {
      delegate.toInstance(instance);
    }

    public ScopedBindingBuilder toProvider(final Provider<? extends T> provider) {
      return delegate.toProvider(provider);
    }

    public ScopedBindingBuilder toProvider(
        final Class<? extends Provider<? extends T>> providerType) {
      return delegate.toProvider(providerType);
    }

    public ScopedBindingBuilder toProvider(
        final Key<? extends Provider<? extends T>> providerKey) {
      return delegate.toProvider(providerKey);
    }

    public void in(final Class<? extends Annotation> scopeAnnotation) {
      delegate.in(scopeAnnotation);
    }

    public void in(final Scope scope) {
      delegate.in(scope);
    }

    public void asEagerSingleton() {
      delegate.asEagerSingleton();
    }

    public ChainBuilder<T> toChainStartingWith(final Class<? extends T> clazz) {
      return new ChainBuilderImpl<T>(binder, bindType, delegate, clazz);
    }

    public ChainBuilder<T> toChainStartingWith(
        final Provider<? extends T> provider) {
      return new ChainBuilderImpl<T>(binder, bindType, delegate, provider);
    }

    public ChainBuilder<T> toChainStartingWith(final T instance) {
      return new ChainBuilderImpl<T>(binder, bindType, delegate, instance);
    }
  }

  static class ChainBuilderImpl<T> implements ChainBuilder<T> {
    final ScopedBindingBuilder delegate;
    final ChainProvider<T>     chainProvider;
    final Binder               binder;

    ChainBuilderImpl(final Binder binder, final Class<? super T> chainType,
        final LinkedBindingBuilder<T> delegate,
        final Provider<? extends T> firstProvider) {
      this.binder = binder;
      this.chainProvider = new ChainProvider<T>(chainType);
      chainProvider.add(firstProvider);
      this.delegate = delegate.toProvider(chainProvider);
    }

    ChainBuilderImpl(final Binder binder, final Class<? super T> chainType,
        final LinkedBindingBuilder<T> delegate,
        final Class<? extends T> firstClass) {
      this(binder, chainType, delegate, binder.getProvider(firstClass));
      checkDelegateClass(firstClass);
    }

    ChainBuilderImpl(final Binder binder, final Class<? super T> chainType,
        final LinkedBindingBuilder<T> delegate, final T firstInstance) {
      this(binder, chainType, delegate, Providers.of(firstInstance));
      checkDelegateClass(firstInstance.getClass());
    }

    void checkDelegateClass(final Class<?> elemClass) {
      boolean delegateFound = false;
      Class<?> clazz = elemClass;
      topLoop : do {
        for (final Field f : clazz.getDeclaredFields()) {
          if (f.getAnnotation(InjectDelegate.class) != null
              && f.getType().isAssignableFrom(chainProvider.chainType)) {
            delegateFound = true;
            break topLoop;
          }
        }
      } while ((clazz = clazz.getSuperclass()) != null);
      if (!delegateFound) {
        binder.addError(new Message(elemClass,
            "Invalid delegate class : the class does not have a field of type "
                + chainProvider.chainType + " annotated with @InjectDelegate"));
      }
    }

    public ChainBuilder<T> followedBy(final Class<? extends T> elemClass) {
      checkDelegateClass(elemClass);
      chainProvider.add(binder.getProvider(elemClass));
      return this;
    }

    public ChainBuilder<T> followedBy(final Provider<? extends T> elemProvider) {
      chainProvider.add(elemProvider);
      return this;
    }

    public ChainBuilder<T> followedBy(final T elem) {
      checkDelegateClass(elem.getClass());
      chainProvider.add(Providers.of(elem));
      return this;
    }

    public ChainBuilder<T> followedBy(final Key<T> elemKey) {
      checkDelegateClass(elemKey.getTypeLiteral().getRawType());
      chainProvider.add(binder.getProvider(elemKey));
      return this;
    }

    public ScopedBindingBuilder endingWith(final Class<? extends T> elemClass) {
      chainProvider.add(binder.getProvider(elemClass));
      return delegate;
    }

    public ScopedBindingBuilder endingWith(final Provider<T> elemProvider) {
      chainProvider.add(elemProvider);
      return delegate;
    }

    public ScopedBindingBuilder endingWith(final T elem) {
      chainProvider.add(Providers.of(elem));
      return delegate;
    }

    public ScopedBindingBuilder endingWith(final Key<T> elemKey) {
      chainProvider.add(binder.getProvider(elemKey));
      return delegate;
    }

    public void in(final Class<? extends Annotation> scopeAnnotation) {
      delegate.in(scopeAnnotation);
    }

    public void in(final Scope scope) {
      delegate.in(scope);
    }

    public void asEagerSingleton() {
      delegate.asEagerSingleton();
    }

  }

  protected static class ChainProvider<T> implements Provider<T> {

    final Class<? super T>      chainType;
    List<Provider<? extends T>> providers = new ArrayList<Provider<? extends T>>();
    T                           head      = null;

    ChainProvider(final Class<? super T> chainType) {
      this.chainType = chainType;
    }

    public T get() {
      if (head == null) {
        T prevElement = null;
        for (final Provider<? extends T> provider : providers) {
          final T elem = provider.get();

          if (prevElement != null) {
            // inject delegate
            Class<?> clazz = prevElement.getClass();
            do {
              for (final Field f : clazz.getDeclaredFields()) {
                if (f.getAnnotation(InjectDelegate.class) != null
                    && f.getType().isAssignableFrom(chainType)) {
                  f.setAccessible(true);
                  try {
                    f.set(prevElement, elem);
                  } catch (final IllegalArgumentException e) {
                    throw new RuntimeException(e);
                  } catch (final IllegalAccessException e) {
                    throw new RuntimeException(e);
                  }
                }
              }
            } while ((clazz = clazz.getSuperclass()) != null);
          } else {
            head = elem;
          }
          prevElement = elem;
        }
      }
      return head;
    }

    void add(final Provider<? extends T> provider) {
      providers.add(provider);
    }
  }
}
