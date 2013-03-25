package powercrystals.minefactoryreloaded.net;

import java.io.File;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommonProxy implements IMFRProxy
{
	@Override
	public void preInit(File configFile)
	{
	}

	@Override
	public void init()
	{
	}
	
	@Override
	public void movePlayerToCoordinates(EntityPlayer e, double x, double y, double z)
	{
		if (e instanceof EntityPlayerMP)
		{
			((EntityPlayerMP)e).playerNetServerHandler.setPlayerLocation(x, y, z, e.cameraYaw, e.cameraPitch);
		}
	}
}
