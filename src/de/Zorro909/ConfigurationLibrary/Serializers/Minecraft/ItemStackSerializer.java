package de.Zorro909.ConfigurationLibrary.Serializers.Minecraft;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import de.Zorro909.ConfigurationLibrary.ConfigurationPane;
import de.Zorro909.ConfigurationLibrary.Serializers.Serializer;

public class ItemStackSerializer implements Serializer<ItemStack> {

    @Override
    public void serialize(ItemStack object, ConfigurationPane configPane) {
        configPane.set("material", object.getType().toString());
        configPane.set("amount", object.getAmount());
        if (object.getItemMeta().hasDisplayName()) {
            configPane.set("displayName", object.getItemMeta().getDisplayName());
        }
        if (object.getItemFlags().size() > 0) {
            configPane.setList("itemFlags", object.getItemFlags().stream()
                    .map((flag) -> flag.toString()).collect(Collectors.toList()));
        }
        if (object.getLore() != null) {
            configPane.setList("lore", object.getLore());
        }
        for (Enchantment ench : object.getEnchantments().keySet()) {
            configPane.set("enchantments." + ench.toString(), object.getEnchantmentLevel(ench));
        }
    }

    @Override
    public ItemStack deserialize(ConfigurationPane configPane) {
        String matString = configPane.get("material", String.class);
        if (matString == null || matString.equals("null")) {
            return null;
        }
        Material material = Material.getMaterial(matString);
        if (material == null) {
            return null;
        }
        ItemStack stack = new ItemStack(material);
        Integer amount = configPane.get("amount", Integer.class);
        if (amount == null) {
            amount = 1;
        }
        stack.setAmount(amount);

        String displayName = configPane.get("displayName", String.class);
        if (displayName != null) {
            stack.getItemMeta().setDisplayName(displayName);
        }

        List<String> flags = configPane.getList("itemFlags", String.class);
        if (flags != null) {
            for (String flag : flags) {
                stack.addItemFlags(ItemFlag.valueOf(flag));
            }
        }

        List<String> lore = configPane.getList("lore", String.class);
        if (lore != null) {
            stack.setLore(lore);
        }

        List<String> enchantments = configPane.getKeys("enchantments");
        for (String ench : enchantments) {
            stack.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(ench)),
                    configPane.get("enchantments." + ench, Integer.class));
        }
        return stack;
    }

    @Override
    public Class<ItemStack> getSerializedType() {
        return ItemStack.class;
    }

}
