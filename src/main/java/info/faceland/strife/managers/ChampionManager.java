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
package info.faceland.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static info.faceland.strife.attributes.StrifeAttribute.LEVEL_REQUIREMENT;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.ChampionSaveData;
import info.faceland.strife.data.champion.PlayerEquipmentCache;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.ItemUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

public class ChampionManager {

  private final StrifePlugin plugin;
  private final Map<UUID, Champion> championMap = new HashMap<>();

  private final static String LVL_REQ_MAIN_WEAPON =
      "&e&lYou do not meet the level requirement for your weapon!";
  private final static String LVL_REQ_OFF_WEAPON =
      "&e&lYou do not meet the level requirement for your offhand item!";
  private final static String LVL_REQ_HELMET =
      "&e&lYou do not meet the level requirement for your helmet!";
  private final static String LVL_REQ_CHEST =
      "&c&lYou do not meet the level requirement for your chest armor!";
  private final static String LVL_REQ_LEGS =
      "&e&lYou do not meet the level requirement for your leg armor!";
  private final static String LVL_REQ_BOOTS =
      "&c&lYou do not meet the level requirement for your boots!";
  private final static String LVL_REQ_GENERIC =
      "&c&lYou will not benefit from the stats on this item!";

  public ChampionManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public Champion getChampion(Player player) {
    UUID uuid = player.getUniqueId();
    if (championExists(uuid)) {
      championMap.get(uuid).setPlayer(player);
      return championMap.get(uuid);
    }
    ChampionSaveData saveData = plugin.getStorage().load(player.getUniqueId());
    if (saveData == null) {
      saveData = new ChampionSaveData(player.getUniqueId());
      Champion champ = new Champion(player, saveData);
      championMap.put(uuid, champ);
      return champ;
    }
    Champion champion = new Champion(player, saveData);
    championMap.put(uuid, champion);
    return championMap.get(uuid);
  }

  public boolean championExists(UUID uuid) {
    return championMap.containsKey(uuid);
  }

  public Collection<Champion> getChampions() {
    return new HashSet<>(championMap.values());
  }

  public void addListOfChampions(List<Champion> champions) {
    for (Champion c : champions) {
      championMap.put(c.getUniqueId(), c);
    }
  }

  public void clearAllChampions() {
    championMap.clear();
  }

  private void buildBaseAttributes(Champion champion) {
    champion.setAttributeBaseCache(plugin.getMonsterManager().getBaseStats(champion.getPlayer()));
  }

  private void buildPointAttributes(Champion champion) {
    Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
    for (StrifeStat stat : champion.getLevelMap().keySet()) {
      int statLevel = champion.getLevelMap().get(stat);
      if (statLevel == 0) {
        continue;
      }
      for (StrifeAttribute attr : stat.getAttributeMap().keySet()) {
        double amount = stat.getAttributeMap().get(attr) * statLevel;
        if (attributeDoubleMap.containsKey(attr)) {
          amount += attributeDoubleMap.get(attr);
        }
        attributeDoubleMap.put(attr, amount);
      }
    }
    champion.setAttributeLevelPointCache(attributeDoubleMap);
  }

  private void buildEquipmentAttributes(Champion champion) {
    EntityEquipment equipment = champion.getPlayer().getEquipment();
    PlayerEquipmentCache equipmentCache = champion.getEquipmentCache();

    boolean recombine = false;
    if (!doesHashMatch(equipment.getItemInMainHand(), equipmentCache.getMainHandHash()) ||
        !doesHashMatch(equipment.getItemInOffHand(), equipmentCache.getOffHandHash())) {

      // TODO: Configurable list of non-valid main/offhand materialTypes that won't be picked up
      if (ItemUtil.isArmor(equipment.getItemInMainHand().getType())
          || equipment.getItemInMainHand().getType() == Material.EMERALD) {
        equipmentCache.setMainhandStats(new HashMap<>());
      } else {
        equipmentCache.setMainhandStats(
            plugin.getAttributeUpdateManager().getItemStats(equipment.getItemInMainHand()));
        equipmentCache.getMainAbilities().clear();
        equipmentCache.getMainAbilities().addAll(
            plugin.getLoreAbilityManager().getLoreAbilitiesFromItem(equipment.getItemInMainHand()));
      }
      double offhandStatMultiplier = ItemUtil
          .getDualWieldEfficiency(equipment.getItemInMainHand(), equipment.getItemInOffHand());
      equipmentCache.setOffhandStats(plugin.getAttributeUpdateManager().getItemStats(
          equipment.getItemInOffHand(), offhandStatMultiplier));
      equipmentCache.getOffhandAbilities().clear();
      equipmentCache.getOffhandAbilities().addAll(
          plugin.getLoreAbilityManager().getLoreAbilitiesFromItem(equipment.getItemInOffHand()));

      removeAttributes(equipment.getItemInMainHand());
      removeAttributes(equipment.getItemInOffHand());

      equipmentCache.setMainHandHash(hashItem(equipment.getItemInMainHand()));
      equipmentCache.setOffHandHash(hashItem(equipment.getItemInOffHand()));
      recombine = true;
    }
    if (!doesHashMatch(equipment.getHelmet(), equipmentCache.getHelmetHash())) {
      equipmentCache.setHelmetStats(
          plugin.getAttributeUpdateManager().getItemStats(equipment.getHelmet()));
      equipmentCache.getHelmetAbilities().clear();
      equipmentCache.getHelmetAbilities().addAll(
          plugin.getLoreAbilityManager().getLoreAbilitiesFromItem(equipment.getHelmet()));

      removeAttributes(equipment.getHelmet());
      equipmentCache.setHelmetHash(hashItem(equipment.getHelmet()));
      recombine = true;
    }
    if (!doesHashMatch(equipment.getChestplate(), equipmentCache.getChestHash())) {
      equipmentCache.setChestplateStats(
          plugin.getAttributeUpdateManager().getItemStats(equipment.getChestplate()));
      equipmentCache.getChestAbilities().clear();
      equipmentCache.getChestAbilities().addAll(
          plugin.getLoreAbilityManager().getLoreAbilitiesFromItem(equipment.getChestplate()));
      removeAttributes(equipment.getChestplate());
      equipmentCache.setChestHash(hashItem(equipment.getChestplate()));
      recombine = true;
    }
    if (!doesHashMatch(equipment.getLeggings(), equipmentCache.getLegsHash())) {
      equipmentCache.setLeggingsStats(
          plugin.getAttributeUpdateManager().getItemStats(equipment.getLeggings()));
      equipmentCache.getLegsAbilities().clear();
      equipmentCache.getLegsAbilities().addAll(
          plugin.getLoreAbilityManager().getLoreAbilitiesFromItem(equipment.getLeggings()));
      removeAttributes(equipment.getLeggings());
      equipmentCache.setLegsHash(hashItem(equipment.getLeggings()));
      recombine = true;
    }
    if (!doesHashMatch(equipment.getBoots(), equipmentCache.getBootsHash())) {
      equipmentCache.setBootsStats(
          plugin.getAttributeUpdateManager().getItemStats(equipment.getBoots()));
      equipmentCache.getBootAbilities().clear();
      equipmentCache.getBootAbilities().addAll(
          plugin.getLoreAbilityManager().getLoreAbilitiesFromItem(equipment.getBoots()));
      removeAttributes(equipment.getBoots());
      equipmentCache.setBootsHash(hashItem(equipment.getBoots()));
      recombine = true;
    }

    if (recombine) {
      if (!meetsLevelRequirement(champion.getPlayer(), equipmentCache.getMainhandStats())) {
        sendMessage(champion.getPlayer(), LVL_REQ_MAIN_WEAPON);
        sendMessage(champion.getPlayer(), LVL_REQ_GENERIC);
        equipmentCache.getMainhandStats().clear();
      }
      if (!meetsLevelRequirement(champion.getPlayer(), equipmentCache.getOffhandStats())) {
        sendMessage(champion.getPlayer(), LVL_REQ_OFF_WEAPON);
        sendMessage(champion.getPlayer(), LVL_REQ_GENERIC);
        equipmentCache.getOffhandStats().clear();
      }
      if (!meetsLevelRequirement(champion.getPlayer(), equipmentCache.getHelmetStats())) {
        sendMessage(champion.getPlayer(), LVL_REQ_HELMET);
        sendMessage(champion.getPlayer(), LVL_REQ_GENERIC);
        equipmentCache.getHelmetStats().clear();
      }
      if (!meetsLevelRequirement(champion.getPlayer(), equipmentCache.getChestplateStats())) {
        sendMessage(champion.getPlayer(), LVL_REQ_CHEST);
        sendMessage(champion.getPlayer(), LVL_REQ_GENERIC);
        equipmentCache.getChestplateStats().clear();
      }
      if (!meetsLevelRequirement(champion.getPlayer(), equipmentCache.getLeggingsStats())) {
        sendMessage(champion.getPlayer(), LVL_REQ_LEGS);
        sendMessage(champion.getPlayer(), LVL_REQ_GENERIC);
        equipmentCache.getLeggingsStats().clear();
      }
      if (!meetsLevelRequirement(champion.getPlayer(), equipmentCache.getBootsStats())) {
        sendMessage(champion.getPlayer(), LVL_REQ_BOOTS);
        sendMessage(champion.getPlayer(), LVL_REQ_GENERIC);
        equipmentCache.getBootsStats().clear();
      }
      equipmentCache.recombine(champion);
    }
  }

  public boolean addBoundLoreAbility(Champion champion, LoreAbility loreAbility) {
    if (champion.getSaveData().getBoundAbilities().contains(loreAbility)) {
      return false;
    }
    champion.getSaveData().getBoundAbilities().add(loreAbility);
    champion.getEquipmentCache().combineLoreAbilities(champion);
    return true;
  }

  public boolean removeBoundLoreAbility(Champion champion, LoreAbility loreAbility) {
    if (!champion.getSaveData().getBoundAbilities().contains(loreAbility)) {
      return false;
    }
    champion.getSaveData().getBoundAbilities().remove(loreAbility);
    champion.getEquipmentCache().combineLoreAbilities(champion);
    return true;
  }

  public void updatePointAttributes(Champion champion) {
    buildPointAttributes(champion);
    pushChampionUpdate(champion);
  }

  public void updateBaseAttributes(Champion champion) {
    buildBaseAttributes(champion);
    pushChampionUpdate(champion);
  }

  public void updateEquipmentAttributes(Champion champion) {
    buildEquipmentAttributes(champion);
    pushChampionUpdate(champion);
  }

  public void updateAll(Champion champion) {
    buildPointAttributes(champion);
    buildBaseAttributes(champion);
    buildEquipmentAttributes(champion);

    pushChampionUpdate(champion);
  }

  private void pushChampionUpdate(Champion champion) {
    champion.recombineCache();
    plugin.getAttributedEntityManager().setEntityStats(champion.getPlayer(), AttributeUpdateManager
        .combineMaps(champion.getCombinedCache(), plugin.getGlobalBoostManager().getAttributes()));
  }

  private static void removeAttributes(ItemStack item) {
    if (item == null || item.getType() == Material.AIR) {
      return;
    }
    if (item.getType().getMaxDurability() < 15) {
      return;
    }
    if (!MinecraftReflection.isCraftItemStack(item)) {
      item = MinecraftReflection.getBukkitItemStack(item);
    }
    NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
    compound.put(NbtFactory.ofList("AttributeModifiers"));
  }

  private boolean meetsLevelRequirement(Player player, Map<StrifeAttribute, Double> statMap) {
    return statMap.getOrDefault(LEVEL_REQUIREMENT, 0D) <= player.getLevel();
  }

  private int hashItem(ItemStack itemStack) {
    if (itemStack == null || itemStack.getType() == Material.AIR) {
      return -1;
    }
    return itemStack.hashCode();
  }

  private boolean doesHashMatch(ItemStack itemStack, int hash) {
    if (itemStack == null || itemStack.getType() == Material.AIR) {
      return hash == -1;
    }
    return itemStack.hashCode() == hash;
  }
}
