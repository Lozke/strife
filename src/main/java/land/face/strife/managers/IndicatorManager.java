package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.IndicatorData;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import net.minecraft.server.v1_14_R1.ChatBaseComponent;
import net.minecraft.server.v1_14_R1.EntityArmorStand;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_14_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_14_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_14_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class IndicatorManager {

  private Map<EntityArmorStand, IndicatorData> indicators = new ConcurrentHashMap<>();
  public static float GRAVITY_FALL_SPEED;
  private static final int MAX_STAGE = 10;

  public IndicatorManager() {
    GRAVITY_FALL_SPEED = (float) StrifePlugin.getInstance().getSettings()
        .getDouble("config.indicators.gravity-fall-speed", 20);
  }

  public void addIndicator(LivingEntity creator, LivingEntity target, IndicatorData data, String text) {
    if (!(creator instanceof Player)) {
      return;
    }
    Location loc = TargetingUtil.getOriginLocation(target, OriginLocation.CENTER);
    Location midway = creator.getEyeLocation().clone()
        .add(creator.getEyeLocation().clone().subtract(loc).multiply(-0.65));

    WorldServer w = ((CraftWorld) loc.getWorld()).getHandle();

    EntityArmorStand armorstand = new EntityArmorStand(w.getMinecraftWorld(),
        midway.getX(), midway.getY(), midway.getZ());

    armorstand.setNoGravity(true);
    armorstand.setInvisible(true);
    armorstand.setSmall(true);
    armorstand.setMarker(true);
    armorstand.setCustomNameVisible(false);
    armorstand.setCustomName(ChatBaseComponent.ChatSerializer.b(TextUtils.color(text)));
    armorstand.setCustomNameVisible(true);

    PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving(armorstand);

    ((CraftPlayer) creator).getHandle().playerConnection.sendPacket(spawnPacket);
    indicators.put(armorstand, data);
  }

  public void tickAllIndicators() {
    for (EntityArmorStand stand : indicators.keySet()) {
      IndicatorData data = indicators.get(stand);
      if (data.getStage() >= MAX_STAGE) {
        indicators.remove(stand);
        PacketPlayOutEntityDestroy killStand = new PacketPlayOutEntityDestroy(stand.getId());
        for (Player p : data.getOwners()) {
          ((CraftPlayer) p).getHandle().playerConnection.sendPacket(killStand);
        }
        continue;
      }
      data.setStage(data.getStage() + 1);
      Vector change = IndicatorData.getRelativeChange(data);
      PacketPlayOutRelEntityMove goal = new PacketPlayOutRelEntityMove(stand.getId(),
          (short) (change.getX() * data.getStage()),
          (short) (change.getY() * data.getStage()),
          (short) (change.getZ() * data.getStage()),
          false);
      for (Player p : data.getOwners()) {
        ((CraftPlayer) p).getHandle().playerConnection.sendPacket(goal);
      }
    }
  }

}
