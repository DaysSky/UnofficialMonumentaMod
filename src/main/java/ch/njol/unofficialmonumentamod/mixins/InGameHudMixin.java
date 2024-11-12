package ch.njol.unofficialmonumentamod.mixins;

import ch.njol.unofficialmonumentamod.AbilityHandler;
import ch.njol.unofficialmonumentamod.UnofficialMonumentaModClient;
import ch.njol.unofficialmonumentamod.features.misc.managers.MessageNotifier;
import ch.njol.unofficialmonumentamod.hud.strike.ChestCountOverlay;
import ch.njol.unofficialmonumentamod.hud.AbilitiesHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(InGameHud.class)
public class InGameHudMixin {

	@Shadow @Final private LayeredDrawer layeredDrawer;
	@Unique
	private final AbilitiesHud abilitiesHud = AbilitiesHud.INSTANCE;

	@Inject(method = "setOverlayMessage", at = @At("TAIL"))
	public void onActionbar(Text message, boolean tinted, CallbackInfo ci) {
		ChestCountOverlay.INSTANCE.onActionbarReceived(message);
	}

	@Unique //TODO might want to find a way to move that to before the status effect layer to reflect earlier versions.
	private void umm$preChatRenderHook(DrawContext context, @NotNull RenderTickCounter counter) {
		if (!abilitiesHud.renderInFrontOfChat()) {
			umm$InGameHudRender(context, counter);
		}
	}

	@Unique
	private void umm$postChatRenderHook(DrawContext context, @NotNull RenderTickCounter counter) {
		if (abilitiesHud.renderInFrontOfChat()) {
			umm$InGameHudRender(context, counter);
		}
	}

	@Unique
	private void umm$InGameHudRender(DrawContext context, @NotNull RenderTickCounter counter) {
		UnofficialMonumentaModClient.effectOverlay.renderAbsolute(context, counter);
		ChestCountOverlay.INSTANCE.renderAbsolute(context, counter);
		MessageNotifier.getInstance().renderAbsolute(context, counter);
		abilitiesHud.renderAbsolute(context, counter);
	}

	@Inject(method = "<init>",
	at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addSubDrawer(Lnet/minecraft/client/gui/LayeredDrawer;Ljava/util/function/BooleanSupplier;)Lnet/minecraft/client/gui/LayeredDrawer;", shift = At.Shift.BEFORE, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD)
	void umm$inGameHudDrawerHook(MinecraftClient client, CallbackInfo ci, LayeredDrawer layeredDrawer, LayeredDrawer layeredDrawer2) {
		//pre-chat
		layeredDrawer.addLayer(this::umm$preChatRenderHook);
		//post-chat
		layeredDrawer2.addLayer(this::umm$postChatRenderHook);
	}

	/**
	 * If configured, do not show ability messages
	 * TODO The messages here are translated, so only work for English. Maybe just check for the ability name in the message? (assuming that one isn't translated as well...)
	 * TODO Or maybe make this an option server-side? That would probably be the best option. Maybe even make the client mod send this request.
	 */
	@Inject(method = "setOverlayMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"), cancellable = true)
	void setOverlayMessage(Text message, boolean tinted, CallbackInfo ci) {
		if (message == null) {
			return;
		}
		synchronized (UnofficialMonumentaModClient.abilityHandler) {
			if (!UnofficialMonumentaModClient.options.abilitiesDisplay_hideAbilityRelatedMessages
				    || !UnofficialMonumentaModClient.options.abilitiesDisplay_enabled
				    || UnofficialMonumentaModClient.abilityHandler.abilityData.isEmpty()) {
				return;
			}
			String m = message.getString();
			if (StringUtils.startsWithIgnoreCase(m, "You are silenced")
				    || StringUtils.startsWithIgnoreCase(m, "All your cooldowns have been reset")
				    || StringUtils.startsWithIgnoreCase(m, "Cloak stacks:")
				    || StringUtils.startsWithIgnoreCase(m, "Rage:")
				    || StringUtils.startsWithIgnoreCase(m, "Holy energy radiates from your hands")
				    || StringUtils.startsWithIgnoreCase(m, "The light from your hands fades")) {
				ci.cancel();
				return;
			}
			for (AbilityHandler.AbilityInfo abilityInfo : UnofficialMonumentaModClient.abilityHandler.abilityData) {
				if (StringUtils.startsWithIgnoreCase(m, abilityInfo.name + " is now off cooldown")
					    || StringUtils.startsWithIgnoreCase(m, abilityInfo.name + " has been activated")
					    || StringUtils.startsWithIgnoreCase(m, abilityInfo.name + " stacks")
					    || StringUtils.startsWithIgnoreCase(m, abilityInfo.name + " charges")) {
					ci.cancel();
					return;
				}
			}
		}
	}

}
