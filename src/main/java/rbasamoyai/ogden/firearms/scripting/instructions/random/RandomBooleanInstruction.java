package rbasamoyai.ogden.firearms.scripting.instructions.random;

import java.util.Random;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionSerializer;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class RandomBooleanInstruction extends RandomValueInstruction {

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.RANDOM_BOOLEAN; }
    @Override protected ScriptValue randomValue(Random random, ScriptContext context) { return ScriptValue.bool(random.nextBoolean()); }

    public static class Serializer implements ScriptInstructionSerializer {
        @Override
        public ScriptInstruction deserialize(JsonObject obj) throws JsonParseException {
            return new RandomBooleanInstruction();
        }
    }

}
