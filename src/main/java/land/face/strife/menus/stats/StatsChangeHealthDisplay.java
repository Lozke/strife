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
package land.face.strife.menus.stats;

import com.tealcube.minecraft.bukkit.TextUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.menus.BlankIcon;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StatsChangeHealthDisplay extends MenuItem {

  private final StrifePlugin plugin;
  private Map<UUID, Boolean> selfInspectMap = new HashMap<>();

  StatsChangeHealthDisplay(StrifePlugin plugin) {
    super(TextUtils.color("&c&lHealth Display Options"), new ItemStack(Material.APPLE));
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player commandSender) {
    Player player = StrifePlugin.getInstance().getStatsMenu().getTargetPlayer();
    if (!player.isValid() || commandSender != player) {
      selfInspectMap.put(commandSender.getUniqueId(), false);
      return BlankIcon.getBlankStack();
    }
    selfInspectMap.put(commandSender.getUniqueId(), true);
    ItemStack itemStack = new ItemStack(Material.APPLE);
    ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
    itemMeta.setDisplayName(getDisplayName());
    List<String> lore = new ArrayList<>();

    Champion champion = plugin.getChampionManager().getChampion(player);

    lore.add(TextUtils.color("&7Click this icon to change how your"));
    lore.add(TextUtils.color("&7hearts are displayed!"));
    lore.add(TextUtils.color("&fSetting: " + WordUtils.capitalizeFully(
        champion.getSaveData().getHealthDisplayType().toString().replaceAll("_", " "))));

    itemMeta.setLore(lore);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (!selfInspectMap.getOrDefault(event.getPlayer().getUniqueId(), false)) {
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(event.getPlayer());
    int ordinal = champion.getSaveData().getHealthDisplayType().ordinal();
    ordinal++;
    if (ordinal == ChampionSaveData.DISPLAY_OPTIONS.length) {
      ordinal = 0;
    }
    champion.getSaveData().setHealthDisplayType(ChampionSaveData.DISPLAY_OPTIONS[ordinal]);
    plugin.getStatUpdateManager().updateHealth(plugin.getStrifeMobManager().getStatMob(event.getPlayer()));
    event.setWillUpdate(true);
    event.setWillClose(false);
  }

}
