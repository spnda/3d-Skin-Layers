package dev.tr7zw.skinlayers.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.tr7zw.skinlayers.Settings;
import dev.tr7zw.skinlayers.accessor.PlayerEntityModelAccessor;
import dev.tr7zw.skinlayers.accessor.PlayerSettings;
import dev.tr7zw.skinlayers.render.CustomizableModelPart;
import dev.tr7zw.skinlayers.renderlayers.BodyLayerFeatureRenderer;
import dev.tr7zw.skinlayers.renderlayers.HeadLayerFeatureRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(Context context, PlayerModel<AbstractClientPlayer> entityModel, float f) {
        super(context, entityModel, f);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onCreate(CallbackInfo info) {
        this.addLayer(new HeadLayerFeatureRenderer(this));
        this.addLayer(new BodyLayerFeatureRenderer(this));
    }
    
    @Inject(method = "setModelProperties", at = @At("RETURN"))
    public void setModelProperties(AbstractClientPlayer abstractClientPlayer, CallbackInfo info) {
        if(Minecraft.getInstance().player.distanceToSqr(abstractClientPlayer) > Settings.viewDistanceSqr)return;
        PlayerModel<AbstractClientPlayer> playerModel = this.getModel();
        playerModel.hat.visible = false;
        playerModel.jacket.visible = false;
        playerModel.leftSleeve.visible = false;
        playerModel.rightSleeve.visible = false;
        playerModel.leftPants.visible = false;
        playerModel.rightPants.visible = false;
    }
    
    @Inject(method = "renderHand", at = @At("RETURN"))
    private void renderHand(PoseStack poseStack, MultiBufferSource multiBufferSource, int i,
            AbstractClientPlayer abstractClientPlayer, ModelPart arm, ModelPart sleeve, CallbackInfo info) {
        PlayerSettings settings = (PlayerSettings) abstractClientPlayer;
        float pixelScaling = 1.1f;
        float armHeightScaling = 1.1f;
        if(settings.getSkinLayers() != null) {
            CustomizableModelPart part = null;
            boolean thinArms = ((PlayerEntityModelAccessor)getModel()).hasThinArms();
            if(sleeve == this.model.leftSleeve) {
                part = settings.getSkinLayers()[2];
            }else {
                part = settings.getSkinLayers()[3];
            }
            part.copyFrom(arm);
            poseStack.pushPose();
            poseStack.scale(pixelScaling, armHeightScaling, pixelScaling);
            part.y -= 0.6;
            if(!thinArms) {
                part.x -= 0.4;
            }
            part.render(poseStack,
                multiBufferSource
                        .getBuffer(RenderType.entityTranslucent(abstractClientPlayer.getSkinTextureLocation())),
                i, OverlayTexture.NO_OVERLAY);
            part.setPos(0, 0, 0);
            part.setRotation(0, 0, 0);
            poseStack.popPose();
        }
    }
    
}
