package com.badbones69.crazyenchantments.paper.utilities;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Starter;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import com.badbones69.crazyenchantments.paper.api.objects.CEnchantment;
import com.badbones69.crazyenchantments.paper.api.objects.EnchantedArrow;
import com.badbones69.crazyenchantments.paper.controllers.settings.EnchantmentBookSettings;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BowUtils {

    private final CrazyEnchantments plugin = CrazyEnchantments.getPlugin();

    private final Starter starter = plugin.getStarter();

    private final EnchantmentBookSettings enchantmentBookSettings = starter.getEnchantmentBookSettings();

    // Sticky Shot
    private final List<Block> webBlocks = new ArrayList<>();

    private final List<EnchantedArrow> enchantedArrows = new ArrayList<>();
    public void addArrow(Arrow arrow, ItemStack bow, Map<CEnchantment, Integer> enchantments) {
        if (arrow == null) return;

        EnchantedArrow enchantedArrow = new EnchantedArrow(arrow, bow, enchantments);

        enchantedArrows.add(enchantedArrow);
    }

    public void removeArrow(EnchantedArrow enchantedArrow) {
        if (!enchantedArrows.contains(enchantedArrow) || enchantedArrow == null) return;

        enchantedArrows.remove(enchantedArrow);
    }

    public boolean isBowEnchantActive(CEnchantments customEnchant, EnchantedArrow enchantedArrow) {
        return customEnchant.isActivated() &&
                enchantedArrow.hasEnchantment(customEnchant) &&
                customEnchant.chanceSuccessful(enchantedArrow.getLevel(customEnchant));
    }

    public boolean allowsCombat(Entity entity) {
        return starter.getPluginSupport().allowCombat(entity.getLocation());
    }

    public EnchantedArrow getEnchantedArrow(Arrow arrow) {
        return enchantedArrows.stream().filter((enchArrow) -> enchArrow != null && enchArrow.arrow() != null && enchArrow.arrow().equals(arrow)).findFirst().orElse(null);
    }

    // Multi Arrow Start!

    public void spawnArrows(Entity entity, Entity projectile, ItemStack bow) {
        Arrow spawnedArrow = entity.getWorld().spawn(projectile.getLocation(), Arrow.class);

        EnchantedArrow enchantedMultiArrow = new EnchantedArrow(spawnedArrow, bow, enchantmentBookSettings.getEnchantments(bow));

        enchantedArrows.add(enchantedMultiArrow);

        spawnedArrow.setShooter((ProjectileSource) entity);

        Vector vector = new Vector(randomSpread(), 0, randomSpread());

        spawnedArrow.setVelocity(projectile.getVelocity().add(vector));

        if (((Arrow) projectile).isCritical()) spawnedArrow.setCritical(true);

        if (projectile.getFireTicks() > 0) spawnedArrow.setFireTicks(projectile.getFireTicks());

        spawnedArrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

        enchantedArrows.remove(enchantedMultiArrow);
    }

    private float randomSpread() {
        float spread = (float) .2;
        return -spread + (float) (Math.random() * (spread * 2));
    }

    // Multi Arrow End!

    // Sticky Shot Start!
    public List<Block> getWebBlocks() {
        return webBlocks;
    }

    public void spawnWebs(Entity hitEntity, EnchantedArrow enchantedArrow, Arrow arrow) {
        if (enchantedArrow == null) return;

        if (!isBowEnchantActive(CEnchantments.STICKY_SHOT, enchantedArrow)) return;

        if (hitEntity == null) {
            Location entityLocation = arrow.getLocation();

            if (entityLocation.getBlock().getType() != Material.AIR) return;

            entityLocation.getBlock().setType(Material.COBWEB);
            webBlocks.add(entityLocation.getBlock());

            arrow.remove();

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                entityLocation.getBlock().setType(Material.AIR);
                webBlocks.remove(entityLocation.getBlock());
            }, 5 * 20);
        } else {
            arrow.remove();
            setWebBlocks(hitEntity);
        }
    }

    private void setWebBlocks(Entity hitEntity) {
        for (Block block : getCube(hitEntity.getLocation())) {

            block.setType(Material.COBWEB);
            webBlocks.add(block);

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (block.getType() == Material.COBWEB) {
                    block.setType(Material.AIR);
                    webBlocks.remove(block);
                }
            }, 5 * 20);
        }
    }

    // Sticky Shot End!

    private List<Block> getCube(Location start) {
        List<Block> newBlocks = new ArrayList<>();

        for (double x = start.getX() - 1; x <= start.getX() + 1; x++) {
            for (double z = start.getZ() - 1; z <= start.getZ() + 1; z++) {
                Location loc = new Location(start.getWorld(), x, start.getY(), z);
                if (loc.getBlock().getType() == Material.AIR) newBlocks.add(loc.getBlock());
            }
        }

        return newBlocks;
    }
}