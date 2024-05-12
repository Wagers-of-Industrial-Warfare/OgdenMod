package rbasamoyai.ogden.firearms.scripting.instructions.nbt;

import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;

import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;

public abstract class SourceNbtInstruction implements ScriptInstruction {

    private final ScriptValueSupplier id;
    @Nullable private final ScriptValueSupplier nbtStack;

    protected SourceNbtInstruction(ScriptValueSupplier id, @Nullable ScriptValueSupplier nbtStack) {
        this.id = id;
        this.nbtStack = nbtStack;
    }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        String idRes = this.id.run(context).str();
        if (idRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        CompoundTag tag = context.stack().getOrCreateTag();
        if (this.nbtStack != null) {
            ItemStack nbtStackRes = this.nbtStack.run(context).stack();
            if (nbtStackRes == null) {
                ; // TODO log error once
                return ScriptValue.VOID;
            } else {
                tag = nbtStackRes.getOrCreateTag();
            }
        }
        try {
            NbtPathArgument.NbtPath path = (new NbtPathArgument()).parse(new StringReader(idRes));
            return this.operate(path, tag);
        } catch (Exception e) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
    }

    protected abstract ScriptValue operate(NbtPathArgument.NbtPath id, CompoundTag tag);

    public static abstract class Serializer implements ScriptInstructionSerializer {
        private final BiFunction<ScriptValueSupplier, ScriptValueSupplier, ScriptInstruction> constructor;

        protected Serializer(BiFunction<ScriptValueSupplier, ScriptValueSupplier, ScriptInstruction> constructor) {
            this.constructor = constructor;
        }

        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("id"))
                throw new JsonSyntaxException("NBT instruction missing parameter 'id'");
            ScriptValueSupplier id = ScriptValueSupplier.fromJson(obj.get("id"));
            ScriptValueSupplier nbtSource = obj.has("source") ? ScriptValueSupplier.fromJson(obj.get("source")) : null;
            return this.constructor.apply(id, nbtSource);
        }
    }

}
