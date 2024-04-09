package rbasamoyai.ogden.entities;

import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import rbasamoyai.ogden.ammunition.AmmunitionProperties;
import rbasamoyai.ogden.ammunition.AmmunitionPropertiesSerializer;

public class OgdenBulletProperties implements AmmunitionProperties {

    private final float damage;
    private final float penetration;
    private final float knockback;
    private final boolean ignoresInvulnerability;
    private final boolean rendersInvulnerable;

    public OgdenBulletProperties(float damage, float penetration, float knockback, boolean ignoresInvulnerability,
                                 boolean rendersInvulnerable) {
        this.damage = damage;
        this.penetration = penetration;
        this.knockback = knockback;
        this.ignoresInvulnerability = ignoresInvulnerability;
        this.rendersInvulnerable = rendersInvulnerable;
    }

    public OgdenBulletProperties(JsonObject obj) {
        this.damage = GsonHelper.getAsFloat(obj, "damage"); // Can be negative - healing bullets!
        this.penetration = Math.max(0, GsonHelper.getAsFloat(obj, "penetration"));
        this.knockback = Math.max(0, GsonHelper.getAsFloat(obj, "knockback"));
        this.ignoresInvulnerability = GsonHelper.getAsBoolean(obj, "ignores_invulnerability", true);
        this.rendersInvulnerable = GsonHelper.getAsBoolean(obj, "renders_invulnerable", false);
    }

    public OgdenBulletProperties(FriendlyByteBuf buf) {
        this.damage = buf.readFloat();
        this.penetration = buf.readFloat();
        this.knockback = buf.readFloat();
        this.ignoresInvulnerability = buf.readBoolean();
        this.rendersInvulnerable = buf.readBoolean();
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeFloat(this.damage)
            .writeFloat(this.penetration)
            .writeFloat(this.knockback)
            .writeBoolean(this.ignoresInvulnerability)
            .writeBoolean(this.rendersInvulnerable);
    }

    public float damage() { return this.damage; }
    public float penetration() { return this.penetration; }
    public float knockback() { return this.knockback; }
    public boolean ignoresEntityArmor() { return this.ignoresInvulnerability; }
    public boolean rendersInvulnerable() { return this.rendersInvulnerable; }

    public static class Serializer implements AmmunitionPropertiesSerializer<OgdenBulletProperties> {
        @Override public OgdenBulletProperties fromJson(ResourceLocation loc, JsonObject obj) { return new OgdenBulletProperties(obj); }
        @Override public OgdenBulletProperties fromNetwork(ResourceLocation loc, FriendlyByteBuf buf) { return new OgdenBulletProperties(buf); }
        @Override public void toNetwork(OgdenBulletProperties properties, FriendlyByteBuf buf) { properties.toNetwork(buf); }
    }

}
