package rbasamoyai.ogden.firearms.scripting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public interface ScriptInstructionSerializer {
    ScriptInstruction deserialize(JsonObject obj) throws JsonParseException;
}
