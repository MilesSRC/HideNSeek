package net.fnafmc.hideandseek.utils;

import net.fnafmc.hideandseek.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EventItemManager implements Listener {
    // Event Manager
    private final EventDataManager manager;

    // Items
    private final ItemStack playerFinder;
    private final ItemStack mysticMunchie;
    private boolean seekerItemCooldown;

    private final ItemStack nothingSus;
    private final ItemStack stickyWebs;

    public EventItemManager(EventDataManager eventDataManager){
        manager = eventDataManager;
        Main.getPlugin().getServer().getPluginManager().registerEvents(this, Main.getPlugin());

        // Create Items
        // Seeker Items
        playerFinder = new ItemStack(Material.HEART_OF_THE_SEA);
        mysticMunchie = new ItemStack(Material.PUFFERFISH);

        // Hider Items
        nothingSus = new ItemStack(Material.QUARTZ);
        stickyWebs = new ItemStack(Material.COBWEB);

        // Create Meta
        // Player Finder
        ItemMeta pfMeta = playerFinder.getItemMeta();
        pfMeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Player Finder");
        List<String> pfLore = new ArrayList<>();
        pfLore.add(ChatColor.LIGHT_PURPLE + "Reveals how many players are nearby");
        pfLore.add(ChatColor.LIGHT_PURPLE + "2 minute cooldown.");
        pfMeta.setLore(pfLore);
        pfMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        playerFinder.setItemMeta(pfMeta);

        // SEEKER - Mystic Munchie
        // Activates outlines for all players for 4 seconds.
        // 2 minute cooldown.
        ItemMeta mmMeta = mysticMunchie.getItemMeta();
        mmMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Mystic Munchie");
        List<String> mmLore = new ArrayList<>();
        mmLore.add(ChatColor.DARK_GREEN + "Outlines players for 4 seconds.");
        mmLore.add(ChatColor.DARK_GREEN + "3 minute cooldown.");
        mmMeta.setLore(mmLore);
        mmMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        mysticMunchie.setItemMeta(mmMeta);

        ItemMeta nsMeta = nothingSus.getItemMeta();
        nsMeta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Nothing Suspicious");
        List<String> nsLore = new ArrayList<>();
        nsLore.add(ChatColor.LIGHT_PURPLE + "Gain a speed boost for 5 seconds.");
        nsLore.add(ChatColor.LIGHT_PURPLE + "Single use.");
        nsMeta.setLore(nsLore);
        nsMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        nothingSus.setItemMeta(nsMeta);

        ItemMeta swMeta = stickyWebs.getItemMeta();
        swMeta.setDisplayName(ChatColor.GRAY + "" + ChatColor.BOLD + "Stick Webs");
        List<String> swLore = new ArrayList<>();
        swLore.add(ChatColor.GRAY + "Slow the seeker for 5 seconds.");
        swLore.add(ChatColor.GRAY + "Single use.");
        swMeta.setLore(swLore);
        swMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        stickyWebs.setItemMeta(swMeta);
    }

    //
    // Event on spawn
    //
    public void onEventSpawn(){
        Player seeker = manager.getSeeker();
        List<Player> joined = manager.getJoined();
        List<Player> hiders = manager.getHiders();

        joined.forEach(player -> {
            player.getInventory().clear();
            player.updateInventory();
        });

        int seekerItem = getRandomNumber(1,101);
        ItemStack cloned;
        if(seekerItem > 50){
            cloned = playerFinder.clone();
        } else {
            cloned = mysticMunchie.clone();
        }
        seeker.getInventory().setItem(0, cloned);
        seeker.updateInventory();

        // Seeker Bow And Arrow
        seeker.getInventory().addItem(new ItemStack(Material.BOW));
        ItemStack arrows = new ItemStack(Material.ARROW);
        arrows.setAmount(3);
        seeker.getInventory().addItem(arrows);

        // Pick random items for each player
        hiders.forEach(player -> {
            int random = getRandomNumber(1, 101);

            if(random > 75){
                // 25% chance
                if(getRandomNumber(1, 101) > 50){
                    ItemStack item = nothingSus.clone();
                    player.getInventory().setItem(0, item);
                    player.updateInventory();
                } else {
                    ItemStack item = stickyWebs.clone();
                    player.getInventory().setItem(0, item);
                    player.updateInventory();
                }
            }
        });
    }

    // Event
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player seeker = manager.getSeeker();
        List<Player> hiders = manager.getHiders();

        String pf = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Player Finder";
        String mm = ChatColor.GREEN + "" + ChatColor.BOLD + "Mystic Munchie";
        String ns = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Nothing Suspicious";
        String sw = ChatColor.GRAY + "" + ChatColor.BOLD + "Stick Webs";

        if(manager.isEventSpawned() && manager.isEventActive()){
            if(manager.isPlayerInEvent(event.getPlayer())){
                if(manager.isSeekerReleased()){
                    Player player = event.getPlayer();

                    if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR)
                        return;

                    if(event.getItem() == null)
                        return;

                    ItemStack item = event.getItem();
                    ItemMeta itemMeta = item.getItemMeta();

                    if(manager.isPlayerHider(player)){
                        if(itemMeta.getDisplayName().equals(ns)){
                            player.setWalkSpeed(.6f);
                            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 100, 1);
                            player.getInventory().setItemInMainHand(null);

                            new BukkitRunnable(){
                                @Override
                                public void run(){
                                    player.setWalkSpeed(.2f);
                                }
                            }.runTaskLater(Main.getPlugin(), 20 * 5);
                        } else if(itemMeta.getDisplayName().equals(sw)){
                            seeker.setWalkSpeed(.1f);
                            seeker.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.BLUE + player.getName() + " hit you with sticky webs!");
                            seeker.playSound(manager.getSeeker().getLocation(), Sound.ITEM_TOTEM_USE, 100, 1);
                            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 100, 1);
                            player.getInventory().setItemInMainHand(null);

                            new BukkitRunnable(){
                                @Override
                                public void run(){
                                    seeker.setWalkSpeed(.4f);
                                }
                            }.runTaskLater(Main.getPlugin(), 20 * 15);
                        }
                    } else {
                        if(seekerItemCooldown){
                            event.setCancelled(true);
                            seeker.playSound(seeker.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 100, 1);
                            seeker.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.BLUE + "Item is on cooldown!");
                            return;
                        }

                        if(itemMeta.getDisplayName().equals(pf)){
                            AtomicInteger nearby = new AtomicInteger();
                            hiders.forEach(hider -> {
                                if(hider.getLocation().distance(player.getLocation()) < 101){
                                    nearby.addAndGet(1);
                                    hider.sendTitle(ChatColor.DARK_RED + "" + ChatColor.BOLD + "YOU'VE BEEN DETECTED.", ChatColor.DARK_RED + "RUN.", 20, 120, 20);
                                    hider.playSound(hider.getLocation(), Sound.AMBIENT_CRIMSON_FOREST_ADDITIONS, SoundCategory.MASTER, 100, 0.2f);
                                }
                            });

                            seekerItemCooldown = true;
                            player.sendTitle(ChatColor.GREEN + Integer.toString(nearby.get()) + ChatColor.AQUA + " players nearby.", "Within 100 blocks.", 20, 120, 20);
                            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 100, 0.5f);

                            itemMeta.removeEnchant(Enchantment.DURABILITY);
                            item.setItemMeta(itemMeta);

                            event.setCancelled(true);

                            new BukkitRunnable(){
                                @Override
                                public void run(){
                                    seekerItemCooldown = false;
                                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                                    item.setItemMeta(itemMeta);
                                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 100, 1);
                                }
                            }.runTaskLater(Main.getPlugin(), 20 * 90);
                        } else if(itemMeta.getDisplayName().equals(mm)) {
                            hiders.forEach(hider -> {
                                hider.setGlowing(true);

                                if(player.getLocation().distance(hider.getLocation()) < 60){
                                    hider.sendTitle(ChatColor.DARK_RED + "" + ChatColor.BOLD + "YOU'VE BEEN DETECTED.", ChatColor.DARK_RED + "RUN.", 20, 120, 20);
                                    hider.playSound(hider.getLocation(), Sound.AMBIENT_CRIMSON_FOREST_ADDITIONS, SoundCategory.MASTER, 100, 0.2f);
                                }
                            });
                            player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 100, 0.5f);

                            itemMeta.removeEnchant(Enchantment.DURABILITY);
                            item.setItemMeta(itemMeta);

                            seekerItemCooldown = true;
                            new BukkitRunnable(){
                                @Override
                                public void run(){
                                    hiders.forEach(hider -> hider.setGlowing(false));
                                }
                            }.runTaskLater(Main.getPlugin(), 20 * 3);

                            new BukkitRunnable(){
                                @Override
                                public void run(){
                                    seekerItemCooldown = false;
                                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                                    item.setItemMeta(itemMeta);
                                    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 100, 1);
                                }
                            }.runTaskLater(Main.getPlugin(), 20 * 120);
                        }
                    }
                }
            }
        }
    }

    // Helpers
    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
}
