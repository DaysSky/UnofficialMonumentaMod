package ch.njol.unofficialmonumentamod.mixins;

import ch.njol.unofficialmonumentamod.UnofficialMonumentaModClient;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ThreadExecutor.class)
public abstract class ThreadSilencerMixin<R extends Runnable> {
    @Inject(method = "executeTask", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;fatal(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", remap = false), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void silenceTeamFatal(R task, CallbackInfo ci, Exception exception) {
        if (UnofficialMonumentaModClient.options.silenceTeamError && (exception.getMessage().matches("Player is either on another team or not on any team\\. Cannot remove from team 'players'\\.") || exception.getMessage().matches("Cannot invoke \"net\\.minecraft\\.scoreboard\\.Team\\.getName\\(\\)\" because \"team\" is null"))) {
            ci.cancel();
        }
    }
}
