package rbasamoyai.ogden.firearms.scripting.instructions.control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.GsonHelper;
import net.minecraft.util.Tuple;
import rbasamoyai.ogden.firearms.scripting.FirearmScriptHandler;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class IfInstruction implements ScriptInstruction {

    private final List<Tuple<ScriptValueSupplier, List<ScriptValueSupplier>>> conditionals;
    private final List<ScriptValueSupplier> elseFunctions;

    public IfInstruction(List<Tuple<ScriptValueSupplier, List<ScriptValueSupplier>>> conditionals, List<ScriptValueSupplier> elseFunctions) {
        this.conditionals = conditionals;
        this.elseFunctions = elseFunctions;
    }

    public IfInstruction(ScriptValueSupplier condition, List<ScriptValueSupplier> thenValue, List<ScriptValueSupplier> elseFunctions) {
        this(List.of(new Tuple<>(condition, thenValue)), elseFunctions);
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.IF; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        if (this.conditionals.isEmpty())
            ; // TODO: log error once
        for (Tuple<ScriptValueSupplier, List<ScriptValueSupplier>> t : this.conditionals) {
            Boolean conditionResult = t.getA().run(context).bool();
            if (conditionResult == null) {
                ; // TODO log error once
            } else if (conditionResult) {
                ScriptValue ret = ScriptValue.VOID;
                ScriptContext context1 = context.pushLocalFrame();
                for (ScriptValueSupplier run : t.getB())
                    ret = run.run(context1);
                return ret;
            }
        }
        ScriptValue ret = ScriptValue.VOID;
        ScriptContext context1 = context.pushLocalFrame();
        for (ScriptValueSupplier run : this.elseFunctions)
            ret = run.run(context1);
        return ret;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            List<ScriptValueSupplier> elseFunctions;
            if (GsonHelper.isArrayNode(obj, "else")) {
                elseFunctions = new ArrayList<>();
                for (JsonElement el : GsonHelper.getAsJsonArray(obj, "else"))
                    elseFunctions.add(ScriptValueSupplier.fromJson(el));
            } else if (obj.has("else")) {
                elseFunctions = List.of(ScriptValueSupplier.fromJson(obj.get("else")));
            } else {
                elseFunctions = List.of(ScriptValue.VOID);
            }
            if (obj.has("condition") && obj.has("then")) {
                ScriptValueSupplier condition = ScriptValueSupplier.fromJson(obj.get("condition"));
                List<ScriptValueSupplier> thenFunctions;
                if (GsonHelper.isArrayNode(obj, "then")) {
                    thenFunctions = new ArrayList<>();
                    for (JsonElement el : GsonHelper.getAsJsonArray(obj, "then"))
                        thenFunctions.add(ScriptValueSupplier.fromJson(el));
                } else {
                    thenFunctions = List.of(ScriptValueSupplier.fromJson(obj.get("then")));
                }
                return new IfInstruction(condition, thenFunctions, elseFunctions);
            } else if (GsonHelper.isArrayNode(obj, "conditionals")) {
                JsonArray conditionalArr = obj.getAsJsonArray("conditionals");
                List<Tuple<ScriptValueSupplier, List<ScriptValueSupplier>>> conditionals = new ArrayList<>();
                for (int i = 0; i < conditionalArr.size(); ++i) {
                    JsonElement el = conditionalArr.get(i);
                    if (!el.isJsonObject())
                        throw new JsonParseException("If instruction 'conditionals' list has invalid conditional on index " + i + ": expected JSON object");
                    JsonObject conditionalObj = el.getAsJsonObject();

                    if (!conditionalObj.has("condition"))
                        throw new JsonParseException("If instruction 'conditionals' list has invalid conditional on index " + i + ": missing 'condition' value");
                    ScriptValueSupplier condition = ScriptValueSupplier.fromJson(conditionalObj.get("condition"));

                    List<ScriptValueSupplier> thenFunctions;
                    if (GsonHelper.isArrayNode(conditionalObj, "then")) {
                        thenFunctions = new ArrayList<>();
                        for (JsonElement el1 : GsonHelper.getAsJsonArray(conditionalObj, "then"))
                            thenFunctions.add(ScriptValueSupplier.fromJson(el1));
                    } else if (conditionalObj.has("then")) {
                        thenFunctions = List.of(ScriptValueSupplier.fromJson(conditionalObj.get("then")));
                    } else {
                        throw new JsonParseException("If instruction 'conditionals' list has invalid conditional on index " + i + ": missing 'then' value");
                    }
                    conditionals.add(new Tuple<>(condition, thenFunctions));
                }
                if (conditionals.isEmpty())
                    FirearmScriptHandler.ReloadListener.LOGGER.warn("If instruction has empty 'conditionals' list and will default to else value or void");
                return new IfInstruction(conditionals, elseFunctions);
            } else {
                throw new JsonParseException("If instruction expected either single 'condition'/'then' pair or list 'conditionals' of 'condition'/'then' pairs");
            }
        }
    }

}
