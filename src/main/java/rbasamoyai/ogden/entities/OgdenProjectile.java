package rbasamoyai.ogden.entities;

import java.util.Map;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import rbasamoyai.ogden.ammunition.AmmunitionProperties;
import rbasamoyai.ogden.network.ClientboundSyncOgdenProjectile;
import rbasamoyai.ogden.network.OgdenNetwork;
import rbasamoyai.ritchiesprojectilelib.PreciseProjectile;
import rbasamoyai.ritchiesprojectilelib.RitchiesProjectileLib;

public abstract class OgdenProjectile<T extends AmmunitionProperties> extends Projectile
    implements AmmunitionPropertiesEntity<T>, PreciseProjectile {

    private static final EntityDataAccessor<Float> COLLISION_SUBTICK = SynchedEntityData.defineId(OgdenProjectile.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> REMOVE_TICK_COUNTER = SynchedEntityData.defineId(OgdenProjectile.class, EntityDataSerializers.INT);

    protected double displacement = 0;

    protected OgdenProjectile(EntityType<? extends OgdenProjectile<?>> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(COLLISION_SUBTICK, -1.0f);
        this.entityData.define(REMOVE_TICK_COUNTER, 0);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level.isClientSide) {
            OgdenNetwork.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new ClientboundSyncOgdenProjectile(this));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.writeProjectileSyncData(tag);
        float subTick = this.getCollisionSubTick();
        if (0 <= subTick && subTick <= 1) tag.putFloat("CollisionSubTick", this.getCollisionSubTick());
        if (this.getRemovalTicks() > 0) tag.putInt("RemovalTicks", this.getRemovalTicks());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readProjectileSyncData(tag);
        this.setCollisionSubTick(tag.contains("CollisionSubTick", Tag.TAG_FLOAT) ? tag.getFloat("CollisionSubTick") : -1.0f);
        if (tag.contains("RemovalTicks", Tag.TAG_INT)) this.entityData.set(REMOVE_TICK_COUNTER, Mth.clamp(tag.getInt("RemovalTicks"), 0, 3));
    }

    public void writeProjectileSyncData(CompoundTag tag) {
        tag.putDouble("Displacement", this.displacement);
    }

    public void readProjectileSyncData(CompoundTag tag) {
        this.displacement = tag.getDouble("Displacement");
    }

    public void tick() {
        boolean toBeRemoved = this.isToBeRemoved();
        if (toBeRemoved) {
            int removalTicks = this.getRemovalTicks();
            this.entityData.set(REMOVE_TICK_COUNTER, ++removalTicks);
            if (removalTicks == 3) {
                this.discard();
                return;
            }
        }
        ChunkPos cpos = new ChunkPos(this.blockPosition());
        if (this.level.isClientSide || this.level.hasChunk(cpos.x, cpos.z)) {
            if (this.level instanceof ServerLevel slevel) {
                if (this.canChunkload()) {
                    RitchiesProjectileLib.queueForceLoad(slevel, this, cpos.x, cpos.z, false);
                }
            }
            super.tick();

            if (!toBeRemoved) this.clipAndDamage();

            this.onTickRotate();

            Vec3 uel = this.getDeltaMovement();
            Vec3 vel = uel;
            Vec3 oldPos = this.position();
            Vec3 newPos = oldPos.add(vel);
            if (!this.isNoGravity()) vel = vel.add(0.0d, this.getGravity(), 0.0d);
            vel = vel.scale(this.getDrag());
            this.setDeltaMovement(vel);
            Vec3 position = newPos.add(vel.subtract(uel).scale(0.5));
            this.setPos(position);

            this.displacement += newPos.distanceTo(oldPos);

            if (this.level instanceof ServerLevel slevel && !this.isRemoved()) {
                if (this.canChunkload()) {
                    ChunkPos cpos1 = new ChunkPos(this.blockPosition());
                    RitchiesProjectileLib.queueForceLoad(slevel, this, cpos1.x, cpos1.z, true);
                }
            }
        }
    }

    public boolean canChunkload() { return false; }

    public double getDisplacement() { return this.displacement; }

    protected void onTickRotate() {}

    public double getDrag() {
        return 0.99; // TODO: how to config this?
    }

    public double getGravity() {
        return -0.025f; // TODO: how to config this?
    }

    protected void clipAndDamage() {
        ProjectileContext projCtx = new ProjectileContext(this/*, CBCConfigs.SERVER.munitions.damageRestriction.get()*/);

        Vec3 pos = this.position();
        Vec3 start = pos;
        double reach = Math.max(this.getBbWidth(), this.getBbHeight()) * 0.5;

        Vec3 vel = this.getDeltaMovement();

        double t = 1;
        int MAX_ITER = 20;
        for (int p = 0; p < MAX_ITER; ++p) {
            boolean breakEarly = false;
            Vec3 scaledVel = vel.scale(t);
            double scaledLength = scaledVel.length();
            if (scaledLength < 1e-2d) break;

            Vec3 end = start.add(scaledVel);
            BlockHitResult bResult = this.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (bResult.getType() != HitResult.Type.MISS) end = bResult.getLocation();

            AABB currentMovementRegion = this.getBoundingBox().expandTowards(end.subtract(start)).inflate(1).move(start.subtract(pos));

            AABB thisBB = this.getBoundingBox();
            for (Entity target : this.level.getEntities(this, currentMovementRegion, e -> {
                return !projCtx.hasHitEntity(e) && this.canHitEntity(e);
            })) {
                AABB bb = target.getBoundingBox();
                if (bb.intersects(thisBB)) {
                    projCtx.addEntity(target, 0);
                    projCtx.setFinalHitTime(0);
                } else {
                    Optional<Vec3> op = bb.inflate(reach).clip(start, end);
                    if (op.isPresent()) {
                        Vec3 hitLoc = op.get();
                        Vec3 disp = hitLoc.subtract(start);
                        double subTime = disp.length() / scaledLength;
                        double currentTime = 1 - t + subTime * t;
                        projCtx.addEntity(target, currentTime);
                        if (currentTime < projCtx.getFinalHitTime()) // TODO: rules on penetrating multiple entities
                            projCtx.setFinalHitTime(currentTime);
                    }
                }
            }

            Vec3 hitLoc = end;
            if (bResult.getType() != HitResult.Type.MISS) {
                BlockPos bpos = bResult.getBlockPos().immutable();
                BlockState state = this.level.getChunkAt(bpos).getBlockState(bpos);

                boolean flag1 = state.isAir();
                //boolean flag1 = projCtx.getLastState().isAir();
                if (!flag1) {
                    projCtx.setLastState(state);
                    state.onProjectileHit(this.level, state, bResult, this);
                    breakEarly = true;

                    // TODO: block breaking

                    Vec3 disp = hitLoc.subtract(start);
                    double subTime = disp.length() / scaledLength;
                    double currentTime = 1 - t + subTime * t;
                    if (currentTime < projCtx.getFinalHitTime())
                        projCtx.setFinalHitTime(currentTime);
                }
            }
            Vec3 disp = hitLoc.subtract(start);
            start = hitLoc;
            if (this.onClip(projCtx, start)) break;
            if (breakEarly) break;
            t -= disp.length() / scaledLength;
            if (t <= 1e-2d) break;
        }

        double finalHitTime = projCtx.getFinalHitTime();
        for (Map.Entry<Entity, Double> entry : projCtx.hitEntities().entrySet()) {
            if (entry.getValue() <= finalHitTime)
                this.onHitEntity(entry.getKey(), entry.getValue());
        }
        if (0 <= finalHitTime && finalHitTime <= 1) {
            this.markForFutureRemoval();
            this.setCollisionSubTick((float) finalHitTime);
        }
    }

    @Override
    public boolean ignoreExplosion() {
        return this.isToBeRemoved() || super.ignoreExplosion();
    }

    protected boolean onClip(ProjectileContext ctx, Vec3 pos) {
        return false;
    }

    protected abstract void onHitEntity(Entity entity, double hitTime);

    public void setCollisionSubTick(float subTick) { this.entityData.set(COLLISION_SUBTICK, 0 <= subTick && subTick <= 1 ? subTick : -1); }
    public float getCollisionSubTick() { return this.entityData.get(COLLISION_SUBTICK); }

    protected void markForFutureRemoval() { this.entityData.set(REMOVE_TICK_COUNTER, 1); }
    public boolean isToBeRemoved() { return this.isRemoved() || this.getRemovalTicks() > 0; }
    public int getRemovalTicks() { return this.entityData.get(REMOVE_TICK_COUNTER); }

}
