package net.modfest.fireblanket.compat;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.fabric.FabricWorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;

public class WorldEditCompat {

	public static BlockBox getSelection(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		LocalSession localSession = FabricWorldEdit.inst.getSession(ctx.getSource().getPlayerOrThrow());
		Region region; // declare the region variable
		World selectionWorld = localSession.getSelectionWorld();
		try {
			if (selectionWorld == null) throw new IncompleteRegionException();
			region = localSession.getSelection(selectionWorld);
		} catch (IncompleteRegionException ex) {
			throw new CommandException(Text.literal("Please make a region selection first."));
		}
		if (region instanceof CuboidRegion cr) {
			BlockVector3 min = cr.getMinimumPoint();
			BlockVector3 max = cr.getMaximumPoint();
			return new BlockBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ());
		} else {
			throw new CommandException(Text.literal("Only cuboid regions are supported."));
		}
	}

	
}
