package net.mistersecret312.rocketry_science.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.mistersecret312.rocketry_science.RocketryScience;

public class PlumeModel extends EntityModel<Entity>
{
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(RocketryScience.MODID, "plume_model"), "main");
    private final ModelPart bb_main;

    public PlumeModel(ModelPart root)
    {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, -16)
                                                                                     .addBox(0.0F, -8.0F, -8.0F, 0.0F,
                                                                                             16.0F, 16.0F,
                                                                                             new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, -16)
                                                                                     .addBox(0.0F, -8.0F, -8.0F, 0.0F,
                                                                                             16.0F, 16.0F,
                                                                                             new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                          float headPitch)
    {

    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int r, int g, int b)
    {
        bb_main.render(poseStack, vertexConsumer, r, g, b);
    }
}
