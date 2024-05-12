package rbasamoyai.ogden.firearms.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import rbasamoyai.ogden.base.LazyDataLoader;
import rbasamoyai.ogden.base.OgdenRegistryUtils;
import rbasamoyai.ogden.firearms.OgdenFirearmItem;
import rbasamoyai.ogden.index.OgdenRegistries;

public class FirearmScriptHandler {

    private static final Map<Item, IFirearmScript> SCRIPTS = new Reference2ObjectOpenHashMap<>();

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        private static final Gson GSON = new Gson();
        public static final Logger LOGGER = LogUtils.getLogger();
        public static final ReloadListener INSTANCE = new ReloadListener();

        ReloadListener() { super(GSON, "firearm_properties/scripts"); }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller profiler) {
            SCRIPTS.clear();

            Map<Item, ResourceLocation> itemLocations = new Reference2ObjectOpenHashMap<>();
            Map<ResourceLocation, IFirearmScript> builtInScripts = new Object2ObjectOpenHashMap<>();
            StateScriptLoader stateLoader = new StateScriptLoader();
            MainScriptLoader mainLoader = new MainScriptLoader(stateLoader, builtInScripts);

            for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
                ResourceLocation loc = entry.getKey();
                JsonElement el = entry.getValue();
                try {
                    if (!el.isJsonObject())
                        throw new JsonParseException("Firearm script file '" + loc + "' is not properly formatted");
                    JsonObject obj = el.getAsJsonObject();
                    String scriptType = GsonHelper.getAsString(obj, "script_type");
                    Optional<Item> itemOp = OgdenRegistryUtils.getOptionalItemFromId(loc);
                    if (itemOp.isPresent()) {
                        Item item = itemOp.get();
                        if (!(item instanceof OgdenFirearmItem))
                            LOGGER.warn("Warning: item '" + loc + "' is not an Ogden firearm item and will not work with a firearm script");
                        if (!scriptType.equals("main_script"))
                            throw new JsonParseException("Firearm script file for item " + loc + "expected to have script_type=main_script");
                        itemLocations.put(item, loc);
                        mainLoader.holdElement(loc, obj);
                    } else if (scriptType.equals("main_script")) {
                        mainLoader.holdElement(loc, obj);
                    } else if (scriptType.equals("state")) {
                        stateLoader.holdElement(loc, obj);
                    }
                } catch (Exception e) {
                    LOGGER.error("Error loading firearm script file " + loc + ".json:" + e.getMessage());
                }
            }
            LOGGER.debug("Found " + itemLocations.size() + " firearm item action script(s)");
            LOGGER.debug("Found " + stateLoader.getFoundElementsCount() + " firearm state script(s)");
            LOGGER.debug("Found " + mainLoader.getFoundElementsCount() + " firearm action script(s)");

            for (Map.Entry<Item, ResourceLocation> entry : itemLocations.entrySet()) {
                try {
                    SCRIPTS.put(entry.getKey(), mainLoader.loadObject(entry.getValue()));
                } catch (Exception e) {
                    LOGGER.error("Error loading firearm script file " + entry.getValue() + ".json: " + e.getMessage());
                }
            }
            LOGGER.debug("Loaded " + SCRIPTS.size() + " firearm item script(s) and " + mainLoader.getLoadedObjectsCount() + " firearm script(s) of all types");
        }
    }

    private static class MainScriptLoader extends LazyDataLoader<JsonObject, IFirearmScript> {
        private final StateScriptLoader stateLoader;
        private final Map<ResourceLocation, IFirearmScript> builtInScripts;
        private final Multimap<ResourceLocation, ResourceLocation> scriptDependents = HashMultimap.create();

        private MainScriptLoader(StateScriptLoader stateLoader, Map<ResourceLocation, IFirearmScript> builtInScripts) {
            this.stateLoader = stateLoader;
            this.builtInScripts = builtInScripts;
        }

        @Override
        public IFirearmScript loadObject(ResourceLocation id) throws JsonParseException {
            return this.builtInScripts.containsKey(id) ? this.builtInScripts.get(id) : super.loadObject(id);
        }

        @Override
        protected IFirearmScript parseJson(JsonObject obj, ResourceLocation id) throws JsonParseException {
            IFirearmScript parentScript = null;
            if (obj.has("parent")) {
                ResourceLocation parentLoc = new ResourceLocation(GsonHelper.getAsString(obj, "parent"));
                if (this.hasCircularDependency(parentLoc, id))
                    throw new JsonParseException("Circular dependency found: " + parentLoc + " depends on " + id + " while " + id + " depends on " + parentLoc);
                parentScript = this.loadObject(parentLoc);
            }

            String equipState = null;
            Map<String, FirearmState> statesMap = new HashMap<>();
            if (parentScript instanceof FirearmScript merge) {
                equipState = merge.equipState();
                statesMap.putAll(merge.states());
            } else if (parentScript != null) {
                return parentScript;
            }
            if (obj.has("equip_state"))
                equipState = GsonHelper.getAsString(obj, "equip_state");
            if (equipState == null)
                throw new JsonParseException("Firearm script requires 'equip_state' parameter or a parent with an 'equip_state' parameter");

            if (obj.has("states") || statesMap.isEmpty()) {
                JsonObject statesObj = GsonHelper.getAsJsonObject(obj, "states");
                for (Map.Entry<String, JsonElement> entry : statesObj.entrySet()) {
                    String key = entry.getKey();
                    JsonElement stateEl = entry.getValue();
                    if (GsonHelper.isStringValue(stateEl)) {
                        ResourceLocation loc = new ResourceLocation(stateEl.getAsString());
                        try {
                            statesMap.put(key, this.stateLoader.loadObject(loc).copyWithId(key));
                        } catch (JsonParseException e) {
                            throw new JsonParseException("Invalid firearm state '" + key + "': " + e.getMessage());
                        }
                    } else if (stateEl.isJsonObject()) {
                        statesMap.put(key, parseStateObject(stateEl.getAsJsonObject(), key));
                    } else {
                        throw new JsonParseException("Invalid firearm state '" + key + "': expected string pointing to state script file or JSON object describing state");
                    }
                }
            }

            if (!statesMap.containsKey(equipState))
                throw new JsonParseException("Firearm script 'states' parameter does not have specified 'equip_state' parameter '" + equipState + "'");
            return new FirearmScript(equipState, statesMap);
        }

        private boolean hasCircularDependency(ResourceLocation parentId, ResourceLocation childId) {
            if (this.scriptDependents.get(childId).contains(parentId))
                return true;
            Set<ResourceLocation> parentsOfParent = new HashSet<>();
            parentsOfParent.add(parentId);
            for (Map.Entry<ResourceLocation, ResourceLocation> entry : this.scriptDependents.entries()) {
                if (entry.getValue().equals(parentId))
                    parentsOfParent.add(entry.getKey());
            }
            Collection<ResourceLocation> childrenOfChild = this.scriptDependents.get(childId);
            childrenOfChild.add(childId);
            for (ResourceLocation ancestorId : parentsOfParent) {
                this.scriptDependents.putAll(ancestorId, childrenOfChild);
            }
            return false;
        }
    }

    private static class StateScriptLoader extends LazyDataLoader<JsonObject, FirearmState> {
        @Override
        protected FirearmState parseJson(JsonObject obj, ResourceLocation id) throws JsonParseException {
            return parseStateObject(obj, id.toString());
        }
    }

    private static FirearmState parseStateObject(JsonObject obj, String id) {
        JsonArray instructionsJson = GsonHelper.getAsJsonArray(obj, "ticked_instructions");
        if (instructionsJson.isEmpty())
            throw new JsonParseException("Invalid firearm state '" + id + "' has no valid 'ticked_instructions' list");
        List<ScriptInstruction> instructions = new ArrayList<>(instructionsJson.size());
        for (int i = 0; i < instructionsJson.size(); ++i) {
            JsonElement el = instructionsJson.get(i);
            if (!el.isJsonObject())
                throw new JsonParseException("Invalid state instruction at index " + i + ": expected JSON object");
            ResourceLocation loc = new ResourceLocation(GsonHelper.getAsString(obj, "instruction"));
            ScriptInstructionType type = OgdenRegistries.SCRIPT_INSTRUCTIONS.getOptional(loc).orElseThrow(() -> {
                return new JsonParseException("Unknown firearm instruction '" + loc + "'");
            });
            instructions.add(type.getSerializer().deserialize(obj));
        }
        return new FirearmState(id, instructions);
    }


    @Nullable public static IFirearmScript getScript(ItemStack stack) { return getScript(stack.getItem()); }
    @Nullable public static IFirearmScript getScript(Item item) { return SCRIPTS.get(item); }

}
