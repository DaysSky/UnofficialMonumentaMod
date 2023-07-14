package ch.njol.unofficialmonumentamod.core.shard;

import ch.njol.unofficialmonumentamod.hud.strike.ChestCountOverlay;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShardDebugCommand {
    public LiteralArgumentBuilder<FabricClientCommandSource> register() {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = LiteralArgumentBuilder.literal("ummShard");

        //list shards
        builder.then(ClientCommandManager.literal("list").executes(ShardDebugCommand::executeList));
        builder.then(ClientCommandManager.literal("debug")
                .then(ClientCommandManager.literal("set").then(ClientCommandManager.argument("shard", ShardArgumentType.Key()).executes(ShardDebugCommand::executeDebugSet)))
                .then(ClientCommandManager.literal("get").then(ClientCommandManager.argument("shard", ShardArgumentType.Key()).executes(ShardDebugCommand::executeDebugGet)))
                .then(ClientCommandManager.literal("loaded").executes(ShardDebugCommand::executeDebugLoaded)));
        return builder;
    }

    public String getName() {
        return ShardDebugCommand.class.getSimpleName();
    }

    public static int executeList(CommandContext<FabricClientCommandSource> context) {
        try {
            final HashMap<String, ShardData.Shard> shards = ShardData.getShards();

            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(MutableText.of(Text.of("Currently loaded shards:").getContent()).setStyle(Style.EMPTY.withColor(Formatting.AQUA).withBold(true)));

            for (Map.Entry<String, ShardData.Shard> shardEntry : shards.entrySet()) {
                MutableText shardText = MutableText.of(Text.of(shardEntry.getKey()).getContent());
                ShardData.Shard shard = shardEntry.getValue();

                shardText.setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("Official name: " + shard.officialName + "\nShard type: " + shard.shardType + "\nMax chests: " + (shard.maxChests != null ? shard.maxChests : "None")))).withColor(Formatting.AQUA));

                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(shardText);

            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int executeDebugSet(CommandContext<FabricClientCommandSource> context) {
        String shardName = context.getArgument("shard", String.class);

        ShardData.editedShard = true;
        ShardData.onShardChangeSkipChecks(shardName);
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(MutableText.of(Text.of("The Mod will now believe you are in: " + shardName).getContent()).setStyle(Style.EMPTY.withBold(true).withColor(Formatting.AQUA)));
        return 0;
    }

    public static int executeDebugGet(CommandContext<FabricClientCommandSource> context) {
        MutableText shardText = MutableText.of(Text.of("Shard: " + context.getArgument("shard", String.class)).getContent());
        ShardData.Shard shard = ShardArgumentType.getShardFromKey(context, "shard");

        assert shard != null;
        shardText.setStyle(
                Style.EMPTY
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(
                                "Official name: " + shard.officialName + "\nShard type: " + shard.shardType + "\nMax chests: " + (shard.maxChests != null ? shard.maxChests : "None")
                        )))
                        .withColor(Formatting.AQUA).withBold(true)
        );

        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(shardText);
        return 0;
    }

    public static int executeDebugLoaded(CommandContext<FabricClientCommandSource> context) {
        try {
            ChestCountOverlay chestCountOverlay = ChestCountOverlay.INSTANCE;

            Integer count = chestCountOverlay.getCurrentCount();
            Integer max = chestCountOverlay.getTotalChests();
            String lastShard = ShardData.getLastShard().shardString;
            String currentShard = ShardData.getCurrentShard().shardString;
            boolean isSearching = ShardData.isSearchingForShard();
            boolean isEdited = ShardData.editedShard;

            //check if it loaded correctly when entering the shard (should show false if it wasn't able to load the shard after world load)
            boolean loadedCorrectly = !isSearching && !Objects.equals(lastShard, currentShard);

            //count: (if max exists then count/max else just count) loaded shard: lastShard, current shard: currentShard
            MutableText text = MutableText.of(Text.of("[Current Shard]\n").getContent()).setStyle(Style.EMPTY.withColor(Formatting.AQUA));

            text.append(MutableText.of(Text.of("Count: ").getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
            text.append(MutableText.of(Text.of((max != null ? count + "/" + max : count) + "\n").getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)));

            text.append(MutableText.of(Text.of("Last shard: ").getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
            text.append(MutableText.of(Text.of(lastShard).getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)));

            text.append(MutableText.of(Text.of(" | Current shard: ").getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
            text.append(MutableText.of(Text.of(currentShard + "\n").getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)));

            text.append(MutableText.of(Text.of("Loaded correctly: ").getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
            text.append(MutableText.of(Text.of(loadedCorrectly ? "Yes" : "No").getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)));

            text.append(MutableText.of(Text.of(" | Was edited: ").getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
            text.append(MutableText.of(Text.of(isEdited ? "Yes" : "No").getContent()).setStyle(Style.EMPTY.withColor(Formatting.DARK_AQUA)));

            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(text);
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
