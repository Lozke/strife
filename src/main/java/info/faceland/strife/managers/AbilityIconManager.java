package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AbilityCooldownContainer;
import info.faceland.strife.data.AbilityIconData;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.ChampionSaveData;
import info.faceland.strife.stats.AbilitySlot;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.LogUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AbilityIconManager {

  private final StrifePlugin plugin;

  static final String ABILITY_PREFIX = "Ability: ";

  public AbilityIconManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public void removeIconItem(Player player, AbilitySlot slot) {
    if (slot.getSlotIndex() != -1 && isAbilityIcon(
        player.getInventory().getItem(slot.getSlotIndex()))) {
      player.getInventory().setItem(slot.getSlotIndex(), null);
    }
  }

  public void clearAbilityIcon(Player player, AbilitySlot slot) {
    plugin.getChampionManager().getChampion(player).getSaveData().setAbility(slot, null);
    removeIconItem(player, slot);
  }

  public void setAllAbilityIcons(Player player) {
    ChampionSaveData data = plugin.getChampionManager().getChampion(player).getSaveData();
    for (Ability ability : data.getAbilities().values()) {
      setAbilityIcon(player, ability.getAbilityIconData());
    }
  }

  public void setAbilityIcon(Player player, AbilityIconData abilityIconData) {
    if (abilityIconData == null || abilityIconData.getAbilitySlot() == AbilitySlot.INVALID) {
      return;
    }
    AbilitySlot slot = abilityIconData.getAbilitySlot();
    if (slot.getSlotIndex() == -1) {
      return;
    }
    ItemStack displacedItem = player.getInventory().getItem(slot.getSlotIndex());
    player.getInventory().setItem(slot.getSlotIndex(), abilityIconData.getStack());
    if (displacedItem != null && !isAbilityIcon(displacedItem)) {
      HashMap<Integer, ItemStack> excessItems = player.getInventory().addItem(displacedItem);
      for (ItemStack extraStack : excessItems.values()) {
        player.getWorld().dropItem(player.getLocation(), extraStack);
      }
    }
    Bukkit.getScheduler().runTaskLater(plugin,
        () -> plugin.getAbilityIconManager().updateIconProgress(player, slot), 2L);
  }

  public boolean playerHasAbility(Player player, Ability ability) {
    ChampionSaveData data = plugin.getChampionManager().getChampion(player).getSaveData();
    return data.getAbilities().containsValue(ability);
  }

  public boolean isAbilityIcon(ItemStack stack) {
    if (stack == null) {
      return false;
    }
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    return !StringUtils.isBlank(name) && name.startsWith(ABILITY_PREFIX) && !name.contains("✫");
  }

  public void triggerAbility(Player player, int slotNumber) {
    if (player.getCooldown(Material.DIAMOND_CHESTPLATE) > 0) {
      return;
    }
    AbilitySlot slot = AbilitySlot.fromSlot(slotNumber);
    if (slot == AbilitySlot.INVALID) {
      return;
    }
    Ability ability = plugin.getChampionManager().getChampion(player).getSaveData()
        .getAbility(slot);
    if (ability == null) {
      if (isAbilityIcon(player.getInventory().getItem(slotNumber))) {
        player.getInventory().setItem(slotNumber, null);
      }
      return;
    }
    boolean abilitySucceeded = plugin.getAbilityManager()
        .execute(ability, plugin.getStrifeMobManager().getStatMob(player), null);
    if (!abilitySucceeded) {
      LogUtil.printDebug("Ability " + ability.getId() + " failed execution");
      plugin.getAbilityManager().setGlobalCooldown(player, 5);
      return;
    }
    plugin.getAbilityManager().setGlobalCooldown(player, ability);
    player.playSound(player.getEyeLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
    updateIconProgress(player, ability);
  }

  private void updateIconProgress(Player player, AbilitySlot slot) {
    Champion champion = plugin.getChampionManager().getChampion(player);
    setIconDamage(champion, champion.getSaveData().getAbility(slot));
  }

  public void updateIconProgress(Player player, Ability ability) {
    setIconDamage(plugin.getChampionManager().getChampion(player), ability);
  }

  private void setIconDamage(Champion champion, Ability ability) {
    if (ability == null || !champion.getSaveData().getAbilities().containsValue(ability)) {
      return;
    }
    AbilityCooldownContainer container = plugin.getAbilityManager()
        .getCooldownContainer(champion.getPlayer(), ability.getId());
    int charges = ability.getMaxCharges();
    double percent = 1D;
    if (container != null) {
      if (container.getSpentCharges() == charges) {
        charges = 1;
        percent = plugin.getAbilityManager().getCooldownPercent(container);
      } else {
        charges -= container.getSpentCharges();
      }
    }
    ItemUtil.sendAbilityIconPacket(ability.getAbilityIconData().getStack(), champion.getPlayer(),
        ability.getAbilityIconData().getAbilitySlot().getSlotIndex(), percent, charges);
  }
}
