package rbasamoyai.ogden.firearms.scripting.instructions.nbt;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import rbasamoyai.ogden.base.Components;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;

public class GetNbtInstruction extends SourceNbtInstruction {

    // Adapted from DataCommands --ritchie
    private static final SimpleCommandExceptionType ERROR_MULTIPLE_TAGS = new SimpleCommandExceptionType(Components.translatable("commands.data.get.multiple"));

    private final Getter getter;
    private final ScriptInstructionType type;

    public GetNbtInstruction(ScriptValueSupplier id, @Nullable ScriptValueSupplier nbtSource, Getter getter, ScriptInstructionType type) {
        super(id, nbtSource);
        this.getter = getter;
        this.type = type;
    }

    @Override public ScriptInstructionType type() { return this.type; }

    @Override
    protected ScriptValue operate(NbtPathArgument.NbtPath id, CompoundTag tag) {
        try {
            return this.getter.getValue(tag, id);
        } catch (CommandSyntaxException e) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
    }

    public static class Serializer extends SourceNbtInstruction.Serializer {
        public Serializer(ScriptInstructionType type, Getter getter) {
            super((id, source) -> new GetNbtInstruction(id, source, getter, type));
        }

        public static Function<ScriptInstructionType, ScriptInstructionSerializer> provider(Getter getter) {
            return type -> new Serializer(type, getter);
        }

        public static Function<ScriptInstructionType, ScriptInstructionSerializer> provider(Function<Tag, ScriptValue> valueProvider, int tagId, ScriptValue defaultValue) {
            return type -> new Serializer(type, createSimpleGetter(valueProvider, tagId, defaultValue));
        }
    }

    // Adapted from DataCommands#getSingleTag
    public static Tag getSingleTag(CompoundTag tag, NbtPathArgument.NbtPath path) throws CommandSyntaxException {
        Collection<Tag> collection = path.get(tag);
        Iterator<Tag> iterator = collection.iterator();
        Tag tag1 = iterator.next();
        if (iterator.hasNext()) {
            throw ERROR_MULTIPLE_TAGS.create();
        } else {
            return tag1;
        }
    }

    public static ScriptValue getScriptValueList(CompoundTag tag, NbtPathArgument.NbtPath id) throws CommandSyntaxException {
        // Using raw tag to not require tag type check in CompoundTag#getList --ritchie
        Tag tag1 = getSingleTag(tag, id);
        return tag1 instanceof CollectionTag<?> ? ScriptValue.fromNbt(tag1) : ScriptValue.EMPTY_LIST;
    }

    public static Getter createSimpleGetter(Function<Tag, ScriptValue> valueProvider, int tagId, ScriptValue defaultValue) {
        return (tag, id) -> {
            Tag tag1 = getSingleTag(tag, id);
            return tag1.getId() == tagId ? valueProvider.apply(tag1) : defaultValue;
        };
    }

    @FunctionalInterface
    public interface Getter {
        ScriptValue getValue(CompoundTag tag, NbtPathArgument.NbtPath id) throws CommandSyntaxException;
    }

}
