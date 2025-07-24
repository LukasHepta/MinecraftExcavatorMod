package com.lk.excavator;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;
import java.util.Queue;

@Mod(modid = "excavator", version = "1.0")
public class ExcavatorMod {

    private final Minecraft mc = Minecraft.getMinecraft();
    private BlockPos pos1 = null;
    private BlockPos pos2 = null;
    private Queue<BlockPos> toBreak = new LinkedList<>();
    private boolean running = false;

    private final KeyBinding setPos1Key = new KeyBinding("Set Pos 1", Keyboard.KEY_P, "Excavator");
    private final KeyBinding setPos2Key = new KeyBinding("Set Pos 2", Keyboard.KEY_L, "Excavator");
    private final KeyBinding startMacroKey = new KeyBinding("Start Excavator", Keyboard.KEY_F6, "Excavator");

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        net.minecraftforge.fml.client.registry.ClientRegistry.registerKeyBinding(setPos1Key);
        net.minecraftforge.fml.client.registry.ClientRegistry.registerKeyBinding(setPos2Key);
        net.minecraftforge.fml.client.registry.ClientRegistry.registerKeyBinding(startMacroKey);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (setPos1Key.isPressed()) {
            pos1 = mc.thePlayer.getPosition();
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("Posição 1 setada: " + pos1));
        }

        if (setPos2Key.isPressed()) {
            pos2 = mc.thePlayer.getPosition();
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("Posição 2 setada: " + pos2));
        }

        if (startMacroKey.isPressed()) {
            if (pos1 != null && pos2 != null) {
                generateArea();
                running = true;
                mc.thePlayer.capabilities.allowFlying = true; // ativa voo
                mc.thePlayer.capabilities.isFlying = true;
                mc.thePlayer.sendPlayerAbilities();
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("Escavação iniciada."));
            }
        }
    }

    private void generateArea() {
        toBreak.clear();
        int x1 = Math.min(pos1.getX(), pos2.getX());
        int y1 = Math.min(pos1.getY(), pos2.getY());
        int z1 = Math.min(pos1.getZ(), pos2.getZ());

        int x2 = Math.max(pos1.getX(), pos2.getX());
        int y2 = Math.max(pos1.getY(), pos2.getY());
        int z2 = Math.max(pos1.getZ(), pos2.getZ());

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    toBreak.add(new BlockPos(x, y, z));
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!running || mc.thePlayer == null || mc.theWorld == null || toBreak.isEmpty())
            return;

        BlockPos target = toBreak.poll();
        Block block = mc.theWorld.getBlockState(target).getBlock();

        if (block != net.minecraft.init.Blocks.air) {
            mc.playerController.onPlayerDamageBlock(target, mc.thePlayer.getHorizontalFacing());
        }
    }
}
