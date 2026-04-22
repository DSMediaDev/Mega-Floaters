package gg.dsmedia.megafloaters.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server-side guard for BDD v1.3.0-alpha's broken {@code fireProjectile} path.
 *
 * <p>{@code BddAbilityDragon.fireProjectile} unconditionally evaluates
 * {@code MISC.getKey(BDDKeyMappings.BLAST).isDown()} — which internally reads
 * {@code BddKeybinds.BLAST_KEY}. {@code BddKeybinds} is
 * {@code @OnlyIn(Dist.CLIENT)}, so NeoForge's {@code RuntimeDistCleaner}
 * rewrites it to throw on first use. On a dedicated server this kills the
 * tick thread the moment a wild dragon tries to fire.
 *
 * <p>We cancel the method on the server side. Dragons can still bite and
 * melee (handled by a separate method with no keybind refs) but lose their
 * ranged attack until BDD ships a fix. A redirect-only fix would have been
 * cleaner but requires referencing {@code net.minecraft.client.KeyMapping}
 * in the handler signature, and that class isn't present on the dedicated
 * server classloader — mixin attach fails at transform time.
 *
 * <p>Client behaviour is untouched.
 */
@Mixin(targets = "com.bdc.bdd.api.entity.core_classes.BddAbilityDragon", remap = false)
public abstract class BddAbilityDragonMixin {

    @Inject(
        method = "fireProjectile",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void megafloaters$guardServerSide(Vec3 from, Vec3 to, CallbackInfo ci) {
        if (!((Entity) (Object) this).level().isClientSide) {
            ci.cancel();
        }
    }
}
