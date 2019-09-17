/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.strife.tasks;

import static org.bukkit.potion.PotionEffectType.POISON;
import static org.bukkit.potion.PotionEffectType.REGENERATION;
import static org.bukkit.potion.PotionEffectType.WITHER;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HealthRegenTask extends BukkitRunnable {

  private final StrifePlugin plugin;

  public HealthRegenTask(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public void run() {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (player.getHealth() <= 0 || player.isDead()) {
        continue;
      }
      StrifeMob pStats = plugin.getStrifeMobManager().getStatMob(player);
      if (plugin.getBarrierManager().hasBarrierEntry(player)) {
        plugin.getBarrierManager().restoreBarrier(pStats, pStats.getStat(StrifeStat.BARRIER_REGEN));
      }
      double playerMaxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
      if (player.getHealth() >= playerMaxHealth) {
        continue;
      }
      if (player.hasPotionEffect(WITHER) || player.hasPotionEffect(POISON)) {
        return;
      }
      double lifeAmount = StatUtil.getRegen(pStats);
      // Bonus for players that have just eaten
      if (player.getSaturation() > 0.1) {
        lifeAmount *= 1.6;
      }
      if (player.hasPotionEffect(REGENERATION)) {
        int potionIntensity = player.getPotionEffect(REGENERATION).getAmplifier() + 1;
        lifeAmount += potionIntensity + playerMaxHealth * 0.02 * potionIntensity;
      }
      // Restore 40% of your regen per 2s tick (This task runs every 2s)
      // Equals out to be 200% regen healed per 10s, aka 100% per 5s average
      lifeAmount *= 0.4;
      if (player.getFoodLevel() <= 6) {
        lifeAmount *= player.getFoodLevel() / 6F;
      }
      player.setHealth(Math.min(player.getHealth() + lifeAmount, playerMaxHealth));
    }
  }
}
