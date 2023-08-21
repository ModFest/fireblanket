package net.modfest.fireblanket.command;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import com.google.common.collect.Iterables;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.modfest.fireblanket.Fireblanket;
import net.modfest.fireblanket.compat.WorldEditCompat;
import net.modfest.fireblanket.render_regions.RegionSyncRequest;
import net.modfest.fireblanket.render_regions.RenderRegion;
import net.modfest.fireblanket.render_regions.RenderRegions;
import net.modfest.fireblanket.render_regions.RenderRegionsState;

public class RegionCommand {

	private static final Predicate<ServerCommandSource> WORLDEDIT = ctx -> FabricLoader.getInstance().isModLoaded("worldedit");
	
	public static void init(LiteralArgumentBuilder<ServerCommandSource> base, CommandRegistryAccess access) {
		base.then(literal("region")
			.requires(ctx -> ctx.hasPermissionLevel(4))
			.then(literal("add")
				.then(argument("name", StringArgumentType.string())
					.then(addBranch("deny", RenderRegion.Mode.DENY))
					.then(addBranch("exclusive", RenderRegion.Mode.EXCLUSIVE))
					.then(addBranch("allow", RenderRegion.Mode.ALLOW))
				)
			)
			.then(literal("redefine")
				.then(argument("name", StringArgumentType.string())
					.suggests(RegionCommand::suggestRegionNames)
					.then(literal("from")
						.then(literal("worldedit")
							.requires(WORLDEDIT)
							.executes(ctx -> {
								String name = StringArgumentType.getString(ctx, "name");
								BlockBox box = WorldEditCompat.getSelection(ctx);
								RenderRegion ol = getRegion(ctx);
								RenderRegions regions = getRegions(ctx);
								RenderRegion nw = new RenderRegion(box.getMinX(), box.getMinY(), box.getMinZ(),
										box.getMaxX(), box.getMaxY(), box.getMaxZ(),
										ol.mode());
								regions.redefine(name, nw);
								ctx.getSource().sendFeedback(() -> Text.literal("Redefined region "+name), true);
								return 1;
							})
						)
					)
					.then(argument("corner1", BlockPosArgumentType.blockPos())
						.then(argument("corner2", BlockPosArgumentType.blockPos())
							.executes(ctx -> {
								String name = StringArgumentType.getString(ctx, "name");
								BlockPos min = BlockPosArgumentType.getBlockPos(ctx, "corner1");
								BlockPos max = BlockPosArgumentType.getBlockPos(ctx, "corner2");
								RenderRegion ol = getRegion(ctx);
								RenderRegions regions = getRegions(ctx);
								RenderRegion nw = new RenderRegion(min.getX(), min.getY(), min.getZ(),
										max.getX(), max.getY(), max.getZ(),
										ol.mode());
								regions.redefine(name, nw);
								ctx.getSource().sendFeedback(() -> Text.literal("Redefined region "+name), true);
								return 1;
							})
						)
					)
				)
			)
			.then(applyBranch(access, true))
			.then(applyBranch(access, false))
			.then(literal("select")
				.requires(WORLDEDIT)
				.then(argument("name", StringArgumentType.string())
					.suggests(RegionCommand::suggestRegionNames)
					.executes(ctx -> {
						RenderRegion r = getRegion(ctx);
						WorldEditCompat.setSelection(ctx, new BlockBox(r.minX(), r.minY(), r.minZ(), r.maxX(), r.maxY(), r.maxZ()));
						return 1;
					})
				)
			)
			.then(literal("destroy")
				.then(literal("everything")
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
				.then(argument("name", StringArgumentType.string())
					.suggests(RegionCommand::suggestRegionNames)
					.executes(ctx -> {
						RenderRegion r = getRegion(ctx);
						RenderRegions regions = getRegions(ctx);
						regions.remove(r);
						ctx.getSource().sendFeedback(() -> Text.literal("Destroyed region "+StringArgumentType.getString(ctx, "name")), true);
						return 1;
					})
				)
			)
			.then(literal("list")
				.executes(ctx -> {
					RenderRegions regions = getRegions(ctx);
					int size = regions.getRegionsByName().size();
					if (size == 1) {
						ctx.getSource().sendMessage(Text.literal("§lThere is 1 region defined"));
					} else {
						ctx.getSource().sendMessage(Text.literal("§lThere are "+size+" regions defined"));
					}
					for (var en : regions.getRegionsByName().entrySet()) {
						RenderRegion r = en.getValue();
						int ea = regions.getEntityAttachments(en.getValue()).size();
						int ba = regions.getBlockAttachments(en.getValue()).size();
						int eta = regions.getEntityTypeAttachments(en.getValue()).size();
						int beta = regions.getBlockEntityTypeAttachments(en.getValue()).size();
						String mode = switch (r.mode()) {
							case ALLOW -> "§aallow";
							case DENY -> "§cdeny";
							case EXCLUSIVE -> "§bexclusive";
							case UNKNOWN -> "§dunknown";
						};
						ctx.getSource().sendMessage(Text.literal("- §d§o"+en.getKey()+"§r "+mode+"§r ("+ea+"E, "+ba+"B, "+eta+"Et, "+beta+"BEt)"));
						ctx.getSource().sendMessage(Text.literal("  "+r.minX()+", "+r.minY()+", "+r.minZ()+" → "+r.maxX()+", "+r.maxY()+", "+r.maxZ()));
					}
					return 1;
				})
			)
			.then(literal("query")
				.then(argument("name", StringArgumentType.string())
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
						var ba = regions.getBlockAttachments(r);
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
						var eta = regions.getEntityTypeAttachments(r);
						if (!eta.isEmpty()) {
							ctx.getSource().sendMessage(Text.literal(eta.size()+" entity type attachment"+(eta.size() == 1 ? "" : "s")+":"));
							for (Identifier id : eta) {
								ctx.getSource().sendMessage(Text.literal("  - "+id));
							}
						}
						var beta = regions.getBlockEntityTypeAttachments(r);
						if (!beta.isEmpty()) {
							ctx.getSource().sendMessage(Text.literal(beta.size()+" block entity type attachment"+(beta.size() == 1 ? "" : "s")+":"));
							for (Identifier id : beta) {
								ctx.getSource().sendMessage(Text.literal("  - "+id));
							}
						}
						if (ea.isEmpty() && ba.isEmpty() && eta.isEmpty() && beta.isEmpty()) {
							if (r.mode() == RenderRegion.Mode.DENY) {
								ctx.getSource().sendMessage(Text.literal("No attachments, will cause all entities and block entities to not render unless added to an overlapping allow/exclusive region"));
							} else {
								ctx.getSource().sendMessage(Text.literal("No attachments, won't do anything"));
							}
						}
						return 1;
					})
				)
			)
			.then(literal("resync")
					.requires(ctx -> ctx.isExecutedByPlayer())
					.executes(ctx -> {
						Fireblanket.fullRegionSync(ctx.getSource().getWorld(), ctx.getSource().getPlayerOrThrow().networkHandler::sendPacket);
						return 1;
					})
				)
			.then(literal("ignore")
				.requires(ctx -> ctx.isExecutedByPlayer())
				.then(argument("name", StringArgumentType.string())
					.suggests(RegionCommand::suggestRegionNames)
					.executes(ctx -> {
						var req = new RegionSyncRequest.DestroyRegion(StringArgumentType.getString(ctx, "name"));
						ctx.getSource().getPlayer().networkHandler.sendPacket(req.toPacket(Fireblanket.REGIONS_UPDATE));
						return 1;
					})
				)
				.executes(ctx -> {
					var req = new RegionSyncRequest.Reset(true);
					ctx.getSource().getPlayer().networkHandler.sendPacket(req.toPacket(Fireblanket.REGIONS_UPDATE));
					return 1;
				})
			)
		);
	}

	private static ArgumentBuilder<ServerCommandSource, ?> addBranch(String arg, RenderRegion.Mode mode) {
		return literal(arg)
			.then(literal("from")
				.then(literal("worldedit")
					.requires(WORLDEDIT)
					.executes(ctx -> {
						BlockBox box = WorldEditCompat.getSelection(ctx);
						RenderRegion rr = new RenderRegion(box.getMinX(), box.getMinY(), box.getMinZ(), box.getMaxX(), box.getMaxY(), box.getMaxZ(), mode);
						return addRegion(ctx, rr);
					})
				)
				.then(literal("region")
					.then(argument("src-name", StringArgumentType.string())
						.suggests(RegionCommand::suggestRegionNames)
						.executes(ctx -> {
							RenderRegion src = getRegion(ctx, "src-name");
							RenderRegion rr = new RenderRegion(src.minX(), src.minY(), src.minZ(), src.maxX(), src.maxY(), src.maxZ(), mode);
							return addRegion(ctx, rr);
						})
					)
				)
			)
			.then(argument("corner1", BlockPosArgumentType.blockPos())
				.then(argument("corner2", BlockPosArgumentType.blockPos())
					.executes(ctx -> {
						BlockPos min = BlockPosArgumentType.getBlockPos(ctx, "corner1");
						BlockPos max = BlockPosArgumentType.getBlockPos(ctx, "corner2");
						RenderRegion rr = new RenderRegion(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), mode);
						return addRegion(ctx, rr);
					})
				)
			);
	}
	
	private static ArgumentBuilder<ServerCommandSource, ?> applyBranch(CommandRegistryAccess cra, boolean attach) {
		return literal(attach ? "attach" : "detach")
				.then(argument("name", StringArgumentType.string())
					.suggests(RegionCommand::suggestRegionNames)
					.then(literal("everything")
						.then(literal("from")
							.then(literal("region")
								.then(argument("src-name", StringArgumentType.string())
									.suggests(RegionCommand::suggestRegionNames)
									.executes(ctx -> {
										String name = StringArgumentType.getString(ctx, "name");
										RenderRegions rr = getRegions(ctx);
										RenderRegion r1 = getRegion(ctx);
										RenderRegion r2 = getRegion(ctx, "src-name");
										if (r1 == r2 && attach)
											throw new CommandException(Text.literal("All of the objects in "+name+" are already attached to "+name+"… wait…"));
										applyEntitiesByIdToRegion(ctx, r -> rr.getEntityAttachments(r2), attach);
										applyBlocksToRegion(ctx, (Iterable<BlockPos>)() -> {
											BlockPos.Mutable mut = new BlockPos.Mutable();
											LongIterator li = LongIterators.asLongIterator(rr.getBlockAttachments(r2).iterator());
											return new Iterator<BlockPos>() {
												@Override
												public boolean hasNext() {
													return li.hasNext();
												}
												@Override
												public BlockPos next() {
													mut.set(li.nextLong());
													return mut;
												}
											};
										}, null, attach);
										return 1;
									})
								)
								.executes(ctx -> {
									return 1;
								})
							)
						)
						.executes(ctx -> {
							if (attach) {
								throw new CommandException(Text.literal("Cowardly refusing to attach every single block and entity to this region"));
							}
							RenderRegion r = getRegion(ctx);
							RenderRegions regions = getRegions(ctx);
							int count = regions.detachAll(r);
							ctx.getSource().sendFeedback(() -> Text.literal("Detached "+count+" object"+(count == 1 ? "" : "s")+" from region "+StringArgumentType.getString(ctx, "name")), true);
							return count;
						})
					)
					.then(literal("entities")
						.then(literal("in")
							.then(literal("region")
								.then(argument("src-name", StringArgumentType.string())
									.suggests(RegionCommand::suggestRegionNames)
									.executes(ctx -> {
										return applyEntitiesToRegion(ctx, r -> ctx.getSource().getWorld().getOtherEntities(null, getRegion(ctx, "src-name").toBox()), attach);
									})
								)
								.executes(ctx -> {
									return applyEntitiesToRegion(ctx, r -> ctx.getSource().getWorld().getOtherEntities(null, r.toBox()), attach);
								})
							)
							.then(literal("worldedit")
								.requires(WORLDEDIT)
								.executes(ctx -> {
									return applyEntitiesToRegion(ctx, r -> ctx.getSource().getWorld().getOtherEntities(null, toBox(WorldEditCompat.getSelection(ctx))), attach);
								})
							)
						)
						.then(literal("from")
							.then(literal("region")
								.then(argument("src-name", StringArgumentType.string())
									.suggests(RegionCommand::suggestRegionNames)
									.executes(ctx -> {
										return applyEntitiesToRegion(ctx, r -> ctx.getSource().getWorld().getOtherEntities(null, getRegion(ctx, "src-name").toBox()), attach);
									})
								)
							)
						)
						.then(argument("entities", EntityArgumentType.entities())
							.executes(ctx -> {
								return applyEntitiesToRegion(ctx, r -> EntityArgumentType.getEntities(ctx, "entities"), attach);
							})
						)
					)
					.then(literal("block")
						.then(argument("position", BlockPosArgumentType.blockPos())
							.executes(ctx -> {
								RenderRegion r = getRegion(ctx);
								RenderRegions regions = getRegions(ctx);
								BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "position");
								if (attach) {
									regions.attachBlock(r, pos.asLong());
								} else {
									regions.detachBlock(r, pos.asLong());
								}
								ctx.getSource().sendFeedback(() -> Text.literal("Attached block at "+pos.toShortString()+" to region "+StringArgumentType.getString(ctx, "name")), true);
								return 1;
							})
						)
					)
					.then(literal("blocks")
						.then(literal("in")
							.then(literal("worldedit")
								.requires(WORLDEDIT)
								.then(argument("filter", BlockPredicateArgumentType.blockPredicate(cra))
									.executes(ctx -> {
										return applyBlocksToRegion(ctx, WorldEditCompat.getSelection(ctx), BlockPredicateArgumentType.getBlockPredicate(ctx, "filter"), attach);
									})
								)
								.then(literal("entities")
									.executes(ctx -> {
										return applyBlocksToRegion(ctx, WorldEditCompat.getSelection(ctx), cbp -> cbp.getBlockEntity() != null, attach);
									})
								)
								.executes(ctx -> {
									return applyBlocksToRegion(ctx, WorldEditCompat.getSelection(ctx), null, attach);
								})
							)
						)
						.then(argument("corner1", BlockPosArgumentType.blockPos())
							.then(argument("corner2", BlockPosArgumentType.blockPos())
								.then(literal("entities")
									.executes(ctx -> {
										BlockPos corner1 = BlockPosArgumentType.getBlockPos(ctx, "corner1");
										BlockPos corner2 = BlockPosArgumentType.getBlockPos(ctx, "corner2");
										return applyBlocksToRegion(ctx, BlockBox.create(corner1, corner2), cbp -> cbp.getBlockEntity() != null, attach);
									})
								)
								.then(argument("filter", BlockPredicateArgumentType.blockPredicate(cra))
									.executes(ctx -> {
										BlockPos corner1 = BlockPosArgumentType.getBlockPos(ctx, "corner1");
										BlockPos corner2 = BlockPosArgumentType.getBlockPos(ctx, "corner2");
										return applyBlocksToRegion(ctx, BlockBox.create(corner1, corner2), BlockPredicateArgumentType.getBlockPredicate(ctx, "filter"), attach);
									})
								)
								.executes(ctx -> {
									BlockPos corner1 = BlockPosArgumentType.getBlockPos(ctx, "corner1");
									BlockPos corner2 = BlockPosArgumentType.getBlockPos(ctx, "corner2");
									return applyBlocksToRegion(ctx, BlockBox.create(corner1, corner2), null, attach);
								})
							)
						)
					)
					.then(literal("be-type")
						.then(argument("type", RegistryKeyArgumentType.registryKey(RegistryKeys.BLOCK_ENTITY_TYPE))
							.executes(ctx -> {
								RenderRegion r = getRegion(ctx);
								RenderRegions regions = getRegions(ctx);
								Identifier id = ctx.getArgument("type", RegistryKey.class).getValue();
								if (attach) {
									regions.attachBlockEntityType(r, id);
								} else {
									regions.detachBlockEntityType(r, id);
								}
								ctx.getSource().sendFeedback(() -> Text.literal("Attached block entity type "+id+" to region "+StringArgumentType.getString(ctx, "name")), true);
								return 1;
							})
						)
					)
					.then(literal("entity-type")
						.then(argument("type", RegistryKeyArgumentType.registryKey(RegistryKeys.ENTITY_TYPE))
							.executes(ctx -> {
								RenderRegion r = getRegion(ctx);
								RenderRegions regions = getRegions(ctx);
								Identifier id = ctx.getArgument("type", RegistryKey.class).getValue();
								if (attach) {
									regions.attachEntityType(r, id);
								} else {
									regions.detachEntityType(r, id);
								}
								ctx.getSource().sendFeedback(() -> Text.literal("Attached entity type "+id+" to region "+StringArgumentType.getString(ctx, "name")), true);
								return 1;
							})
						)
					)
				);
	}

	private static Box toBox(BlockBox bb) {
		return new Box(bb.getMinX(), bb.getMinY(), bb.getMinZ(),
				bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());
	}
	
	private static Iterable<BlockPos> iterate(BlockBox region) {
		return BlockPos.iterate(region.getMinX(), region.getMinY(), region.getMinZ(),
				region.getMaxX(), region.getMaxY(), region.getMaxZ());
	}

	private interface EntitySource {
		Iterable<? extends Entity> supply(RenderRegion region) throws CommandSyntaxException;
	}
	
	private interface UUIDSource {
		Iterable<UUID> supply(RenderRegion region) throws CommandSyntaxException;
	}

	private static int applyBlocksToRegion(CommandContext<ServerCommandSource> ctx, BlockBox region, Predicate<CachedBlockPosition> pred, boolean attach) {
		return applyBlocksToRegion(ctx, iterate(region), pred, attach);
	}
	
	private static int applyBlocksToRegion(CommandContext<ServerCommandSource> ctx, Iterable<BlockPos> region, Predicate<CachedBlockPosition> pred, boolean attach) {
		RenderRegion r = getRegion(ctx);
		RenderRegions regions = getRegions(ctx);
		int count = 0;
		for (BlockPos bp : region) {
			if (pred == null || pred.test(new CachedBlockPosition(ctx.getSource().getWorld(), bp, false))) {
				if (attach) {
					regions.attachBlock(r, bp.asLong());
					count++;
				} else if (regions.detachBlock(r, bp.asLong())) {
					count++;
				}
			}
		}
		final int fcount = count;
		if (attach) {
			if (count == 0) {
				throw new CommandException(Text.literal("None of the blocks matched the filter"));
			} else {
				ctx.getSource().sendFeedback(() -> Text.literal("Attached "+fcount+" block"+(fcount == 1 ? "" : "s")+" to region "+StringArgumentType.getString(ctx, "name")), true);
			}
		} else {
			if (count == 0) {
				throw new CommandException(Text.literal("None of those blocks are attached to the region"));
			} else {
				ctx.getSource().sendFeedback(() -> Text.literal("Detached "+fcount+" block"+(fcount == 1 ? "" : "s")+" from region "+StringArgumentType.getString(ctx, "name")), true);
			}
		}
		return count;
	}
	
	private static int applyEntitiesToRegion(CommandContext<ServerCommandSource> ctx, EntitySource src, boolean attach) throws CommandSyntaxException {
		return applyEntitiesByIdToRegion(ctx, r -> Iterables.transform(src.supply(r), Entity::getUuid), attach);
	}
	
	private static int applyEntitiesByIdToRegion(CommandContext<ServerCommandSource> ctx, UUIDSource src, boolean attach) throws CommandSyntaxException {
		RenderRegion r = getRegion(ctx);
		RenderRegions regions = getRegions(ctx);
		int count = 0;
		boolean anything = false;
		for (UUID id : src.supply(r)) {
			anything = true;
			if (attach) {
				regions.attachEntity(r, id);
				count++;
			} else if (regions.detachEntity(r, id)) {
				count++;
			}
		}
		final int fcount = count;
		if (attach) {
			ctx.getSource().sendFeedback(() -> Text.literal("Attached "+fcount+" entit"+(fcount == 1 ? "y" : "ies")+" to region "+StringArgumentType.getString(ctx, "name")), true);
		} else {
			if (count == 0) {
				if (anything) {
					throw new CommandException(Text.literal("None of those entities are attached to the region"));
				} else {
					throw new CommandException(Text.literal("No entities were specified"));
				}
			} else {
				ctx.getSource().sendFeedback(() -> Text.literal("Detached "+fcount+" entit"+(fcount == 1 ? "y" : "ies")+" from region "+StringArgumentType.getString(ctx, "name")), true);
			}
		}
		return count;
	}

	public static RenderRegions getRegions(CommandContext<ServerCommandSource> ctx) {
		return RenderRegionsState.get(ctx.getSource().getWorld()).getRegions();
	}
	
	private static RenderRegion getRegion(CommandContext<ServerCommandSource> ctx) {
		return getRegion(ctx, "name");
	}
	
	private static RenderRegion getRegion(CommandContext<ServerCommandSource> ctx, String tgt) {
		String name = StringArgumentType.getString(ctx, tgt);
		RenderRegion r = getRegions(ctx).getByName(name);
		if (r == null) throw new CommandException(Text.literal("No render region with name \""+name+"\" exists"));
		return r;
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
		regions.add(name, rr);
		ctx.getSource().sendFeedback(() -> Text.literal("Created new "+rr.mode().name().toLowerCase(Locale.ROOT)+" region "+name), true);
		return 1;
	}

}
