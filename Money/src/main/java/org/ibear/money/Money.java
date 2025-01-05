package org.ibear.money;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandMap;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Money extends JavaPlugin {

    private Map<String, Double> playerMoney = new HashMap<>();

    @Override
    public void onEnable() {
        // ลงทะเบียนคำสั่งผ่าน CommandMap
        registerCommand("mymoney", new MyMoneyCommand());
        registerCommand("money", new MoneyCommand());

        // ตั้งค่า Scoreboard
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("money", "dummy", ChatColor.GREEN + "Money");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player, scoreboard);
        }

        getLogger().info("MoneyPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("MoneyPlugin has been disabled!");
    }

    private void registerCommand(String name, Command command) {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
            commandMap.register(name, command);
        } catch (Exception e) {
            getLogger().severe("Failed to register command: " + name);
            e.printStackTrace();
        }
    }

    // คำสั่ง /mymoney
    public class MyMoneyCommand extends BukkitCommand {

        public MyMoneyCommand() {
            super("mymoney");
            setDescription("Check your money");
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can check their money.");
                return true;
            }

            Player player = (Player) sender;
            double money = getMoney(player);
            player.sendMessage(ChatColor.GREEN + "Your money: $" + money);
            return true;
        }
    }

    // คำสั่ง /money set|add|del
    public class MoneyCommand extends BukkitCommand {

        public MoneyCommand() {
            super("money");
            setDescription("Manage player's money (set, add, del)");
            setUsage("/money set|add|del <player> <amount>");
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.isOp()) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return true;
            }

            if (args.length < 2) {
                return false;
            }

            String targetPlayerName = args[1];

            switch (args[0].toLowerCase()) {
                case "set":
                    if (args.length < 3) return false;
                    double setAmount = Double.parseDouble(args[2]);
                    setMoney(targetPlayerName, setAmount);
                    player.sendMessage(ChatColor.GREEN + "Set " + targetPlayerName + "'s money to $" + setAmount);
                    break;
                case "add":
                    if (args.length < 3) return false;
                    double addAmount = Double.parseDouble(args[2]);
                    addMoney(targetPlayerName, addAmount);
                    player.sendMessage(ChatColor.GREEN + "Added $" + addAmount + " to " + targetPlayerName + "'s money");
                    break;
                case "del":
                    if (args.length < 3) return false;
                    double delAmount = Double.parseDouble(args[2]);
                    deleteMoney(targetPlayerName, delAmount);
                    player.sendMessage(ChatColor.GREEN + "Deleted $" + delAmount + " from " + targetPlayerName + "'s money");
                    break;
                default:
                    return false;
            }
            return true;
        }
    }

    private double getMoney(Player player) {
        return playerMoney.getOrDefault(player.getName(), 0.0);
    }

    private void setMoney(String playerName, double amount) {
        playerMoney.put(playerName, amount);
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            updateScoreboard(player, Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }

    private void addMoney(String playerName, double amount) {
        double currentMoney = playerMoney.getOrDefault(playerName, 0.0);
        setMoney(playerName, currentMoney + amount);
    }

    private void deleteMoney(String playerName, double amount) {
        double currentMoney = playerMoney.getOrDefault(playerName, 0.0);
        setMoney(playerName, currentMoney - amount);
    }

    private void updateScoreboard(Player player, Scoreboard scoreboard) {
        Objective objective = scoreboard.getObjective("money");
        if (objective == null) return;

        double money = getMoney(player);
        Score score = objective.getScore(ChatColor.GREEN + "Money: $" + money);
        score.setScore(1);  // Set score to 1 (to display on the scoreboard)
        player.setScoreboard(scoreboard);
    }
}
