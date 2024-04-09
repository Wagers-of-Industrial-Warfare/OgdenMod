package rbasamoyai.ogden.ammunition;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface AmmunitionPropertiesSerializer<T extends AmmunitionProperties> {

    T fromJson(ResourceLocation loc, JsonObject obj);
    T fromNetwork(ResourceLocation loc, FriendlyByteBuf buf);
    void toNetwork(T properties, FriendlyByteBuf buf);

}
