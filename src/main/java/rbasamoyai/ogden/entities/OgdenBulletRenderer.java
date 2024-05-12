package rbasamoyai.ogden.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.ogden.OgdenMod;

public class OgdenBulletRenderer extends EntityRenderer<OgdenBullet> {
    private static final RenderType QUAD = RenderType.entityCutoutNoCull(OgdenMod.resource("textures/entity/bullet.png"));

    public OgdenBulletRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public void render(OgdenBullet entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        Vec3 start = new Vec3(entity.xOld, entity.yOld, entity.zOld);
        Vec3 diff = entity.position().subtract(start);
        double dlSqr = diff.lengthSqr();
        boolean isFastButNotTeleported = 1e-4d <= dlSqr && dlSqr <= entity.getDeltaMovement().lengthSqr() * 4;
        double diffLength = isFastButNotTeleported ? diff.length() : 0;
        double displacement = entity.getDisplacement() - diffLength * (1 - partialTicks);

        int removalTicks = Math.max(0, entity.getRemovalTicks() - 1);
        float subTick = entity.getCollisionSubTick();
        boolean hasCollided = 0 <= subTick && subTick <= 1 && partialTicks + removalTicks > subTick;
        boolean isTracer = entity.isTracer();
        float tickDiff = partialTicks + removalTicks - subTick;
        if (hasCollided) {
            if (tickDiff > subTick) return;
            diffLength *= 1 - tickDiff;
        }
        float length;
        if (isTracer) {
            length = (float) Math.min(diffLength, displacement);
        } else {
            length = 0.125f;
        }

        int packedTracerColor = entity.getPackedTracerColor();
        int r = isTracer ? (packedTracerColor >> 16) & 255 : 128;
        int g = isTracer ? (packedTracerColor >> 8) & 255 : 128;
        int b = isTracer ? packedTracerColor & 255 : 128;
        int light = isTracer ? LightTexture.FULL_BRIGHT : packedLight;

        float thickness = 1 / 32f;
        float x1 = -thickness;
        float y1 = -thickness;
        float z1 = -thickness;
        float x2 = thickness;
        float y2 = thickness;
        float z2 = length + thickness;

        float yaw = entity.getViewYRot(partialTicks);
        float pitch = entity.getViewXRot(partialTicks);
        Quaternion q = Vector3f.YP.rotationDegrees(yaw + 180.0f);
        Quaternion q1 = Vector3f.XP.rotationDegrees(pitch);
        q.mul(q1);

        poseStack.pushPose();
        poseStack.mulPose(q);
        poseStack.translate(0, entity.getBbHeight() / 2, 0);

        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f pose = lastPose.pose();
        Matrix3f normal = lastPose.normal();

        VertexConsumer builder = buffers.getBuffer(QUAD);

        // Right
        vertex(builder, pose, normal, r, g, b, 0, y1, z1, light);
        vertex(builder, pose, normal, r, g, b, 0, y1, z2, light);
        vertex(builder, pose, normal, r, g, b, 0, y2, z2, light);
        vertex(builder, pose, normal, r, g, b, 0, y2, z1, light);

        // Left
        vertex(builder, pose, normal, r, g, b, 0, y1, z1, light);
        vertex(builder, pose, normal, r, g, b, 0, y2, z1, light);
        vertex(builder, pose, normal, r, g, b, 0, y2, z2, light);
        vertex(builder, pose, normal, r, g, b, 0, y1, z2, light);

        // Down
        vertex(builder, pose, normal, r, g, b, x2, 0, z2, light);
        vertex(builder, pose, normal, r, g, b, x1, 0, z2, light);
        vertex(builder, pose, normal, r, g, b, x1, 0, z1, light);
        vertex(builder, pose, normal, r, g, b, x2, 0, z1, light);

        // Up
        vertex(builder, pose, normal, r, g, b, x1, 0, z1, light);
        vertex(builder, pose, normal, r, g, b, x1, 0, z2, light);
        vertex(builder, pose, normal, r, g, b, x2, 0, z2, light);
        vertex(builder, pose, normal, r, g, b, x2, 0, z1, light);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
    }

    @Override
    public boolean shouldRender(OgdenBullet entity, Frustum frustrum, double x, double y, double z) {
        return entity.isTracer() || super.shouldRender(entity, frustrum, x, y, z);
    }

    private static void vertex(VertexConsumer builder, Matrix4f pose, Matrix3f normal, int r, int g, int b,
                               float x, float y, float z, int lightTexture) {
        builder.vertex(pose, x, y, z)
            .color(r, g, b, 255)
            .uv(0, 0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(lightTexture)
            .normal(normal, 0, 1, 0)
            .endVertex();
    }

    @Override public ResourceLocation getTextureLocation(OgdenBullet entity) { return null; }
}
