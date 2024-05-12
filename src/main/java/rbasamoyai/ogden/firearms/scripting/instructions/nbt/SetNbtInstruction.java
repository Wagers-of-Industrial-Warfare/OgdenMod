package rbasamoyai.ogden.firearms.scripting.instructions.nbt;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;

import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;

public class SetNbtInstruction implements ScriptInstruction {

    private final ScriptValueSupplier id;
    @Nullable private final ScriptValueSupplier nbtSource;
    private final Getter getter;
    private final ScriptValueSupplier value;
    private final ScriptInstructionType type;

    public SetNbtInstruction(ScriptValueSupplier id, @Nullable ScriptValueSupplier nbtSource, Getter getter,
                             ScriptValueSupplier value, ScriptInstructionType type) {
        this.id = id;
        this.nbtSource = nbtSource;
        this.getter = getter;
        this.value = value;
        this.type = type;
    }

    @Override public ScriptInstructionType type() { return this.type; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        String idRes = this.id.run(context).str();
        if (idRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        CompoundTag tag = context.stack().getOrCreateTag();
        if (this.nbtSource != null) {
            ItemStack nbtSourceRes = this.nbtSource.run(context).stack();
            if (nbtSourceRes == null) {
                ; // TODO log error once
                return ScriptValue.VOID;
            } else {
                tag = nbtSourceRes.getOrCreateTag();
            }
        }
        try {
            Tag addTag = this.getter.getValue(this.value.run(context));
            if (addTag == null) {
                ; // TODO log error once
                return ScriptValue.VOID;
            }
            NbtPathArgument.NbtPath path = (new NbtPathArgument()).parse(new StringReader(idRes));
            path.set(tag, addTag);
        } catch (Exception e) {
            ; // TODO log error once
        }
        return ScriptValue.VOID;
    }

    @FunctionalInterface
    public interface Getter {
        @Nullable Tag getValue(ScriptValue value) throws Exception;

        static <T> Getter fromFunctions(Function<ScriptValue, ? extends T> valueGetter, Function<? super T, Tag> tagProvider) {
            return sv -> {
                T t = valueGetter.apply(sv);
                return t == null ? null : tagProvider.apply(t);
            };
        }

        static <I, T> Getter fromFunctions(Function<ScriptValue, ? extends I> valueGetter, Function<? super I, ? extends T> transformer, Function<? super T, Tag> tagProvider) {
            return sv -> {
                I i = valueGetter.apply(sv);
                if (i == null)
                    return null;
                T t = transformer.apply(i);
                return t == null ? null : tagProvider.apply(t);
            };
        }
    }

    public static class Serializer implements ScriptInstructionSerializer {
        private final Constructor constructor;

        public Serializer(ScriptInstructionType type, Getter getter) {
            this.constructor = (id, nbtSource, value) -> new SetNbtInstruction(id, nbtSource, getter, value, type);
        }

        public static Function<ScriptInstructionType, ScriptInstructionSerializer> provider(Getter getter) {
            return type -> new Serializer(type, getter);
        }

        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("id"))
                throw new JsonParseException("Set NBT instruction missing value 'id'");
            if (!obj.has("value"))
                throw new JsonParseException("Set NBT instruction missing value 'value'");
            ScriptValueSupplier id = ScriptValueSupplier.fromJson(obj.get("id"));
            ScriptValueSupplier value = ScriptValueSupplier.fromJson(obj.get("value"));
            ScriptValueSupplier nbtSource = obj.has("source") ? ScriptValueSupplier.fromJson(obj.get("source")) : null;
            return this.constructor.create(id, nbtSource, value);
        }

        public interface Constructor {
            ScriptInstruction create(ScriptValueSupplier id, @Nullable ScriptValueSupplier nbtSource, ScriptValueSupplier value);
        }
    }

    public static Tag getListTag(List<ScriptValue> list) {
        ListTag listTag = new ListTag();
        for (ScriptValue scriptValue : list)
            listTag.add(scriptValue.toNbt());
        return listTag;
    }

    public static Tag getObjectTag(Map<String, ScriptValue> obj) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, ScriptValue> entry : obj.entrySet())
            tag.put(entry.getKey(), entry.getValue().toNbt());
        return tag;
    }

    public static Tag getItemStackTag(ItemStack stack) {
        return stack.save(new CompoundTag());
    }

}
