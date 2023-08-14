package net.modfest.fireblanket;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Bootstrap;

public class FireblanketMixin implements IMixinConfigPlugin {

	public static final boolean DO_BE_MASKING = Boolean.getBoolean("fireblanket.beMasking");

	@Override
	public void onLoad(String mixinPackage) {
		// aaaaagh
		boolean ignoreRenderingMods = Boolean.getBoolean("fireblanket.iSolemnlySwearIWillNotReportRenderingCrashesAndAcceptResponsibilityForBreakage");
		if (ignoreRenderingMods) {
			if (FabricLoader.getInstance().isModLoaded("nvidium")) {
				LoggerFactory.getLogger("Fireblanket").error("===================================================");
				LoggerFactory.getLogger("Fireblanket").error("         IGNORING THE PRESENCE OF NVIDIUM.         ");
				LoggerFactory.getLogger("Fireblanket").error("YOU ACCEPT EVERYTHING THAT WILL HAPPEN FROM NOW ON.");
				LoggerFactory.getLogger("Fireblanket").error("===================================================");
			}
			if (FabricLoader.getInstance().isModLoaded("bobby")) {
				LoggerFactory.getLogger("Fireblanket").error("Ignoring the presence of Bobby. Be ready for missing chunks.");
			}
		}
		if (!ignoreRenderingMods && FabricLoader.getInstance().isModLoaded("nvidium")) {
			Bootstrap.SYSOUT.println("""
			----------------------------------------------------------------------------------------
			###### Fireblanket is cowardly refusing to launch the game with Nvidium installed ######
			----------------------------------------------------------------------------------------
			 In the Blanketcon pack, Nvidium  causes impossible-to-debug render errors that lead to
			 sudden native crashes with no crash report or any useful logs. These  errors happen at
			  completely random, and there is nothing we can really do to solve them until Nvidium
			     receives a fix.  Additionally, Nvidium does not help with the main performance
			                bottlenecks in the pack, and is as such fairly unhelpful.
			========================================================================================
			If you would like to ignore these warnings and launch anyway, you must add the following
			to your JVM arguments:
			   -Dfireblanket.iSolemnlySwearIWillNotReportRenderingCrashesAndAcceptResponsibilityForBreakage=true
			The JVM will now exit.
			""");
			System.exit(0xDEAD);
		}
		if (FabricLoader.getInstance().isModLoaded("entityculling")) {
			Bootstrap.SYSOUT.println("""
			----------------------------------------------------------------------------------------
			### Fireblanket is cowardly refusing to launch the game with EntityCulling installed ###
			----------------------------------------------------------------------------------------
			  EntityCulling causes performance issues and hard-to-debug crashes due to poor use of
			  threading. It does not help with performance bottlenecks in the Blanketcon pack, and
			   in fact tends to make them worse.  Sodium's entity culling is already enabled, and
			  Fireblanket contains additional fixes and  optimizations for entities that are tuned
			                              specifically for Blanketcon.
			========================================================================================
			You may not override this. The JVM will now exit.
			""");
			System.exit(0xDEAD);
		}
		if (!ignoreRenderingMods && FabricLoader.getInstance().isModLoaded("bobby")) {
			Bootstrap.SYSOUT.println("""
			----------------------------------------------------------------------------------------
			####### Fireblanket is cowardly refusing to launch the game with Bobby installed #######
			----------------------------------------------------------------------------------------
			While Bobby is a great mod and makes navigating the map easier,  it is causing difficult
			 to debug culling issues that keep resulting in false issue reports.  We do not ship it
			 with the pack for a reason — we have had so many other issues to chase and fix that we
			simply do not have time to field the Bobby issues.  It additionally can cause crashes if
			            its option to keep block entities in fake chunks is not enabled.
			========================================================================================
			If you would like to ignore these warnings and launch anyway, you must add the following
			to your JVM arguments:
			   -Dfireblanket.iSolemnlySwearIWillNotReportRenderingCrashesAndAcceptResponsibilityForBreakage=true
			The JVM will now exit.
			""");
			System.exit(0xDEAD);
		}
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.contains("be_masking")) {
			return DO_BE_MASKING;
		}
		return true;
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
