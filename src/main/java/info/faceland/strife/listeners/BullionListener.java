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
package info.faceland.strife.listeners;

import com.tealcube.minecraft.bukkit.bullion.GoldDropEvent;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

public class BullionListener implements Listener {

    private final StrifePlugin plugin;

    public BullionListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    public StrifePlugin getPlugin() {
        return plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onGoldDrop(GoldDropEvent event) {
        if (event.getKiller() == null) {
            return;
        }
        Champion champion = plugin.getChampionManager().getChampion(event.getKiller().getUniqueId());
        Map<StrifeAttribute, Double> attributeDoubleMap = champion.getAttributeValues();
        double amount = event.getAmount() + event.getAmount() * attributeDoubleMap.get(StrifeAttribute.GOLD_FIND);
        event.setAmount(amount);
    }

}
