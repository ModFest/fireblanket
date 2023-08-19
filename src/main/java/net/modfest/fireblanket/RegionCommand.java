package net.modfest.fireblanket;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.modfest.fireblanket.compat.WorldEditCompat;
import net.modfest.fireblanket.render_regions.RegionSyncCommand;
import net.modfest.fireblanket.render_regions.RenderRegion;
import net.modfest.fireblanket.render_regions.RenderRegions;
import net.modfest.fireblanket.render_regions.RenderRegionsState;

public class RegionCommand {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("fireblanket:region")
                .requires(ctx -> ctx.hasPermissionLevel(4))
                .then(CommandManager.literal("add")
                    .then(CommandManager.argument("name", StringArgumentType.string())
                        .then(buildAddBranch("deny", RenderRegion.Mode.DENY))
                        .then(buildAddBranch("allow", RenderRegion.Mode.ALLOW))
                    )
                )
                .then(CommandManager.literal("attach")
                    .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests(RegionCommand::suggestRegionNames)
                        .then(CommandManager.literal("entity")
                            .then(CommandManager.literal("auto")
                                .executes(ctx -> {
                                    return attachEntitiesToRegion(ctx, r -> ctx.getSource().getWorld().getOtherEntities(null, r.toBox()));
                                })
                            )
                            .then(CommandManager.argument("entities", EntityArgumentType.entities())
                                .executes(ctx -> {
                                    return attachEntitiesToRegion(ctx, r -> EntityArgumentType.getEntities(ctx, "entities"));
                                })
                            )
                        )
                        .then(CommandManager.literal("block")
                            .then(CommandManager.argument("position", BlockPosArgumentType.blockPos())
                                .executes(ctx -> {
                                    RenderRegion r = getRegion(ctx);
                                    RenderRegions regions = getRegions(ctx);
                                    BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");
                                    regions.attachBlockEntity(r, pos.asLong());
                                    ctx.getSource().sendFeedback(() -> Text.literal("Attached block at "+pos.toShortString()+" to region "+StringArgumentType.getString(ctx, "name")), true);
                                    return 1;
                                })
                            )
                        )
                        .then(CommandManager.literal("blocks")
                            .then(CommandManager.literal("from")
                                .then(CommandManager.literal("worldedit")
                                    .requires(ctx -> FabricLoader.getInstance().isModLoaded("worldedit"))
                                    .executes(ctx -> {
                                        return attachBlocksToRegion(ctx, WorldEditCompat.getSelection(ctx));
                                    })
                                )
                            )
                            .then(CommandManager.argument("corner1", BlockPosArgumentType.blockPos())
                                .then(CommandManager.argument("corner2", BlockPosArgumentType.blockPos())
                                    .executes(ctx -> {
                                        BlockPos corner1 = BlockPosArgumentType.getBlockPos(ctx, "corner1");
                                        BlockPos corner2 = BlockPosArgumentType.getBlockPos(ctx, "corner2");
                                        return attachBlocksToRegion(ctx, BlockBox.create(corner1, corner2));
                                    })
                                )
                            )
                        )
                    )
                )
                .then(CommandManager.literal("detach")
                    .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests(RegionCommand::suggestRegionNames)
                        .then(CommandManager.literal("everything")
                            .executes(ctx -> {
                                RenderRegion r = getRegion(ctx);
                                RenderRegions regions = getRegions(ctx);
                                int count = regions.detachAll(r);
                                ctx.getSource().sendFeedback(() -> Text.literal("Detached "+count+" object"+(count == 1 ? "" : "s")+" from region "+StringArgumentType.getString(ctx, "name")), true);
                                return count;
                            })
                        )
                        .then(CommandManager.literal("entity")
                            .then(CommandManager.literal("auto")
                                .executes(ctx -> {
                                    return detachEntitiesFromRegion(ctx, r -> ctx.getSource().getWorld().getOtherEntities(null, r.toBox()));
                                })
                            )
                            .then(CommandManager.argument("entities", EntityArgumentType.entities())
                                .executes(ctx -> {
                                    return detachEntitiesFromRegion(ctx, r -> EntityArgumentType.getEntities(ctx, "entities"));
                                })
                            )
                        )
                        .then(CommandManager.literal("block")
                            .then(CommandManager.argument("block", BlockPosArgumentType.blockPos())
                                .executes(ctx -> {
                                    RenderRegion r = getRegion(ctx);
                                    RenderRegions regions = getRegions(ctx);
                                    BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");
                                    if (regions.detachBlockEntity(r, pos.asLong())) {
                                        ctx.getSource().sendFeedback(() -> Text.literal("Detached block at "+pos.toShortString()+" from region "+StringArgumentType.getString(ctx, "name")), true);
                                    } else {
                                        throw new CommandException(Text.literal("That block is not attached to the region"));
                                    }
                                    return 1;
                                })
                            )
                        )
                        .then(CommandManager.literal("blocks")
                            .then(CommandManager.literal("from")
                                .then(CommandManager.literal("worldedit")
                                    .requires(ctx -> FabricLoader.getInstance().isModLoaded("worldedit"))
                                    .executes(ctx -> {
                                        return detachBlocksFromRegion(ctx, WorldEditCompat.getSelection(ctx));
                                    })
                                )
                            )
                            .then(CommandManager.argument("corner1", BlockPosArgumentType.blockPos())
                                .then(CommandManager.argument("corner2", BlockPosArgumentType.blockPos())
                                    .executes(ctx -> {
                                        BlockPos corner1 = BlockPosArgumentType.getBlockPos(ctx, "corner1");
                                        BlockPos corner2 = BlockPosArgumentType.getBlockPos(ctx, "corner2");
                                        return detachBlocksFromRegion(ctx, BlockBox.create(corner1, corner2));
                                    })
                                )
                            )
                        )
                    )
                )
                .then(CommandManager.literal("destroy")
                    .then(CommandManager.literal("everything")
                        .executes(ctx -> {
                            if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
                                throw new CommandException(Text.literal("Cowardly refusing to destroy everything outside of a dev env"));
                            }
                            RenderRegions regions = getRegions(ctx);
                            int count = regions.getRegionsByName().size();
                            regions.clear();
                            ctx.getSource().sendFeedback(() -> Text.literal("Destroyed "+count+" region"+(count == 1 ? "" : "s")), true);
                            return count;
                        })
                    )
                    .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests(RegionCommand::suggestRegionNames)
                        .executes(ctx -> {
                            RenderRegion r = getRegion(ctx);
                            RenderRegions regions = getRegions(ctx);
                            regions.removeRegion(r);
                            ctx.getSource().sendFeedback(() -> Text.literal("Destroyed region "+StringArgumentType.getString(ctx, "name")), true);
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("list")
                    .executes(ctx -> {
                        RenderRegions regions = getRegions(ctx);
                        int size = regions.getRegionsByName().size();
                        if (size == 1) {
                            ctx.getSource().sendMessage(Text.literal("There is 1 region defined"));
                        } else {
                            ctx.getSource().sendMessage(Text.literal("There are "+size+" regions defined"));
                        }
                        for (var en : regions.getRegionsByName().entrySet()) {
                            RenderRegion r = en.getValue();
                            int ea = regions.getEntityAttachments(en.getValue()).size();
                            int ba = regions.getBlockEntityAttachments(en.getValue()).size();
                            ctx.getSource().sendMessage(Text.literal("- "+en.getKey()+" ("+ea+"E, "+ba+"B)"));
                            ctx.getSource().sendMessage(Text.literal("  "+r.minX()+", "+r.minY()+", "+r.minZ()+" → "+r.maxX()+", "+r.maxY()+", "+r.maxZ()));
                        }
                        return 1;
                    })
                )
                .then(CommandManager.literal("query")
                    .then(CommandManager.argument("name", StringArgumentType.string())
                        .suggests(RegionCommand::suggestRegionNames)
                        .executes(ctx -> {
                            RenderRegion r = getRegion(ctx);
                            RenderRegions regions = getRegions(ctx);
                            String mn = r.mode().name();
                            ctx.getSource().sendMessage(Text.literal(mn.charAt(0)+mn.toLowerCase(Locale.ROOT).substring(1)+" region "+StringArgumentType.getString(ctx, "name")));
                            ctx.getSource().sendMessage(Text.literal(""+r.minX()+", "+r.minY()+", "+r.minZ()+" → "+r.maxX()+", "+r.maxY()+", "+r.maxZ()));
                            var ea = regions.getEntityAttachments(r);
                            if (!ea.isEmpty()) {
                                ctx.getSource().sendMessage(Text.literal(ea.size()+" entity attachment"+(ea.size() == 1 ? "" : "s")+":"));
                                for (UUID id : ea) {
                                    Entity e = ctx.getSource().getWorld().getEntity(id);
                                    if (e == null) {
                                        ctx.getSource().sendMessage(Text.literal("  - "+id+" (unknown)"));
                                    } else {
                                        ctx.getSource().sendMessage(Text.literal("  - "+id+" ("+Registries.ENTITY_TYPE.getId(e.getType())+" @ "+e.getPos()+")"));
                                    }
                                }
                            }
                            var ba = regions.getBlockEntityAttachments(r);
                            if (!ba.isEmpty()) {
                                BlockPos.Mutable mut = new BlockPos.Mutable();
                                ctx.getSource().sendMessage(Text.literal(ba.size()+" block attachment"+(ba.size() == 1 ? "" : "s")+":"));
                                for (long posl : ba) {
                                    mut.set(posl);
                                    BlockEntity be = ctx.getSource().getWorld().getBlockEntity(mut);
                                    if (be == null) {
                                        ctx.getSource().sendMessage(Text.literal("  - "+mut.toShortString()+" (unknown)"));
                                    } else {
                                        ctx.getSource().sendMessage(Text.literal("  - "+mut.toShortString()+" ("+Registries.BLOCK_ENTITY_TYPE.getId(be.getType())+")"));
                                    }
                                }
                            }
                            if (ea.isEmpty() && ba.isEmpty()) {
                                if (r.mode() == RenderRegion.Mode.DENY) {
                                    ctx.getSource().sendMessage(Text.literal("No attachments, will cause all entities and block entities to not render unless added to an overlapping allow region"));
                                } else {
                                    ctx.getSource().sendMessage(Text.literal("No attachments, won't do anything"));
                                }
                            }
                            return 1;
                        })
                    )
                )
                .then(CommandManager.literal("resync")
                        .requires(ctx -> ctx.isExecutedByPlayer())
                        .executes(ctx -> {
                            Fireblanket.fullRegionSync(ctx.getSource().getWorld(), ctx.getSource().getPlayerOrThrow().networkHandler::sendPacket);
                            return 1;
                        })
                    )
                .then(CommandManager.literal("ignore")
                    .requires(ctx -> ctx.isExecutedByPlayer())
                    .executes(ctx -> {
                        var cmd = new RegionSyncCommand.Reset(true);
                        ctx.getSource().getPlayer().networkHandler.sendPacket(cmd.toPacket(Fireblanket.REGIONS_UPDATE));
                        return 1;
                    })
                )
            );
        });
    }

    private static int detachBlocksFromRegion(CommandContext<ServerCommandSource> ctx, BlockBox region) {
        RenderRegion r = getRegion(ctx);
        RenderRegions regions = getRegions(ctx);
        int count = 0;
        for (BlockPos bp : BlockPos.iterate(region.getMinX(), region.getMinY(), region.getMinZ(),
                region.getMaxX(), region.getMaxY(), region.getMaxZ())) {
            if (regions.detachBlockEntity(r, bp.asLong())) {
                count++;
            }
        }
        if (count == 0) {
            throw new CommandException(Text.literal("None of those blocks are attached to the region"));
        } else {
            final int fcount = count;
            ctx.getSource().sendFeedback(() -> Text.literal("Detached "+fcount+" block"+(fcount == 1 ? "" : "s")+" from region "+StringArgumentType.getString(ctx, "name")), true);
        }
        return count;
    }

    private static int attachBlocksToRegion(CommandContext<ServerCommandSource> ctx, BlockBox region) {
        RenderRegion r = getRegion(ctx);
        RenderRegions regions = getRegions(ctx);
        int count = 0;
        for (BlockPos bp : BlockPos.iterate(region.getMinX(), region.getMinY(), region.getMinZ(),
                region.getMaxX(), region.getMaxY(), region.getMaxZ())) {
            regions.attachBlockEntity(r, bp.asLong());
            count++;
        }
        final int fcount = count;
        ctx.getSource().sendFeedback(() -> Text.literal("Attached "+fcount+" block"+(fcount == 1 ? "" : "s")+" to region "+StringArgumentType.getString(ctx, "name")), true);
        return count;
    }

    private interface EntitySource {
        Iterable<? extends Entity> supply(RenderRegion region) throws CommandSyntaxException;
    }
    
    private static int attachEntitiesToRegion(CommandContext<ServerCommandSource> ctx, EntitySource src) throws CommandSyntaxException {
        RenderRegion r = getRegion(ctx);
        RenderRegions regions = getRegions(ctx);
        int count = 0;
        for (Entity e : src.supply(r)) {
            regions.attachEntity(r, e);
            count++;
        }
        final int fcount = count;
        ctx.getSource().sendFeedback(() -> Text.literal("Attached "+fcount+" entit"+(fcount == 1 ? "y" : "ies")+" to region "+StringArgumentType.getString(ctx, "name")), true);
        return count;
    }
    
    private static int detachEntitiesFromRegion(CommandContext<ServerCommandSource> ctx, EntitySource src) throws CommandSyntaxException {
        RenderRegion r = getRegion(ctx);
        RenderRegions regions = getRegions(ctx);
        int count = 0;
        boolean anything = false;
        for (Entity e : src.supply(r)) {
            anything = true;
            if (regions.detachEntity(r, e)) count++;
        }
        if (count == 0) {
            if (anything) {
                throw new CommandException(Text.literal("None of those entities are attached to the region"));
            } else {
                throw new CommandException(Text.literal("No entities were specified"));
            }
        } else {
            final int fcount = count;
            ctx.getSource().sendFeedback(() -> Text.literal("Detached "+fcount+" entit"+(fcount == 1 ? "y" : "ies")+" from region "+StringArgumentType.getString(ctx, "name")), true);
        }
        return count;
    }

    public static RenderRegions getRegions(CommandContext<ServerCommandSource> ctx) {
        return RenderRegionsState.get(ctx.getSource().getWorld()).getRegions();
    }
    
    private static RenderRegion getRegion(CommandContext<ServerCommandSource> ctx) {
        String name = StringArgumentType.getString(ctx, "name");
        RenderRegion r = getRegions(ctx).getByName(name);
        if (r == null) throw new CommandException(Text.literal("No render region with name \""+name+"\" exists"));
        return r;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> buildAddBranch(String arg, RenderRegion.Mode mode) {
        return CommandManager.literal(arg)
            .then(CommandManager.literal("from")
                .then(CommandManager.literal("worldedit")
                    .requires(ctx -> FabricLoader.getInstance().isModLoaded("worldedit"))
                    .executes(ctx -> {
                        BlockBox box = WorldEditCompat.getSelection(ctx);
                        RenderRegion rr = new RenderRegion(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ(), mode);
                        return addRegion(ctx, rr);
                    })
                )
            )
            .then(CommandManager.argument("corner1", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("corner2", BlockPosArgumentType.blockPos())
                    .executes(ctx -> {
                        BlockPos min = BlockPosArgumentType.getBlockPos(ctx, "corner1");
                        BlockPos max = BlockPosArgumentType.getBlockPos(ctx, "corner2");
                        RenderRegion rr = new RenderRegion(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), mode);
                        return addRegion(ctx, rr);
                    })
                )
            );
    }

    private static CompletableFuture<Suggestions> suggestRegionNames(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder builder) {
        for (String name : getRegions(ctx).getRegionsByName().keySet()) {
            if (name.startsWith(builder.getRemaining())) {
                builder.suggest(name);
            }
        }
        return builder.buildFuture();
    }

    public static int addRegion(CommandContext<ServerCommandSource> ctx, RenderRegion rr) {
    	String name = StringArgumentType.getString(ctx, "name");
    	RenderRegions regions = RegionCommand.getRegions(ctx);
    	if (regions.getByName(name) != null) {
    		throw new CommandException(Text.literal("A region with that name already exists"));
    	}
        regions.addRegion(name, rr);
        ctx.getSource().sendFeedback(() -> Text.literal("Created new region "+name), true);
        return 1;
    }

}
