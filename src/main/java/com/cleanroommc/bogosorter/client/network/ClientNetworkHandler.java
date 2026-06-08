package com.cleanroommc.bogosorter.client.network;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.client.network.NetHandlerPlayClient;

import com.cleanroommc.bogosorter.BogoSorter;
import com.cleanroommc.bogosorter.common.network.IPacket;

import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientNetworkHandler {

    private static final int MAX_CLIENT_QUEUE_SIZE = 4096;
    private static final int MAX_TASKS_PER_TICK = 256;
    private static final long MAX_TASK_TIME_NS = 5_000_000L;

    private static final Queue<Runnable> clientTasks = new ConcurrentLinkedQueue<>();
    private static final AtomicInteger clientQueueSize = new AtomicInteger();

    private static final IMessageHandler<IPacket, IPacket> S2C_HANDLER = (message, ctx) -> {
        // skip packets that failed to decode
        if (message.isRejected()) {
            message.acknowledge();
            return null;
        }

        NetHandlerPlayClient handler = ctx.getClientHandler();
        if (clientQueueSize.incrementAndGet() > MAX_CLIENT_QUEUE_SIZE) {
            clientQueueSize.decrementAndGet();
            BogoSorter.LOGGER.warn(
                "Dropping {} because the client packet queue is full",
                message.getClass()
                    .getName());
            return null;
        }
        clientTasks.add(() -> {
            try {
                message.executeClient(handler);
            } finally {
                message.acknowledge();
                clientQueueSize.decrementAndGet();
            }
        });
        return null;
    };

    public static void registerMessage(SimpleNetworkWrapper network, Class<? extends IPacket> clazz, int id) {
        network.registerMessage(S2C_HANDLER, clazz, id, Side.CLIENT);
    }

    public static void drainClientTasks() {
        long deadline = System.nanoTime() + MAX_TASK_TIME_NS;
        int processed = 0;
        Runnable task;
        while (processed < MAX_TASKS_PER_TICK && System.nanoTime() < deadline && (task = clientTasks.poll()) != null) {
            task.run();
            processed++;
        }
    }
}
