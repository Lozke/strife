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
package info.faceland.strife.attributes;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;

import io.pixeloutlaw.minecraft.spigot.hilt.HiltItemStack;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeHandler {

    public static double getValue(ItemStack itemStack, StrifeAttribute attribute) {
        return getValue(new HiltItemStack(itemStack), attribute);
    }

    public static double getValue(HiltItemStack itemStack, StrifeAttribute attribute) {
        double amount = 0D;
        if (itemStack == null || itemStack.getType() == Material.AIR || attribute == null) {
            return amount;
        }
        List<String> lore = itemStack.getLore();
        List<String> strippedLore = stripColor(lore);
        for (String s : strippedLore) {
            String retained = CharMatcher.JAVA_LETTER.or(CharMatcher.is(' ')).retainFrom(s).trim();
            if (retained.equals(attribute.getName().trim())) {
                amount += NumberUtils.toDouble(CharMatcher.DIGIT.or(CharMatcher.is('-')).retainFrom(s));
            }
        }
        if (attribute.isPercentage()) {
            amount /= 100;
        }
        return attribute.getCap() > 0D ? Math.min(amount, attribute.getCap()) : amount;
    }

    private static List<String> stripColor(List<String> strings) {
        List<String> stripped = new ArrayList<>();
        for (String s : strings) {
            stripped.add(ChatColor.stripColor(s));
        }
        return stripped;
    }

    public static void updateHealth(Player player, double maxHealth) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        player.setHealthScaled(true);
        player.setHealthScale(Math.ceil(maxHealth / 2));
    }

    public static boolean meetsLevelRequirement(Player player, ItemStack itemStack) {
        return meetsLevelRequirement(player, new HiltItemStack(itemStack));
    }

    public static boolean meetsLevelRequirement(Player player, HiltItemStack hiltItemStack) {
        return !(player == null || hiltItemStack == null) && player.getLevel() >= getValue(hiltItemStack, StrifeAttribute.LEVEL_REQUIREMENT);
    }

    @SafeVarargs
    public static Map<StrifeAttribute, Double> combineMaps(Map<StrifeAttribute, Double>... maps) {
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        for (Map<StrifeAttribute, Double> map : maps) {
            for (Map.Entry<StrifeAttribute, Double> ent : map.entrySet()) {
                double val = attributeDoubleMap.containsKey(ent.getKey()) ? attributeDoubleMap.get(ent.getKey()) : 0;
                double calculatedValue = val + ent.getValue();
                attributeDoubleMap.put(ent.getKey(), calculatedValue);
            }
        }
        return attributeDoubleMap;
    }

}
