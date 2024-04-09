package rbasamoyai.ogden.entities;

import org.jetbrains.annotations.Nullable;

import com.mojang.math.Constants;

import net.minecraft.core.Registry;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.ogden.OgdenMod;
import rbasamoyai.ogden.ammunition.AmmunitionPropertiesHandler;
import rbasamoyai.ogden.index.OgdenEntityTypes;

public class OgdenBullet extends OgdenProjectile<OgdenBulletProperties> implements AmmunitionPropertiesEntity<OgdenBulletProperties> {

    private static final EntityDataAccessor<Boolean> IS_TRACER = SynchedEntityData.defineId(OgdenBullet.class, EntityDataSerializers.BOOLEAN);

    private Item ammunitionItem = Items.AIR;
    private Item firearmItem = Items.AIR;

    private float penetrationDamage = 0;


	public OgdenBullet(EntityType<? extends OgdenBullet> entityType, Level level) { super(entityType, level); }

    public OgdenBullet(Level level, Item ammunitionItem, Item firearmItem) {
        super(OgdenEntityTypes.BULLET.get(), level);
        this.ammunitionItem = ammunitionItem;
        this.firearmItem = firearmItem;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_TRACER, false);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Tracer", this.isTracer());
        tag.putFloat("PenetrationDamage", this.penetrationDamage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setTracer(tag.getBoolean("Tracer"));
        this.penetrationDamage = tag.getFloat("PenetrationDamage");
    }

    @Override
    public void writeProjectileSyncData(CompoundTag tag) {
        super.writeProjectileSyncData(tag);
        tag.putString("Bullet", Registry.ITEM.getKey(this.ammunitionItem).toString());
        tag.putString("Firearm", Registry.ITEM.getKey(this.firearmItem).toString());
    }

    @Override
    public void readProjectileSyncData(CompoundTag tag) {
        super.readProjectileSyncData(tag);
        this.ammunitionItem = tag.contains("Bullet", Tag.TAG_STRING) ? Registry.ITEM.get(new ResourceLocation(tag.getString("Bullet"))) : Items.AIR;
        this.firearmItem = tag.contains("Firearm", Tag.TAG_STRING) ? Registry.ITEM.get(new ResourceLocation(tag.getString("Firearm"))) : Items.AIR;
    }

    public void setTracer(boolean tracer) { this.entityData.set(IS_TRACER, tracer); }
    public boolean isTracer() { return this.entityData.get(IS_TRACER); }

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
    protected void onHitEntity(Entity entity) {
        //if (this.getProjectileMass() <= 0) return;
        if (this.isRemoved())
            return;
        if (!this.level.isClientSide) {
            OgdenBulletProperties properties = this.getAmmunitionProperties();
            entity.setDeltaMovement(this.getDeltaMovement().scale(this.getKnockback(entity)));
            DamageSource source = this.getEntityDamage();

            // TODO better bullet compat for other mods -- first aid and others
            Vec3 start = this.position();
            Vec3 end = start.add(this.getDeltaMovement());
            float w = this.getBbWidth() * 0.55f;
            float h = this.getBbHeight() * 0.55f;
            AABB box = entity.getBoundingBox().inflate(w, h, w);

            Vec3 hitPos = box.clip(start, end).orElse(entity.position());

            float damage = this.getDamage(entity, hitPos);
            float entityHealth = entity instanceof LivingEntity living ? living.getHealth() : 0;

            if (properties == null || properties.ignoresEntityArmor()) entity.invulnerableTime = 0;
            entity.hurt(source, damage);
            if (properties == null || !properties.rendersInvulnerable()) entity.invulnerableTime = 0;

            entityHealth -= entity instanceof LivingEntity living ? living.getHealth() : 0;
            float damageResisted = damage - entityHealth;
            this.penetrationDamage += damage + damageResisted;
            if (properties == null || this.penetrationDamage >= properties.penetration()) {
                this.discard();
            }
        }
    }

    protected float getDamage(Entity target, Vec3 hitPos) {
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

}
