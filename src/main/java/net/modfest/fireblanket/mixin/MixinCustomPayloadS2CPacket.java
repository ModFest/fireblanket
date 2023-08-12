package net.modfest.fireblanket.mixin;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;
import net.modfest.fireblanket.net.writer.CustomPayloadS2CExt;
import net.modfest.fireblanket.net.writer.ServerPacketWriter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * Main logic behind writing on net thread. See {@link ServerPacketWriter} for explanation
 */
@Mixin(CustomPayloadS2CPacket.class)
public class MixinCustomPayloadS2CPacket implements CustomPayloadS2CExt {
    @Shadow @Final private Identifier channel;
    @Nullable
    @Unique
    private ServerPacketWriter writer;

    @Override
    public void fireblanket$setWriter(ServerPacketWriter packet) {
        this.writer = packet;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void fireblanket$write(PacketByteBuf buf, CallbackInfo ci) {
        if (this.writer != null) {
            this.writer.write(buf, this.channel);
        }
    }

    /*
    * This section is a bit of a "hack" to make sure it behaves more or less the same on singleplayer as on server.
    * Battle tested in polymer™
    */
    @Environment(EnvType.CLIENT)
    @ModifyArg(method = "getData", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;<init>(Lio/netty/buffer/ByteBuf;)V"))
    private ByteBuf fireblanket$replaceEmpty(ByteBuf vanilla) {
        if (this.writer != null) {
            return Unpooled.buffer();
        }
        return vanilla;
    }

    @Environment(EnvType.CLIENT)
    @Inject(method = "getData", at = @At("RETURN"))
    private void fireblanket$writeData(CallbackInfoReturnable<PacketByteBuf> cir) {
        if (this.writer != null) {
            var buf = cir.getReturnValue();
            this.writer.write(buf, this.channel);
        }
    }
}
