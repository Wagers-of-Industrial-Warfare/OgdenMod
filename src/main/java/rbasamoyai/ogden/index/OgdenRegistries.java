package rbasamoyai.ogden.index;

import com.mojang.serialization.Lifecycle;

import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import rbasamoyai.ogden.OgdenMod;
import rbasamoyai.ogden.firearms.scripting.ScriptInstructionType;

public class OgdenRegistries {

    public static class Keys {
        public static final ResourceKey<Registry<ScriptInstructionType>> SCRIPT_INSTRUCTIONS = key("script_instructions");

        private static <T> ResourceKey<Registry<T>> key(String id) { return ResourceKey.createRegistryKey(OgdenMod.resource(id)); }
    }

    @SuppressWarnings("rawtypes")
    private static <T> MappedRegistry<T> makeRegistrySimple(ResourceKey<? extends Registry<T>> key) {
        MappedRegistry<T> registry = new MappedRegistry<>(key, Lifecycle.stable(), null);
        WritableRegistry root = (WritableRegistry) Registry.REGISTRY;
        root.register(key, registry, Lifecycle.stable());
        return registry;
    }

    public static MappedRegistry<ScriptInstructionType> SCRIPT_INSTRUCTIONS;

    private static boolean initialized = false;

    public static void initRegistries() {
        if (initialized) return;
        SCRIPT_INSTRUCTIONS = makeRegistrySimple(Keys.SCRIPT_INSTRUCTIONS);
        initialized = true;
    }

}
