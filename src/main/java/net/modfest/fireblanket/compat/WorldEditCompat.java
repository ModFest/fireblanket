package net.modfest.fireblanket.compat;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.fabric.FabricWorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.limit.PermissiveSelectorLimits;
import com.sk89q.worldedit.world.World;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.modfest.fireblanket.command.CommandUtils;

public class WorldEditCompat {

	public static BlockBox getSelection(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		LocalSession localSession = FabricWorldEdit.inst.getSession(ctx.getSource().getPlayerOrThrow());
		Region region;
		try {
			region = localSession.getSelection(FabricWorldEdit.inst.getWorld(ctx.getSource().getWorld()));
		} catch (IncompleteRegionException ex) {
			throw CommandUtils.GENERIC_EXCEPTION.create(Text.literal("Please make a region selection first."));
		}
		if (region instanceof CuboidRegion cr) {
			BlockVector3 min = cr.getMinimumPoint();
			BlockVector3 max = cr.getMaximumPoint();
			return new BlockBox(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
		} else {
			throw CommandUtils.GENERIC_EXCEPTION.create(Text.literal("Only cuboid regions are supported."));
		}
	}

	public static void setSelection(CommandContext<ServerCommandSource> ctx, BlockBox box) throws CommandSyntaxException {
		LocalSession localSession = FabricWorldEdit.inst.getSession(ctx.getSource().getPlayerOrThrow());
		World w = FabricWorldEdit.inst.getWorld(ctx.getSource().getWorld());
		CuboidRegionSelector crs = new CuboidRegionSelector(w);
		crs.selectPrimary(BlockVector3.at(box.getMinX(), box.getMinY(), box.getMinZ()), PermissiveSelectorLimits.getInstance());
		crs.selectSecondary(BlockVector3.at(box.getMaxX(), box.getMaxY(), box.getMaxZ()), PermissiveSelectorLimits.getInstance());
		localSession.setRegionSelector(w, crs);
		localSession.dispatchCUISelection(FabricAdapter.adaptCommandSource(ctx.getSource()));
	}


}
