package net.modfest.fireblanket.mixin.mods.emergency.surveyor;

import folk.sisby.surveyor.client.ClientSummary;
import net.modfest.fireblanket.mixinsupport.modifiers.Require;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

/**
 * Forcefully fixes a tooltip crash bug in hoofprint by not returning nulls.
 *
 * @author Ampflower
 **/
@Pseudo
@Require(value = "surveyor", version = "1.2.4+26.1")
@Mixin(ClientSummary.class)
public class MixinClientSummary {
	@Redirect(
		method = "players(Ljava/util/Set;)Ljava/util/Map;",
		at = @At(
			value = "INVOKE",
			target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
		)
	)
	private static Object fireblanket$forceNotNullElseSilentlyDiscard(
		final Map map,
		final Object uuid,
		final Object summary
	) {
		if (summary == null) {
			return map.remove(uuid);
		}
		return map.put(uuid, summary);
	}
}

