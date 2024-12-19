package com.cleanroommc.bogosorter.core.visitor;

import com.cleanroommc.bogosorter.core.BogoSorterCore;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class PIMVisitor extends ClassVisitor implements Opcodes {

    public static final String CLASS_NAME = "net.minecraft.server.management.PlayerInteractionManager";
    public static final boolean DEOBF = BogoSorterCore.isDevEnv();
    public static final String METHOD_NAME = DEOBF ? "processRightClick" : "func_187250_a";
    public static final String TRY_HARVEST_BLOCK_FUNC = DEOBF ? "tryHarvestBlock" : "func_180237_b";
    public static final String PLAYER_CLASS = "net/minecraft/entity/player/EntityPlayer";
    public static final String PLAYER_MP_CLASS = "net/minecraft/entity/player/EntityPlayerMP";
    public static final String SET_HELD_ITEM_FUNC = DEOBF ? "setHeldItem" : "func_184611_a";
    public static final String ITEM_STACK_CLASS = "net/minecraft/item/ItemStack";
    public static final String EMPTY = DEOBF ? "EMPTY" : "field_190927_a";
    public static final String REFILL_HANDLER_CLASS = "com/cleanroommc/bogosorter/common/refill/RefillHandler";
    public static final String ON_DESTROY_FUNC = "onDestroyItem";
    public static final String ON_DESTROY_FUNC_DESC = "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/EnumHand;)V";
    public static final String FEF_CLASS = "net/minecraftforge/event/ForgeEventFactory";
    public static final String ON_DESTROY_ITEM_FUNC = "onPlayerDestroyItem";
    public static final String PIM_CLASS = "net/minecraft/server/management/PlayerInteractionManager";
    public static final String HAND_CLASS = "net/minecraft/util/EnumHand";

    public PIMVisitor(ClassVisitor cv) {
        super(ASM5, cv);
    }

    public static void visitOnDestroy(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, REFILL_HANDLER_CLASS, ON_DESTROY_FUNC, ON_DESTROY_FUNC_DESC, false);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor visitor = super.visitMethod(access, name, desc, signature, exceptions);
        if (METHOD_NAME.equals(name)) {
            return new TargetMethodVisitor(visitor);
        }
        if (access == ACC_PUBLIC && TRY_HARVEST_BLOCK_FUNC.equals(name)) {
            return new TryHarvestBlockVisitor(visitor);
        }
        return visitor;
    }

    public static class TargetMethodVisitor extends MethodVisitor {

        private boolean inject = false;

        public TargetMethodVisitor(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (inject && opcode == INVOKEVIRTUAL && PLAYER_CLASS.equals(owner) && SET_HELD_ITEM_FUNC.equals(name)) {
                visitVarInsn(ALOAD, 1);
                visitVarInsn(ALOAD, 8);
                visitVarInsn(ALOAD, 4);
                visitOnDestroy(this);
                inject = false;
                BogoSorterCore.LOGGER.info("Applied PIM processRightClick ASM");
            }
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);
            if (opcode == GETSTATIC && ITEM_STACK_CLASS.equals(owner) && EMPTY.equals(name)) {
                inject = true;
            }
        }
    }

    public static class TryHarvestBlockVisitor extends MethodVisitor {

        public TryHarvestBlockVisitor(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (opcode == INVOKESTATIC && FEF_CLASS.equals(owner) && ON_DESTROY_ITEM_FUNC.equals(name)) {
                visitVarInsn(ALOAD, 0);
                visitFieldInsn(GETFIELD, PIM_CLASS, DEOBF ? "player" : "field_73090_b", "L" + PLAYER_MP_CLASS + ";");
                visitVarInsn(ALOAD, 9);
                visitFieldInsn(GETSTATIC, HAND_CLASS, "MAIN_HAND", "L" + HAND_CLASS + ";");
                visitOnDestroy(this);
                BogoSorterCore.LOGGER.info("Applied PIM tryHarvestBlock ASM");
            }
        }
    }
}
