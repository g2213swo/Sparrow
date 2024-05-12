package net.momirealms.sparrow.bukkit.command.feature;

import net.momirealms.sparrow.bukkit.SparrowNMSProxy;
import net.momirealms.sparrow.bukkit.command.MessagingCommandFeature;
import net.momirealms.sparrow.bukkit.command.key.SparrowBukkitArgumentKeys;
import net.momirealms.sparrow.bukkit.util.CommandUtils;
import net.momirealms.sparrow.common.command.key.SparrowArgumentKeys;
import net.momirealms.sparrow.common.command.key.SparrowFlagKeys;
import net.momirealms.sparrow.common.helper.AdventureHelper;
import net.momirealms.sparrow.common.locale.MessageConstants;
import net.momirealms.sparrow.common.util.Pair;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector;
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.Optional;

public class TitleAdminCommand extends MessagingCommandFeature<CommandSender> {

    @Override
    public String getFeatureID() {
        return "title_admin";
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required(SparrowBukkitArgumentKeys.PLAYER_SELECTOR, MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
                .required("fadeIn", IntegerParser.integerParser(0))
                .required("stay", IntegerParser.integerParser(0))
                .required("fadeOut", IntegerParser.integerParser(0))
                .required("title", StringParser.greedyFlagYieldingStringParser())
                .flag(SparrowFlagKeys.SILENT_FLAG)
                .flag(SparrowFlagKeys.LEGACY_COLOR_FLAG)
                .handler(commandContext -> {
                    MultiplePlayerSelector selector = commandContext.get(SparrowBukkitArgumentKeys.PLAYER_SELECTOR);
                    var players = selector.values();
                    boolean legacy = commandContext.flags().hasFlag(SparrowFlagKeys.LEGACY_COLOR_FLAG);
                    int fadeIn = commandContext.get("fadeIn");
                    int stay = commandContext.get("stay");
                    int fadeOut = commandContext.get("fadeIn");
                    String titleContent = commandContext.get("title");
                    String title;
                    String subTitle;
                    String[] split = titleContent.split("\\\\n");
                    if (split.length > 2) {
                        commandContext.store(SparrowArgumentKeys.MESSAGE, MessageConstants.COMMANDS_ADMIN_TITLE_FAILED_FORMAT);
                        return;
                    }
                    title = split[0].isEmpty() ? null : split[0];
                    subTitle = split.length == 2 && !split[1].isEmpty() ? split[1] : null;
                    for (Player player : players) {
                        SparrowNMSProxy.getInstance().sendTitle(
                                player,
                                Optional.ofNullable(title)
                                        .map(t -> AdventureHelper.componentToJson(
                                                AdventureHelper.getMiniMessage().deserialize(legacy ? AdventureHelper.legacyToMiniMessage(t) : t))
                                        )
                                        .orElse(null),
                                Optional.ofNullable(subTitle)
                                        .map(t -> AdventureHelper.componentToJson(
                                                AdventureHelper.getMiniMessage().deserialize(legacy ? AdventureHelper.legacyToMiniMessage(t) : t))
                                        )
                                        .orElse(null),
                                fadeIn,
                                stay,
                                fadeOut
                        );
                    }
                    CommandUtils.storeEntitySelectorMessage(commandContext, selector,
                            Pair.of(MessageConstants.COMMANDS_ADMIN_TITLE_SUCCESS_SINGLE, MessageConstants.COMMANDS_ADMIN_TITLE_SUCCESS_MULTIPLE)
                    );
                });
    }
}
