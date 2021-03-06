package mods.railcraft.common.plugins.jei.crafting;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 *
 */
public final class FluidRecipeInterpreter {

    static IStackHelper helper;
    static IIngredientRegistry ingredientRegistry;

    private FluidRecipeInterpreter() {
    }

    public static void init(IStackHelper helper, IIngredientRegistry registry) {
        FluidRecipeInterpreter.helper = helper;
        FluidRecipeInterpreter.ingredientRegistry = registry;
    }

    public static List<List<ItemStack>> expand(List<Object> inputs) {
        List<List<ItemStack>> ret = new ArrayList<>(inputs.size());
        for (Object input : inputs) {
            ret.add(getPossibilities(input));
        }
        return ret;
    }

    public static List<ItemStack> getPossibilities(Object input) {
        if (input instanceof ItemStack) {
            ItemStack stack = (ItemStack) input;
            if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
                return helper.getSubtypes(stack);
            }
            return Collections.singletonList(stack);
        } else if (input instanceof String) {
            return OreDictionary.getOres((String) input);
        } else if (input instanceof Iterable) {
            return StreamSupport.stream(((Iterable<?>) input).spliterator(), false).flatMap((obj) -> getPossibilities(obj).stream()).collect(Collectors.toList());
        } else if (input instanceof FluidStack) {
            return getAllContainersFilledWith((FluidStack) input);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("deprecation")
    public static List<ItemStack> getAllContainersFilledWith(@Nullable FluidStack fluid) {
        if (fluid == null)
            return Collections.emptyList();
        ImmutableList<ItemStack> ingredients = ingredientRegistry.getIngredients(ItemStack.class);
        List<ItemStack> ret = new ArrayList<>();

        for (ItemStack ingredient : ingredients) {
            ItemStack toTest = ingredient.copy();
            if (ingredient.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                IFluidHandler emptyCapability = toTest.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                FluidStack drain = emptyCapability.drain(fluid.amount, false);
                if (drain != null && drain.isFluidStackIdentical(fluid)) {
                    ret.add(toTest);
                }
            } else if (ingredient.getItem() instanceof IFluidContainerItem) {
                IFluidContainerItem container = (IFluidContainerItem) ingredient.getItem();
                FluidStack drain = container.drain(toTest, fluid.amount, false);
                if (drain != null && drain.isFluidStackIdentical(fluid)) {
                    ret.add(toTest);
                }
            }
        }
        return ret;
    }

}
