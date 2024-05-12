package rbasamoyai.ogden.firearms.scripting;

import java.util.function.Function;

public class ScriptInstructionType {

    private final ScriptInstructionSerializer serializer;

    public ScriptInstructionType(ScriptInstructionSerializer ser) {
        this.serializer = ser;
    }

    public ScriptInstructionType(Function<ScriptInstructionType, ScriptInstructionSerializer> serProv) {
        this.serializer = serProv.apply(this);
    }

    public ScriptInstructionSerializer getSerializer() { return this.serializer; }

}
