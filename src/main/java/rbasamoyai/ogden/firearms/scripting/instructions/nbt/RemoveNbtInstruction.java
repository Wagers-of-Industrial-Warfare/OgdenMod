package rbasamoyai.ogden.firearms.scripting.instructions.nbt;

import javax.annotation.Nullable;

import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class RemoveNbtInstruction extends SourceNbtInstruction {

    public RemoveNbtInstruction(ScriptValueSupplier id, @Nullable ScriptValueSupplier nbtSource) { super(id, nbtSource); }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.REMOVE_NBT; }

    @Override
    protected ScriptValue operate(NbtPathArgument.NbtPath id, CompoundTag tag) {
        id.remove(tag);
        return ScriptValue.VOID;
    }

    public static class Serializer extends SourceNbtInstruction.Serializer {
        public Serializer() { super(RemoveNbtInstruction::new); }
    }

}
