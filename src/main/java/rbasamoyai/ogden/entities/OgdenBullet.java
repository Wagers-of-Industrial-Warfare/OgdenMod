package rbasamoyai.ogden.entities;

import org.jetbrains.annotations.Nullable;

import com.mojang.math.Constants;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.ogden.OgdenConfigs;
import rbasamoyai.ogden.OgdenMod;
import rbasamoyai.ogden.ammunition.AmmunitionPropertiesHandler;
import rbasamoyai.ogden.base.OgdenRegistryUtils;
import rbasamoyai.ogden.index.OgdenEntityTypes;

public class OgdenBullet extends OgdenProjectile<OgdenBulletProperties> implements AmmunitionPropertiesEntity<OgdenBulletProperties> {

    private static final EntityDataAccessor<Boolean> IS_TRACER = SynchedEntityData.defineId(OgdenBullet.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> TRACER_COLOR = SynchedEntityData.defineId(OgdenBullet.class, EntityDataSerializers.INT);

    private Item ammunitionItem = Items.AIR;
    private Item firearmItem = Items.AIR;

    private float penetrationDamage = 0;


	public OgdenBullet(EntityType<? extends OgdenBullet> entityType, Level level) { super(entityType, level); }

    public OgdenBullet(Level level, Item ammunitionItem, Item firearmItem, double posX, double posY, double posZ) {
        super(OgdenEntityTypes.BULLET.get(), level);
        this.setPos(posX, posY, posZ);
        this.ammunitionItem = ammunitionItem;
        this.firearmItem = firearmItem;
    }

    public OgdenBullet(Level level, Item ammunitionItem, Item firearmItem, LivingEntity living) {
        this(level, ammunitionItem, firearmItem, living.getX(), living.getEyeY() - 0.1d, living.getZ());
        this.setOwner(living);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_TRACER, false);
        this.entityData.define(TRACER_COLOR, 0xFFD800); // A yellow-orange
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Tracer", this.isTracer());
        if (this.isTracer()) tag.putInt("TracerColor", this.getPackedTracerColor());
        tag.putFloat("PenetrationDamage", this.penetrationDamage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTracer(tag.getBoolean("Tracer"));
        if (this.isTracer() && tag.contains("TracerColor", Tag.TAG_INT)) this.setTracerColor(tag.getInt("TracerColor"));
        this.penetrationDamage = tag.getFloat("PenetrationDamage");
    }

    @Override
    public void writeProjectileSyncData(CompoundTag tag) {
        super.writeProjectileSyncData(tag);
        tag.putString("Bullet", OgdenRegistryUtils.getItemId(this.ammunitionItem).toString());
        tag.putString("Firearm", OgdenRegistryUtils.getItemId(this.firearmItem).toString());
    }

    @Override
    public void readProjectileSyncData(CompoundTag tag) {
        super.readProjectileSyncData(tag);
        this.ammunitionItem = tag.contains("Bullet", Tag.TAG_STRING) ? OgdenRegistryUtils.getItemFromId(new ResourceLocation(tag.getString("Bullet"))) : Items.AIR;
        this.firearmItem = tag.contains("Firearm", Tag.TAG_STRING) ? OgdenRegistryUtils.getItemFromId(new ResourceLocation(tag.getString("Firearm"))) : Items.AIR;
    }

    public void setTracer(boolean tracer) { this.entityData.set(IS_TRACER, tracer); }
    public boolean isTracer() { return this.entityData.get(IS_TRACER); }

    public void setTracerColor(int r, int g, int b) { this.setTracerColor(r << 16 | g << 8 | b); }
    public void setTracerColor(int packed) { this.entityData.set(TRACER_COLOR, packed); }
    public int getPackedTracerColor() { return this.entityData.get(TRACER_COLOR); }

    @Override
    protected void onTickRotate() {
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        Vec3 vel = this.getDeltaMovement();
        if (vel.lengthSqr() > 0.005d) {
            this.setYRot((float) (Mth.atan2(vel.x, vel.z) * (double) Constants.RAD_TO_DEG));
            this.setXRot((float) (Mth.atan2(vel.y, vel.horizontalDistance()) * (double) Constants.RAD_TO_DEG));
        }

        this.setYRot(this.getYRot());
        this.setXRot(this.getXRot());
    }

    @Override
    protected void onHitEntity(Entity entity, double hitTime) {
        //if (this.getProjectileMass() <= 0) return;
        if (this.isToBeRemoved())
            return;
        if (!this.level.isClientSide) {
            OgdenBulletProperties properties = this.getAmmunitionProperties();
            entity.setDeltaMovement(this.getDeltaMovement().scale(this.getKnockback(entity)));
            DamageSource source = this.getEntityDamage();

            // TODO better bullet compat for other mods -- first aid and others
            Vec3 hitPos = this.position().add(this.getDeltaMovement().scale(hitTime));
            float damage = this.getDamage(entity, hitPos, hitTime);
            float entityHealth = entity instanceof LivingEntity living ? living.getHealth() : 0;

            if (properties == null || properties.ignoresEntityArmor()) entity.invulnerableTime = 0;
            entity.hurt(source, damage);
            if (properties == null || !properties.rendersInvulnerable()) entity.invulnerableTime = 0;

            entityHealth -= entity instanceof LivingEntity living ? living.getHealth() : 0;
            float damageResisted = damage - entityHealth;
            this.penetrationDamage += damage + damageResisted;
            if (properties == null || this.penetrationDamage >= properties.penetration()) {
                this.markForFutureRemoval();
            }
        }
    }

    protected float getDamage(Entity target, Vec3 hitPos, double hitTime) {
        float damage = 0;
        OgdenBulletProperties properties = this.getAmmunitionProperties();
        if (properties != null) damage += properties.damage();
        double finalDisplacement = this.displacement + hitPos.subtract(this.position()).length(); // TODO feed this into firearm damage model
        return damage;
    }

    protected float getKnockback(Entity target) {
        OgdenBulletProperties properties = this.getAmmunitionProperties();
        return properties == null ? 0 : properties.knockback();
    }

    protected DamageSource getEntityDamage() {
        return new IndirectEntityDamageSource(OgdenMod.MOD_ID + ".shot", this, null);
    }

    @Nullable
    @Override
    public OgdenBulletProperties getAmmunitionProperties() {
        return AmmunitionPropertiesHandler.getProperties(this.ammunitionItem) instanceof OgdenBulletProperties p ? p : null;
    }

    @Override
    public boolean canChunkload() {
        return OgdenConfigs.server().projectilesCanLoadChunks.get();
    }

}
