package rbasamoyai.ogden.firearms.scripting.instructions.random;

import java.util.Random;

import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class RandomIntInstruction extends RandomBoundedNumberInstruction {

    public RandomIntInstruction(ScriptValueSupplier minBound, ScriptValueSupplier maxBound) {
        super(minBound, maxBound);
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.RANDOM_INT; }

    @Override protected boolean validRange(Number min, Number max) { return min.longValue() < max.longValue(); }

    @Override
    protected ScriptValue randomNumber(Random random, Number min, Number max) {
        return ScriptValue.ofLong(random.nextLong(min.longValue(), max.longValue()));
    }

    public static class Serializer extends RandomBoundedNumberInstruction.Serializer {
        public Serializer() { super(RandomIntInstruction::new); }
        @Override protected ScriptValue getDefaultMinValue() { return ScriptValue.ofLong(Long.MIN_VALUE); }
        @Override protected ScriptValue getDefaultMaxValue() { return ScriptValue.ofLong(Long.MAX_VALUE); }
    }

}
