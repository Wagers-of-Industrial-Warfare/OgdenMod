package rbasamoyai.ogden.entities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

public class ProjectileContext {

	private BlockState lastState = Blocks.AIR.defaultBlockState();
	private final CollisionContext collisionContext;
	private final Map<Entity, Double> hitEntities = new LinkedHashMap<>();
	//private final CBCCfgMunitions.GriefState griefState; TODO: config this instead of Create Big Cannons

    private double finalHitTime = Double.POSITIVE_INFINITY;

	private final Map<BlockPos, Float> queuedExplosions = new HashMap<>();

	public ProjectileContext(OgdenProjectile<?> projectile/*, CBCCfgMunitions.GriefState griefState*/) {
		this.collisionContext = CollisionContext.of(projectile);
		//this.griefState = griefState;
	}

	public void setLastState(BlockState state) { this.lastState = state; }
	public BlockState getLastState() { return this.lastState; }
	public CollisionContext collisionContext() { return this.collisionContext; }
	//public CBCCfgMunitions.GriefState griefState() { return this.griefState; }

	public boolean hasHitEntity(Entity entity) { return this.hitEntities.containsKey(entity); }
	public void addEntity(Entity entity, double hitTime) { this.hitEntities.put(entity, hitTime); }
	public Map<Entity, Double> hitEntities() { return this.hitEntities; }

    public void setFinalHitTime(double hitTime) { this.finalHitTime = hitTime; }
    public double getFinalHitTime() { return this.finalHitTime; }

	public void queueExplosion(BlockPos pos, float power) { this.queuedExplosions.put(pos, power); }
	public Map<BlockPos, Float> getQueuedExplosions() { return this.queuedExplosions; }

}
