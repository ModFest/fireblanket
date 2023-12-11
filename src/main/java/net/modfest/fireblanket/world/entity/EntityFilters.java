package net.modfest.fireblanket.world.entity;

import com.google.common.io.Files;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.mixin.accessor.EntityTypeAccessor;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class EntityFilters {
	private static final List<EntityFilter> FILTERS = new ArrayList<>();
	private static final Set<EntityType<?>> FORCE_VELOCITY_UPDATE_OFF = new HashSet<>();

	public static void parse(Path path) {
		List<String> strings = new ArrayList<>();

		try {
			List<String> read = Files.readLines(path.toFile(), Charset.defaultCharset());
			strings.addAll(read);
		} catch (Exception e) {
			Fireblanket.LOGGER.error("Exception parsing entity filters", e);
		}

		if (strings.isEmpty()) {
			return;
		}

		for (String string : strings) {
			string = string.trim();

			if (string.startsWith("#") || string.isEmpty()) {
				continue;
			}

			String[] parts = string.split(" ");
			Pattern pattern = Pattern.compile(parts[0]);
			int range = Integer.parseInt(parts[1]);
			int ticks = Integer.parseInt(parts[2]);
			boolean forceNoUpdates = false;
			if (parts.length > 3) {
				forceNoUpdates = Boolean.parseBoolean(parts[3]);
			}

			FILTERS.add(new EntityFilter(pattern, range, ticks, forceNoUpdates));
		}
	}

	public static void apply() {
		for (EntityFilter filter : FILTERS) {
			Fireblanket.LOGGER.debug("Applying entity type filter " + filter.pattern().pattern());
			for (EntityType<?> type : Registries.ENTITY_TYPE) {
				Identifier id = Registries.ENTITY_TYPE.getId(type);
				if (filter.pattern().asMatchPredicate().test(id.toString())) {
					((EntityTypeAccessor)type).setMaxTrackDistance(filter.trackingRangeChunks());
					((EntityTypeAccessor)type).setTrackTickInterval(filter.tickRate());

					if (filter.forceNoVelcityUpdate()) {
						FORCE_VELOCITY_UPDATE_OFF.add(type);
					}

					Fireblanket.LOGGER.debug("Filter applied to " + id + " successfully");
				}
			}
		}
	}

	public static boolean isTypeForcedVelocityOff(EntityType<?> type) {
		return FORCE_VELOCITY_UPDATE_OFF.contains(type);
	}
}
