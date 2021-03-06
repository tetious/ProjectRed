package mrtjp.projectred.multipart.wiring.wires;

import java.util.List;

import mrtjp.projectred.crafting.ProjectRedTabs;
import mrtjp.projectred.multipart.BlockMultipartBase;
import mrtjp.projectred.multipart.wiring.CommandDebug;
import mrtjp.projectred.multipart.wiring.InvalidTile;
import mrtjp.projectred.renderstuffs.RenderIDs;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockWire extends BlockMultipartBase {

	public BlockWire(int id) {
		super(id, new MaterialLogic(Material.circuits.materialMapColor));
		setCreativeTab(ProjectRedTabs.tabWires);
		setUnlocalizedName("projred.redwire");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister reg) {
		for (EnumWire wireType : EnumWire.VALUES) {
			wireType.loadTextures(reg, wireType.textureName, wireType.texNameSuffix);
		}
	}

	@Override
	public int wrappedGetRenderType() {
		return RenderIDs.renderIdRedwire;
	}

	@Override
	public void getSubBlocks(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		for (EnumWire type : EnumWire.VALUES) {
			par3List.add(new ItemStack(this, 1, type.ordinal()));
		}

		for (EnumWire type : EnumWire.VALUES) {
			if (type.hasJacketedForm()) {
				par3List.add(new ItemStack(this, 1, type.ordinal() | WireDamageValues.DMG_FLAG_JACKETED));
			}
		}
	}

	@Override
	public TileEntity createTileEntity(World world, int meta) {
		Class<? extends TileWire> clazz = EnumWire.META_TO_CLASS.get(meta);

		if (clazz == null) {
			return new InvalidTile();
		}

		try {
			return clazz.getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world) {
		return null;
	}

	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, int par5) {
		((TileWire) par1World.getBlockTileEntity(par2, par3, par4)).onNeighbourBlockChange();
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess w, int x, int y, int z, int opposite_dir) {
		int meta = w.getBlockMetadata(x, y, z);

		if (meta == EnumWire.PLAIN_RED_ALLOY_META) {
			TileRedAlloy tile = ((TileRedAlloy) w.getBlockTileEntity(x, y, z));

			return tile.canProvideStrongPowerInDirection(opposite_dir ^ 1) ? tile.getVanillaRedstoneStrength() : 0;
		}

		return 0;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess w, int x, int y, int z, int opposite_dir) {
		int meta = w.getBlockMetadata(x, y, z);

		if (meta == EnumWire.PLAIN_RED_ALLOY_META || meta == EnumWire.INSULATED_RED_ALLOY_META) {
			TileRedAlloy tile = ((TileRedAlloy) w.getBlockTileEntity(x, y, z));

			return tile.canProvideWeakPowerInDirection(opposite_dir ^ 1) ? tile.getVanillaRedstoneStrength() : 0;
		}

		return 0;
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean onBlockActivated(World par1World, int par2, int par3, int par4, EntityPlayer par5EntityPlayer, int par6, float par7, float par8, float par9) {
		if (CommandDebug.WIRE_READING)
			return ((TileWire) par1World.getBlockTileEntity(par2, par3, par4)).debug(par5EntityPlayer);
		return super.onBlockActivated(par1World, par2, par3, par4, par5EntityPlayer, par6, par7, par8, par9);
	}

	@SideOnly(Side.CLIENT)
	public boolean addBlockHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addBlockDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		TileEntity te = world.getBlockTileEntity(x, y, z);

		EnumWire wireType;

		if (te instanceof TileWire)
			wireType = ((TileWire) te).getType();
		else
			return true; // suppress particles

		byte b0 = 4;

		int col = wireType.itemColour;
		final float red = ((col >> 16) & 0xFF) / 255.0f;
		final float green = ((col >> 8) & 0xFF) / 255.0f;
		final float blue = (col & 0xFF) / 255.0f;

		for (int j1 = 0; j1 < b0; ++j1) {
			for (int k1 = 0; k1 < b0; ++k1) {
				for (int l1 = 0; l1 < b0; ++l1) {
					double d0 = (double) x + ((double) j1 + 0.5D) / (double) b0;
					double d1 = (double) y + ((double) k1 + 0.5D) / (double) b0;
					double d2 = (double) z + ((double) l1 + 0.5D) / (double) b0;
					int i2 = world.rand.nextInt(6);

					EntityDiggingFX particle = new EntityDiggingFX(world, d0, d1, d2, d0 - (double) x - 0.5D, d1 - (double) y - 0.5D, d2 - (double) z - 0.5D, this, i2, meta, Minecraft.getMinecraft().renderEngine) {
						{
							particleRed = red;
							particleGreen = green;
							particleBlue = blue;
						}
					};
					particle.setParticleIcon(Minecraft.getMinecraft().renderEngine, wireType.texture_cross);
					effectRenderer.addEffect(particle);
				}
			}
		}

		return true; // suppress default effect
	}

}
