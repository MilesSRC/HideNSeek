package net.fnafmc.hideandseek.utils;

import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import net.ess3.api.MaxMoneyException;
import net.fnafmc.hideandseek.Main;
import net.fnafmc.hideandseek.listeners.EventListener;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import com.earth2me.essentials.api.Economy;

import java.math.BigDecimal;
import java.util.*;

public class EventDataManager {
    // Meta Data
    private boolean eventActive;
    private boolean eventSpawned;
    private static int minutesToStart;
    private static int secondsToStart;
    private List<Player> joinedPlayers;
    private Player host;

    // Game data
    private Player seeker;
    private Player riggedSeeker;

    public void setRiggedSeeker(Player riggedSeeker) {
        this.riggedSeeker = riggedSeeker;
    }

    private List<Player> hiders;

    private Scoreboard scoreboard;
    private Team team;

    private boolean seekerReleased;
    private boolean hidersReleased;

    private static int minutesToEnd;
    private BukkitRunnable minuteRunnable;
    private static int secondsToEnd;
    private BukkitRunnable secondRunnable;

    // Obj Data
    private Timer countdown;
    private Timer secondsCountdown;
    private Server server;
    private final EventItemManager itemManager;
    private final EventListener listener;

    // Constructor
    public EventDataManager(){
        itemManager = new EventItemManager(this);
        listener = new net.fnafmc.hideandseek.listeners.EventListener(this);
    }

    /*
     * Event Main Functions
     */
    public void clearTimers(){
        if(countdown != null){
            countdown.cancel();
            countdown.purge();
        }

        if(secondsCountdown != null){
            secondsCountdown.cancel();
            secondsCountdown.purge();
        }

       if(minuteRunnable != null){
           minuteRunnable.cancel();
       }

       if(secondRunnable != null){
           secondRunnable.cancel();
       }
    }

    public void gameEnd(boolean seekerWon, boolean forceful){
        listener.onClean();

        for (Player player : joinedPlayers) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }

            player.setPlayerListName(player.getName());
        }

        if(seekerWon){
            System.out.println("Event's is cleaning up...");

            // Restore defaults
            eventSpawned = false;
            eventActive = false;

            joinedPlayers.forEach(player -> {
                // Clean up players
                player.performCommand("spawn");
                player.setFlySpeed(0.2f);
                player.setWalkSpeed(0.2f);

                for (String entry : team.getEntries()) {
                    team.removeEntry(entry);
                }

                if(player != seeker){
                    try {
                        Economy.add(player.getUniqueId(), BigDecimal.valueOf(100));
                    } catch (NoLoanPermittedException | UserDoesNotExistException | MaxMoneyException e) {
                        e.printStackTrace();
                    }

                    player.sendTitle(ChatColor.GREEN + "Seeker won!", seeker.getName() + " won the hide and seek event!", 20, 120, 20);
                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Thanks for participating. You've been automagically been granted $100 for participation!");
                }
            });

            // Clean joined players
            joinedPlayers = new ArrayList<>();
            hiders = new ArrayList<>();

            // Seeker payout
            try {
                Economy.add(seeker.getUniqueId(), BigDecimal.valueOf(500));
            } catch(NoLoanPermittedException | UserDoesNotExistException | MaxMoneyException e){
                e.printStackTrace();
            }

            seeker.playSound(seeker.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 100, 1);
            seeker.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "You won!", "You've been paid " + ChatColor.GREEN + "$500" + ChatColor.RESET + " for winning.", 20, 120, 20);

            // Clean host
            try {
                Economy.add(host.getUniqueId(), BigDecimal.valueOf(500));
            } catch(NoLoanPermittedException | UserDoesNotExistException | MaxMoneyException e){
                e.printStackTrace();
            }

            host.setInvisible(false);
            host.setInvulnerable(false);
            host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Event complete. " + seeker.getName() + " won the game.");
            host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " You've been awarded for hosting this event. +$500");
            host.performCommand("spawn");

            // Clean seeker and host
            seeker = null;
            host = null;
            seekerReleased = false;
            hidersReleased = false;
        } else if(forceful){
            System.out.println("Event's is cleaning up...");

            // Restore defaults
            eventSpawned = false;
            eventActive = false;

            joinedPlayers.forEach(player -> {
                // Clean up players
                player.performCommand("spawn");
                player.setFlySpeed(0.2f);
                player.setWalkSpeed(0.2f);

                for (String entry : team.getEntries()) {
                    team.removeEntry(entry);
                }

                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }

                player.sendTitle(ChatColor.RED + "Event ended.", "Event forcefully stopped by Host.", 20, 120, 20);
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + " Event was forcefully stopped by the host.");
            });

            // Clean joined players
            joinedPlayers = new ArrayList<>();
            hiders = new ArrayList<>();

            // Clean host
            host.setInvisible(false);
            host.setInvulnerable(false);
            host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + " Event forcefully stopped.");
            host.performCommand("spawn");

            // Clean seeker and host
            seeker = null;
            host = null;
            seekerReleased = false;
            hidersReleased = false;
        } else {
            System.out.println("Event's is cleaning up...");

            // Restore defaults
            eventSpawned = false;
            eventActive = false;

            joinedPlayers.forEach(player -> {
                // Clean up players
                player.performCommand("spawn");
                player.setFlySpeed(0.2f);
                player.setWalkSpeed(0.2f);

                for (String entry : team.getEntries()) {
                    team.removeEntry(entry);
                }

                if(player != seeker && isPlayerHider(player)){
                    try {
                        Economy.add(player.getUniqueId(), BigDecimal.valueOf(250));
                    } catch (NoLoanPermittedException | UserDoesNotExistException | MaxMoneyException e) {
                        e.printStackTrace();
                    }

                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Thanks for participating. You've been automagically been granted $250 for surviving!");
                } else {
                    try {
                        Economy.add(player.getUniqueId(), BigDecimal.valueOf(100));
                    } catch (NoLoanPermittedException | UserDoesNotExistException | MaxMoneyException e) {
                        e.printStackTrace();
                    }

                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Thanks for participating. You've been automagically been granted $100 for participating!");
                }

                player.sendTitle(ChatColor.GREEN + "Hiders won!", hiders.size() + " player(s) won the hide and seek event!", 20, 120, 20);
            });

            // Clean joined players
            joinedPlayers = new ArrayList<>();
            hiders = new ArrayList<>();

            // Clean host
            try {
                Economy.add(host.getUniqueId(), BigDecimal.valueOf(500));
            } catch(NoLoanPermittedException | UserDoesNotExistException | MaxMoneyException e){
                e.printStackTrace();
            }

            host.setInvisible(false);
            host.setInvulnerable(false);
            host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Event complete. " + seeker.getName() + " won the game.");
            host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " You've been awarded for hosting this event. +$500");
            host.performCommand("spawn");

            // Clean seeker and host
            seeker = null;
            host = null;
            seekerReleased = false;
            hidersReleased = false;
        }

        // Backups
        eventSpawned = false;
        eventActive = false;

        if(minuteRunnable != null)
            minuteRunnable.cancel();
        minuteRunnable = null;

        if(secondRunnable != null)
            secondRunnable.cancel();
        secondRunnable = null;
    }

    public void spawnEvent(){
        listener.onSpawn();

        // Nametag disable
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        team = scoreboard.registerNewTeam("holidayevent");
        team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);

        joinedPlayers.forEach(player -> {
            // Freeze Players
            player.setWalkSpeed(0);
            player.setFlySpeed(0);

            // Set
            player.setPlayerListName(ChatColor.GRAY + "[" + ChatColor.AQUA + "In Event" + ChatColor.GRAY + "]" + ChatColor.RESET + " " + player.getName());

            // Blind
            PotionEffect effect = new PotionEffect(PotionEffectType.BLINDNESS, 20 * 180, 100);
            player.addPotionEffect(effect);

            // Title
            player.sendTitle(ChatColor.GREEN + "Starting event...", "", 20, 120, 20);

            // Add players
            team.addEntry(player.getName());
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                // Add host to nametag ignore list (Even though its probably irrelevant)
                team.addEntry(host.getName());

                // Send host elsewhere
                if(!isPlayerInEvent(host)){
                    Location spawn = new Location(server.getWorld("holidayevent"), -76, 110, -107); // Spawn
                    host.teleport(spawn);
                    host.setInvisible(true);
                    host.setInvulnerable(true);
                    host.setGameMode(GameMode.CREATIVE);
                }

                // Find seekers and hiders
                int playerCount = joinedPlayers.size();

                if(riggedSeeker == null){
                    // Pick seeker
                    int seekerNum = getRandomNumber(0, playerCount - 1);
                    // Get player
                    seeker = joinedPlayers.get(seekerNum);
                } else {
                    joinedPlayers.forEach(player -> {
                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.RED + " " + ChatColor.BOLD + "WARNING: " + ChatColor.AQUA + "Seeker was rigged by the host.");
                    });
                    seeker = riggedSeeker;
                    riggedSeeker = null;
                }

                // Assign the rest as hiders.
                joinedPlayers.forEach(player -> { if(player != seeker){
                    hiders.add(player);
                } });

                // Seeker Spawn
                Location seekerspawn = new Location(server.getWorld("holidayevent"), -76, 68, -124);
                seeker.teleport(seekerspawn);

                // Hiders Spawn
                Location hiderspawn = new Location(server.getWorld("holidayevent"), -76, 68, -91);
                hiders.forEach(player -> player.teleport(hiderspawn));

                // Spawn event
                eventSpawned = true;

                // Notify host and queue the games.
                host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Games have been decided.");
                host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " " + playerCount + " player's have joined.");
                host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " " + seeker.getName() + " has been choosen as Seeker.");

                // Notify all players of the games starting in 10 seconds.
                joinedPlayers.forEach(player -> {
                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " The game will begin in 10 seconds.");
                });

                // Queue Game
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        hidersReleased = true;

                        hiders.forEach(player -> {
                            for (PotionEffect effect : player.getActivePotionEffects()) {
                                player.removePotionEffect(effect.getType());
                            }

                            player.setWalkSpeed(0.2f);
                            player.setFlySpeed(0); // Prevent players from trying to fly x 1
                            player.setAllowFlight(false); // Prevent players from trying to fly x 2
                            player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "Hider", "Hide while you can... (1 minute)", 20, 120, 20);
                        });

                        seeker.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "Seeker", "Waiting for players to hide (1 minute)...", 20, 200, 20);
                        seeker.setAllowFlight(false); // Prevent players from trying to fly x 2

                        itemManager.onEventSpawn();
                    }
                }.runTaskLater(Main.getPlugin(), 20 * 10);

                // Seeker release
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        seeker.getActivePotionEffects().forEach(Effect -> {
                            seeker.removePotionEffect(Effect.getType());
                        });

                        seeker.setWalkSpeed(0.4f);
                        seeker.setFlySpeed(0); // Prevent players from trying to fly x 1
                        seeker.setAllowFlight(false); // Prevent players from trying to fly x 2

                        joinedPlayers.forEach(player -> {
                            player.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "Let the hunt begin!", "The seeker has been released.", 20, 120, 20);
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 100, 1);
                        });

                        if(!isPlayerInEvent(host)){
                            host.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "Let the hunt begin!", "The seeker has been released.", 20, 120, 20);
                            host.playSound(host.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 100, 1);
                        }

                        seekerReleased = true;

                        // Start the count.
                        minutesToEnd = 15;
                        secondsToEnd = 30;

                        minuteRunnable = new BukkitRunnable(){
                            @Override
                            public void run(){
                                if(!eventSpawned && !eventActive){
                                    this.cancel();
                                    return;
                                }

                                minutesToEnd -= 1;

                                perMinute(minutesToEnd);

                                if(minutesToEnd == 1){
                                    // Finish up
                                    minuteRunnable.cancel();
                                }
                            }
                        };
                        minuteRunnable.runTaskTimer(Main.getPlugin(), 0, 20 * 60);
                    }
                }.runTaskLater(Main.getPlugin(), 20 * 120);
            }
        }.runTask(Main.getPlugin());
    }

    public void startEvent(Player hostPlayer){
        server = Main.getPlugin().getServer();
        host = hostPlayer;

        server.getOnlinePlayers().forEach(player -> {
            player.sendTitle(ChatColor.AQUA + "Event Starting in " + ChatColor.GREEN + "5 minutes.", ChatColor.GRAY + "Event hosted by " + ChatColor.GREEN + host.getDisplayName(),20, 120, 20);
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Hide and Seek event starting in 5 minutes!");
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Do " + ChatColor.BLUE + "'/event join'" + ChatColor.AQUA + " to join the event!");
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, SoundCategory.MASTER, 100, (float) 0.1);
        });

        // Start counting
        eventActive = true;
        eventSpawned = false;
        minutesToStart = 5;
        secondsToStart = 60;
        joinedPlayers = new ArrayList<>();
        hiders = new ArrayList<>();
        hidersReleased = false;

        // Timers
        countdown = new Timer();
        secondsCountdown = new Timer();

        // Do minute timer
        countdown.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                minutesToStart -= 1;
                if(minutesToStart == 1){
                    server.getOnlinePlayers().forEach(player -> {
                        player.sendTitle(ChatColor.AQUA + "Event Starting in " + ChatColor.GREEN + "" + minutesToStart + " minute(s).", ChatColor.GRAY + "To join, do " + ChatColor.GREEN + "/event join",20, 120, 20);
                        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Hide and Seek event starting in " + minutesToStart + " minute(s)!");
                    });
                    countdown.cancel();
                    countdown.purge();

                    secondsCountdown.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            secondsToStart -= 1;

                            if(secondsToStart < 11){
                                if(secondsToStart == 0){
                                    secondsCountdown.cancel();
                                    secondsCountdown.purge();
                                    spawnEvent();
                                    return;
                                }

                                server.getOnlinePlayers().forEach(player -> {
                                    player.sendTitle(ChatColor.AQUA + "Event Starting in " + ChatColor.GREEN + "" + secondsToStart + " second(s).", ChatColor.GRAY + "To join, do " + ChatColor.GREEN + "/event join",0, 25, 0);
                                });
                            }
                        }
                    }, 0, 1000);
                    return;
                }

                server.getOnlinePlayers().forEach(player -> {
                    player.sendTitle(ChatColor.AQUA + "Event Starting in " + ChatColor.GREEN + "" + minutesToStart + " minute(s).", ChatColor.GRAY + "To join, do " + ChatColor.GREEN + "/event join",20, 120, 20);
                    player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " Hide and Seek event starting in " + minutesToStart + " minute(s)!");
                });
            }
        }, 60000, 60000);
    }

    /*
       Getters and Setters
     */
    public boolean isEventActive(){
        return eventActive;
    }

    public void setEventActive(boolean a){
        eventActive = a;
    }

    public boolean isEventSpawned(){
        return eventSpawned;
    }

    public void setEventSpawned(boolean a){
        System.out.println("WARNING: Unstable/dangerous function call to setEventSpawned detected.");
        eventSpawned = a;
    }

    public boolean isSeekerReleased(){
        return seekerReleased;
    }

    public boolean areHidersReleased(){
        return hidersReleased;
    }

    public boolean isPlayerInEvent(Player player){
        if(!eventSpawned && !eventActive)
            return false;
        return joinedPlayers.contains(player);
    }

    public boolean isPlayerHider(Player player){
        return hiders.contains(player);
    }

    // Gets
    public Player getHost(){
        return host;
    }

    public Player getSeeker(){
        return seeker;
    }

    /*
     * Event Data Functions
     */
    public boolean hasPlayerJoined(Player player){
        return joinedPlayers.contains(player);
    }

    public void addPlayer(Player player){
        joinedPlayers.add(player);
        player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " You joined the event.");
        host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.BLUE + " " + player.getName() + ChatColor.AQUA + " joined the event.");
    }

    public void sendMessageToAllPlayers(String message){
        joinedPlayers.forEach(player -> {
            player.sendMessage(message);
        });
    }

    public void cleanPlayer(Player player, boolean safely){
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.2f);
        team.removeEntry(player.getName());

        if(!safely) player.setGameMode(GameMode.ADVENTURE);
    }

    public void removePlayer(Player player, boolean forceful){
        if(isPlayerInEvent(player) && player != seeker){
            hiders.remove(player);
            cleanPlayer(player, true);
            playerRemoved();
        }

        if(forceful){
            joinedPlayers.remove(player);
        }
    }

    public void playerRemoved(){
        if(hiders.size() == 0){
            gameEnd(true, false);
        }
    }

    public void respawnPlayers() {
        // Respawn all
        hiders.forEach(this::respawn);
        respawn(seeker);
        respawn(host);

        // Announce
        joinedPlayers.forEach(player -> {
            player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "]" + ChatColor.AQUA + " The host has respawned all players! " + ChatColor.DARK_RED + "RUN!");
        });
    }

    public void respawn(Player player){
        Location hostspawn = new Location(server.getWorld("holidayevent"), -76, 110, -107); // H Spawn
        Location seekerspawn = new Location(server.getWorld("holidayevent"), -76, 68, -124);
        Location hiderspawn = new Location(server.getWorld("holidayevent"), -76, 68, -91);

        if(isPlayerHider(player)){
            player.teleport(hiderspawn);
        } else if(seeker == player) {
            player.teleport(seekerspawn);
        } else if(host == player && !isPlayerInEvent(host)){
            player.teleport(hostspawn);
        }
    }

    /*
        Ticks
     */
    public void perMinute(int minutesLeft){ // Every minute after the seeker has been released for 15 minutes.
        if(minutesLeft != 1){
            joinedPlayers.forEach(player -> {
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 100, 1);
                player.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.GREEN + Integer.toString(minutesLeft) + ChatColor.AQUA + " minutes remain");
            });

            if(!isPlayerInEvent(host)){
                host.playSound(host.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 100, 1);
                host.sendMessage(ChatColor.GRAY + "[" + ChatColor.GREEN + "Events" + ChatColor.GRAY + "] " + ChatColor.GREEN + Integer.toString(minutesLeft) + ChatColor.AQUA + " minutes remain");
            }
        } else {
            // Start seconds countdown
            secondRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if(!eventSpawned && !eventActive){
                        this.cancel();
                        return;
                    }

                    if(secondsToEnd == 30){
                        joinedPlayers.forEach(player -> {
                            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 100, 1);
                            player.sendTitle(ChatColor.GREEN + Integer.toString(secondsToEnd) + ChatColor.AQUA + " seconds remain", "until hiders win.", 20, 120, 20);
                        });

                        if(!isPlayerInEvent(host)){
                            host.playSound(host.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_BREAK, 100, 1);
                            host.sendTitle(ChatColor.GREEN + Integer.toString(secondsToEnd) + ChatColor.AQUA + " seconds remain", "until hiders win.", 20, 120, 20);
                        }
                    }

                    secondsToEnd -= 1;

                    if(secondsToEnd == 0){
                        // END IT
                        gameEnd(false, false);
                        secondRunnable.cancel();
                        return;
                    }

                    if(secondsToEnd < 16){
                        joinedPlayers.forEach(player -> {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100, (float) secondsToEnd / 15);
                            player.sendTitle(ChatColor.GREEN + Integer.toString(secondsToEnd) + ChatColor.AQUA + " second(s)", "", 0, 21, 0);
                        });

                        if(!isPlayerInEvent(host)){
                            host.playSound(host.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100, (float) secondsToEnd / 15);
                            host.sendTitle(ChatColor.GREEN + Integer.toString(secondsToEnd) + ChatColor.AQUA + " second(s)", "", 0, 21, 0);
                        }
                    }
                }
            };

            secondRunnable.runTaskTimer(Main.getPlugin(), 0, 20);
        }
    }

    // Helpers
    private int getRandomNumber(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }


    public List<Player> getHiders() {
        return hiders;
    }

    public List<Player> getJoined() {
        return joinedPlayers;
    }
}
