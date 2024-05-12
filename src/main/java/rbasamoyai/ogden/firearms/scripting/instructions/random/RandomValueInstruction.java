package rbasamoyai.ogden.firearms.scripting.instructions.random;

import java.util.Random;

import javax.annotation.Nonnull;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;

public abstract class RandomValueInstruction implements ScriptInstruction {

    @Nonnull
    @Override
    public final ScriptValue run(ScriptContext context) {
        return this.randomValue(context.level().getRandom(), context);
    }

    protected abstract ScriptValue randomValue(Random random, ScriptContext context);

}
