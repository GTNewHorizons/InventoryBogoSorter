package com.cleanroommc.bogosorter.mixins.early.minecraft;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.cleanroommc.bogosorter.mixins.interfaces.NBTTagCompoundExt;

@Mixin(NBTTagCompound.class)
public class NBTTagCompoundMixin implements NBTTagCompoundExt {

    @Shadow
    public Map tagMap;

    @Override
    public int bogo$getSize() {
        return tagMap.size();
    }
}
