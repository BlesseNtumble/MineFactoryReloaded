package powercrystals.minefactoryreloaded;

import static powercrystals.minefactoryreloaded.setup.MFRThings.*;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderPlayerEvent.SetArmorModel;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent.Unload;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.Point;

import powercrystals.minefactoryreloaded.block.*;
import powercrystals.minefactoryreloaded.block.decor.BlockDecorativeBricks;
import powercrystals.minefactoryreloaded.block.decor.BlockDecorativeStone;
import powercrystals.minefactoryreloaded.block.decor.BlockFactoryDecoration;
import powercrystals.minefactoryreloaded.block.decor.BlockFactoryPlastic;
import powercrystals.minefactoryreloaded.block.transport.BlockFactoryRail;
import powercrystals.minefactoryreloaded.block.transport.BlockFactoryRoad;
import powercrystals.minefactoryreloaded.core.IHarvestAreaContainer;
import powercrystals.minefactoryreloaded.item.gun.ItemRocketLauncher;
import powercrystals.minefactoryreloaded.render.MachineStateMapper;
import powercrystals.minefactoryreloaded.setup.MFRThings;
import powercrystals.minefactoryreloaded.tile.transport.TileEntityConveyor;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class MineFactoryReloadedClient implements IResourceManagerReloadListener {

	public static MineFactoryReloadedClient instance;

	private static final ResourceLocation targetingBlue =
			new ResourceLocation(MineFactoryReloadedCore.hudFolder + "lockon_blue.png");
	private static final ResourceLocation targetingRed =
			new ResourceLocation(MineFactoryReloadedCore.hudFolder + "lockon_red.png");
	private static final int _lockonMax = 30;
	private static final int _lockonLostMax = 60;
	private int _lockonTicks = 0;
	private int _lockonLostTicks = 0;
	private Entity _lastEntityOver = null;
	@SuppressWarnings("unused")
	private static boolean gl14 = false;

	public static HashMap<BlockPos, Integer> prcPages = new HashMap<BlockPos, Integer>();

	public static Set<IHarvestAreaContainer> _areaTileEntities = new LinkedHashSet<IHarvestAreaContainer>();

	public static void preInit() {
		
		//decorative blocks
		registerModel(MFRThings.factoryDecorativeBrickBlock, "variant", BlockDecorativeBricks.Variant.NAMES);
		registerModel(MFRThings.factoryDecorativeStoneBlock, "variant", BlockDecorativeStone.Variant.NAMES);
		registerModel(MFRThings.machineBlock, "variant", BlockFactoryDecoration.Variant.NAMES);
		registerModel(MFRThings.factoryPlasticBlock, "variant", BlockFactoryPlastic.Variant.NAMES);
		registerModel(MFRThings.pinkSlimeBlock);

		//fluids
		registerModel(MFRThings.milkLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "milk"));
		registerModel(MFRThings.sludgeLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "sludge"));
		registerModel(MFRThings.sewageLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "sewage"));
		registerModel(MFRThings.essenceLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "mob_essence"));
		registerModel(MFRThings.biofuelLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "biofuel"));
		registerModel(MFRThings.meatLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "meat"));
		registerModel(MFRThings.pinkSlimeLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "pink_slime"));
		registerModel(MFRThings.chocolateMilkLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "chocolate_milk"));
		registerModel(MFRThings.mushroomSoupLiquid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "mushroom_soup"));
		registerModel(MFRThings.steamFluid, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":fluid", "steam"));
		ModelLoader.setCustomModelResourceLocation(MFRThings.milkBottleItem, 0, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":milk_bottle"));

		//transport
		Item item = Item.getItemFromBlock(MFRThings.conveyorBlock);
		for (int i=0; i < 17; i++)
			ModelLoader.setCustomModelResourceLocation(item, i,  new ModelResourceLocation(MFRThings.conveyorBlock.getRegistryName(), "inventory"));

		registerRailModel(MFRThings.railPickupCargoBlock, "cargo_pickup");
		registerRailModel(MFRThings.railDropoffCargoBlock, "cargo_dropoff");
		registerRailModel(MFRThings.railPickupPassengerBlock, "passenger_pickup");
		registerRailModel(MFRThings.railDropoffPassengerBlock, "passenger_dropoff");

		registerModel(MFRThings.factoryRoadBlock, "variant", BlockFactoryRoad.Variant.NAMES);
		
		//machines
		ModelLoader.setCustomStateMapper(MFRThings.machineBlocks.get(0), MachineStateMapper.getInstance());
		ModelLoader.setCustomStateMapper(MFRThings.machineBlocks.get(1), MachineStateMapper.getInstance());
		ModelLoader.setCustomStateMapper(MFRThings.machineBlocks.get(2), MachineStateMapper.getInstance());

		for (BlockFactoryMachine.Type type : BlockFactoryMachine.Type.values()) {
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(MFRThings.machineBlocks.get(type.getGroupIndex())), type.getMeta(),
					new ModelResourceLocation(MineFactoryReloadedCore.modId + ":" + MachineStateMapper.getModelName(type), "type=" + type.getName()));
		}

		registerModel(MFRThings.fertileSoil, BlockFertileSoil.MOISTURE);
		
		ModelLoader.setCustomStateMapper(MFRThings.rubberLeavesBlock, new StateMap.Builder().ignore(BlockRubberLeaves.CHECK_DECAY, BlockRubberLeaves.DECAYABLE).build());
		item = Item.getItemFromBlock(MFRThings.rubberLeavesBlock);
		ModelLoader.setCustomMeshDefinition(item, stack -> {

			String variant = "fancy=" + Minecraft.getMinecraft().gameSettings.fancyGraphics;
			variant += ",variant=" + (stack.getMetadata() == 0 ? "normal" : "dry");
			return new ModelResourceLocation(MFRThings.rubberLeavesBlock.getRegistryName(), variant); //TODO cache values
		});

		ModelLoader.setCustomStateMapper(MFRThings.rubberSaplingBlock, new StateMap.Builder().ignore(BlockRubberSapling.TYPE, BlockRubberSapling.STAGE).build());
		item = Item.getItemFromBlock(MFRThings.rubberSaplingBlock);
		for (int i=0; i<4; i++) {
			ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(MFRThings.rubberSaplingBlock.getRegistryName(), "inventory"));
		}

		ModelLoader.setCustomStateMapper(MFRThings.rubberWoodBlock, new StateMap.Builder().ignore(BlockRubberWood.RUBBER_FILLED).build());
		item = Item.getItemFromBlock(MFRThings.rubberWoodBlock);
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(MFRThings.rubberWoodBlock.getRegistryName(), "axis=y"));
		
		registerModel(MFRThings.vineScaffoldBlock);
		
		ModelLoader.setCustomModelResourceLocation(MFRThings.factoryHammerItem, 0, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":hammer"));
	}

	private static void registerRailModel(Block railBlock, final String typeVariant) {
		ModelLoader.setCustomStateMapper(railBlock, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return new ModelResourceLocation(MineFactoryReloadedCore.modId + ":rail", "shape=" + state.getValue(BlockFactoryRail.SHAPE) + ",type=" + typeVariant);
			}
		});

		Item item = Item.getItemFromBlock(railBlock);
		if (item != null)
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(MineFactoryReloadedCore.modId + ":rail_" + typeVariant, "inventory"));
	}

	private static void registerModel(Block block, String propertyName, String[] values, IProperty<?>... propertiesToIgnore) {

		if (propertiesToIgnore.length > 0)
			ModelLoader.setCustomStateMapper(block, new StateMap.Builder().ignore(propertiesToIgnore).build());

		Item item = Item.getItemFromBlock(block);
		if (item != null) {
			for (int i = 0; i < values.length; i++) {
				ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(block.getRegistryName(), propertyName + "=" + values[i]));
			}
		}
	}

	private static void registerModel(Block block, ModelResourceLocation modelLocation) {

		ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
				return modelLocation;
			}
		});
		Item item = Item.getItemFromBlock(block);
		if (item != null)
			ModelLoader.setCustomModelResourceLocation(item, 0,  modelLocation);
	}

	private static void registerModel(Block block, IProperty<?>... propertiesToIgnore) {

		if (propertiesToIgnore.length > 0)
			ModelLoader.setCustomStateMapper(block, new StateMap.Builder().ignore(propertiesToIgnore).build());
	
		Item item = Item.getItemFromBlock(block);
		if (item != null)
			ModelLoader.setCustomModelResourceLocation(item, 0,  new ModelResourceLocation(block.getRegistryName(), "normal"));
	}

	public static void init() {

		instance = new MineFactoryReloadedClient();

		BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
		blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {

			if(tintIndex == 0 && pos != null) {
				TileEntity te = world.getTileEntity(pos);

				if(te instanceof TileEntityConveyor) {
					EnumDyeColor dyeColor = ((TileEntityConveyor) te).getDyeColor();

					if(dyeColor != null) {
						return dyeColor.getMapColor().colorValue;
					}
					return 0xf6a82c;
				}
			}
			return 0xFFFFFF;
		}, MFRThings.conveyorBlock);
		
		blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
			
			BlockRubberLeaves.Variant variant = state.getValue(BlockRubberLeaves.VARIANT);

			int foliageColor;
			if (world != null && pos != null) {
				foliageColor = BiomeColorHelper.getFoliageColorAtPos(world, pos);
			} else {
				foliageColor = ColorizerFoliage.getFoliageColorBasic();
			}
			
			if (variant == BlockRubberLeaves.Variant.DRY) {
				int r = (foliageColor & 16711680) >> 16;
				int g = (foliageColor & 65280) >> 8;
				int b = foliageColor & 255;
				return ( r / 4 << 16 | g / 4 << 8 | b / 4) + 0xc0c0c0;
			}

			return foliageColor;
		}, MFRThings.rubberLeavesBlock);
		
		blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> 
				(world != null && pos != null) ? BiomeColorHelper.getFoliageColorAtPos(world, pos) : ColorizerFoliage.getFoliageColorBasic(), vineScaffoldBlock);
		
		ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
		itemColors.registerItemColorHandler((stack, tintIndex) -> {

			if (tintIndex != 0)
				return 0xFFFFFF;

			if (stack.getItemDamage() == 16)
				return 0xf6a82c;

			return EnumDyeColor.byMetadata(stack.getItemDamage()).getMapColor().colorValue;
		}, MFRThings.conveyorBlock);
		
		itemColors.registerItemColorHandler((stack, tintIndex) -> stack.getMetadata() == 1 ? 0xFFFFFF : ColorizerFoliage.getFoliageColorBasic(), MFRThings.rubberLeavesBlock);
		itemColors.registerItemColorHandler((stack, tintIndex) -> ColorizerFoliage.getFoliageColorBasic(), MFRThings.vineScaffoldBlock);

	/* TODO fix rendering
		// IDs
		renderIdConveyor = RenderingRegistry.getNextAvailableRenderId();
		renderIdFactoryGlassPane = RenderingRegistry.getNextAvailableRenderId();
		renderIdFluidTank = RenderingRegistry.getNextAvailableRenderId();
		renderIdFluidClassic = RenderingRegistry.getNextAvailableRenderId();
		renderIdRedNetLogic = RenderingRegistry.getNextAvailableRenderId();
		renderIdVineScaffold = RenderingRegistry.getNextAvailableRenderId();
		renderIdFactoryGlass = RenderingRegistry.getNextAvailableRenderId();
		renderIdDetCord = RenderingRegistry.getNextAvailableRenderId();
		renderIdRedNet = RenderingRegistry.getNextAvailableRenderId();
		renderIdPPipe = RenderingRegistry.getNextAvailableRenderId();

		// Blocks
		RenderingRegistry.registerBlockHandler(renderIdConveyor,
			new ConveyorRenderer());
		RenderingRegistry.registerBlockHandler(renderIdFactoryGlassPane,
			new FactoryGlassPaneRenderer());
		BlockTankRenderer tankRender = new BlockTankRenderer();
		RenderingRegistry.registerBlockHandler(renderIdFluidTank, tankRender);
		*/
/*RenderingRegistry.registerBlockHandler(renderIdFluidClassic,
				new RenderBlockFluidClassic(renderIdFluidClassic));/*/
/*//*

		RenderingRegistry.registerBlockHandler(renderIdVineScaffold,
			new VineScaffoldRenderer());
		RenderingRegistry.registerBlockHandler(renderIdFactoryGlass,
			new FactoryGlassRenderer());
		RenderingRegistry.registerBlockHandler(renderIdDetCord,
			new DetCordRenderer());
		RedNetCableRenderer cableRenderer = new RedNetCableRenderer();
		RenderingRegistry.registerBlockHandler(renderIdRedNet, cableRenderer);
		RenderingRegistry.registerBlockHandler(renderIdPPipe,
			new PlasticPipeRenderer());
		RenderingRegistry.registerBlockHandler(renderIdRedNetLogic,
			new RedNetLogicRenderer());

		// TODO: convert card renderer and remove this
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedNetLogic.class,
			new powercrystals.minefactoryreloaded.render.tileentity.RedNetLogicRenderer());

		// Items
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(conveyorBlock),
			new ConveyorItemRenderer());

		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(factoryGlassPaneBlock),
			new FactoryGlassPaneItemRenderer());

		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(plasticTank), tankRender);

		MinecraftForgeClient.registerItemRenderer(logicCardItem, new RedNetCardItemRenderer());
		MinecraftForgeClient.registerItemRenderer(needlegunItem, new NeedleGunItemRenderer());
		MinecraftForgeClient.registerItemRenderer(rocketItem, new RocketItemRenderer());
		MinecraftForgeClient.registerItemRenderer(rocketLauncherItem, new RocketLauncherItemRenderer());
		MinecraftForgeClient.registerItemRenderer(potatoLauncherItem, new PotatoLauncherItemRenderer());

		RenderFluidOverlayItem fluidRender = new RenderFluidOverlayItem();
		MinecraftForgeClient.registerItemRenderer(plasticCupItem, fluidRender);
		MinecraftForgeClient.registerItemRenderer(sewageBucketItem, fluidRender);
		MinecraftForgeClient.registerItemRenderer(sludgeBucketItem, fluidRender);
		MinecraftForgeClient.registerItemRenderer(mobEssenceBucketItem, fluidRender);
		MinecraftForgeClient.registerItemRenderer(bioFuelBucketItem, fluidRender);
		MinecraftForgeClient.registerItemRenderer(meatBucketItem, fluidRender);
		MinecraftForgeClient.registerItemRenderer(pinkSlimeBucketItem, fluidRender);
		MinecraftForgeClient.registerItemRenderer(chocolateMilkBucketItem, fluidRender);
		MinecraftForgeClient.registerItemRenderer(mushroomSoupBucketItem, fluidRender);
		if (syringeEmptyItem instanceof IFluidContainerItem)
			MinecraftForgeClient.registerItemRenderer(syringeEmptyItem,
				new RenderFluidOverlayItem(false));
		//MinecraftForgeClient.registerItemRenderer(MineFactoryReloadedCore.plasticCellItem.itemID,
		//		new FactoryFluidOverlayRenderer());

		// TileEntities
		RedNetHistorianRenderer panelRenderer = new RedNetHistorianRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedNetHistorian.class, panelRenderer);
		RenderingRegistry.registerBlockHandler(panelRenderer);

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedNetCable.class, cableRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRedNetEnergy.class, cableRenderer);

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserDrill.class, new LaserDrillRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLaserDrillPrecharger.class,
			new LaserDrillPrechargerRenderer());

		// Entities
		RenderingRegistry.registerEntityRenderingHandler(DebugTracker.class, new EntityDebugTrackerRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntitySafariNet.class, new EntitySafariNetRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityPinkSlime.class,
			new EntityPinkSlimeRenderer(new ModelSlime(16), new ModelSlime(0), 0.25F));
		RenderingRegistry.registerEntityRenderingHandler(EntityNeedle.class, new EntityNeedleRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityRocket.class, new EntityRocketRenderer());
		RenderingRegistry.registerEntityRenderingHandler(EntityFishingRod.class,
			new RenderSnowball(fishingRodItem));
*/

		MinecraftForge.EVENT_BUS.register(instance);
		gl14 = GLContext.getCapabilities().OpenGL14; //TODO what is this used for? doesn't seem to have anything referring to it

		IReloadableResourceManager manager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
		manager.registerReloadListener(instance);
	}

	@Override
	public void onResourceManagerReload(IResourceManager p_110549_1_) {

/* TODO fix rendering
		NeedleGunItemRenderer.updateModel();
*/
	}

	@SubscribeEvent
	public void onPreTextureStitch(TextureStitchEvent.Pre e) {

		registerFluidSprites(e.getMap(), milkLiquid.getFluid());
		registerFluidSprites(e.getMap(), sludgeLiquid.getFluid());
		registerFluidSprites(e.getMap(), sewageLiquid.getFluid());
		registerFluidSprites(e.getMap(), essenceLiquid.getFluid());
		registerFluidSprites(e.getMap(), biofuelLiquid.getFluid());
		registerFluidSprites(e.getMap(), meatLiquid.getFluid());
		registerFluidSprites(e.getMap(), pinkSlimeLiquid.getFluid());
		registerFluidSprites(e.getMap(), chocolateMilkLiquid.getFluid());
		registerFluidSprites(e.getMap(), mushroomSoupLiquid.getFluid());
		registerFluidSprites(e.getMap(), steamFluid.getFluid());
		
/* TODO add code to gen GUI background
		SlotAcceptReusableSafariNet.background = e.map.registerIcon("minefactoryreloaded:gui/reusablenet");
		ContainerAutoDisenchanter.background = e.map.registerIcon("minefactoryreloaded:gui/book");
		ContainerAutoJukebox.background = e.map.registerIcon("minefactoryreloaded:gui/record");
		SlotAcceptLaserFocus.background = e.map.registerIcon("minefactoryreloaded:gui/laserfocus");
		ContainerAutoBrewer.ingredient = e.map.registerIcon("minefactoryreloaded:gui/netherwart");
		ContainerAutoBrewer.bottle = e.map.registerIcon("minefactoryreloaded:gui/bottle");
		ContainerFisher.background = e.map.registerIcon("minefactoryreloaded:gui/fishingrod");
*/
	}

	private void registerFluidSprites(TextureMap textureMap, Fluid fluid) {
		if (fluid != null) {
			textureMap.registerSprite(fluid.getStill());
			textureMap.registerSprite(fluid.getFlowing());
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onPostTextureStitch(TextureStitchEvent.Post e) {

/* TODO add proper liquid registration
		setIcons("milk", MFRThings.milkLiquid);
		setIcons("sludge", MFRThings.sludgeLiquid);
		setIcons("sewage", MFRThings.sewageLiquid);
		setIcons("mob_essence", MFRThings.essenceLiquid);
		setIcons("biofuel", MFRThings.biofuelLiquid);
		setIcons("meat", MFRThings.meatLiquid);
		setIcons("pinkslime", MFRThings.pinkSlimeLiquid);
		setIcons("chocolatemilk", MFRThings.chocolateMilkLiquid);
		setIcons("mushroomsoup", MFRThings.mushroomSoupLiquid);
		setIcons("steam", MFRThings.steamFluid);
*/
	}

	@SubscribeEvent
	public void onPlayerChangedDimension(Unload world) {

		if (world.getWorld().provider == null ||
				Minecraft.getMinecraft().thePlayer == null ||
				Minecraft.getMinecraft().thePlayer.worldObj == null ||
				Minecraft.getMinecraft().thePlayer.worldObj.provider == null) {
			return;
		}
		if (world.getWorld().provider.getDimension() == Minecraft.getMinecraft().thePlayer.worldObj.provider.getDimension()) {
			_areaTileEntities.clear();
			prcPages.clear();
		}
	}

	@SubscribeEvent
	public void tickStart(PlayerTickEvent evt) {

		if (evt.side != Side.CLIENT | evt.phase != Phase.START)
			return;

		EntityPlayer player = evt.player;
		ItemStack equipped = player.inventory.getCurrentItem();
		if (equipped != null && equipped.getItem() instanceof ItemRocketLauncher) {
			Entity e = rayTrace();
			if (_lastEntityOver != null && _lastEntityOver.isDead) {
				_lastEntityOver = null;
				_lockonTicks = 0;
			} else if ((e == null || e != _lastEntityOver) && _lockonLostTicks > 0) {
				_lockonLostTicks--;
			} else if (e == null && _lockonLostTicks == 0) {
				if (_lockonTicks > 0) {
					_lockonTicks--;
				}
				_lastEntityOver = null;
			} else if (_lastEntityOver == null) {
				_lastEntityOver = e;
			} else if (_lockonTicks < _lockonMax) {
				_lockonTicks++;
				if (_lockonTicks >= _lockonMax) {
					_lockonLostTicks = _lockonLostMax;
				}
			} else if (e != null && e == _lastEntityOver) {
				_lockonLostTicks = _lockonLostMax;
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void tickEnd(RenderTickEvent evt) {

		if (evt.phase != Phase.END)
			return;
		renderHUD(evt.renderTickTime);
		// this solves a bug where render pass 0 textures have alpha forced by
		// minecraft's fog on small and tiny render distances
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private void renderHUD(float partialTicks) {

		Minecraft mc = Minecraft.getMinecraft();
		if (!mc.isGamePaused() && mc.currentScreen == null && mc.thePlayer != null &&
				mc.thePlayer.inventory.getCurrentItem() != null
				&& mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemRocketLauncher) {
			ScaledResolution sr = new ScaledResolution(mc);
			Point center = new Point(sr.getScaledWidth() / 2, sr.getScaledHeight() / 2);

			if (MineFactoryReloadedClient.instance.getLockedEntity() != Integer.MIN_VALUE) {
				mc.renderEngine.bindTexture(targetingBlue);
			} else {
				mc.renderEngine.bindTexture(targetingRed);
			}

			GL11.glPushMatrix();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glTranslatef(center.getX(), center.getY(), 0);
			GL11.glRotatef(((mc.theWorld.getTotalWorldTime() & 511) * 4) % 360 + partialTicks, 0, 0, 1);

			float distance = MineFactoryReloadedClient.instance.getLockTimeRemaining();

			drawLockonPart(center, distance, 0);
			drawLockonPart(center, distance, 90);
			drawLockonPart(center, distance, 180);
			drawLockonPart(center, distance, 270);

			GL11.glPopMatrix();
		}
	}

	private void drawLockonPart(Point center, float distanceFromCenter, int rotation) {

		GL11.glPushMatrix();

		GL11.glRotatef(rotation, 0, 0, 1);
		GL11.glTranslatef(-8, -13, 0);
		GL11.glTranslatef(0, -distanceFromCenter, 0);

		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2i(0, 0);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2i(0, 16);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2i(16, 16);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2i(16, 0);
		GL11.glEnd();

		GL11.glPopMatrix();
	}

	@SubscribeEvent
	public void setArmorModel(SetArmorModel e) {

/* TODO fix armor model
		ItemStack itemstack = e.getStack();

		if (itemstack != null) {
			Item item = itemstack.getItem();
			int par2 = 3 - e.getSlot();
			//if (item.isValidArmor(itemstack, e.slot, e.entity))
			if (item == plasticCupItem) {
				Minecraft.getMinecraft().renderEngine.
						bindTexture(new ResourceLocation(item.getArmorTexture(itemstack, e.getEntity(), par2, null)));
				ModelBiped modelbiped = new ModelBiped(1.0F);
				modelbiped.bipedHead.showModel = par2 == 0;
				modelbiped.bipedHeadwear.showModel = par2 == 0;
				modelbiped.bipedBody.showModel = par2 == 1 || par2 == 2;
				modelbiped.bipedRightArm.showModel = par2 == 1;
				modelbiped.bipedLeftArm.showModel = par2 == 1;
				modelbiped.bipedRightLeg.showModel = par2 == 2 || par2 == 3;
				modelbiped.bipedLeftLeg.showModel = par2 == 2 || par2 == 3;
				e.getRenderer().setRenderPassModel(modelbiped);
				modelbiped.onGround = e.entityLiving.getSwingProgress(e.partialRenderTick);
				modelbiped.isRiding = e.entity.isRiding();
				modelbiped.isChild = e.entityLiving.isChild();
				float f1 = 1.0F;
				GL11.glColor3f(f1, f1, f1);

				if (itemstack.isItemEnchanted()) {
					e.result = 15;
					return;
				}

				e.result = 1;
			}
		}
*/
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	// first to render, so everything else is overlayed
	public void renderWorldLast(RenderWorldLastEvent e) {

		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
		if (player.inventory.getCurrentItem() == null ||
				!player.inventory.getCurrentItem().getItem().equals(factoryHammerItem)) {
			return;
		}

		float playerOffsetX = -(float) (player.lastTickPosX + (player.posX - player.lastTickPosX) * e.getPartialTicks());
		float playerOffsetY = -(float) (player.lastTickPosY + (player.posY - player.lastTickPosY) * e.getPartialTicks());
		float playerOffsetZ = -(float) (player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * e.getPartialTicks());

		GL11.glColorMask(true, true, true, true);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_FOG);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);

		for (IHarvestAreaContainer c : _areaTileEntities) {
			if (((TileEntity) c).isInvalid())
				continue;

			float r = colorFromCoord(c.getHAM().getOriginX(), 0xF8525888);
			float g = colorFromCoord(c.getHAM().getOriginY(), 0x85BDBD8C);
			float b = colorFromCoord(c.getHAM().getOriginZ(), 0x997696BF);

			GL11.glPushMatrix();
			GL11.glColor4f(r, g, b, 0.4F);
			GL11.glTranslatef(playerOffsetX, playerOffsetY, playerOffsetZ);
			renderAABB(c.getHAM().getHarvestArea().toAxisAlignedBB());
			GL11.glPopMatrix();
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	private float colorFromCoord(int c, long h) {

		h = (h * c) + 0xBA;
		h ^= (h >>> 20) ^ (h >>> 12);
		h ^= (h >>> 7) ^ (h >>> 4);
		return ((h & 255) / 319f) + 0.1f; // odd values bound to 0.1 <= x < 0.9
	}

	public static void addTileToAreaList(IHarvestAreaContainer tile) {

		_areaTileEntities.add(tile);
	}

	public static void removeTileFromAreaList(IHarvestAreaContainer tile) {

		_areaTileEntities.remove(tile);
	}

	public int getLockedEntity() {

		if (_lastEntityOver != null && _lockonTicks >= _lockonMax) {
			return _lastEntityOver.getEntityId();
		}

		return Integer.MIN_VALUE;
	}

	public int getLockTimeRemaining() {

		if (_lastEntityOver != null && _lockonTicks >= _lockonMax) {
			return _lockonLostMax - _lockonLostTicks;
		} else {
			return (_lockonMax - _lockonTicks) * 2;
		}
	}

	private Entity rayTrace() {

		if (Minecraft.getMinecraft().getRenderViewEntity() == null || Minecraft.getMinecraft().theWorld == null) {
			return null;
		}

		double range = 64;
		Vec3d playerPos = new Vec3d(Minecraft.getMinecraft().getRenderViewEntity().getPosition());

		Vec3d playerLook = Minecraft.getMinecraft().getRenderViewEntity().getLook(1.0F);
		Vec3d playerLookRel = playerPos.addVector(playerLook.xCoord * range, playerLook.yCoord * range, playerLook.zCoord * range);
		List<?> list = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABBExcludingEntity(
			Minecraft.getMinecraft().getRenderViewEntity(),
			Minecraft.getMinecraft().getRenderViewEntity().getEntityBoundingBox().addCoord(playerLook.xCoord * range, playerLook.yCoord * range,
				playerLook.zCoord * range)
					.expand(1, 1, 1));

		double entityDistTotal = range;
		Entity pointedEntity = null;
		for (int i = 0; i < list.size(); ++i) {
			Entity entity = (Entity) list.get(i);

			if (entity.canBeCollidedWith()) {
				double entitySize = entity.getCollisionBorderSize();
				AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(entitySize, entitySize, entitySize);
				RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(playerPos, playerLookRel);

				if (axisalignedbb.isVecInside(playerPos)) {
					if (0.0D < entityDistTotal || entityDistTotal == 0.0D) {
						pointedEntity = entity;
						entityDistTotal = 0.0D;
					}
				} else if (movingobjectposition != null) {
					double entityDist = playerPos.distanceTo(movingobjectposition.hitVec);

					if (entityDist < entityDistTotal || entityDistTotal == 0.0D) {
						pointedEntity = entity;
						entityDistTotal = entityDist;
					}
				}
			}
		}

		if (pointedEntity != null) {
			return pointedEntity;
		}
		return null;
	}

	public static void renderAABB(AxisAlignedBB par0AxisAlignedBB) {

		double eps = 0.006;

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buffer = tessellator.getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION);
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();

		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();

		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();

		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();

		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.minX + eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();

		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.minZ + eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.maxY - eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		buffer.pos(par0AxisAlignedBB.maxX - eps, par0AxisAlignedBB.minY + eps, par0AxisAlignedBB.maxZ - eps).endVertex();
		tessellator.draw();
	}

}
