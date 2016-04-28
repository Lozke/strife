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
package info.faceland.strife.listeners;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;

public class HealthListener implements Listener {

    private final StrifePlugin plugin;

    public HealthListener(StrifePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                Player player = event.getPlayer();
                Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
                champion.getAttributeValues(true);
                double maxHealth = champion.getCache().getAttribute(StrifeAttribute.HEALTH);
                AttributeHandler.updateHealth(player, maxHealth);
                double perc = champion.getCache().getAttribute(StrifeAttribute.MOVEMENT_SPEED) / 100D;
                //double attackSpeed = 1 / (2 / (1 + champion.getCache().getAttribute(StrifeAttribute.ATTACK_SPEED)));
                float speed = 0.2F * (float) perc;
                player.setWalkSpeed(Math.min(Math.max(-1F, speed), 1F));
                player.setFlySpeed(Math.min(Math.max(-1F, speed), 1F));
                //player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(attackSpeed);
            }
        }, 20L);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player) ||
                !(event.getRegainReason() == EntityRegainHealthEvent.RegainReason.REGEN ||
                        event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) ||
                event.isCancelled()) {
            return;
        }
        Player player = (Player) event.getEntity();
        Champion champion = plugin.getChampionManager().getChampion(player.getUniqueId());
        double amount = champion.getCache().getAttribute(StrifeAttribute.REGENERATION);
        if (player.hasPotionEffect(PotionEffectType.POISON)) {
            amount *= 0.33;
        }
        event.setAmount(amount);
    }

}
