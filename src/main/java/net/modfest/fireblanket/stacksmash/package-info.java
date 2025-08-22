/**
 * A slightly evil package of stack tracing tooling.
 * <p>
 * If you need to implement debugging for an arbitrary stack,
 * you may be interested in {@link net.modfest.fireblanket.stacksmash.Guard}.
 * The functions {@link net.modfest.fireblanket.stacksmash.Stack#fireblanket$push()}
 * and {@link net.modfest.fireblanket.stacksmash.Stack#fireblanket$pop()}
 * may be used bare in such cases.
 * <p>
 * If you're injecting into a stack, i.e. {@link net.modfest.fireblanket.mixin.client.pose_stack.MixinMatrixStack MatrixStack},
 * you will want to proxy {@link net.modfest.fireblanket.stacksmash.Tracer},
 * and may want to also proxy {@link net.modfest.fireblanket.stacksmash.Guard}.
 *
 * <h2>Potential proxy footguns</h2>
 * You will need to add 1 to the depth of proxied functions from the following classes:
 * <ul>
 *     <li>{@link net.modfest.fireblanket.stacksmash.Stack}</li>
 *     <li>{@link net.modfest.fireblanket.stacksmash.Tracer}</li>
 * </ul>
 * Otherwise, your proxy functions will be blamed if it is not part of a
 * {@link net.modfest.fireblanket.stacksmash.StackUtil#knownSkippableClasses known skippable class}.
 * <p>
 * {@link net.modfest.fireblanket.stacksmash.StackProxy} and
 * {@link net.modfest.fireblanket.stacksmash.TracerProxy} can be used to minimise the effort.
 *
 * @author Ampflower
 * @implSpec All overrides of returning methods from
 *    {@link net.modfest.fireblanket.stacksmash.Guard},
 *    {@link net.modfest.fireblanket.stacksmash.Stack} and
 *    {@link net.modfest.fireblanket.stacksmash.Tracer}
 * 	must use {@link org.jetbrains.annotations.CheckReturnValue @CheckReturnValue}
 * 	on returns, regardless of if the method is unsupported for the implementation.
 * 	This aids in notifying devs to help prevent bugs caused by forgetting to mutate related objects.
 **/
package net.modfest.fireblanket.stacksmash;
