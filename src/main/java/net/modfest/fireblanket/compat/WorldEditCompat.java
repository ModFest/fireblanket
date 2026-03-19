package net.modfest.fireblanket.compat;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class WorldEditCompat {

	public static BoundingBox getSelection(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		/*
		LocalSession localSession = FabricWorldEdit.inst.getSession(ctx.getSource().getPlayerOrException());
		Region region;
		try {
			region = localSession.getSelection(FabricWorldEdit.inst.getWorld(ctx.getSource().getLevel()));
		} catch (IncompleteRegionException ex) {
			throw CommandUtils.GENERIC_EXCEPTION.create(Component.literal("Please make a region selection first."));
		}
		if (region instanceof CuboidRegion cr) {
			BlockVector3 min = cr.getMinimumPoint();
			BlockVector3 max = cr.getMaximumPoint();
			return new BoundingBox(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
		} else {
			throw CommandUtils.GENERIC_EXCEPTION.create(Component.literal("Only cuboid regions are supported."));
		}
		*/
		throw new AssertionError();
	}

	public static void setSelection(CommandContext<CommandSourceStack> ctx, BoundingBox box) throws CommandSyntaxException {
		/*
		LocalSession localSession = FabricWorldEdit.inst.getSession(ctx.getSource().getPlayerOrException());
		World w = FabricWorldEdit.inst.getWorld(ctx.getSource().getLevel());
		CuboidRegionSelector crs = new CuboidRegionSelector(w);
		crs.selectPrimary(BlockVector3.at(box.minX(), box.minY(), box.minZ()), PermissiveSelectorLimits.getInstance());
		crs.selectSecondary(BlockVector3.at(box.maxX(), box.maxY(), box.maxZ()), PermissiveSelectorLimits.getInstance());
		localSession.setRegionSelector(w, crs);
		localSession.dispatchCUISelection(FabricAdapter.adaptCommandSource(ctx.getSource()));
		*/
		throw new AssertionError();
	}


}
