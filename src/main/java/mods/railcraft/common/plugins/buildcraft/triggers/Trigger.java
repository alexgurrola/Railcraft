package mods.railcraft.common.plugins.buildcraft.triggers;

import buildcraft.api.statements.IStatementParameter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class Trigger {

    public abstract boolean isTriggerActive(EnumFacing side, TileEntity tile, IStatementParameter[] parameter);
}
