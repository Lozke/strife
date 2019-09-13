package info.faceland.strife.data.ability;

import info.faceland.strife.data.conditions.Condition;
import info.faceland.strife.data.effects.Effect;
import java.util.List;
import java.util.Set;

public class Ability {

  private final String id;
  private final String name;
  private final TargetType targetType;
  private final boolean raycastsTargetEntities;
  private final float range;
  private final List<Effect> effects;
  private final int cooldown;
  private final int maxCharges;
  private final int globalCooldownTicks;
  private final boolean showMessages;
  private final Set<Condition> conditions;
  private final AbilityIconData abilityIconData;
  private final boolean friendly;

  public Ability(String id, String name, List<Effect> effects, TargetType targetType, float range,
      int cooldown, int maxCharges, int globalCooldownTicks, boolean showMsgs,
      boolean raycastsTargetEntities, Set<Condition> conditions, boolean friendly,
      AbilityIconData abilityIconData) {
    this.id = id;
    this.name = name;
    this.cooldown = cooldown;
    this.maxCharges = maxCharges;
    this.globalCooldownTicks = globalCooldownTicks;
    this.effects = effects;
    this.targetType = targetType;
    this.raycastsTargetEntities = raycastsTargetEntities;
    this.range = range;
    this.showMessages = showMsgs;
    this.conditions = conditions;
    this.abilityIconData = abilityIconData;
    this.friendly = friendly;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public TargetType getTargetType() {
    return targetType;
  }

  public float getRange() {
    return range;
  }

  public List<Effect> getEffects() {
    return effects;
  }

  public int getCooldown() {
    return cooldown;
  }

  public int getMaxCharges() {
    return maxCharges;
  }

  public int getGlobalCooldownTicks() {
    return globalCooldownTicks;
  }

  public boolean isShowMessages() {
    return showMessages;
  }

  public Set<Condition> getConditions() {
    return conditions;
  }

  public AbilityIconData getAbilityIconData() {
    return abilityIconData;
  }

  public boolean isFriendly() {
    return friendly;
  }

  public boolean isRaycastsTargetEntities() {
    return raycastsTargetEntities;
  }

  public enum TargetType {
    SELF, MASTER, MINIONS, PARTY, SINGLE_OTHER, TARGET_AREA, TARGET_GROUND, NONE
  }
}
