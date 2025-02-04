package vazkii.botania.forge.mixin;

import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import vazkii.botania.common.crafting.RecipeSerializerBase;

/**
 * [SelfMixin]
 * Cursed self-mixin to create a manual bridge method.
 */
@Mixin(value = RecipeSerializerBase.class, remap = false)
public class RecipeSerializerBaseForgeMixin {
	@Shadow
	@Nullable
	private ResourceLocation registryName;

	public Object setRegistryName(ResourceLocation name) {
		registryName = name;
		return this;
	}
}
