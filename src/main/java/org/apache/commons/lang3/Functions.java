/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.lang3;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;


/** This class provides utility functions, and classes for working with the
 * {@code java.util.function} package, or more generally, with Java 8
 * lambdas.
 * More specifically, it attempts to address the fact that lambdas are supposed
 * not to throw Exceptions, at least not checked Exceptions, aka instances of
 * {@link Exception}. This enforces the use of constructs like
 * <pre>
 *   Consumer<java.lang.reflect.Method> consumer = (m) -> {
 *       try {
 *           m.invoke(o, args);
 *       } catch (Throwable t) {
 *           throw Functions.rethrow(t);
 *       }
 *   };
 * </pre>
 * By replacing a {@link Consumer Consumer<O>} with a
 * {@link FailableConsumer FailableConsumer<O,? extends Throwable}, this can be
 * written like follows:
 * <pre>
 *   Functions.accept((m) -> m.invoke(o,args));
 * </pre>
 * Obviously, the second version is much more concise and the spirit of
 * Lambda expressions is met better than the second version.
 */
public class Functions {
	@FunctionalInterface
	public interface FailableRunnable<T extends Throwable> {
		public void run() throws T;
	}
	@FunctionalInterface
	public interface FailableCallable<O,T extends Throwable> {
		public O call() throws T;
	}
	@FunctionalInterface
	public interface FailableConsumer<O,T extends Throwable> {
		public void accept(O pObject) throws T;
	}
	@FunctionalInterface
	public interface FailableBiConsumer<O1,O2,T extends Throwable> {
		public void accept(O1 pObject1, O2 pObject2) throws T;
	}
	@FunctionalInterface
	public interface FailableFunction<I,O,T extends Throwable> {
		public O apply(I pInput) throws T;
	}
	@FunctionalInterface
	public interface FailableBiFunction<I1,I2,O,T extends Throwable> {
		public O apply(I1 pInput1, I2 pInput2) throws T;
	}
	@FunctionalInterface
	public interface FailablePredicate<O,T extends Throwable> {
		public boolean test(O pObject) throws T;
	}
	@FunctionalInterface
	public interface FailableBiPredicate<O1,O2,T extends Throwable> {
		public boolean test(O1 pObject1, O2 pObject2) throws T;
	}

	public static <T extends Throwable> void run(FailableRunnable<T> pRunnable) {
		try {
			pRunnable.run();
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	public static <O,T extends Throwable> O call(FailableCallable<O,T> pCallable) {
		try {
			return pCallable.call();
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	public static <O,T extends Throwable> void accept(FailableConsumer<O,T> pConsumer, O pObject) {
		try {
			pConsumer.accept(pObject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	public static <O1,O2,T extends Throwable> void accept(FailableBiConsumer<O1,O2,T> pConsumer, O1 pObject1, O2 pObject2) {
		try {
			pConsumer.accept(pObject1, pObject2);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	public static <I,O,T extends Throwable> O apply(FailableFunction<I,O,T> pFunction, I pInput) {
		try {
			return pFunction.apply(pInput);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	public static <I1,I2,O,T extends Throwable> O apply(FailableBiFunction<I1,I2,O,T> pFunction, I1 pInput1, I2 pInput2) {
		try {
			return pFunction.apply(pInput1, pInput2);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	public static <O,T extends Throwable> boolean test(FailablePredicate<O,T> pPredicate, O pObject) {
		try {
			return pPredicate.test(pObject);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	public static <O1,O2,T extends Throwable> boolean test(FailableBiPredicate<O1,O2,T> pPredicate, O1 pObject1, O2 pObject2) {
		try {
			return pPredicate.test(pObject1, pObject2);
		} catch (Throwable t) {
			throw rethrow(t);
		}
	}

	/**
	 * A simple try-with-resources implementation, that can be used, if your
	 * objects do not implement the {@link AutoCloseable} interface. The method
	 * executes the {@code pAction}. The method guarantees, that <em>all</em>
	 * the {@code pResources} are being executed, in the given order, afterwards,
	 * and regardless of success, or failure. If either the original action, or
	 * any of the resource action fails, then the <em>first</em> failure (aka
	 * {@link Throwable} is rethrown. Example use:
	 * <pre>
	 *   final FileInputStream fis = new FileInputStream("my.file");
	 *   Functions.tryWithResources(useInputStream(fis), null, () -> fis.close());
	 * </pre>
	 * @param pAction The action to execute. This object <em>will</em> always
	 *   be invoked.
	 * @param pErrorHandler An optional error handler, which will be invoked finally,
	 *   if any error occurred. The error handler will receive the first
	 *   error, aka {@link Throwable}.
	 * @param pResources The resource actions to execute. <em>All</em> resource
	 *   actions will be invoked, in the given order. A resource action is an
	 *   instance of {@link FailableRunnable}, which will be executed.
	 * @see #tryWithResources(FailableRunnable, FailableRunnable...)
	 */
	@SafeVarargs
	public static <O> void tryWithResources(FailableRunnable<? extends Throwable> pAction,
			                                FailableConsumer<Throwable,? extends Throwable> pErrorHandler,
											FailableRunnable<? extends Throwable>... pResources) {
		final FailableConsumer<Throwable,? extends Throwable> errorHandler;
		if (pErrorHandler == null) {
			errorHandler = (t) -> rethrow(t);
		} else {
			errorHandler = pErrorHandler;
		}
		if (pResources != null) {
			for (FailableRunnable<? extends Throwable> runnable : pResources) {
				if (runnable == null) {
					throw new NullPointerException("A resource action must not be null.");
				}
			}
		}
		Throwable th = null;
		try {
			pAction.run();
		} catch (Throwable t) {
			th = t;
		}
		if (pResources != null) {
			for (FailableRunnable<? extends Object> runnable : pResources) {
				try {
					runnable.run();
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
		}
		if (th != null) {
			try {
				errorHandler.accept(th);
			} catch (Throwable t) {
				throw rethrow(t);
			}
		}
	}

	/**
	 * A simple try-with-resources implementation, that can be used, if your
	 * objects do not implement the {@link AutoCloseable} interface. The method
	 * executes the {@code pAction}. The method guarantees, that <em>all</em>
	 * the {@code pResources} are being executed, in the given order, afterwards,
	 * and regardless of success, or failure. If either the original action, or
	 * any of the resource action fails, then the <em>first</em> failure (aka
	 * {@link Throwable} is rethrown. Example use:
	 * <pre>
	 *   final FileInputStream fis = new FileInputStream("my.file");
	 *   Functions.tryWithResources(useInputStream(fis), () -> fis.close());
	 * </pre>
	 * @param pAction The action to execute. This object <em>will</em> always
	 *   be invoked.
	 * @param pResources The resource actions to execute. <em>All</em> resource
	 *   actions will be invoked, in the given order. A resource action is an
	 *   instance of {@link FailableRunnable}, which will be executed.
	 * @see #tryWithResources(FailableRunnable, FailableConsumer, FailableRunnable...)
	 */
	@SafeVarargs
	public static <O> void tryWithResources(FailableRunnable<? extends Throwable> pAction,
											FailableRunnable<? extends Throwable>... pResources) {
		tryWithResources(pAction, null, pResources);
	}

	public static RuntimeException rethrow(Throwable pThrowable) {
		if (pThrowable == null) {
			throw new NullPointerException("The Throwable must not be null.");
		} else {
			if (pThrowable instanceof RuntimeException) {
				throw (RuntimeException) pThrowable;
			} else if (pThrowable instanceof Error) {
				throw (Error) pThrowable;
			} else if (pThrowable instanceof IOException) {
				throw new UncheckedIOException((IOException) pThrowable);
			} else {
				throw new UndeclaredThrowableException(pThrowable);
			}
		}
	}
}
