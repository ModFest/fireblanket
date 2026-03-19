package net.modfest.fireblanket.mixin.entity_sync;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.modfest.fireblanket.mixinsupport.TrackerEntityHolder;
import net.modfest.fireblanket.net.ProtoVelocityUpdate;
import net.modfest.fireblanket.net.TrackerGlobal;
import net.modfest.fireblanket.net.VelocityUpdate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.HashSet;
import java.util.Set;

@Mixin(ServerEntity.class)
public class MixinEntityTrackerEntry implements TrackerEntityHolder {
	@Shadow
	@Final
	private Entity entity;
	@Unique
	private Set<ServerPlayerConnection> heldListeners;

	/*
		The indices of the packets get fiddly as mojang updates.
		If you're here because of an IllegalStateException, you probably need to
		figure out the new offsets.
	 */

	@Redirect(
		method = "sendChanges",
		slice = @Slice( // comes after bundle
			from = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ClientboundBundlePacket;<init>(Ljava/lang/Iterable;)V")
		),
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/level/ServerEntity$Synchronizer;sendToTrackingPlayers(Lnet/minecraft/network/protocol/Packet;)V",
			ordinal = 1
		)
	)
	public void fireblanket$velocity1(ServerEntity.Synchronizer instance, Packet<?> o) {
		if (o instanceof ClientboundSetEntityMotionPacket packet) {
			if (heldListeners == null) {
				throw new IllegalStateException();
			}
			TrackerGlobal.UPDATES.add(new ProtoVelocityUpdate(this.heldListeners, VelocityUpdate.of(packet)));
		} else {
			throw new IllegalStateException("Mixin failure, was actually " + o);
		}
	}

	@Redirect(method = "sendChanges", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerEntity$Synchronizer;sendToTrackingPlayersAndSelf(Lnet/minecraft/network/protocol/Packet;)V"))
	public void fireblanket$velocity2(ServerEntity.Synchronizer instance, Packet<?> o) {
		if (o instanceof ClientboundSetEntityMotionPacket packet) {
			if (heldListeners == null) {
				throw new IllegalStateException();
			}
			Set<ServerPlayerConnection> nset = new HashSet<>(this.heldListeners);
			if (this.entity instanceof ServerPlayer spe) {
				nset.add(spe.connection);
			}

			TrackerGlobal.UPDATES.add(new ProtoVelocityUpdate(nset, VelocityUpdate.of(packet)));
		} else {
			throw new IllegalStateException("Mixin failure, was actually " + o);
		}
	}

	@Override
	public void fireblanket$setListeners(Set<ServerPlayerConnection> listeners) {
		this.heldListeners = listeners;
	}
}
