package ch.njol.unofficialmonumentamod.compat;

import com.provismet.provihealth.api.ProviHealthApi;

import ch.njol.unofficialmonumentamod.UnofficialMonumentaModClient;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.Identifier;

public class HealthBarHook implements ProviHealthApi {
    @Override
    public void onInitialize () {
        final Identifier DUELIST_BORDER = Identifier.of(UnofficialMonumentaModClient.MOD_IDENTIFIER, "textures/gui/healthbars/duelist_portrait.png");
        final Identifier SLAYER_BORDER = Identifier.of(UnofficialMonumentaModClient.MOD_IDENTIFIER, "textures/gui/healthbars/slayer_portrait.png");

        // Creates new displays for Monumenta's custom entity groupings.
        final EntityType<?>[] duelist = {
            EntityType.VEX,
            EntityType.WITCH,
            EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.IRON_GOLEM,
            EntityType.GIANT,
            EntityType.ALLAY,
            EntityType.WARDEN
        };

        final EntityType<?>[] slayer = {
            EntityType.CREEPER,
            EntityType.ENDERMAN,
            EntityType.BLAZE,
            EntityType.GHAST,
            EntityType.SLIME,
            EntityType.MAGMA_CUBE,
            EntityType.SHULKER,
            EntityType.WOLF,
            EntityType.RAVAGER,
            EntityType.HOGLIN,
            EntityType.COW,
            EntityType.PIG,
            EntityType.SHEEP,
            EntityType.CHICKEN
        };

        // Duelist
        this.registerPortrait(EntityTypeTags.ILLAGER, DUELIST_BORDER, 1);
        this.registerIcon(EntityTypeTags.ILLAGER, Items.PLAYER_HEAD, 1);
        for (EntityType<?> type : duelist) {
            this.registerPortrait(type, DUELIST_BORDER, 1);
            this.registerIcon(type, Items.PLAYER_HEAD, 1);
        }

        // Slayer
        this.registerPortrait(EntityTypeTags.AQUATIC, SLAYER_BORDER, 1);
        this.registerPortrait(EntityTypeTags.ARTHROPOD, SLAYER_BORDER, 1);
        this.registerIcon(EntityTypeTags.AQUATIC, Items.CREEPER_HEAD, 1);
        this.registerIcon(EntityTypeTags.ARTHROPOD, Items.CREEPER_HEAD, 1);
        for (EntityType<?> type : slayer) {
            this.registerPortrait(type, SLAYER_BORDER, 1);
            this.registerIcon(type, Items.CREEPER_HEAD, 1);
        }
    }
}
