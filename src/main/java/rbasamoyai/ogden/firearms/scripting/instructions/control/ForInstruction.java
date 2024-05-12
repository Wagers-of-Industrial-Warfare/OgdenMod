package rbasamoyai.ogden.firearms.scripting.instructions.control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.GsonHelper;
import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class ForInstruction implements ScriptInstruction {

    private final ScriptValueSupplier definition;
    private final ScriptValueSupplier condition;
    private final ScriptValueSupplier step;
    private final List<ScriptValueSupplier> runFunctions;

    public ForInstruction(ScriptValueSupplier definition, ScriptValueSupplier condition, ScriptValueSupplier step, List<ScriptValueSupplier> runFunctions) {
        this.definition = definition;
        this.condition = condition;
        this.step = step;
        this.runFunctions = runFunctions;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.FOR; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        this.definition.run(context);
        int HARD_LIMIT = 5000;
        for (int p = 0; p < HARD_LIMIT; ++p) {
            Boolean check = this.condition.run(context).bool();
            if (check == null) {
                ; // TODO log error once
                break;
            } else if (!check) {
                break;
            }
            ScriptContext context1 = context.pushLocalFrame();
            for (ScriptValueSupplier sup : this.runFunctions)
                sup.run(context1);
            this.step.run(context);
        }
        return ScriptValue.VOID;
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            if (!obj.has("definition"))
                throw new JsonParseException("For instruction missing parameter 'definition'");
            if (!obj.has("condition"))
                throw new JsonParseException("For instruction missing parameter 'condition'");
            if (!obj.has("step"))
                throw new JsonParseException("For instruction missing parameter 'step'");
            if (!obj.has("run"))
                throw new JsonParseException("For instruction missing parameter 'run'");
            ScriptValueSupplier definition = ScriptValueSupplier.fromJson(obj.get("definition"));
            ScriptValueSupplier condition = ScriptValueSupplier.fromJson(obj.get("condition"));
            ScriptValueSupplier step = ScriptValueSupplier.fromJson(obj.get("step"));
            List<ScriptValueSupplier> runFunctions;
            if (GsonHelper.isArrayNode(obj, "run")) {
                runFunctions = new ArrayList<>();
                for (JsonElement el : GsonHelper.getAsJsonArray(obj, "run"))
                    runFunctions.add(ScriptValueSupplier.fromJson(el));
            } else {
                runFunctions = List.of(ScriptValueSupplier.fromJson(obj.get("run")));
            }
            return new ForInstruction(definition, condition, step, runFunctions);
        }
    }

}
