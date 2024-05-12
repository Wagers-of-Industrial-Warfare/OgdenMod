package rbasamoyai.ogden.firearms.scripting.instructions.random;

import java.util.Random;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;

public abstract class RandomBoundedNumberInstruction extends RandomValueInstruction {

    private final ScriptValueSupplier minBound;
    private final ScriptValueSupplier maxBound;

    protected RandomBoundedNumberInstruction(ScriptValueSupplier minBound, ScriptValueSupplier maxBound) {
        this.minBound = minBound;
        this.maxBound = maxBound;
    }

    @Override
    protected final ScriptValue randomValue(Random random, ScriptContext context) {
        Number minRes = this.minBound.run(context).num();
        if (minRes == null) {
            ; // TODO log error once
            return ScriptValue.ZERO;
        }
        Number maxRes = this.maxBound.run(context).num();
        if (maxRes == null) {
            ; // TODO log error once
            return ScriptValue.ZERO;
        }
        if (!this.validRange(minRes, maxRes)) {
            ; // TODO log error once
            return ScriptValue.ZERO;
        }
        return this.randomNumber(random, minRes, maxRes);
    }

    protected abstract boolean validRange(Number min, Number max);
    protected abstract ScriptValue randomNumber(Random random, Number min, Number max);

    public static abstract class Serializer implements ScriptInstructionSerializer {
        private final BiFunction<ScriptValueSupplier, ScriptValueSupplier, ScriptInstruction> constructor;

        protected Serializer(BiFunction<ScriptValueSupplier, ScriptValueSupplier, ScriptInstruction> constructor) {
            this.constructor = constructor;
        }

        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            boolean hasMinBound = obj.has("min");
            boolean hasMaxBound = obj.has("max");
            ScriptValueSupplier minBound;
            ScriptValueSupplier maxBound;
            if (hasMinBound && hasMaxBound) {
                minBound = ScriptValueSupplier.fromJson(obj.get("min"));
                maxBound = ScriptValueSupplier.fromJson(obj.get("max"));
            } else if (hasMinBound) {
                minBound = ScriptValueSupplier.fromJson(obj.get("min"));
                maxBound = ScriptValue.ZERO;
            } else if (hasMaxBound) {
                minBound = ScriptValue.ZERO;
                maxBound = ScriptValueSupplier.fromJson(obj.get("max"));
            } else {
                minBound = this.getDefaultMinValue();
                maxBound = this.getDefaultMaxValue();
            }
            return this.constructor.apply(minBound, maxBound);
        }

        protected abstract ScriptValue getDefaultMinValue();
        protected abstract ScriptValue getDefaultMaxValue();
    }

}
