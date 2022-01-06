package net.fnafmc.hideandseek.commands;

import net.fnafmc.hideandseek.Main;
import net.fnafmc.hideandseek.utils.EventDataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class EventCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Variables
        Player player = (Player) sender;
        EventDataManager edm = Main.getEventDataManager();

        // Regular Commands (No Perms)
        if(args.length == 0){
            player.sendMessage(ChatColor.GREEN + "Holiday Event Command by Dubby/MilesSRC/Miles#0003");
            player.sendMessage(ChatColor.RED + "To access commands: /event help");
        } else {
            // Regular Commands (No Perms)
            if(args[0].equals("join")){
                if(!edm.isEventActive()){
                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + " Theres no event running right now!");
                    return true;
                }

                if(edm.isEventSpawned()){
                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + " The event has already spawned!");
                    return true;
                }

                if(!edm.hasPlayerJoined(player)){
                    if(edm.getHost() == player){
                        edm.addPlayer(player);
                    } else {
                        edm.addPlayer(player);
                    }
                } else {
                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + " You've already joined the event. To leave, do /event leave");
                }
            } else if(args[0].equals("leave")){
                if(edm.hasPlayerJoined(player)){
                    edm.removePlayer(player, true);
                    edm.getHost().sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.BLUE + player.getName() + ChatColor.AQUA + " left the game.");
                }
            }

            // Var
            Server server = Main.getPlugin().getServer();

            // Admin Commands
            if(player.isOp() || player.hasPermission("fnafmc.events")){
                switch (args[0]) {
                    // Basic Debug/Creation/Control
                    case "create":
                        edm.startEvent(player);
                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Started a hide and seek event.");
                        break;
                    case "forcestart":
                        edm.clearTimers();
                        edm.spawnEvent();
                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Game forcefully started.");
                        break;
                    case "forceend":
                        edm.clearTimers();
                        edm.gameEnd(false, true);
                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Game forcefully ended.");
                        break;
                    case "cancel":
                        edm.setEventActive(false);
                        edm.clearTimers();
                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Countdown forcefully ended.");
                        break;

                    // Player Management
                    case "disqualify":
                        if(!Objects.equals(args[1], "")){
                            Player plr = server.getPlayer(args[1]);

                            if(plr == null){
                                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Couldn't find any player matching '" + args[1] + "'");
                                return true;
                            }

                            if(edm.isEventSpawned() && edm.isEventActive()){
                                if(edm.isPlayerInEvent(plr)){
                                    edm.removePlayer(plr, true);
                                    plr.performCommand("spawn");
                                    plr.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " You were kicked from the event by the host. (Host: " + edm.getHost().getDisplayName() + ChatColor.AQUA + ")");
                                    edm.sendMessageToAllPlayers(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.AQUA + plr.getName() + " was disqualified by the host!");
                                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.AQUA + "Player disqualified.");
                                } else {
                                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Player isnt in event!");
                                }
                            }

                        } else {
                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " You must provide a player to disqualify.");
                        }
                        break;

                    case "respawn":
                        if(Objects.equals(args[1], "")){
                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " You must provide /event respawn [all/seeker/hiders/playername]");
                        } else {
                            switch(args[1]){
                                case "all":
                                    edm.respawnPlayers();
                                    break;
                                case "seeker":
                                    edm.respawn(edm.getSeeker());
                                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Respawned seeker!");
                                    break;
                                case "hiders":
                                    edm.getHiders().forEach(edm::respawn);
                                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Respawned hiders!");
                                    break;
                                case "host":
                                    edm.respawn(edm.getHost());
                                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Respawned host!");
                                    break;
                                default:
                                    Player toRespawn = server.getPlayer(args[1]);

                                    if(toRespawn != null){
                                        edm.respawn(toRespawn);
                                    } else {
                                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Couldn't find a player to respawn!");
                                    }
                            }
                        }
                        break;

                    case "status":
                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Outputting event status! (DEBUG)");
                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Event Started: " + edm.isEventActive());
                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Event Active: " + edm.isEventActive());

                        if(edm.isEventActive()){
                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Event Host: " + edm.getHost().getName() + " is host.");
                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Event Players: " + edm.getJoined().size() + " players");
                        }

                        if(edm.isEventSpawned()){
                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Event Seeker: " + edm.getSeeker().getName() + " is seeker.");
                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Event Hiders: " + edm.getHiders().size() + " hiders");

                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Seeker Released: " + edm.isSeekerReleased());
                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Hiders Released: " + edm.areHidersReleased());
                        }
                        break;
                    case "rigseeker":
                        Player rigAs = server.getPlayer(args[1]);

                        if(rigAs != null){
                            if(player == edm.getHost()){
                                edm.setRiggedSeeker(rigAs);
                            } else {
                                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " ONLY THE HOST IS ALLOWED THIS COMMAND >:(");
                            }
                        } else {
                            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " WAAAH! RIGGED SEEKER NOT FOUND");
                        }
                }
            }
        }

        return true;
    }
}
