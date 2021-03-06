package mrtjp.projectred.items;

import mrtjp.projectred.ProjectRed;
import mrtjp.projectred.crafting.ProjectRedTabs;
import mrtjp.projectred.multipart.wiring.gates.TileGate;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class ItemScrewdriver extends Item {
	public ItemScrewdriver(int id) {
		super(id);
		setUnlocalizedName("projectred:screwdriver");
		setMaxStackSize(1);
		setMaxDamage(64);
		setNoRepair();
		setCreativeTab(ProjectRedTabs.tabTools);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World w, int x, int y, int z, int side, float par8, float par9, float par10) {
		if (w.getBlockId(x, y, z) == ProjectRed.blockGate.blockID) {
			if (w.isRemote) {
				return true;
			}
			TileGate te = (TileGate) w.getBlockTileEntity(x, y, z);
			if (player.isSneaking()) {
				te.configure();
			} else {
				te.rotate();
			}
			ItemStack held = player.getHeldItem();
			if (held.itemID == ProjectRed.itemScrewdriver.itemID) {
				held.damageItem(1, player);
			}
			return true;
		}

		return false;
	}
}
