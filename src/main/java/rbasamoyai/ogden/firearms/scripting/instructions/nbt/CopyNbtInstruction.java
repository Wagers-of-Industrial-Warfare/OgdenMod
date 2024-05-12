package rbasamoyai.ogden.firearms.scripting.instructions.nbt;

import javax.annotation.Nullable;

import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class CopyNbtInstruction extends SourceToTargetNbtInstruction {

    public CopyNbtInstruction(ScriptValueSupplier sourcePath, @Nullable ScriptValueSupplier sourceStack,
                              ScriptValueSupplier targetPath, @Nullable ScriptValueSupplier targetStack) {
        super(sourcePath, sourceStack, targetPath, targetStack);
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.COPY_NBT; }
    @Override protected CopyNbtFunction.MergeStrategy getMergeOp() { return CopyNbtFunction.MergeStrategy.REPLACE; }

    public static class Serializer extends SourceToTargetNbtInstruction.Serializer {
        public Serializer() { super(CopyNbtInstruction::new); }
    }

}
