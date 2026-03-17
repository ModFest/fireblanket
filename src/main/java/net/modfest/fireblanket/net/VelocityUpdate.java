package net.modfest.fireblanket.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.modfest.fireblanket.mixin.accessor.EntityVelocityUpdateS2CPacketAccessor;

public record VelocityUpdate(int entity, boolean allZero, int velocityX, int velocityY,	int velocityZ) {
	public static final StreamCodec<RegistryFriendlyByteBuf, VelocityUpdate> CODEC = new StreamCodec<>() {

		@Override
		public void encode(RegistryFriendlyByteBuf buf, VelocityUpdate value) {
			if (value.entity > (1 << 30)) {
				throw new IllegalStateException("Optimized entity velocity encoding failed (Do we have 1 billion entities???)");
			}

			int bi = value.allZero ? 1 : 0;
			int id = (value.entity << 1) | bi;
			buf.writeVarInt(id);
			if (!value.allZero) {
				buf.writeShort((short) value.velocityX);
				buf.writeShort((short) value.velocityY);
				buf.writeShort((short) value.velocityZ);
			}
		}

		@Override
		public VelocityUpdate decode(RegistryFriendlyByteBuf buf) {
			int packed = buf.readVarInt();
			boolean allZero = (packed & 1) == 1;
			int id = packed >>> 1;
			int vx = 0;
			int vy = 0;
			int vz = 0;
			if (!allZero) {
				vx = buf.readShort();
				vy = buf.readShort();
				vz = buf.readShort();
			}

			return new VelocityUpdate(id, allZero, vx, vy, vz);
		}
	};

	public static VelocityUpdate of(ClientboundSetEntityMotionPacket packet) {
		EntityVelocityUpdateS2CPacketAccessor acc = (EntityVelocityUpdateS2CPacketAccessor) packet;
		int vx = acc.vx();
		int vy = acc.vy();
		int vz = acc.vz();
		return new VelocityUpdate(packet.getId(), vx == 0 && vy == 0 && vz == 0, vx, vy, vz);
	}

	public double getVelocityX() {
		if (allZero) {
			return 0;
		}

		return this.velocityX / 8000.0;
	}

	public double getVelocityY() {
		if (allZero) {
			return 0;
		}

		return this.velocityY / 8000.0;
	}

	public double getVelocityZ() {
		if (allZero) {
			return 0;
		}

		return this.velocityZ / 8000.0;
	}
}
