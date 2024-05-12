package rbasamoyai.ogden.firearms.scripting.instructions.random;

import java.util.Random;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class RandomGaussianInstruction extends RandomValueInstruction {

    private final ScriptValueSupplier mean;
    private final ScriptValueSupplier stdDev;

    public RandomGaussianInstruction(ScriptValueSupplier mean, ScriptValueSupplier stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.RANDOM_GAUSSIAN; }

    @Override
    protected ScriptValue randomValue(Random random, ScriptContext context) {
        Number meanRes = this.mean.run(context).num();
        if (meanRes == null) {
            ; // TODO log error once
            return ScriptValue.ZERO;
        }
        Number stdDevRes = this.stdDev.run(context).num();
        if (stdDevRes == null) {
            ; // TODO log error once
            return ScriptValue.ZERO;
        }
        if (stdDevRes.doubleValue() < 0) {
            ; // TODO log error once
            return ScriptValue.ZERO;
        }
        return ScriptValue.ofDouble(random.nextGaussian(meanRes.doubleValue(), stdDevRes.doubleValue()));
    }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            ScriptValueSupplier mean = obj.has("mean") ? ScriptValueSupplier.fromJson(obj.get("mean")) : ScriptValue.ZERO;
            ScriptValueSupplier stdDev = obj.has("standard_deviation") ? ScriptValueSupplier.fromJson(obj.get("standard_deviation")) : ScriptValue.ofDouble(1);
            return new RandomGaussianInstruction(mean, stdDev);
        }
    }

}
