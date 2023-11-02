package org.bukkit.craftbukkit.v1_20_R2.inventory;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmokingRecipe;

public class CraftSmokingRecipe extends SmokingRecipe implements CraftRecipe {

    public CraftSmokingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) {
        super(key, result, source, experience, cookingTime);
    }

    public static CraftSmokingRecipe fromBukkitRecipe(SmokingRecipe recipe) {
        if (recipe instanceof CraftSmokingRecipe) {
            return (CraftSmokingRecipe) recipe;
        } else {
            CraftSmokingRecipe ret = new CraftSmokingRecipe(recipe.getKey(), recipe.getResult(), recipe.getInputChoice(), recipe.getExperience(), recipe.getCookingTime());

            ret.setGroup(recipe.getGroup());
            ret.setCategory(recipe.getCategory());
            return ret;
        }
    }

    public void addToCraftingManager() {
        ItemStack result = this.getResult();

        MinecraftServer.getServer().getRecipeManager().addRecipe(new RecipeHolder(CraftNamespacedKey.toMinecraft(this.getKey()), new net.minecraft.world.item.crafting.SmokingRecipe(this.getGroup(), CraftRecipe.getCategory(this.getCategory()), this.toNMS(this.getInputChoice(), true), CraftItemStack.asNMSCopy(result), this.getExperience(), this.getCookingTime())));
    }
}