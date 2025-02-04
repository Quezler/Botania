/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei.orechid;

import com.google.common.collect.ListMultimap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.OrechidRecipe;
import vazkii.botania.common.handler.OrechidManager;

import java.util.List;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public abstract class OrechidRecipeCategoryBase<T extends OrechidRecipe> implements IRecipeCategory<T> {

	private final IDrawableStatic background;
	private final Component localizedName;
	private final IDrawableStatic overlay;
	private final IDrawable icon;
	private final ItemStack iconStack;

	public OrechidRecipeCategoryBase(IGuiHelper guiHelper, ItemStack iconStack, Component localizedName) {
		overlay = guiHelper.createDrawable(prefix("textures/gui/pure_daisy_overlay.png"),
				0, 0, 64, 44);
		background = guiHelper.createBlankDrawable(96, 44);
		this.localizedName = localizedName;
		this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, iconStack);
		this.iconStack = iconStack;
	}

	@NotNull
	@Override
	public Component getTitle() {
		return localizedName;
	}

	@NotNull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@NotNull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	public static float getTotalOreWeight(List<? extends OrechidRecipe> weights, int myWeight) {
		return weights.stream()
				.map(OrechidRecipe::getWeight)
				.reduce(Integer::sum).orElse(myWeight * 64 * 64);
	}

	protected abstract RecipeType<T> recipeType();

	protected List<? extends OrechidRecipe> getOreWeights(Block input) {
		ListMultimap<Block, ? extends OrechidRecipe> multimap = OrechidManager.getFor(Minecraft.getInstance().level.getRecipeManager(), recipeType());
		return multimap.get(input);
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull OrechidRecipe recipe, @NotNull IFocusGroup focusGroup) {
		builder.addSlot(RecipeIngredientRole.INPUT, 9, 12).addItemStack(new ItemStack(recipe.getInput(), 64));
		builder.addSlot(RecipeIngredientRole.CATALYST, 39, 12).addItemStack(iconStack);

		final int myWeight = recipe.getWeight();
		final int amount = Math.max(1, Math.round((float) myWeight * 64 / getTotalOreWeight(getOreWeights(recipe.getInput()), myWeight)));

		// Shouldn't ever return an empty list since the ore weight
		// list is filtered to only have ores with ItemBlocks
		List<ItemStack> stackList = recipe.getOutput().getDisplayedStacks();
		stackList.forEach(s -> s.setCount(amount));

		builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 12)
				.addItemStacks(stackList)
				.addTooltipCallback((view, tooltip) -> tooltip.addAll(recipe.getOutput().descriptionTooltip()));
	}

	@Override
	public void draw(@NotNull OrechidRecipe recipe, @NotNull IRecipeSlotsView view, @NotNull PoseStack ms, double mouseX, double mouseY) {
		RenderSystem.enableBlend();
		overlay.draw(ms, 17, 0);
		RenderSystem.disableBlend();
	}

}
