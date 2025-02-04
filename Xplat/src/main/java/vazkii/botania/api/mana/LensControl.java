/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.mana;

import net.minecraft.world.item.ItemStack;

/**
 * An extension of Lens that allows for the lens item to control the
 * spreader's behaviour.
 */
public interface LensControl extends Lens {

	boolean isControlLens(ItemStack stack);

	boolean allowBurstShooting(ItemStack stack, ManaSpreader spreader, boolean redstone);

	/**
	 * Used for the tick of a non-redstone spreader.
	 */
	void onControlledSpreaderTick(ItemStack stack, ManaSpreader spreader, boolean redstone);

	/**
	 * Used for when a redstone spreader gets a pulse.
	 */
	void onControlledSpreaderPulse(ItemStack stack, ManaSpreader spreader);

}
