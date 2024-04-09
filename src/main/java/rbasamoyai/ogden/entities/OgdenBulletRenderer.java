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
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.ogden.OgdenMod;

public class OgdenBulletRenderer extends EntityRenderer<OgdenBullet> {
    private static final RenderType QUAD = RenderType.entityCutoutNoCull(OgdenMod.resource("textures/entity/bullet.png"));

    public OgdenBulletRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override
    public void render(OgdenBullet entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        boolean isTracer = entity.isTracer();
        Vec3 start = new Vec3(entity.xOld, entity.yOld, entity.zOld);
        Vec3 totalDiff = entity.position().subtract(start);
        if (totalDiff.lengthSqr() < 1e-4d) totalDiff = Vec3.ZERO;
        double displacement = entity.getDisplacement() + totalDiff.length() * partialTicks;
        boolean isTeleported = totalDiff.lengthSqr() > entity.getDeltaMovement().lengthSqr() * 4;

        float yaw = isTeleported ? entity.getViewYRot(partialTicks) : (float) Math.atan2(totalDiff.x, totalDiff.z) * Mth.RAD_TO_DEG;
        float pitch = isTeleported ? entity.getViewXRot(partialTicks) : (float) Math.atan2(totalDiff.y, totalDiff.horizontalDistance()) * Mth.RAD_TO_DEG;
        Quaternion q = Vector3f.YP.rotationDegrees(yaw + 180.0f);
        Quaternion q1 = Vector3f.XP.rotationDegrees(pitch);
        q.mul(q1);

        poseStack.pushPose();
        poseStack.mulPose(q);
        poseStack.translate(0, entity.getBbHeight() / 2, 0);

        float length = isTracer ? (float) Math.min(isTeleported ? 0 : totalDiff.length(), displacement) : 0.125f;
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f pose = lastPose.pose();
        Matrix3f normal = lastPose.normal();

        int r = isTracer ? 255 : 128;
        int g = isTracer ? 254 : 128;
        int b = isTracer ? 224 : 128;
        int light = isTracer ? LightTexture.FULL_BRIGHT : packedLight;

        float thickness = 1 / 32f;
        float x1 = -thickness;
        float y1 = -thickness;
        float z1 = -thickness;
        float x2 = thickness;
        float y2 = thickness;
        float z2 = length + thickness;

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