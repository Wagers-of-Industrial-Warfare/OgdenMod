package rbasamoyai.ogden.firearms.scripting.instructions.random;

import java.util.Random;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class RandomFloatInstruction extends RandomBoundedNumberInstruction {

    public RandomFloatInstruction(ScriptValueSupplier minBound, ScriptValueSupplier maxBound) {
        super(minBound, maxBound);
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.RANDOM_FLOAT; }

    @Override protected boolean validRange(Number min, Number max) { return min.doubleValue() < max.doubleValue(); }

    @Override
    protected ScriptValue randomNumber(Random random, Number min, Number max) {
        return ScriptValue.ofDouble(random.nextDouble(min.doubleValue(), max.doubleValue()));
    }

    public static class Serializer extends RandomBoundedNumberInstruction.Serializer {
        public Serializer() { super(RandomFloatInstruction::new); }
        @Override protected ScriptValue getDefaultMinValue() { return ScriptValue.ofLong(Long.MIN_VALUE); }
        @Override protected ScriptValue getDefaultMaxValue() { return ScriptValue.ofLong(Long.MAX_VALUE); }
    }

}
