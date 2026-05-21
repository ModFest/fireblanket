package net.modfest.fireblanket;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import com.bawnorton.mixinsquared.canceller.MixinCancellerRegistrar;
import net.fabricmc.loader.api.FabricLoader;
import net.modfest.fireblanket.config.ConfigSpecs;
import net.modfest.fireblanket.config.FireblanketConfig;
import net.modfest.fireblanket.mixinsupport.modifiers.CompatibilityHandler;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class FireblanketMixin implements IMixinConfigPlugin {
	public static final boolean DO_MASKING = Boolean.getBoolean("fireblanket.masking");

	private String mixinPackage;

	@Override
	public void onLoad(String mixinPackage) {
		this.mixinPackage = mixinPackage;

		Path configs = FabricLoader.getInstance().getConfigDir().resolve("fireblanket");
		if (!Files.exists(configs)) {
			try {
				Files.createDirectory(configs);
			} catch (IOException e) {
				Fireblanket.LOGGER.error("Exception creating fireblanket directory!", e);
			}
		}

		FireblanketConfig.init();

		boolean ignoreRenderingMods = Boolean.getBoolean("fireblanket.iSolemnlySwearIWillNotReportRenderingCrashesAndAcceptResponsibilityForBreakage");
		if (ignoreRenderingMods) {
			LoggerFactory.getLogger("Fireblanket").error("Ignoring the presence of rendering mods. You are proceeding at your own mortal peril.");
		}

		MixinCancellerRegistrar.register(new Canceller());
	}

	private static class Canceller implements MixinCanceller {
		@Override
		public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
			if ("net.frozenblock.lib.spotting_icons.mixin.client.LevelRendererMixin".equals(mixinClassName)) {
				return true;
			}
			if ("io.wispforest.affinity.mixin.MapStateMixin".equals(mixinClassName)) {
				return true;
			}
			return CompatibilityHandler.isExternalMixinScrammed(mixinClassName);
		}
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.contains("region_chunk_cache")) {
			return false;
//			return FireblanketConfig.get(ConfigSpecs.FORCED_LOAD_RADIUS) > 0;
		}

		if (mixinClassName.contains("block_format")) {
			return FireblanketConfig.get(ConfigSpecs.FLATTEN_CHUNK_PALETTES);
		}

		if (mixinClassName.contains("ai") || mixinClassName.contains("sounds")) {
			return FireblanketConfig.get(ConfigSpecs.GAMEPLAY_CHANGES);
		}

		if (mixinClassName.contains("footgun")) {
			return !FireblanketConfig.get(ConfigSpecs.ALLOW_FOOTGUNS);
		}

		if (mixinClassName.contains("MixinRegionFile") || mixinClassName.contains("MixinPersistentState")) {
			return !FireblanketConfig.get(ConfigSpecs.AVOID_ZSTD);
		}

		return CompatibilityHandler.isMixinCompatible(this.mixinPackage, mixinClassName);
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return List.of();
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

}
