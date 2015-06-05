/*
 * This file is part of Strife, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.strife.menus;

import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.events.ItemClickEvent;
import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.items.MenuItem;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatsRangedMenuItem extends MenuItem {

    private final StrifePlugin plugin;

    public StatsRangedMenuItem(StrifePlugin plugin) {
        super(ChatColor.WHITE + "Bow Stats", new ItemStack(Material.BOW), "/n");
        this.plugin = plugin;
    }

    @Override
    public ItemStack getFinalIcon(Player player) {
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        Map<StrifeAttribute, Double> valueMap = champion.getAttributeValues();
        ItemStack itemStack = new ItemStack(Material.BOW);
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        itemMeta.setDisplayName(getDisplayName());
        List<String> lore = new ArrayList<>(getLore());
        lore.add(ChatColor.YELLOW + "Ranged Damage: " + ChatColor.WHITE + valueMap.get(StrifeAttribute.RANGED_DAMAGE));
        lore.add(ChatColor.YELLOW + "Snare Chance: " + ChatColor.WHITE + valueMap.get(StrifeAttribute.SNARE_CHANCE) + "%");
        if (valueMap.get(StrifeAttribute.CRITICAL_RATE) > 0) {
            lore.add(ChatColor.YELLOW + "Critical Rate: " + ChatColor.WHITE + valueMap.get(StrifeAttribute.CRITICAL_RATE)*100 + "%");
            lore.add(ChatColor.YELLOW + "Critical Damage: " + ChatColor.WHITE + valueMap.get(StrifeAttribute.CRITICAL_DAMAGE)*100 + "%");
        }
        if (valueMap.get(StrifeAttribute.ARMOR_PENETRATION) > 0) {
            lore.add(ChatColor.YELLOW + "Armor Penetration: " + ChatColor.WHITE
                     + valueMap.get(StrifeAttribute.ARMOR_PENETRATION) * 100 + "%");
        }
        if (valueMap.get(StrifeAttribute.FIRE_DAMAGE) > 0) {
            lore.add(ChatColor.YELLOW + "Ignite Chance:" + ChatColor.WHITE + valueMap.get(StrifeAttribute.IGNITE_CHANCE)*100 + "%");
            lore.add(ChatColor.YELLOW + "Fire Damage:" + ChatColor.WHITE + valueMap.get(StrifeAttribute.FIRE_DAMAGE));
        }
        if (valueMap.get(StrifeAttribute.LIGHTNING_DAMAGE) > 0) {
            lore.add(ChatColor.YELLOW + "Shock Chance:" + ChatColor.WHITE + valueMap.get(StrifeAttribute.SHOCK_CHANCE)*100 + "%");
            lore.add(ChatColor.YELLOW + "Lightning Damage:" + ChatColor.WHITE + valueMap.get(StrifeAttribute.LIGHTNING_DAMAGE));
        }
        if (valueMap.get(StrifeAttribute.ICE_DAMAGE) > 0) {
            lore.add(ChatColor.YELLOW + "Freeze Chance:" + ChatColor.WHITE + valueMap.get(StrifeAttribute.FREEZE_CHANCE)*100 + "%");
            lore.add(ChatColor.YELLOW + "Ice Damage:" + ChatColor.WHITE + valueMap.get(StrifeAttribute.ICE_DAMAGE));
        }
        lore.add(ChatColor.YELLOW + "Life Steal: " + ChatColor.WHITE + valueMap.get(StrifeAttribute.LIFE_STEAL) * 100 + "%");
        itemMeta.setLore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Override
    public void onItemClick(ItemClickEvent event) {
        super.onItemClick(event);
    }

}