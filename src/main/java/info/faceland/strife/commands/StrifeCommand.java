/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.strife.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.shade.fanciful.FancyMessage;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import se.ranzdo.bukkit.methodcommand.Arg;
import se.ranzdo.bukkit.methodcommand.Command;

public class StrifeCommand {

    private final StrifePlugin plugin;

    public StrifeCommand(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @Command(identifier = "strife reload", permissions = "strife.command.strife.reload", onlyPlayers = false)
    public void reloadCommand(CommandSender sender) {
        plugin.disable();
        plugin.enable();
        sendMessage(sender, plugin.getSettings().getString("language.command.reload", "&aStrife reloaded!"));
    }

    @Command(identifier = "strife profile", permissions = "strife.command.strife.profile", onlyPlayers = false)
    public void profileCommand(CommandSender sender, @Arg(name = "target") Player target) {
        Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
        sendMessage(sender, "<gold>----------------------------------");
        sendMessage(sender, "<gray>Unused Stat Points: <white>%amount%",
                new String[][]{{"%amount%", "" + champion.getUnusedStatPoints()}});
        sendMessage(sender, "<gold>----------------------------------");
        for (StrifeStat stat : plugin.getStatManager().getStats()) {
            sendMessage(sender, ChatColor.GRAY + " - " + champion.getLevel(stat));
        }
        sendMessage(sender, "<gold>----------------------------------");
    }

    @Command(identifier = "strife reset", permissions = "strife.command.strife.reset", onlyPlayers = false)
    public void resetCommand(CommandSender sender, @Arg(name = "target") Player target) {
        Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
        for (StrifeStat stat : plugin.getStatManager().getStats()) {
            champion.setLevel(stat, 0);
        }
        champion.setHighestReachedLevel(target.getLevel());
        champion.setUnusedStatPoints(target.getLevel());
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        sendMessage(sender, "<green>You reset <white>%player%<green>.",
                new String[][]{{"%player%", target.getDisplayName()}});
        sendMessage(target, "<green>Your stats have been reset.");
        FancyMessage message = new FancyMessage("");
        message.then("You have unspent levelpoints! ").color(ChatColor.GOLD).then("CLICK HERE").command("/levelup")
                .color(ChatColor.WHITE).then(" or use ").color(ChatColor.GOLD).then("/levelup")
                .color(ChatColor.WHITE).then(" to spend them!").send(target);
        AttributeHandler.updateAttributes(plugin, champion.getPlayer());
    }

    @Command(identifier = "strife clear", permissions = "strife.command.strife.clear", onlyPlayers = false)
    public void clearCommand(CommandSender sender, @Arg(name = "target") Player target) {
        target.setExp(0f);
        target.setLevel(0);
        Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
        for (StrifeStat stat : plugin.getStatManager().getStats()) {
            champion.setLevel(stat, 0);
        }
        champion.setUnusedStatPoints(0);
        champion.setHighestReachedLevel(0);
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        sendMessage(sender, "<green>You cleared <white>%player%<green>.",
                new String[][]{{"%player%", target.getDisplayName()}});
        sendMessage(target, "<green>Your stats have been cleared.");
        AttributeHandler.updateAttributes(plugin, champion.getPlayer());
    }

    @Command(identifier = "strife raise", permissions = "strife.command.strife.raise", onlyPlayers = false)
    public void raiseCommand(CommandSender sender, @Arg(name = "target") Player target,
        @Arg(name = "level") int newLevel) {
        int oldLevel = target.getLevel();
        if (newLevel <= oldLevel) {
            sendMessage(sender, "<red>New level must be higher than old level.");
        }
        target.setExp(0f);
        target.setLevel(newLevel);
        Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
        plugin.getChampionManager().removeChampion(champion.getUniqueId());
        plugin.getChampionManager().addChampion(champion);
        sendMessage(sender, "<green>You raised <white>%player%<green> to level <white>%level%<green>.",
            new String[][]{{"%player%", target.getDisplayName()}, {"%level%", "" + newLevel}});
        sendMessage(target, "<green>Your level has been raised.");
        AttributeHandler.updateAttributes(plugin, champion.getPlayer());
    }

    @Command(identifier = "strife setskill", permissions = "strife.command.strife.setskill", onlyPlayers = false)
    public void skillCommand(CommandSender sender, @Arg(name = "target") Player target,
        @Arg(name = "skill") String skill, @Arg(name = "level") int newLevel) {
        if (!target.hasPermission("strife.weirdlevels") && (newLevel > 60 || newLevel < 0)) {
            sendMessage(sender, "<red>Skill must be between level 0 and 60.");
            return;
        }
        if (skill.equalsIgnoreCase("crafting")) {
            Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
            champion.getSaveData().setCraftingLevel(newLevel);
            sendMessage(target, "<green>Your skill in crafting is now " + newLevel);
            sendMessage(sender, "<green>Set crafting level of " + target + " to " + newLevel);
            return;
        } else if (skill.equalsIgnoreCase("enchanting")) {
            Champion champion = plugin.getChampionManager().getChampion(target.getUniqueId());
            champion.getSaveData().setEnchantLevel(newLevel);
            sendMessage(target, "<green>Your skill in enchanting is now " + newLevel);
            sendMessage(sender, "<green>Set enchanting level of " + target + " to " + newLevel);
            return;
        }
        sendMessage(sender, "<red>Cannot set level of unknown skill '" + skill + "'.");
    }

    @Command(identifier = "strife addxp", permissions = "strife.command.strife.addxp", onlyPlayers = false)
    public void addXpCommand(CommandSender sender, @Arg(name = "target") Player player, @Arg(name = "amount") double amount) {
        plugin.getExperienceManager().addExperience(player, amount, true);
        sendMessage(player, "&aYou gained &f" + (int) amount + " &aXP!");
    }

    @Command(identifier = "strife xpmult", permissions = "strife.command.strife.xpmult", onlyPlayers = false)
    public void setExpMultCommand(CommandSender sender, @Arg(name = "amount") double amount) {
        sendMessage(sender, "&aBonus XP mult changed to " + amount + "x from " + (plugin
                .getMultiplierManager().getExpMult() + 1) +"x!");
        plugin.getMultiplierManager().setExpMult(amount);
    }

    @Command(identifier = "strife dropmult", permissions = "strife.command.strife.dropmult", onlyPlayers = false)
    public void setDropMultCommand(CommandSender sender, @Arg(name = "amount") double amount) {
        sendMessage(sender, "&aBonus drop mult changed to " + amount + "x from " + (plugin
                .getMultiplierManager().getDropMult() + 1) +"x!");
        plugin.getMultiplierManager().setDropMult(amount);
    }

}
