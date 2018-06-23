package info.faceland.strife.managers;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.EntityAbilitySet;
import info.faceland.strife.data.UniqueEntity;
import info.faceland.strife.data.UniqueEntityData;
import info.faceland.strife.util.LogUtil;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Zombie;
import org.bukkit.metadata.FixedMetadataValue;

public class UniqueEntityManager {

  private final StrifePlugin plugin;
  private final Map<LivingEntity, UniqueEntityData> liveUniquesMap;
  private final Map<String, UniqueEntity> loadedUniquesMap;

  public UniqueEntityManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.liveUniquesMap = new HashMap<>();
    this.loadedUniquesMap = new HashMap<>();
  }

  public UniqueEntity getLivingUnique(LivingEntity livingEntity) {
    if (!liveUniquesMap.containsKey(livingEntity)) {
      return null;
    }
    return liveUniquesMap.get(livingEntity).getUniqueEntity();
  }

  public Map<LivingEntity, UniqueEntityData> getLiveUniquesMap() {
    return liveUniquesMap;
  }

  public Map<String, UniqueEntity> getLoadedUniquesMap() {
    return loadedUniquesMap;
  }

  public void addUniqueEntity(String key, UniqueEntity uniqueEntity) {
    loadedUniquesMap.put(key, uniqueEntity);
  }

  public void removeEntity(LivingEntity entity, boolean purge, boolean triggerTimer) {
    if (!liveUniquesMap.containsKey(entity)) {
      return;
    }
    liveUniquesMap.remove(entity);
    if (purge) {
      entity.remove();
    }
    if (triggerTimer) {
      plugin.getSpawnerManager().triggerRespawnCooldown(entity);
    }
  }

  public int getPhase(LivingEntity livingEntity) {
    if (!liveUniquesMap.containsKey(livingEntity)) {
      LogUtil.printWarning("Attempting to get phase of non-unique entity...");
      return 0;
    }
    return liveUniquesMap.get(livingEntity).getPhase();
  }

  public LivingEntity getMaster(LivingEntity livingEntity) {
    if (!liveUniquesMap.containsKey(livingEntity)) {
      return null;
    }
    return liveUniquesMap.get(livingEntity).getMaster();
  }

  public UniqueEntityData getData(LivingEntity livingEntity) {
    if (!liveUniquesMap.containsKey(livingEntity)) {
      return null;
    }
    return liveUniquesMap.get(livingEntity);
  }

  public EntityAbilitySet getAbilitySet(LivingEntity livingEntity) {
    if (!liveUniquesMap.containsKey(livingEntity)) {
      LogUtil.printWarning("Attempting to get ability set of non-unique entity...");
      return null;
    }
    return liveUniquesMap.get(livingEntity).getUniqueEntity().getAbilitySet();
  }

  public EntityAbilitySet getAbilitySet(UniqueEntity uniqueEntity) {
    return uniqueEntity.getAbilitySet();
  }

  public void killAllSpawnedUniques() {
    for (LivingEntity le : liveUniquesMap.keySet()) {
      le.remove();
    }
    liveUniquesMap.clear();
  }

  public boolean isUnique(LivingEntity entity) {
    return liveUniquesMap.containsKey(entity);
  }

  public LivingEntity spawnUnique(String unique, Location location) {
    UniqueEntity uniqueEntity = loadedUniquesMap.get(unique);
    if (uniqueEntity == null) {
      plugin.getLogger().warning("Attempted to spawn non-existing unique: " + unique);
      return null;
    }
    return spawnUnique(uniqueEntity, location);
  }

  public LivingEntity spawnUnique(UniqueEntity uniqueEntity, Location location) {
    if (uniqueEntity.getType() == null) {
      plugin.getLogger()
          .warning("Attempted to unique with null entity type: " + uniqueEntity.getName());
      return null;
    }

    Entity entity = location.getWorld().spawn(location, uniqueEntity.getType().getEntityClass(),
        e -> e.setMetadata("BOSS", new FixedMetadataValue(plugin, true)));
    if (!(entity instanceof LivingEntity)) {
      plugin.getLogger()
          .warning("Attempted to spawn non-living unique entity: " + uniqueEntity.getName());
      return null;
    }

    LivingEntity spawnedUnique = (LivingEntity) entity;

    spawnedUnique.setRemoveWhenFarAway(false);

    double health = uniqueEntity.getAttributeMap().getOrDefault(StrifeAttribute.HEALTH, 5D);
    spawnedUnique.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
    spawnedUnique.setHealth(health);

    if (spawnedUnique instanceof Zombie) {
      ((Zombie) spawnedUnique).setBaby(uniqueEntity.isBaby());
    }
    if (spawnedUnique instanceof Slime) {
      ((Slime) spawnedUnique).setSize(uniqueEntity.getSize());
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE) != null && uniqueEntity
        .isKnockbackImmune()) {
      spawnedUnique.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(100);
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
      double speed =
          uniqueEntity.getAttributeMap().getOrDefault(StrifeAttribute.MOVEMENT_SPEED, 100D) / 100D;
      spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(
          spawnedUnique.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * speed);
    }

    if (spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED) != null) {
      double speed =
          uniqueEntity.getAttributeMap().getOrDefault(StrifeAttribute.MOVEMENT_SPEED, 100D) / 100D;
      spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).setBaseValue(
          spawnedUnique.getAttribute(Attribute.GENERIC_FLYING_SPEED).getBaseValue() * speed);
    }

    spawnedUnique.getEquipment().clear();

    spawnedUnique.getEquipment().setHelmet(uniqueEntity.getHelmetItem());
    spawnedUnique.getEquipment().setHelmetDropChance(0f);
    spawnedUnique.getEquipment().setChestplate(uniqueEntity.getChestItem());
    spawnedUnique.getEquipment().setChestplateDropChance(0f);
    spawnedUnique.getEquipment().setLeggings(uniqueEntity.getLegsItem());
    spawnedUnique.getEquipment().setLeggingsDropChance(0f);
    spawnedUnique.getEquipment().setBoots(uniqueEntity.getBootsItem());
    spawnedUnique.getEquipment().setBootsDropChance(0f);
    spawnedUnique.getEquipment().setItemInMainHand(uniqueEntity.getMainHandItem());
    spawnedUnique.getEquipment().setItemInMainHandDropChance(0f);
    spawnedUnique.getEquipment().setItemInOffHand(uniqueEntity.getOffHandItem());
    spawnedUnique.getEquipment().setItemInOffHandDropChance(0f);

    spawnedUnique.setCustomName(uniqueEntity.getName());
    spawnedUnique.setCustomNameVisible(true);

    plugin.getEntityStatCache().setEntityStats(spawnedUnique, uniqueEntity.getAttributeMap());
    liveUniquesMap.put(spawnedUnique, new UniqueEntityData(uniqueEntity));
    plugin.getAbilityManager().checkPhaseChange(plugin.getEntityStatCache().getAttributedEntity(spawnedUnique));
    return spawnedUnique;
  }
}
