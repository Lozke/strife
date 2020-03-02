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
package land.face.strife.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.IndicatorData;
import land.face.strife.data.IndicatorData.IndicatorStyle;
import land.face.strife.data.StrifeMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DamageManager {

  private StrifePlugin plugin;
  private float IND_GRAVITY_HSPEED;
  private float IND_GRAVITY_VSPEED;
  private Map<UUID, Double> handledDamages = new HashMap<>();

  public DamageManager(StrifePlugin plugin) {
    this.plugin = plugin;
    IND_GRAVITY_HSPEED = (float) plugin.getSettings()
        .getDouble("config.indicators.gravity-horizontal-speed", 30);
    IND_GRAVITY_VSPEED = (float) plugin.getSettings()
        .getDouble("config.indicators.gravity-vertical-speed", 80);
  }

  public boolean isHandledDamage(Entity entity) {
    return handledDamages.containsKey(entity.getUniqueId());
  }

  public double getHandledDamage(Entity entity) {
    return handledDamages.getOrDefault(entity.getUniqueId(), 0D);
  }

  public double dealDamage(StrifeMob attacker, StrifeMob defender, double damage) {
    return dealDamage(attacker, defender, damage, false);
  }

  public double dealDamage(StrifeMob attacker, StrifeMob defender, double damage,
      boolean indicators) {
    if (indicators && attacker.getEntity() instanceof Player) {
      plugin.getIndicatorManager().addIndicator(attacker.getEntity(), defender.getEntity(),
          buildHitIndicator((Player) attacker.getEntity()),
          String.valueOf((int) Math.ceil(damage)));
    }

    damage = plugin.getBarrierManager().damageBarrier(defender, (float) damage);
    damage = Math.min(damage, defender.getEntity().getHealth());

    int noDamageTicks = defender.getEntity().getNoDamageTicks();
    Vector velocity = defender.getEntity().getVelocity();
    defender.getEntity().setNoDamageTicks(0);

    handledDamages.put(attacker.getEntity().getUniqueId(), damage);
    defender.getEntity().damage(damage, attacker.getEntity());
    handledDamages.remove(attacker.getEntity().getUniqueId());

    defender.getEntity().setNoDamageTicks(noDamageTicks);
    defender.getEntity().setVelocity(velocity);

    return damage;
  }

  public IndicatorData buildHitIndicator(Player player) {
    IndicatorData data = new IndicatorData(new Vector(
        IND_GRAVITY_HSPEED - Math.random() * 2 * IND_GRAVITY_HSPEED,
        IND_GRAVITY_VSPEED * (1 + Math.random()),
        IND_GRAVITY_HSPEED - Math.random() * 2 * IND_GRAVITY_HSPEED),
        IndicatorStyle.GRAVITY);
    data.addOwner(player);
    return data;
  }
}
