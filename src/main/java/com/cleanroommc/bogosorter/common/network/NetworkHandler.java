package com.cleanroommc.bogosorter.common.network;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.network.ae2.CAe2AmountBatchRequest;
import com.cleanroommc.bogosorter.common.network.ae2.SAe2AmountBatchResponse;
import com.cleanroommc.bogosorter.common.network.ae2.STooltipFeatureState;
import com.cleanroommc.bogosorter.compat.Mods;
import com.github.bsideup.jabel.Desugar;

import cpw.mods.fml.common.FMLCommonHandler;
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

    private static final String CLIENT_NETWORK_HANDLER = "com.cleanroommc.bogosorter.client.network.ClientNetworkHandler";

    public static final NetworkHandler INSTANCE = new NetworkHandler();
    public static final SimpleNetworkWrapper network = NetworkRegistry.INSTANCE.newSimpleChannel(BogoSorter.ID);
    private static final int MAX_SERVER_QUEUE_SIZE = 4096;
    private static final int MAX_SERVER_QUEUE_SIZE_PER_PLAYER = 256;
    private static final int MAX_TASKS_PER_TICK = 256;
    private static final long MAX_TASK_TIME_NS = 5_000_000L;
    private static int packetId = 0;

    private static final Queue<ServerTask> serverTasks = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger serverQueueSize = new AtomicInteger();
    private static final Map<String, AtomicInteger> playerQueueSizes = new ConcurrentHashMap<>();
    private static final IMessageHandler<IPacket, IPacket> SERVER_S2C_STUB = (message, ctx) -> null;

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
        // only register ae2 packets when ae2 is loaded
        if (Mods.Ae2.isLoaded()) {
            registerC2S(CAe2AmountBatchRequest.class);
            registerS2C(SAe2AmountBatchResponse.class);
            registerS2C(STooltipFeatureState.class);
        }
    }

    private static void registerC2S(Class<? extends IPacket> clazz) {
        network.registerMessage(C2SHandler, clazz, packetId++, Side.SERVER);
    }

    private static void registerS2C(Class<? extends IPacket> clazz) {
        int id = packetId++;
        if (FMLCommonHandler.instance()
            .getSide()
            .isClient()) {
            registerS2COnClient(clazz, id);
        } else {
            network.registerMessage(SERVER_S2C_STUB, clazz, id, Side.CLIENT);
        }
    }

    private static void registerS2COnClient(Class<? extends IPacket> clazz, int id) {
        try {
            Class<?> handlerClass = Class.forName(CLIENT_NETWORK_HANDLER, false, NetworkHandler.class.getClassLoader());
            handlerClass.getMethod("registerMessage", SimpleNetworkWrapper.class, Class.class, int.class)
                .invoke(null, network, clazz, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to register client packet handler for " + clazz.getName(), e);
        }
    }

    public static void sendToServer(IPacket packet) {
        network.sendToServer(packet);
    }

    public static void sendToAll(IPacket packet) {
        network.sendToAll(packet);
    }

    public static void sendToPlayer(IPacket packet, EntityPlayerMP player) {
        network.sendTo(packet, player);
    }

    final static IMessageHandler<IPacket, IPacket> C2SHandler = (message, ctx) -> {
        // skip packets that failed to decode
        if (message.isRejected()) {
            message.acknowledge();
            return null;
        }

        NetHandlerPlayServer handler = ctx.getServerHandler();
        EntityPlayerMP player = handler == null ? null : handler.playerEntity;
        if (player == null) return null;

        String playerKey = player.getUniqueID()
            .toString();
        AtomicInteger playerQueueSize = playerQueueSizes.computeIfAbsent(playerKey, ignored -> new AtomicInteger());
        int globalSize = serverQueueSize.incrementAndGet();
        int perPlayerSize = playerQueueSize.incrementAndGet();
        if (globalSize > MAX_SERVER_QUEUE_SIZE || perPlayerSize > MAX_SERVER_QUEUE_SIZE_PER_PLAYER) {
            serverQueueSize.decrementAndGet();
            if (playerQueueSize.decrementAndGet() == 0) {
                playerQueueSizes.remove(playerKey, playerQueueSize);
            }
            BogoSorter.LOGGER.warn(
                "Dropping {} from {} because the server packet queue is full",
                message.getClass()
                    .getName(),
                player.getCommandSenderName());
            return null;
        }

        serverTasks.add(new ServerTask(message, handler, playerKey, playerQueueSize));
        return null;
    };

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        long deadline = System.nanoTime() + MAX_TASK_TIME_NS;
        int processed = 0;
        ServerTask task;
        while (processed < MAX_TASKS_PER_TICK && System.nanoTime() < deadline && (task = serverTasks.poll()) != null) {
            task.run();
            processed++;
        }
    }

    @Desugar
    private record ServerTask(IPacket message, NetHandlerPlayServer handler, String playerKey,
        AtomicInteger playerQueueSize) implements Runnable {

        @Override
        public void run() {
            try {
                if (this.handler.playerEntity == null) return;
                IPacket reply = this.message.executeServer(this.handler);
                if (reply != null) {
                    sendToPlayer(reply, this.handler.playerEntity);
                }
            } catch (RuntimeException e) {
                BogoSorter.LOGGER.error(
                    "Failed to handle {} from {}",
                    this.message.getClass()
                        .getName(),
                    this.playerKey,
                    e);
            } finally {
                this.message.acknowledge();
                serverQueueSize.decrementAndGet();
                if (this.playerQueueSize.decrementAndGet() == 0) {
                    playerQueueSizes.remove(this.playerKey, this.playerQueueSize);
                }
            }
        }
    }
}
