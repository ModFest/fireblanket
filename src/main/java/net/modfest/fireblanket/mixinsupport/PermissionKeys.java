package net.modfest.fireblanket.mixinsupport;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.impl.permission.PermissionContextKey;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * @author Ampflower
 */
@SuppressWarnings("UnstableApiUsage")
public final class PermissionKeys {
	public static final Map<PermissionContext.Key<?>, Class<?>> MAP = Map.ofEntries(
		entry(PermissionContextKey.NAME, String.class),
		entry(PermissionContextKey.POSITION, Vec3.class),
		entry(PermissionContextKey.BLOCK_POSITION, BlockPos.class),
		entry(PermissionContextKey.ENTITY, Entity.class),
		entry(PermissionContextKey.COMMAND_SOURCE_STACK, CommandSourceStack.class),
		entry(PermissionContextKey.LEVEL, Level.class),
		entry(PermissionContextKey.SERVER, MinecraftServer.class)
	);

	// Delegate to enforce strictness
	private static <T> Map.Entry<PermissionContext.Key<T>, Class<T>> entry(
		final PermissionContext.Key<T> key,
		final Class<T> clazz
	) {
		return Map.entry(key, clazz);
	}

	private static void assertValidity(
		final PermissionContext.Key<?> key,
		final Class<?> clazz,
		final @Nullable Object value,
		final Object context
	) {
		if (value != null && !clazz.isInstance(value)) {
			throw new IllegalArgumentException("Invalid KV pair " + key + " => " + value + "(" + value.getClass()
				.getName() + "); expected " + clazz.getName() + "; contained by: " + context);
		}
	}

	public static void assertValidity(final PermissionContext.Key<?> key, final Object value) {
		if (!MAP.containsKey(key)) {
			return;
		}
		assertValidity(key, MAP.get(key), value, "raw");
	}

	public static void assertValidity(final Map<PermissionContext.Key<?>, ?> map) {
		MAP.forEach((key, clazz) -> assertValidity(key, clazz, map.get(key), map));
	}

	public static void assertValidity(final PermissionContext context) {
		MAP.forEach((key, clazz) -> assertValidity(key, clazz, context.get(key), context));
	}
}
