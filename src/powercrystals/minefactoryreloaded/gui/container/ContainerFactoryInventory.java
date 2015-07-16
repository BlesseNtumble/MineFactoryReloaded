package powercrystals.minefactoryreloaded.gui.container;

import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;

import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryInventory;

public class ContainerFactoryInventory extends Container
{
	protected TileEntityFactoryInventory _te;

	private int _tankAmount;
	private int _tankIndex;

	public ContainerFactoryInventory(TileEntityFactoryInventory tileentity, InventoryPlayer inv)
	{
		_te = tileentity;
		if(_te.getSizeInventory() > 0)
		{
			addSlots();
		}
		bindPlayerInventory(inv);
	}

	protected void addSlots()
	{
		addSlotToContainer(new Slot(_te, 0, 8, 15));
		addSlotToContainer(new Slot(_te, 1, 26, 15));
		addSlotToContainer(new Slot(_te, 2, 44, 15));
		addSlotToContainer(new Slot(_te, 3, 8, 33));
		addSlotToContainer(new Slot(_te, 4, 26, 33));
		addSlotToContainer(new Slot(_te, 5, 44, 33));
		addSlotToContainer(new Slot(_te, 6, 8, 51));
		addSlotToContainer(new Slot(_te, 7, 26, 51));
		addSlotToContainer(new Slot(_te, 8, 44, 51));
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();

		FluidTankInfo[] tank = _te.getTankInfo(ForgeDirection.UNKNOWN);
		int n = tank.length;
		if (n == 0)
			return;
		for(int i = 0; i < crafters.size(); i++)
		{
			for (int j = n; j --> 0; )
			{
				((ICrafting)crafters.get(i)).sendProgressBarUpdate(this, 30, j);
				if(tank[j] != null && tank[j].fluid != null)
				{
					((ICrafting)crafters.get(i)).sendProgressBarUpdate(this, 31, tank[j].fluid.amount);
					((ICrafting)crafters.get(i)).sendProgressBarUpdate(this, 32, tank[j].fluid.getFluid().getID());
				}
				else if(tank[j] != null)
				{
					((ICrafting)crafters.get(i)).sendProgressBarUpdate(this, 31, 0);
					((ICrafting)crafters.get(i)).sendProgressBarUpdate(this, 32, 0);
				}
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(int var, int value)
	{
		super.updateProgressBar(var, value);

		if (var == 30) _tankIndex = value;
		else if (var == 31) _tankAmount = value;
		else if (var == 32)
		{
			Fluid fluid = FluidRegistry.getFluid(value);
			if(fluid == null)
			{
				_te.getTanks()[_tankIndex].setFluid(null);
			}
			else
			{
				_te.getTanks()[_tankIndex].setFluid(new FluidStack(fluid, _tankAmount));
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return !_te.isInvalid() && _te.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);
		int machInvSize = _te.getSizeInventory();

		if(slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if(slot < machInvSize)
			{
				if(!mergeItemStack(stackInSlot, machInvSize, inventorySlots.size(), true))
				{
					return null;
				}
			}
			else if(!mergeItemStack(stackInSlot, 0, machInvSize, false))
			{
				return null;
			}

			if(stackInSlot.stackSize == 0)
			{
				slotObject.putStack(null);
			}
			else
			{
				slotObject.onSlotChanged();
			}

			if(stackInSlot.stackSize == stack.stackSize)
			{
				return null;
			}

			slotObject.onPickupFromSlot(player, stackInSlot);
		}

		return stack;
	}

	protected int getPlayerInventoryVerticalOffset()
	{
		return 84;
	}

	protected int getPlayerInventoryHorizontalOffset()
	{
		return 8;
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		int yOff = getPlayerInventoryVerticalOffset();
		int xOff = getPlayerInventoryHorizontalOffset();
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, xOff + j * 18, yOff + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, xOff + i * 18, yOff + 58));
		}
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int slotStart, int slotRange, boolean reverse)
	{
		boolean successful = false;
		int slotIndex = !reverse ? slotStart : slotRange - 1;
		int iterOrder = !reverse ? 1 : -1;

		Slot slot;
		ItemStack existingStack;

		if (stack.isStackable())
		{
			while (stack.stackSize > 0 && (!reverse && slotIndex < slotRange || reverse && slotIndex >= slotStart))
			{
				slot = (Slot)this.inventorySlots.get(slotIndex);
				existingStack = slot.getStack();

				if (existingStack != null) {
					int maxStack = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int rmv = Math.min(maxStack, stack.stackSize);

					if (slot.isItemValid(ItemHelper.cloneStack(stack, rmv)) &&
							ItemHelper.itemsEqualWithMetadata(existingStack, stack, true))
					{
						int existingSize = existingStack.stackSize + stack.stackSize;

						if (existingSize <= maxStack)
						{
							stack.stackSize -= rmv;
							existingStack.stackSize = existingSize;
							slot.onSlotChanged();
							successful = true;
						}
						else if (existingStack.stackSize < maxStack)
						{
							stack.stackSize -= maxStack - existingStack.stackSize;
							existingStack.stackSize = maxStack;
							slot.onSlotChanged();
							successful = true;
						}
					}
				}

				slotIndex += iterOrder;
			}
		}

		if (stack.stackSize > 0)
		{
			slotIndex = !reverse ? slotStart : slotRange - 1;

			while (stack.stackSize > 0 && (!reverse && slotIndex < slotRange || reverse && slotIndex >= slotStart))
			{
				slot = (Slot)this.inventorySlots.get(slotIndex);
				existingStack = slot.getStack();


				if (existingStack == null) {
					int maxStack = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());
					int rmv = Math.min(maxStack, stack.stackSize);

					if (slot.isItemValid(ItemHelper.cloneStack(stack, rmv)))
					{
						existingStack = stack.splitStack(rmv);
						slot.putStack(existingStack);
						slot.onSlotChanged();
						successful = true;
					}
				}

				slotIndex += iterOrder;
			}
		}

		return successful;
	}
}
