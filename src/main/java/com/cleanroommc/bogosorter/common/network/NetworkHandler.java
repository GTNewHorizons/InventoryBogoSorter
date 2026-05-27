package com.cleanroommc.bogosorter.common.network;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.world.World;

import com.cleanroommc.bogosorter.BogoSorter;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * Joinked from Multiblocked
 */
public class NetworkHandler {

    public static final NetworkHandler INSTANCE = new NetworkHandler();
    public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(BogoSorter.ID);
    private static int packetId = 0;

    // C2S packets are decoded and handled on the netty thread. 1.7.10 has no IThreadListener, so we
    // queue the server-side work here and drain it on the main thread during the server tick to avoid
    // racing with the tick loop over inventory/container state.
    private static final Queue<Runnable> mainThreadTasks = new ConcurrentLinkedQueue<>();

    public static void init() {
        // CSlotSync backs the debug clear/randomize tools. It is fully server-authoritative and gated in
        // executeServer by the enableDebugTools config plus an operator check, so it is safe to register
        // unconditionally.
        registerC2S(CSlotSync.class);
        registerC2S(CShortcut.class);
        registerC2S(CSort.class);
        registerC2S(CHotbarSwap.class);
        registerS2C(SReloadConfig.class);
        registerS2C(SRefillSound.class);
        registerC2S(CDropOff.class);
        registerS2C(SDropOffMessage.class);
        registerS2C(SDropOffThrottled.class);
        registerC2S(CRefill.class);
    }

    private static void registerC2S(Class<? extends IPacket> clazz) {
        network.registerMessage(C2SHandler, clazz, packetId++, Side.SERVER);
    }

    private static void registerS2C(Class<? extends IPacket> clazz) {
        network.registerMessage(S2CHandler, clazz, packetId++, Side.CLIENT);
    }

    public static void sendToServer(IPacket packet) {
        network.sendToServer(packet);
    }

    public static void sendToWorld(IPacket packet, World world) {
        network.sendToDimension(packet, world.provider.dimensionId);
    }

    public static void sendToPlayer(IPacket packet, EntityPlayerMP player) {
        network.sendTo(packet, player);
    }

    final static IMessageHandler<IPacket, IPacket> S2CHandler = (message, ctx) -> {
        NetHandlerPlayClient handler = ctx.getClientHandler();
        return message.executeClient(handler);
    };
    final static IMessageHandler<IPacket, IPacket> C2SHandler = (message, ctx) -> {
        NetHandlerPlayServer handler = ctx.getServerHandler();
        mainThreadTasks.add(() -> {
            IPacket reply = message.executeServer(handler);
            if (reply != null) {
                sendToPlayer(reply, handler.playerEntity);
            }
        });
        return null;
    };

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Runnable task;
        while ((task = mainThreadTasks.poll()) != null) {
            task.run();
        }
    }
}
