package net.fnafmc.hideandseek.listeners;

import net.fnafmc.hideandseek.Main;
import net.fnafmc.hideandseek.utils.EventDataManager;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class EventListener implements Listener {
    private final EventDataManager edm;
    private final Map<Player, Boolean> heartbeats;

    public EventListener(EventDataManager eventDataManager) {
        edm = eventDataManager;
        heartbeats = new HashMap<>();

        Main.getPlugin().getServer().getPluginManager().registerEvents(this, Main.getPlugin());
    }

    /*
     * Events
     */
    public void onSpawn(){
        edm.getJoined().forEach(hider -> heartbeats.put(hider, false));
    }

    public void onClean(){
        edm.getJoined().forEach(heartbeats::remove);
    }

    /*
     * Game restrictions
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event){ // Seeker move prevention
        if(edm.isEventActive() && edm.isEventActive()) {
            Player player = event.getPlayer();

            /*
             * Preventitive
             */
            if (!edm.isSeekerReleased() && edm.getSeeker() == event.getPlayer())
                event.setCancelled(true);

            if (!edm.areHidersReleased() && edm.isPlayerHider(event.getPlayer()))
                event.setCancelled(true);

            /*
             * Seeker and Hider stings/range finders/etc
             */
            if(edm.isPlayerInEvent(player)){
                edm.getHiders().forEach(player1 -> {
                    if(player1.getLocation().distance(edm.getSeeker().getLocation()) < 30){
                        if(heartbeats.get(player1) == null)
                            System.out.println("Bad object key.");

                        if(!heartbeats.get(player1)){
                            heartbeats.replace(player1, true);
                            player1.playSound(player1.getLocation(), Sound.AMBIENT_NETHER_WASTES_ADDITIONS, SoundCategory.MASTER, 100, 1);

                            PotionEffect newPotion = new PotionEffect(PotionEffectType.BLINDNESS, 1000, 1);
                            player1.addPotionEffect(newPotion);
                        }
                    } else {
                        if(heartbeats.get(player1)){
                            heartbeats.replace(player1, false);
                            player1.removePotionEffect(PotionEffectType.BLINDNESS);
                            player1.stopSound(Sound.AMBIENT_NETHER_WASTES_ADDITIONS);
                        }
                    }
                });
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event){
        if(edm.isEventActive() && edm.isEventActive()){
            if(event.getDamager().getType() == EntityType.PLAYER){
                Player damager = (Player) event.getDamager();

                if(damager == edm.getSeeker()){
                    if(event.getEntity().getType() == EntityType.PLAYER){
                        Player hit = (Player) event.getEntity();

                        if(edm.isPlayerHider(hit) && edm.isSeekerReleased()){
                            // Remove player
                            damager.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.GREEN + " You caught a wild " + hit.getName() + "!");
                            hit.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + " You have been caught! That's game over for you.");
                            edm.getHost().sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.BLUE + hit.getName() + ChatColor.AQUA + " has been caught!");

                            // Sound
                            damager.playSound(damager.getLocation(), Sound.ITEM_TOTEM_USE, 100, 1);
                            hit.playSound(damager.getLocation(), Sound.ITEM_TOTEM_USE, 100, 1);

                            // Gamemode switch
                            hit.setGameMode(GameMode.SPECTATOR);
                            hit.setFlySpeed(0.2f); // Allow flying for spectator
                            edm.removePlayer(hit, false);

                            // Announce
                            edm.sendMessageToAllPlayers(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.RED + hit.getName() + " was caught!");

                            // A
                            for (PotionEffect effect : hit.getActivePotionEffects()) {
                                hit.removePotionEffect(effect.getType());
                            }
                        }

                        if(edm.isPlayerHider(hit) || edm.getSeeker() == hit){
                            event.setCancelled(true);
                        }
                    }
                } else if(edm.isPlayerHider(damager)){
                    event.setCancelled(true);
                } else if(edm.getHost() == damager){
                    event.setCancelled(true);
                }
            }
        }
    }

    /*
     * Anticheats/Preventitive
     */
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event){
        if(edm.isEventActive() && edm.isEventSpawned()){
            if(edm.isPlayerInEvent(event.getPlayer())){
                Player player = event.getPlayer();
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + " You tried to change world! This is forbidden while an event is taking place with you in it! Please do /event leave before changing worlds. The event host has been notified. You'll be disqualified by a host if its seen as unsportsmanship-like behavior.");

                Player host = edm.getHost();
                host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + player.getName() + " tried changing worlds!");

                if(edm.getSeeker() != event.getPlayer()){
                    if(edm.isPlayerHider(player))
                        edm.removePlayer(player, true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTryToFly(PlayerToggleFlightEvent event) {
        if (edm.isEventActive() && edm.isEventSpawned()) {
            if (edm.isPlayerInEvent(event.getPlayer())) {
                Player player = event.getPlayer();
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + " You tried to fly! This is forbidden while an event is taking place with you in it! Please do /event leave before trying to fly. The event host has been notified. You'll be disqualified by a host if its seen as unsportsmanship-like behavior.");

                Player host = edm.getHost();
                host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + player.getName() + " tried flying!");

                if (edm.getSeeker() != player)
                    edm.removePlayer(player, true);
            }
        }
    }

    // Disqualifications
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        if(edm.isEventSpawned() && edm.isEventActive()){
            if(edm.isPlayerInEvent(event.getPlayer())){
                if(event.getPlayer() == edm.getSeeker()){
                    edm.removePlayer(event.getPlayer(), true);
                    edm.sendMessageToAllPlayers(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.RED + event.getPlayer().getName() + " left the game.");
                    edm.gameEnd(false, true); // Hiders win
                } else {
                    edm.removePlayer(event.getPlayer(), true);
                    edm.sendMessageToAllPlayers(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.RED + event.getPlayer().getName() + " left the game.");
                }
            }
        }
    }
}
