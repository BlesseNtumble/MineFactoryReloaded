package powercrystals.minefactoryreloaded.tile.machine;

import cofh.core.util.CoreUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemRecord;
import net.minecraft.util.EnumFacing;

import powercrystals.minefactoryreloaded.core.MFRUtil;
import powercrystals.minefactoryreloaded.gui.client.GuiAutoJukebox;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.container.ContainerAutoJukebox;
import powercrystals.minefactoryreloaded.setup.MFRThings;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryInventory;

public class TileEntityAutoJukebox extends TileEntityFactoryInventory {

	private boolean _lastRedstoneState;
	private boolean _canCopy;
	private boolean _canPlay;

	public TileEntityAutoJukebox() {

		super(Machine.AutoJukebox);
		setManageSolids(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer inventoryPlayer) {

		return new GuiAutoJukebox(getContainer(inventoryPlayer), this);
	}

	@Override
	public ContainerAutoJukebox getContainer(InventoryPlayer inventoryPlayer) {

		return new ContainerAutoJukebox(this, inventoryPlayer);
	}

	@SideOnly(Side.CLIENT)
	public void setCanCopy(boolean canCopy) {

		_canCopy = canCopy;
	}

	public boolean getCanCopy() {

		if (worldObj.isRemote) {
			return _canCopy;
		} else if (_inventory[0] != null && _inventory[0].getItem() instanceof ItemRecord && _inventory[1] != null &&
				_inventory[1].getItem().equals(MFRThings.blankRecordItem)) {
			return true;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	public void setCanPlay(boolean canPlay) {

		_canPlay = canPlay;
	}

	public boolean getCanPlay() {

		if (worldObj.isRemote) {
			return _canPlay;
		} else if (_inventory[0] != null && _inventory[0].getItem() instanceof ItemRecord) {
			return true;
		}
		return false;
	}

	public void copyRecord() {

		if (!worldObj.isRemote && getCanCopy()) {
			_inventory[1] = _inventory[0].copy();
		}
	}

	public void playRecord() {

		if (_inventory[0] != null && _inventory[0].getItem() instanceof ItemRecord)
			worldObj.playEvent(1010, pos, Item.getIdFromItem(_inventory[0].getItem()));
		MFRUtil.notifyBlockUpdate(worldObj, pos);
	}

	public void stopRecord() {

		worldObj.playEvent(1010, pos, 0);
		MFRUtil.notifyBlockUpdate(worldObj, pos);
	}

	@Override
	public void onBlockBroken() {

		stopRecord();
		super.onBlockBroken();
	}

	@Override
	public int getSizeInventory() {

		return 2;
	}

	@Override
	public void update() {

		super.update();

		if (worldObj.isRemote) {
			return;
		}

		boolean redstoneState = _rednetState != 0 || CoreUtils.isRedstonePowered(this);
		if (redstoneState && !_lastRedstoneState) {
			stopRecord();
			playRecord();
		}

		_lastRedstoneState = redstoneState;
	}

	@Override
	public int getSizeInventorySide(EnumFacing side) {

		return 1;
	}

}
