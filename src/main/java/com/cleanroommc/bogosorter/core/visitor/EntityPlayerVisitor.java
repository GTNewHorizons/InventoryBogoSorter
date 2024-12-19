package com.cleanroommc.bogosorter.core.visitor;

import com.cleanroommc.bogosorter.core.BogoSorterCore;
import com.cleanroommc.bogosorter.core.CatServerHelper;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class EntityPlayerVisitor extends ClassVisitor implements Opcodes {

    public static final String CLASS_NAME = "net.minecraft.entity.player.EntityPlayer";
    public static final boolean DEOBF = BogoSorterCore.isDevEnv();
    private static final String METHOD_NAME = DEOBF ? "damageShield" : "func_184590_k";
    private static final String INTERACT_ON_FUNC = DEOBF ? "interactOn" : "func_190775_a";
    private static final String ATTACK_ENTITY_FUNC = DEOBF ? "attackTargetEntityWithCurrentItem" : "func_71059_n";
    private static final String FEF_CLASS = "net/minecraftforge/event/ForgeEventFactory";
    private static final String ON_DESTROY_ITEM_FUNC = "onPlayerDestroyItem";

    public EntityPlayerVisitor(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if (access == ACC_PROTECTED && METHOD_NAME.equals(name) && "(F)V".equals(desc)) {
            return new TargetMethodVisitor(mv);
        }
        if (access == ACC_PUBLIC && INTERACT_ON_FUNC.equals(name)) {
            return new InteractOnVisitor(mv);
        }
        if (access == ACC_PUBLIC && ATTACK_ENTITY_FUNC.equals(name)) {
            return new AttackTargetEntityWithCurrentItemVisitor(mv);
        }
        return mv;
    }

    private static class TargetMethodVisitor extends MethodVisitor {

        public TargetMethodVisitor(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);
            if (opcode == PUTFIELD && "activeItemStack".equals(name)) {
                visitVarInsn(ALOAD, 0);
                visitVarInsn(ALOAD, 2);
                visitVarInsn(ALOAD, 4);
                PIMVisitor.visitOnDestroy(this);
                BogoSorterCore.LOGGER.info("Applied EntityPlayer damageShield ASM");
            }
        }
    }

    public static class InteractOnVisitor extends MethodVisitor {

        public InteractOnVisitor(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if ((opcode == INVOKEVIRTUAL && PIMVisitor.PLAYER_CLASS.equals(owner) && PIMVisitor.SET_HELD_ITEM_FUNC.equals(name)) ||
                    (opcode == INVOKESTATIC && FEF_CLASS.equals(owner) && ON_DESTROY_ITEM_FUNC.equals(name))) {
                visitVarInsn(ALOAD, 0);
                visitVarInsn(ALOAD, 5);
                visitVarInsn(ALOAD, 2);
                PIMVisitor.visitOnDestroy(this);
                BogoSorterCore.LOGGER.info("Applied EntityPlayer interactOn ASM");
            }
        }
    }

    public static class AttackTargetEntityWithCurrentItemVisitor extends MethodVisitor {

        public AttackTargetEntityWithCurrentItemVisitor(MethodVisitor mv) {
            super(ASM5, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (opcode == INVOKEVIRTUAL && PIMVisitor.PLAYER_CLASS.equals(owner) && PIMVisitor.SET_HELD_ITEM_FUNC.equals(name)) {
                visitVarInsn(ALOAD, 0);
                visitVarInsn(ALOAD, CatServerHelper.isCatServerLoaded() ? 26 : 25);
                visitFieldInsn(GETSTATIC, PIMVisitor.HAND_CLASS, "MAIN_HAND", "L" + PIMVisitor.HAND_CLASS + ";");
                PIMVisitor.visitOnDestroy(this);
                BogoSorterCore.LOGGER.info("Applied EntityPlayer attackTargetEntityWithCurrentItem ASM");
            }
        }
    }
}
