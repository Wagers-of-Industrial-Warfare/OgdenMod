package rbasamoyai.ogden.firearms.scripting.instructions.nbt;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.StringReader;

import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class InsertNbtInstruction implements ScriptInstruction {

    private final ScriptValueSupplier source;
    private final SourceType type;
    private final ScriptValueSupplier target;
    private final ScriptValueSupplier index;
    private final ScriptValueSupplier start;
    private final ScriptValueSupplier end;
    @Nullable private final ScriptValueSupplier sourceNbtSource;
    @Nullable private final ScriptValueSupplier targetNbtSource;

    public InsertNbtInstruction(ScriptValueSupplier source, SourceType type, ScriptValueSupplier target, ScriptValueSupplier index,
                                ScriptValueSupplier start, ScriptValueSupplier end, @Nullable ScriptValueSupplier sourceNbtSource,
                                @Nullable ScriptValueSupplier targetNbtSource) {
        this.source = source;
        this.type = type;
        this.target = target;
        this.index = index;
        this.start = start;
        this.end = end;
        this.sourceNbtSource = sourceNbtSource;
        this.targetNbtSource = targetNbtSource;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.INSERT_NBT; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        ScriptValue sourceRes = this.source.run(context);
        String targetRes = this.target.run(context).str();
        if (targetRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        Number indexRes = this.index.run(context).num();
        if (indexRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        Number startRes = this.start.run(context).num();
        if (startRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        Number endRes = this.end.run(context).num();
        if (endRes == null) {
            ; // TODO log error once
            return ScriptValue.VOID;
        }
        CompoundTag sourceItemTag = context.stack().getOrCreateTag();
        CompoundTag targetItemTag = sourceItemTag;
        if (this.sourceNbtSource != null) {
            ItemStack sourceNbtSourceRes = this.sourceNbtSource.run(context).stack();
            if (sourceNbtSourceRes == null) {
                ; // TODO log error once
                return ScriptValue.VOID;
            } else {
                sourceItemTag = sourceNbtSourceRes.getOrCreateTag();
            }
        }
        if (this.targetNbtSource != null) {
            ItemStack targetNbtSourceRes = this.targetNbtSource.run(context).stack();
            if (targetNbtSourceRes == null) {
                ; // TODO log error once
                return ScriptValue.VOID;
            } else {
                targetItemTag = targetNbtSourceRes.getOrCreateTag();
            }
        }
        try {
            Tag sourceTag;
            if (this.type == SourceType.FROM) {
                String sourceStr = sourceRes.str();
                if (sourceStr == null) {
                    ; // TODO log error once
                    return ScriptValue.VOID;
                }
                NbtPathArgument.NbtPath path = (new NbtPathArgument()).parse(new StringReader(sourceStr));
                sourceTag = GetNbtInstruction.getSingleTag(sourceItemTag, path);
            } else {
                sourceTag = sourceRes.toNbt();
            }
            NbtPathArgument.NbtPath targetPath = (new NbtPathArgument()).parse(new StringReader(targetRes));
            Tag targetTag = GetNbtInstruction.getSingleTag(targetItemTag, targetPath);
            if (sourceTag.getId() != targetTag.getId()) {
                ; // TODO log error once
                return ScriptValue.VOID;
            }
            int index = indexRes.intValue();
            int start = startRes.intValue();
            int end = endRes.intValue();
            if (sourceTag.getId() == Tag.TAG_STRING) {
                String sourceStr = sourceTag.getAsString();
                StringBuilder sb = new StringBuilder(sourceTag.getAsString());
                index = index < 0 ? sb.length() : Math.min(index, sb.length());
                start = start < 0 ? sb.length() : Mth.clamp(start, 0, sb.length());
                end = end < 0 ? sb.length() : Mth.clamp(end, 0, sb.length());
                sb.insert(index, sourceStr, start, end);
                targetPath.set(sourceItemTag, StringTag.valueOf(sb.toString()));
            } else if (sourceTag.getId() == Tag.TAG_LIST) {
                ListTag sourceList = (ListTag) sourceTag;
                ListTag targetList = (ListTag) targetTag;
                if (sourceList.getElementType() != targetList.getElementType()) {
                    ; // TODO log error once
                    return ScriptValue.VOID;
                } else {
                    insertList(sourceList, targetList, index, start, end);
                }
            } else if (sourceTag.getId() == Tag.TAG_INT_ARRAY) {
                insertList((IntArrayTag) sourceTag, (IntArrayTag) targetTag, index, start, end);
            } else if (sourceTag.getId() == Tag.TAG_LONG_ARRAY) {
                insertList((LongArrayTag) sourceTag, (LongArrayTag) targetTag, index, start, end);
            } else if (sourceTag.getId() == Tag.TAG_BYTE_ARRAY) {
                insertList((ByteArrayTag) sourceTag, (ByteArrayTag) targetTag, index, start, end);
            } else {
                ; // TODO log error once
            }
        } catch (Exception e) {
            ; // TODO log error once
        }
        return ScriptValue.VOID;
    }

    public static <T> void insertList(List<T> source, List<T> target, int index, int start, int end) {
        index = index < 0 ? target.size() : Math.min(index, target.size());
        start = start < 0 ? source.size() : Mth.clamp(start, 0, source.size());
        end = end < 0 ? source.size() : Mth.clamp(end, 0, source.size());
        target.addAll(index, source.subList(start, end));
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("source"))
                throw new JsonParseException("Insert NBT instruction missing parameter 'source'");
            if (!obj.has("target"))
                throw new JsonParseException("Insert NBT instruction missing parameter 'target'");
            if (!obj.has("start"))
                throw new JsonParseException("Insert NBT instruction missing parameter 'start'");
            ScriptValueSupplier source = ScriptValueSupplier.fromJson(obj.get("source"));
            SourceType type = SourceType.valueOf(GsonHelper.getAsString(obj, "type"));
            ScriptValueSupplier target = ScriptValueSupplier.fromJson(obj.get("target"));
            ScriptValueSupplier index = obj.has("index") ? ScriptValueSupplier.fromJson(obj.get("index")) : ScriptValue.ofInt(-1);
            ScriptValueSupplier start = ScriptValueSupplier.fromJson(obj.get("start"));
            ScriptValueSupplier end = obj.has("end") ? ScriptValueSupplier.fromJson(obj.get("end")) : ScriptValue.ofInt(-1);
            ScriptValueSupplier sourceNbtSource = obj.has("source_nbt_stack") ? ScriptValueSupplier.fromJson(obj.get("source_nbt_stack")) : null;
            ScriptValueSupplier targetNbtSource = obj.has("target_nbt_stack") ? ScriptValueSupplier.fromJson(obj.get("target_nbt_stack")) : null;
            return new InsertNbtInstruction(source, type, target, index, start, end, sourceNbtSource, targetNbtSource);
        }
    }

    public enum SourceType implements StringRepresentable {
        VALUE,
        FROM;

        private static final Map<String, SourceType> BY_ID = Arrays.stream(values())
            .collect(Collectors.toMap(SourceType::getSerializedName, Function.identity()));

        private final String id = this.name().toLowerCase(Locale.ROOT);

        @Override public String getSerializedName() { return this.getSerializedName(); }

        public static SourceType byId(String id) { return BY_ID.getOrDefault(id, VALUE); }
    }

}
