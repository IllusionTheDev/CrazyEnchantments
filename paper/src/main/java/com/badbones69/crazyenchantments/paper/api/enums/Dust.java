package com.badbones69.crazyenchantments.paper.api.enums;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.Methods;
import com.badbones69.crazyenchantments.paper.api.FileManager.Files;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DustData;
import com.badbones69.crazyenchantments.paper.api.enums.pdc.DataKeys;
import com.badbones69.crazyenchantments.paper.api.objects.ItemBuilder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.google.gson.Gson;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum Dust {
    
    SUCCESS_DUST("Success-Dust", "SuccessDust", Arrays.asList("s", "success")),
    DESTROY_DUST("Destroy-Dust", "DestroyDust", Arrays.asList("d", "destroy")),
    MYSTERY_DUST("Mystery-Dust", "MysteryDust", Arrays.asList("m", "mystery")),
    FAILED_DUST("Failed-Dust", "FailedDust", Arrays.asList("f", "failed"));
    
    private static final HashMap<Dust, ItemBuilder> itemBuilderDust = new HashMap<>();
    private final String name;
    private final String configName;
    private final List<String> knownNames;
    private final int max;
    private final int min;

    private final CrazyEnchantments plugin = CrazyEnchantments.getPlugin();

    private final Methods methods = plugin.getStarter().getMethods();
    
    Dust(String name, String configName, List<String> knowNames) {
        this.name = name;
        this.knownNames = knowNames;
        this.configName = configName;
        this.max = Files.CONFIG.getFile().getInt("Settings.Dust." + configName + ".PercentRange.Max", 100);
        this.min = Files.CONFIG.getFile().getInt("Settings.Dust." + configName + ".PercentRange.Min", max);
    }
    
    public static void loadDust() {
        FileConfiguration config = Files.CONFIG.getFile();
        itemBuilderDust.clear();

        for (Dust dust : values()) {
            String path = "Settings.Dust." + dust.getConfigName() + ".";
            Dust.itemBuilderDust.put(dust, new ItemBuilder()
            .setName(config.getString(path + "Name"))
            .setLore(config.getStringList(path + "Lore"))
            .setMaterial(config.getString(path + "Item")));
        }
    }
    
    public static Dust getFromName(String nameString) {
        for (Dust dust : Dust.values()) {
            if (dust.getKnownNames().contains(nameString.toLowerCase()) ||
            dust.getConfigName().contains(nameString)) return dust;
        }

        return null;
    }
    
    public String getName() {
        return name;
    }
    
    public List<String> getKnownNames() {
        return knownNames;
    }
    
    public String getConfigName() {
        return configName;
    }
    
    public ItemStack getDust() {
        return getDust(1);
    }
    
    public ItemStack getDust(int amount) {

        return getDust(methods.percentPick(max, min), amount);
    }
    
    public ItemStack getDust(int percent, int amount) {
        ItemStack item = itemBuilderDust.get(this)
                .addLorePlaceholder("%Percent%", String.valueOf(percent))
                .setAmount(amount).build();

        // PDC Start
                Gson gson = new Gson();

                ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(DataKeys.DUST.getKey(), PersistentDataType.STRING, gson.toJson(new DustData(getConfigName(), min, max, percent)));
                item.setItemMeta(meta);
        // PDC End

        return item;
    }
}