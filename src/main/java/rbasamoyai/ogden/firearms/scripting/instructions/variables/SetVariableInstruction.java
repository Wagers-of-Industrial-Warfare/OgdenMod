package rbasamoyai.ogden.firearms.scripting.instructions.variables;

import javax.annotation.Nonnull;

import rbasamoyai.ogden.firearms.scripting.ScriptContext;
import rbasamoyai.ogden.firearms.scripting.ScriptInstruction;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;
import rbasamoyai.ogden.firearms.scripting.ScriptValue;
import rbasamoyai.ogden.firearms.scripting.ScriptValueSupplier;
import rbasamoyai.ogden.firearms.scripting.instructions.TwoObjectOperatorInstruction;
import rbasamoyai.ogden.index.OgdenScriptInstructionTypes;

public class SetVariableInstruction implements ScriptInstruction {

    private final ScriptValueSupplier id;
    private final ScriptValueSupplier value;

    public SetVariableInstruction(ScriptValueSupplier id, ScriptValueSupplier value) {
        this.id = id;
        this.value = value;
    }

    @Override public ScriptInstructionType type() { return OgdenScriptInstructionTypes.SET_VARIABLE; }

    @Nonnull
    @Override
    public ScriptValue run(ScriptContext context) {
        String idRes = this.id.run(context).str();
        ScriptValue result = this.value.run(context);
        if (idRes == null) {
            ; // TODO: log error once
            return result;
        }
        return context.setVariable(idRes, result);
    }

    public static class Serializer extends TwoObjectOperatorInstruction.Serializer {
        public Serializer() { super(SetVariableInstruction::new); }
        @Override protected String getLeftKey() { return "id"; }
        @Override protected String getRightKey() { return "value"; }
    }

}
