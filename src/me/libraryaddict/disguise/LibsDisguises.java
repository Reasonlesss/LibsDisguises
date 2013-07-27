package me.libraryaddict.disguise;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import me.libraryaddict.disguise.Commands.*;
import me.libraryaddict.disguise.DisguiseTypes.Disguise;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseSound;
import me.libraryaddict.disguise.DisguiseTypes.DisguiseType;
import me.libraryaddict.disguise.DisguiseTypes.FlagWatcher;
import me.libraryaddict.disguise.DisguiseTypes.PlayerDisguise;
import me.libraryaddict.disguise.DisguiseTypes.Values;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.AgeableWatcher;
import me.libraryaddict.disguise.DisguiseTypes.Watchers.LivingWatcher;
import net.minecraft.server.v1_6_R2.AttributeSnapshot;
import net.minecraft.server.v1_6_R2.ChatMessage;
import net.minecraft.server.v1_6_R2.ChunkCoordinates;
import net.minecraft.server.v1_6_R2.EntityHuman;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.minecraft.server.v1_6_R2.GenericAttributes;
import net.minecraft.server.v1_6_R2.WatchableObject;
import net.minecraft.server.v1_6_R2.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

public class LibsDisguises extends JavaPlugin {
    private class DisguiseHuman extends EntityHuman {

        public DisguiseHuman(World world) {
            super(world, "LibsDisguises");
        }

        public boolean a(int arg0, String arg1) {
            return false;
        }

        public ChunkCoordinates b() {
            return null;
        }

        public void sendMessage(ChatMessage arg0) {
        }

    }

    private void addPacketListeners() {
        final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new PacketAdapter(this, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST,
                Packets.Server.NAMED_ENTITY_SPAWN, Packets.Server.ENTITY_METADATA, Packets.Server.ARM_ANIMATION,
                Packets.Server.REL_ENTITY_MOVE_LOOK, Packets.Server.ENTITY_LOOK, Packets.Server.ENTITY_TELEPORT,
                Packets.Server.ADD_EXP_ORB, Packets.Server.VEHICLE_SPAWN, Packets.Server.MOB_SPAWN,
                Packets.Server.ENTITY_PAINTING, Packets.Server.COLLECT, 44) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    final Player observer = event.getPlayer();
                    StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
                    org.bukkit.entity.Entity entity = entityModifer.read((Packets.Server.COLLECT == event.getPacketID() ? 1 : 0));
                    if (entity == observer)
                        return;
                    if (DisguiseAPI.isDisguised(entity)) {
                        Disguise disguise = DisguiseAPI.getDisguise(entity);
                        if (event.getPacketID() == 44) {
                            if (disguise.getType().isMisc() && entity.getType().isAlive()) {
                                event.setCancelled(true);
                            } else {
                                HashMap<String, Double> values = Values.getAttributesValues(disguise.getType());
                                Iterator<AttributeSnapshot> itel = ((List<AttributeSnapshot>) event.getPacket().getModifier()
                                        .read(1)).iterator();
                                event.setPacket(new PacketContainer(event.getPacketID()));
                                Collection collection = new ArrayList<AttributeSnapshot>();
                                while (itel.hasNext()) {
                                    AttributeSnapshot att = itel.next();
                                    if (values.containsKey(att.a())) {
                                        collection.add(new AttributeSnapshot(null, att.a(), values.get(att.a()), att.c()));
                                    }
                                }
                                StructureModifier<Object> mods = event.getPacket().getModifier();
                                mods.write(0, entity.getEntityId());
                                mods.write(1, collection);
                            }
                        } else if (event.getPacketID() == Packets.Server.ENTITY_METADATA) {
                            StructureModifier<Object> mods = event.getPacket().getModifier();
                            event.setPacket(new PacketContainer(event.getPacketID()));
                            StructureModifier<Object> newMods = event.getPacket().getModifier();
                            newMods.write(0, mods.read(0));
                            newMods.write(1, disguise.getWatcher().convert((List<WatchableObject>) mods.read(1)));
                        } else if (event.getPacketID() == Packets.Server.NAMED_ENTITY_SPAWN) {
                            if (disguise.getType().isPlayer()) {
                                StructureModifier<Object> mods = event.getPacket().getModifier();
                                String name = (String) mods.read(1);
                                if (!name.equals(((PlayerDisguise) disguise).getName())) {
                                    final PacketContainer[] packets = disguise.constructPacket(entity);
                                    event.setPacket(packets[0]);
                                    if (packets.length > 1) {
                                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                            public void run() {
                                                try {
                                                    manager.sendServerPacket(observer, packets[1]);
                                                } catch (InvocationTargetException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            } else {
                                final PacketContainer[] packets = disguise.constructPacket(entity);
                                event.setPacket(packets[0]);
                                if (packets.length > 1) {
                                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        public void run() {
                                            try {
                                                manager.sendServerPacket(observer, packets[1]);
                                            } catch (InvocationTargetException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                        } else if (event.getPacketID() == Packets.Server.MOB_SPAWN
                                || event.getPacketID() == Packets.Server.ADD_EXP_ORB
                                || event.getPacketID() == Packets.Server.VEHICLE_SPAWN
                                || event.getPacketID() == Packets.Server.ENTITY_PAINTING) {
                            final PacketContainer[] packets = disguise.constructPacket(entity);
                            event.setPacket(packets[0]);
                            if (packets.length > 1) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                    public void run() {
                                        try {
                                            manager.sendServerPacket(observer, packets[1]);
                                        } catch (InvocationTargetException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        } else if (event.getPacketID() == Packets.Server.ARM_ANIMATION
                                || event.getPacketID() == Packets.Server.COLLECT) {
                            if (disguise.getType().isMisc()) {
                                event.setCancelled(true);
                            }
                        } else if (Packets.Server.REL_ENTITY_MOVE_LOOK == event.getPacketID()
                                || Packets.Server.ENTITY_LOOK == event.getPacketID()
                                || Packets.Server.ENTITY_TELEPORT == event.getPacketID()) {
                            event.setPacket(event.getPacket().deepClone());
                            StructureModifier<Object> mods = event.getPacket().getModifier();
                            if (disguise.getType() == DisguiseType.ENDER_DRAGON) {
                                byte value = (Byte) mods.read(4);
                                mods.write(4, (byte) (value - 128));
                            } else if (disguise.getType().isMisc()) {
                                byte value = (Byte) mods.read(4);
                                if (disguise.getType() == DisguiseType.ITEM_FRAME || disguise.getType() == DisguiseType.ARROW) {
                                    mods.write(4, (byte) -value);
                                } else if (disguise.getType() == DisguiseType.PAINTING) {
                                    mods.write(4, (byte) -(value + 128));
                                } else if (disguise.getType().isMisc())
                                    mods.write(4, (byte) (value - 64));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        manager.addPacketListener(new PacketAdapter(this, ConnectionSide.CLIENT_SIDE, ListenerPriority.NORMAL,
                Packets.Client.USE_ENTITY) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                try {
                    Player observer = event.getPlayer();
                    StructureModifier<Entity> entityModifer = event.getPacket().getEntityModifier(observer.getWorld());
                    org.bukkit.entity.Entity entity = entityModifer.read(1);
                    if (DisguiseAPI.isDisguised(entity)
                            && (entity instanceof ExperienceOrb || entity instanceof Item || entity instanceof Arrow)) {
                        event.setCancelled(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            System.out
                    .print("[LibsDisguises] WARNING! WARNING! LibsDisguises couldn't find ProtocolLib! This plugin depends on it to run!");
            System.out
                    .print("[LibsDisguises] WARNING! WARNING! LibsDisguises couldn't find ProtocolLib! LibsDisguises is now shutting down!");
            getPluginLoader().disablePlugin(this);
            return;
        }
        DisguiseAPI.init(this);
        DisguiseAPI.enableSounds(true);
        DisguiseAPI.setVelocitySent(true);
        addPacketListeners();
        saveDefaultConfig();
        if (!getConfig().contains("DisguiseRadiusMax"))
            getConfig().set("DisguiseRadiusMax", getConfig().getInt("DisguiseRadiusMax"));
        if (!getConfig().contains("UndisguiseRadiusMax"))
            getConfig().set("UndisguiseRadiusMax", getConfig().getInt("UndisguiseRadiusMax"));
        DisguiseListener listener = new DisguiseListener(this);
        Bukkit.getPluginManager().registerEvents(listener, this);
        getCommand("disguise").setExecutor(new DisguiseCommand());
        getCommand("undisguise").setExecutor(new UndisguiseCommand());
        getCommand("disguiseplayer").setExecutor(new DisguisePlayerCommand());
        getCommand("undisguiseplayer").setExecutor(new UndisguisePlayerCommand());
        getCommand("undisguiseentity").setExecutor(new UndisguiseEntityCommand(listener));
        getCommand("disguiseentity").setExecutor(new DisguiseEntityCommand(listener));
        getCommand("disguiseradius").setExecutor(new DisguiseRadiusCommand(getConfig().getInt("DisguiseRadiusMax")));
        getCommand("undisguiseradius").setExecutor(new UndisguiseRadiusCommand(getConfig().getInt("UndisguiseRadiusMax")));
        registerValues();
    }

    private void registerValues() {
        World world = ((CraftWorld) Bukkit.getWorlds().get(0)).getHandle();
        for (DisguiseType disguiseType : DisguiseType.values()) {
            Class watcherClass = null;
            try {
                String name;
                switch (disguiseType) {
                case MINECART_FURNACE:
                case MINECART_HOPPER:
                case MINECART_MOB_SPAWNER:
                case MINECART_TNT:
                case MINECART_CHEST:
                    name = "Minecart";
                    break;
                case DONKEY:
                case MULE:
                case UNDEAD_HORSE:
                case SKELETON_HORSE:
                    name = "Horse";
                    break;
                case ZOMBIE_VILLAGER:
                case PIG_ZOMBIE:
                    name = "Zombie";
                    break;
                case MAGMA_CUBE:
                    name = "Slime";
                default:
                    name = toReadable(disguiseType.name());
                    break;
                }
                watcherClass = Class.forName("me.libraryaddict.disguise.DisguiseTypes.Watchers." + name + "Watcher");
            } catch (Exception ex) {
                // There is no watcher for this entity, or a error was thrown.
                try {
                    Class c = disguiseType.getEntityType().getEntityClass();
                    if (c.isAssignableFrom(Ageable.class))
                        watcherClass = AgeableWatcher.class;
                    else if (c.isAssignableFrom(LivingEntity.class))
                        watcherClass = LivingWatcher.class;
                    else
                        watcherClass = FlagWatcher.class;
                } catch (Exception ex1) {
                    ex1.printStackTrace();
                }
            }
            disguiseType.setWatcherClass(watcherClass);
            String name = toReadable(disguiseType.name());
            boolean dontDo = false;
            switch (disguiseType) {
            case WITHER_SKELETON:
            case ZOMBIE_VILLAGER:
            case DONKEY:
            case MULE:
            case UNDEAD_HORSE:
            case SKELETON_HORSE:
                dontDo = true;
                break;
            case PRIMED_TNT:
                name = "TNTPrimed";
                break;
            case MINECART_TNT:
                name = "MinecartTNT";
                break;
            case MINECART:
                name = "MinecartRideable";
                break;
            case FIREWORK:
                name = "Fireworks";
                break;
            case SPLASH_POTION:
                name = "Potion";
                break;
            case GIANT:
                name = "GiantZombie";
                break;
            case DROPPED_ITEM:
                name = "Item";
                break;
            case FIREBALL:
                name = "LargeFireball";
                break;
            default:
                break;
            }
            if (dontDo)
                continue;
            try {
                net.minecraft.server.v1_6_R2.Entity entity = null;
                Class entityClass;
                if (disguiseType == DisguiseType.PLAYER) {
                    entityClass = EntityHuman.class;
                    entity = new DisguiseHuman(world);
                } else {
                    entityClass = Class.forName("net.minecraft.server.v1_6_R2.Entity" + name);
                    entity = (net.minecraft.server.v1_6_R2.Entity) entityClass.getConstructor(World.class).newInstance(world);
                }
                Values value = new Values(disguiseType, entityClass);
                List<WatchableObject> watchers = entity.getDataWatcher().c();
                for (WatchableObject watch : watchers)
                    value.setMetaValue(watch.a(), watch.b());
                if (entity instanceof EntityLiving) {
                    EntityLiving livingEntity = (EntityLiving) entity;
                    value.setAttributesValue(GenericAttributes.d.a(), livingEntity.getAttributeInstance(GenericAttributes.d)
                            .getValue());
                }
                DisguiseSound sound = DisguiseSound.getType(disguiseType.name());
                if (sound != null) {
                    Method soundStrength = EntityLiving.class.getDeclaredMethod("aZ");
                    soundStrength.setAccessible(true);
                    sound.setDamageSoundVolume((Float) soundStrength.invoke(entity));
                }
            } catch (Exception e1) {
                System.out.print("[LibsDisguises] Trouble while making values for " + name + ": " + e1.getMessage());
                System.out.print("[LibsDisguises] Please report this to LibsDisguises author");
                e1.printStackTrace();
            }
        }
    }

    private String toReadable(String string) {
        StringBuilder builder = new StringBuilder();
        for (String s : string.split("_")) {
            builder.append(s.substring(0, 1) + s.substring(1).toLowerCase());
        }
        return builder.toString();
    }
}