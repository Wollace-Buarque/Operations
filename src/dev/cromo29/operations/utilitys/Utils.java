package dev.cromo29.operations.utilitys;

import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Utils {

    public static Enchantment translateEnchant(String enchantName) {


        if (enchantName.equalsIgnoreCase("PROTECTION"))
            return Enchantment.PROTECTION_ENVIRONMENTAL;

        if (enchantName.equalsIgnoreCase("FIREPROTECTION"))
            return Enchantment.PROTECTION_FIRE;

        if (enchantName.equalsIgnoreCase("FEATHERFALLING"))
            return Enchantment.PROTECTION_FALL;

        if (enchantName.equalsIgnoreCase("BLASTPROTECTION"))
            return Enchantment.PROTECTION_EXPLOSIONS;

        if (enchantName.equalsIgnoreCase("PROJECTILEPROTECTION"))
            return Enchantment.PROTECTION_ENVIRONMENTAL;

        if (enchantName.equalsIgnoreCase("PROJECTILE"))
            return Enchantment.PROTECTION_PROJECTILE;

        if (enchantName.equalsIgnoreCase("THORNS"))
            return Enchantment.THORNS;


        if (enchantName.equalsIgnoreCase("OXYGEN"))
            return Enchantment.OXYGEN;

        if (enchantName.equalsIgnoreCase("DEEPBREATH"))
            return Enchantment.WATER_WORKER;


        if (enchantName.equalsIgnoreCase("LURE"))
            return Enchantment.LURE;

        if (enchantName.equalsIgnoreCase("LUCK"))
            return Enchantment.LUCK;


        if (enchantName.equalsIgnoreCase("SHARPNESS"))
            return Enchantment.DAMAGE_ALL;

        if (enchantName.equalsIgnoreCase("ARTHROPODS"))
            return Enchantment.DAMAGE_ARTHROPODS;

        if (enchantName.equalsIgnoreCase("SMITE"))
            return Enchantment.DAMAGE_UNDEAD;

        if (enchantName.equalsIgnoreCase("KNOCKBACK"))
            return Enchantment.KNOCKBACK;

        if (enchantName.equalsIgnoreCase("FIREASPECT"))
            return Enchantment.FIRE_ASPECT;

        if (enchantName.equalsIgnoreCase("LOOTING"))
            return Enchantment.LOOT_BONUS_MOBS;


        if (enchantName.equalsIgnoreCase("EFFICIENCY"))
            return Enchantment.DIG_SPEED;

        if (enchantName.equalsIgnoreCase("SILKTOUCH"))
            return Enchantment.SILK_TOUCH;

        if (enchantName.equalsIgnoreCase("UNBREAKING"))
            return Enchantment.DURABILITY;

        if (enchantName.equalsIgnoreCase("FORTUNE"))
            return Enchantment.LOOT_BONUS_BLOCKS;


        if (enchantName.equalsIgnoreCase("POWER"))
            return Enchantment.ARROW_DAMAGE;

        if (enchantName.equalsIgnoreCase("INFINITY"))
            return Enchantment.ARROW_INFINITE;

        if (enchantName.equalsIgnoreCase("FIRE"))
            return Enchantment.ARROW_FIRE;

        if (enchantName.equalsIgnoreCase("PUNCH"))
            return Enchantment.ARROW_KNOCKBACK;


        return null;
    }

    public static void noAI(Entity entity) {
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        nmsEntity.c(tag);
        tag.setInt("NoAI", 1);
        nmsEntity.f(tag);
    }

    public static Set<Entity> getEntitiesInChunks(Location location, int chunkRadius) {
        Block block = location.getBlock();

        Set<Entity> entities = new HashSet<>();
        for (int x = -16 * chunkRadius; x <= 16 * chunkRadius; x += 16) {
            for (int z = -16 * chunkRadius; z <= 16 * chunkRadius; z += 16) {
                for (Entity entity : block.getRelative(x, 0, z).getChunk().getEntities()) {
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    public static void updateTitle(Player player, String title) {
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        PacketPlayOutOpenWindow packet = new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId,
                "minecraft:chest", new ChatMessage(title), player.getOpenInventory().getTopInventory().getSize());

        entityPlayer.playerConnection.sendPacket(packet);
        entityPlayer.updateInventory(entityPlayer.activeContainer);
    }
}