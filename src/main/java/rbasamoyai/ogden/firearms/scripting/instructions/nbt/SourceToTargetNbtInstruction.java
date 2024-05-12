package rbasamoyai.ogden.firearms.scripting.instructions.nbt;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;

public abstract class SourceToTargetNbtInstruction implements ScriptInstruction {

    private final ScriptValueSupplier sourcePath;
    @Nullable private final ScriptValueSupplier sourceStack;

    private final ScriptValueSupplier targetPath;
    @Nullable private final ScriptValueSupplier targetStack;

    protected SourceToTargetNbtInstruction(ScriptValueSupplier sourcePath, @Nullable ScriptValueSupplier sourceStack,
                                           ScriptValueSupplier targetPath, @Nullable ScriptValueSupplier targetStack) {
        this.sourcePath = sourcePath;
        this.sourceStack = sourceStack;
        this.targetPath = targetPath;
        this.targetStack = targetStack;
    }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        String sourcePathRes = this.sourcePath.run(context).str();
        if (sourcePathRes == null) {
            ; // TODO: log error once
            return ScriptValue.VOID;
        }
        String targetPathRes = this.targetPath.run(context).str();
        if (targetPathRes == null) {
            ; // TODO: log error once
            return ScriptValue.VOID;
        }
        CompoundTag sourceTag = context.stack().getOrCreateTag();
        CompoundTag targetTag = sourceTag;
        if (this.sourceStack != null) {
            ItemStack sourceStackRes = this.sourceStack.run(context).stack();
            if (sourceStackRes == null) {
                ; // TODO log error once
                return ScriptValue.VOID;
            } else {
                sourceTag = sourceStackRes.getOrCreateTag();
            }
        }
        if (this.targetStack != null) {
            ItemStack targetStackRes = this.targetStack.run(context).stack();
            if (targetStackRes == null) {
                ; // TODO log error once
                return ScriptValue.VOID;
            } else {
                targetTag = targetStackRes.getOrCreateTag();
            }
        }
        try {
            NbtPathArgument.NbtPath sourceArg = (new NbtPathArgument()).parse(new StringReader(sourcePathRes));
            NbtPathArgument.NbtPath targetArg = (new NbtPathArgument()).parse(new StringReader(targetPathRes));
            List<Tag> list = sourceArg.get(sourceTag);
            if (!list.isEmpty())
                this.getMergeOp().merge(targetTag, targetArg, list);
        } catch (CommandSyntaxException e) {
            ; // TODO log error once
        }
        return ScriptValue.VOID;
    }

    protected abstract CopyNbtFunction.MergeStrategy getMergeOp();

    public abstract static class Serializer implements ScriptInstructionSerializer {
        private final Constructor constructor;

        protected Serializer(Constructor constructor) {
            this.constructor = constructor;
        }

        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("source_path"))
                throw new JsonSyntaxException("Instruction missing parameter 'source_path'");
            if (!obj.has("target_path"))
                throw new JsonSyntaxException("Instruction missing parameter 'target_path'");
            ScriptValueSupplier sourcePath = ScriptValueSupplier.fromJson(obj.get("source_path"));
            ScriptValueSupplier targetPath = ScriptValueSupplier.fromJson(obj.get("target_path"));
            ScriptValueSupplier sourceStackProvider = obj.has("source_stack") ? ScriptValueSupplier.fromJson(obj.get("source_stack")) : null;
            ScriptValueSupplier targetStackProvider = obj.has("target_stack") ? ScriptValueSupplier.fromJson(obj.get("target_stack")) : null;
            return this.constructor.create(sourcePath, sourceStackProvider, targetPath, targetStackProvider);
        }

        public interface Constructor {
            ScriptInstruction create(ScriptValueSupplier sourcePath, @Nullable ScriptValueSupplier sourceStackProvider,
                                     ScriptValueSupplier targetPath, @Nullable ScriptValueSupplier targetStackProvider);
        }
    }

}
