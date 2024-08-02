package com.nongdajun.mcpi3.mixin.client;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.*;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("itemUseCooldown")
    int getItemUseCooldown();

    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int itemUseCooldown);

    @Invoker("doAttack")
    boolean doAttack();

    @Invoker("doItemUse")
    void doItemUse();

    @Invoker("doItemPick")
    void doItemPick();

}
