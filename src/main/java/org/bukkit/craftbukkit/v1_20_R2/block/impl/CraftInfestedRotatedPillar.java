/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_20_R2.block.impl;

public final class CraftInfestedRotatedPillar extends org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData implements org.bukkit.block.data.Orientable {

    public CraftInfestedRotatedPillar() {
        super();
    }

    public CraftInfestedRotatedPillar(net.minecraft.world.level.block.state.IBlockData state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_20_R2.block.data.CraftOrientable

    private static final net.minecraft.world.level.block.state.properties.EnumProperty<?> AXIS = getEnum(net.minecraft.world.level.block.InfestedRotatedPillarBlock.class, "axis");

    @Override
    public org.bukkit.Axis getAxis() {
        return get(AXIS, org.bukkit.Axis.class);
    }

    @Override
    public void setAxis(org.bukkit.Axis axis) {
        set(AXIS, axis);
    }

    @Override
    public java.util.Set<org.bukkit.Axis> getAxes() {
        return getValues(AXIS, org.bukkit.Axis.class);
    }
}
