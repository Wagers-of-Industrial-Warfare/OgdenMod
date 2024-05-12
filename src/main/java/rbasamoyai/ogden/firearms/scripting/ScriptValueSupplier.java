package rbasamoyai.ogden.firearms.scripting;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import rbasamoyai.ogden.index.OgdenRegistries;

public interface ScriptValueSupplier {

    @Nonnull ScriptValue run(ScriptContext context);

    static ScriptValueSupplier fromJson(JsonElement el) {
        if (el.isJsonObject()) {
            JsonObject obj = el.getAsJsonObject();
            if (GsonHelper.isStringValue(obj, "instruction")) {
                ResourceLocation loc = new ResourceLocation(GsonHelper.getAsString(obj, "instruction"));
                ScriptInstructionType type = OgdenRegistries.SCRIPT_INSTRUCTIONS.getOptional(loc).orElseThrow(() -> {
                   return new JsonParseException("Unknown firearm instruction '" + loc + "'");
                });
                return type.getSerializer().deserialize(obj);
            } else {
                return ScriptValue.VOID;
            }
        } else {
            return ScriptValue.fromJson(el);
        }
    }

}
