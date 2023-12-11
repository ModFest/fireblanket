package net.modfest.fireblanket.mixin.mods.mythicmetals;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;

@Pseudo
@Mixin(targets="nourl/mythicmetals/armor/CarmotShield")
public abstract class MixinCarmotShield {

	@Shadow
	public abstract boolean shouldRenderShield();
	
	/**
	 * Unconditional sync every tick for every player. Sloooooow.
	 * Make it conditional.
	 */
	@Redirect(at=@At(value="INVOKE", target="dev/onyxstudios/cca/api/v3/component/ComponentKey.sync(Ljava/lang/Object;)V"),
			method="tickShield")
	public void fireblanket$preventSync(ComponentKey key, Object provider) {
		if (shouldRenderShield()) {
			key.sync(provider);
		}
	}
	
}
