package rbasamoyai.ogden.base;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;

public abstract class LazyDataLoader<JSON extends JsonElement, OBJ> {

    private final Map<ResourceLocation, JSON> jsonLoc = new Object2ObjectOpenHashMap<>();
    private final Map<ResourceLocation, OBJ> objLoc = new Object2ObjectOpenHashMap<>();

    public void holdElement(ResourceLocation loc, JSON jsonEl) {
        this.jsonLoc.put(loc, jsonEl);
    }

    public OBJ loadObject(ResourceLocation id) throws JsonParseException {
        if (!this.objLoc.containsKey(id)) {
            if (!this.jsonLoc.containsKey(id))
                throw new JsonParseException("Referenced JSON element " + id + " not loaded");
            this.objLoc.put(id, this.parseJson(this.jsonLoc.get(id), id));
        }
        return this.objLoc.get(id);
    }

    protected abstract OBJ parseJson(JSON jsonEl, ResourceLocation id) throws JsonParseException;

    public int getFoundElementsCount() { return this.jsonLoc.size(); }
    public int getLoadedObjectsCount() { return this.objLoc.size(); }

}
