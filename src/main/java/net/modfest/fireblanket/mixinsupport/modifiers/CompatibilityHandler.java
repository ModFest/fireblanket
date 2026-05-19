package net.modfest.fireblanket.mixinsupport.modifiers;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.modfest.fireblanket.FireblanketMixin;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * The mixin compatibility tester, allowing simplification of {@link FireblanketMixin} by hiding it behind
 * magic annotations that all lead to here.
 * <p>
 * This may also be considered the art of making hundreds of line of complexity into one monolithic function.
 *
 * @author Ampflower
 * @implSpec {@link Class}-containing annotations are strictly forbidden.
 * @implNote Called by {@link FireblanketMixin}; avoid calling into Minecraft or its libraries.
 * @see Conflict
 * @see Require
 */
public final class CompatibilityHandler {
	private static final boolean DEBUG = Boolean.getBoolean("fireblanket.compatibilityHandler.debug");

	private static final int SKIP_ALL = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;

	private static final Map<String, Boolean> packageCache = new WeakHashMap<>();

	public static boolean isMixinCompatible(final String mixinPackage, final String mixin) {
		try {
			if (!checkPackagesOf(mixinPackage, mixin)) {
				return false;
			}

			if (isMixinCompatible(mixin, false)) {
				if (DEBUG) {
					System.err.printf("%s is compatible with the runtime. Loading...\n", mixin);
				}
				return true;
			}
			if (DEBUG) {
				System.err.printf("%s is NOT compatible with the runtime. Refusing to load.\n", mixin);
			}
			return false;
		} catch (Throwable thrown) {
			// Preboot restriction: cannot use a logger. Print and carry on.
			System.err.println("Failed to figure out whether " + mixin + " is compatible:");
			thrown.printStackTrace();
			// Well if we can't even read it, *no*, I can't say it is.
			return false;
		}
	}

	private static boolean checkPackagesOf(
		final String rootPackage,
		final String mixin
	) throws Throwable {
		if (!mixin.startsWith(rootPackage)) {
			throw new IllegalArgumentException("mixinPackage(" + mixin + ") not from rootPackage(" + rootPackage + ")");
		}

		return checkPackages(rootPackage, mixin.substring(0, mixin.lastIndexOf('.')));
	}

	private static boolean checkPackages(
		final String rootPackage,
		final String packageName
	) throws Throwable {
		final Boolean cache = packageCache.get(packageName);
		if (cache != null) {
			if (DEBUG) {
				System.err.printf("Cache hit: %s -> %s\n", packageName, cache);
			}
			return cache;
		}

		if (!isMixinCompatible(packageName + ".package-info", true)) {
			if (DEBUG) {
				System.err.printf("Package incompatible: %s\n", packageName);
			}
			packageCache.put(packageName, false);
			return false;
		}

		if (rootPackage.equals(packageName)) {
			if (DEBUG) {
				System.err.printf("Apex package considered compatible: %s\n", packageName);
			}
			packageCache.put(packageName, true);
			return true;
		}

		final boolean bool = checkPackages(rootPackage, packageName.substring(0, packageName.lastIndexOf('.')));
		if (DEBUG) {
			System.err.printf("Propagating: %s -> %s\n", packageName, bool);
		}
		packageCache.put(packageName, bool);
		return bool;
	}

	private static boolean isMixinCompatible(
		final String mixin,
		final boolean isPackage
	) throws Throwable {
		final InputStream stream = CompatibilityHandler.class.getResourceAsStream("/" + mixin.replace(
			'.',
			'/'
		) + ".class");

		if (stream == null) {
			if (DEBUG) {
				System.err.printf("Not found: %s\n", mixin);
			}
			return isPackage;
		}

		try (stream) {
			final ClassNode clazz = new ClassNode();

			new ClassReader(stream).accept(clazz, SKIP_ALL);

			final var breaks = breaks(clazz);
			final var requires = requires(clazz);

			return isMixinCompatible(breaks, requires);
		}
	}

	private static boolean isMixinCompatible(
		final List<? extends Conflict> breaks,
		final List<? extends Require> requires
	) {
		for (final Conflict value : breaks) {
			if (isMatchingModPresent(value.value(), value.version(), true)) {
				return false;
			}
		}

		for (final Require value : requires) {
			if (!isMatchingModPresent(value.value(), value.version(), false)) {
				return false;
			}
		}

		return true;
	}

	private static boolean isMatchingModPresent(
		final String id,
		final String version,
		final boolean failureFallback
	) {
		final ModContainer mod;
		{
			final Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer(id);
			if (optional.isEmpty()) {
				return false;
			}
			mod = optional.get();
		}

		// Short circuit
		if ("*".equals(version)) {
			return true;
		}

		try {
			final VersionPredicate predicate = VersionPredicate.parse(version);

			return predicate.test(mod.getMetadata().getVersion());
		} catch (VersionParsingException e) {
			return failureFallback;
		}
	}

	private static List<? extends Conflict> breaks(final ClassNode clazz) throws Throwable {
		return findAndParse(clazz, ConflictImpl.class);
	}

	private static List<? extends Require> requires(final ClassNode clazz) throws Throwable {
		return findAndParse(clazz, RequireImpl.class);
	}

	private static <T extends Record & Annotation> List<T> findAndParse(
		final ClassNode clazz,
		final Class<T> annotationImpl
	) throws Throwable {
		final List<T> annotations = new ArrayList<>();
		final Class<? extends Annotation> annotation = findAnnotation(annotationImpl);
		final String descriptor = annotation.descriptorString();
		final @Nullable String containerDescriptor = findContainerDescriptor(annotation);

		findAndParse(
			annotations,
			annotation,
			annotationImpl,
			clazz.invisibleAnnotations,
			descriptor,
			containerDescriptor
		);

		findAndParse(
			annotations,
			annotation,
			annotationImpl,
			clazz.visibleAnnotations,
			descriptor,
			containerDescriptor
		);

		return List.copyOf(annotations);
	}

	private static <T extends Record & Annotation> void findAndParse(
		final List<T> annotations,
		final Class<? extends Annotation> annotation,
		final Class<T> annotationImpl,
		final @Nullable List<AnnotationNode> nodes,
		final String descriptor,
		final @Nullable String containerDescriptor
	) throws Throwable {
		if (nodes == null) {
			return;
		}

		for (final AnnotationNode node : nodes) {
			if (node.desc.equals(descriptor)) {
				annotations.add(parse(annotation, annotationImpl, node.values));
			}
			if (node.desc.equals(containerDescriptor)) {
				findAndParseContainer(annotations, descriptor, annotation, annotationImpl, node.values);
			}
		}
	}

	private static @Nullable String findContainerDescriptor(Class<? extends Annotation> annotation) {
		Repeatable repeatable = annotation.getAnnotation(Repeatable.class);
		if (repeatable == null) {
			return null;
		}
		return repeatable.value().descriptorString();
	}

	private static <T extends Record & Annotation> void findAndParseContainer(
		final List<T> annotations,
		final String expectedDescriptor,
		final Class<? extends Annotation> annotation,
		final Class<T> annotationImpl,
		final List<?> values
	) throws Throwable {
		if ((values.size() & 1) != 0) {
			throw new IllegalArgumentException("Not an ASM annotation array: " + values);
		}

		for (int i = 0; i < values.size(); i += 2) {
			if (!"value".equals(values.get(i))) {
				continue;
			}

			final Object value = values.get(i + 1);

			if (!(value instanceof List<?> list)) {
				return;
			}

			for (final Object entry : list) {
				if (!(entry instanceof AnnotationNode node)) {
					continue;
				}

				if (!expectedDescriptor.equals(node.desc)) {
					continue;
				}

				annotations.add(parse(annotation, annotationImpl, node.values));
			}

			return;
		}
	}

	private static <T extends Record & Annotation> T parse(
		final Class<? extends Annotation> annotation,
		final Class<T> annotationImpl,
		final List<?> values
	) throws Throwable {
		if ((values.size() & 1) != 0) {
			throw new IllegalArgumentException("Not an ASM annotation array: " + values);
		}

		final RecordComponent[] components = annotationImpl.getRecordComponents();
		final Object[] constructorArgs = new Object[components.length];
		final Constructor<T> constructor = findApexConstructor(annotationImpl, components);
		final Map<String, Integer> argumentMap = new HashMap<>();

		for (int i = 0; i < components.length; i++) {
			argumentMap.put(components[i].getName(), i);
		}

		for (int i = 0; i < values.size(); i += 2) {
			final String name = (String) values.get(i);
			final Object value = values.get(i + 1);

			final Integer index = argumentMap.get(name);

			if (index == null) {
				throw new AssertionError("Missing component from " + annotationImpl + ": " + name);
			}

			final RecordComponent component = components[index];

			constructorArgs[index] = component.getType().cast(value);
		}

		polyfillMissing(annotation, components, constructorArgs);

		return constructor.newInstance(constructorArgs);
	}

	private static void polyfillMissing(
		final Class<? extends Annotation> annotation,
		final RecordComponent[] components,
		final Object[] constructorArgs
	) throws Throwable {

		for (int i = 0; i < constructorArgs.length; i++) {
			if (constructorArgs[i] != null) {
				continue;
			}

			final RecordComponent component = components[i];
			final Method method = annotation.getDeclaredMethod(component.getName());

			if (!component.getType().isAssignableFrom(method.getReturnType())) {
				throw new AssertionError("Required " + component + ", got " + method);
			}

			constructorArgs[i] = method.getDefaultValue();

			if (constructorArgs[i] != null) {
				continue;
			}

			throw new IllegalArgumentException("Required value is missing: " + component.getName());
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends Annotation> findAnnotation(final Class<? extends Annotation> annotatedImpl) {
		for (final Class<?> clazz : annotatedImpl.getInterfaces()) {
			if (!Annotation.class.isAssignableFrom(clazz)) {
				continue;
			}
			if (Set.of(clazz.getInterfaces()).contains(Annotation.class)) {
				return (Class<? extends Annotation>) clazz;
			}
		}
		throw new IllegalArgumentException("Not a direct descendant of an annotation: " + annotatedImpl);
	}

	private static <T extends Record> Constructor<T> findApexConstructor(
		final Class<T> clazz,
		final RecordComponent[] components
	) {
		try {
			final Class<?>[] args = new Class[components.length];
			Arrays.setAll(args, i -> components[i].getType());
			return clazz.getDeclaredConstructor(args);
		} catch (Exception r) {
			throw new AssertionError("No constructor matching record components.", r);
		}
	}

	/**
	 * @noinspection ClassExplicitlyAnnotation - yes, that's the point
	 */
	private record ConflictImpl(
		String value,
		String version,
		String reason
	) implements Conflict {
		@Override
		public Class<? extends Annotation> annotationType() {
			return Conflict.class;
		}
	}

	/**
	 * @noinspection ClassExplicitlyAnnotation - yes, that's the point
	 */
	private record RequireImpl(
		String value,
		String version,
		String reason
	) implements Require {
		@Override
		public Class<? extends Annotation> annotationType() {
			return Require.class;
		}
	}
}
