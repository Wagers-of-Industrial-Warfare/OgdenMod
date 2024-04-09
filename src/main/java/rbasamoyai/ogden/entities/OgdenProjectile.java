package rbasamoyai.ogden.entities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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

    protected double displacement = 0;

    protected OgdenProjectile(EntityType<? extends OgdenProjectile<?>> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
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
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.readProjectileSyncData(tag);
    }

    public void writeProjectileSyncData(CompoundTag tag) {
        tag.putDouble("Displacement", this.displacement);
    }

    public void readProjectileSyncData(CompoundTag tag) {
        this.displacement = tag.getDouble("Displacement");
    }

    public void tick() {
        ChunkPos cpos = new ChunkPos(this.blockPosition());
        if (this.level.isClientSide || this.level.hasChunk(cpos.x, cpos.z)) {
            if (this.level instanceof ServerLevel slevel) {
                if (this.canChunkload()) {
                    RitchiesProjectileLib.queueForceLoad(slevel, this, cpos.x, cpos.z, false);
                }
            }
            super.tick();

            this.clipAndDamage();

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
        return -0.05f; // TODO: how to config this?
    }

    protected void clipAndDamage() {
        ProjectileContext projCtx = new ProjectileContext(this/*, CBCConfigs.SERVER.munitions.damageRestriction.get()*/);

        Vec3 pos = this.position();
        Vec3 start = pos;
        double reach = Math.max(this.getBbWidth(), this.getBbHeight()) * 0.5;

        Vec3 vel = this.getDeltaMovement();

        double t = 1;
        int MAX_ITER = 20;
        boolean hitBlock = false;
        for (int p = 0; p < MAX_ITER; ++p) {
            boolean breakEarly = false;
            Vec3 scaledVel = vel.scale(t);
            if (scaledVel.lengthSqr() < 1e-4d) break;

            Vec3 end = start.add(scaledVel);
            BlockHitResult bResult = this.level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            if (bResult.getType() != HitResult.Type.MISS) end = bResult.getLocation();

            AABB currentMovementRegion = this.getBoundingBox().expandTowards(end.subtract(start)).inflate(1).move(start.subtract(pos));

            Vec3 finalStart = start;
            Vec3 finalEnd = end;
            AABB thisBB = this.getBoundingBox();
            for (Entity target : this.level.getEntities(this, currentMovementRegion, e -> {
                if (projCtx.hasHitEntity(e) || !this.canHitEntity(e)) return false;
                AABB bb = e.getBoundingBox();
                return bb.intersects(thisBB) || bb.inflate(reach).clip(finalStart, finalEnd).isPresent();
            })) {
                projCtx.addEntity(target);
            }

            Vec3 hitLoc = end;
            if (bResult.getType() != HitResult.Type.MISS) {
                BlockPos bpos = bResult.getBlockPos().immutable();
                BlockState state = this.level.getChunkAt(bpos).getBlockState(bpos);

                boolean flag1 = projCtx.getLastState().isAir();
                if (!flag1) {
                    projCtx.setLastState(state);
                    state.onProjectileHit(this.level, state, bResult, this);
                    breakEarly = true;
                    hitBlock = true;

                    // TODO: block breaking
                }
            }
            Vec3 disp = hitLoc.subtract(start);
            start = hitLoc;
            if (this.onClip(projCtx, start)) break;
            if (breakEarly) break;
            t -= disp.length() / scaledVel.length();
            if (t < 0) break;
        }

        for (Entity e : projCtx.hitEntities()) this.onHitEntity(e);

        if (hitBlock) this.discard();
    }

    protected boolean onClip(ProjectileContext ctx, Vec3 pos) {
        return false;
    }

    protected abstract void onHitEntity(Entity entity);

}
