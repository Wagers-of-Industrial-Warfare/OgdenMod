package rbasamoyai.ogden.firearms.scripting;

import java.util.List;

public class FirearmState {

    private final String id;
    private final List<ScriptInstruction> tickedInstructions;

    public FirearmState(String id, List<ScriptInstruction> tickedInstructions) {
        this.id = id;
        this.tickedInstructions = tickedInstructions;
    }

    public String id() { return this.id; }

    public void tick(ScriptContext context) {
        for (ScriptInstruction instruction : this.tickedInstructions) {
            instruction.run(context);
        }
    }

    public FirearmState copyWithId(String id) { return new FirearmState(id, this.tickedInstructions); }

}
