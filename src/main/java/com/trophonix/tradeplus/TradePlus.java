package com.trophonix.tradeplus;

import com.trophonix.tradeplus.commands.CommandHandler;
import com.trophonix.tradeplus.commands.TradeCommand;
import com.trophonix.tradeplus.commands.TradePlusCommand;
import com.trophonix.tradeplus.trade.InteractListener;
import com.trophonix.tradeplus.trade.Trade;
import com.trophonix.tradeplus.util.InvUtils;
import com.trophonix.tradeplus.util.MsgUtils;
import com.trophonix.tradeplus.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TradePlus extends JavaPlugin {
  public ConcurrentLinkedQueue<Trade> ongoingTrades;
  private File configFile;
  private FileConfiguration config;
  private File langFile;
  private FileConfiguration lang;
  private CommandHandler commandHandler;

  public Trade getTrade(Player player1, Player player2) {
    for (Trade trade : ongoingTrades) {
      if (trade.player1.equals(player1) && trade.player2.equals(player2))
        return trade;
    }
    return null;
  }

  @Override
  public File getFile() { return configFile; }

  @Override
  public FileConfiguration getConfig() { return this.config; }

  @Override
  public void saveConfig() {
    try {
      config.save(configFile);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  @Override
  public void onEnable() {
    Bukkit.getConsoleSender().sendMessage(new String[]{
            " ",
            ChatColor.RED + "YOU ARE USING A BETA VERSION OF TRADE+ MADE FOR SPIGOT VERSION 1.13",
            " ",
            ChatColor.RED + "This is unadvised, as Spigot 1.13 builds are not final, and any changes might break Trade+, resulting in loss of items.",
            ChatColor.RED + "If you experience any issues, please report them here:",
            ChatColor.WHITE + " > https://github.com/Trophonix/TradePlus/issues/",
            ChatColor.RED + "Make sure to mention you are using 1.13.",
            " "
    });
    configFile = new File(getDataFolder(), "config.yml");
    config = YamlConfiguration.loadConfiguration(configFile);
    langFile = new File(getDataFolder(), "lang.yml");
    lang = YamlConfiguration.loadConfiguration(langFile);
    MsgUtils.initMsgUtils();
    if (!getDataFolder().exists()) {
      getDataFolder().mkdirs();
      try {
        configFile.createNewFile();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      config.set("aliases", Collections.singletonList("trade+"));

      config.set("permissions.required", config.getBoolean("permissionrequired", false));
      config.set("permissions.send", config.getString("permissionnode", "tradeplus.send"));
      config.set("permissions.accept", "tradeplus.accept");

      config.set("requestcooldownseconds", 20);
      config.set("allow-trade-in-creative", false);

      config.set("blocked.blacklist", Arrays.asList("bedrock", "97:3"));
      config.set("blocked.named-items", false);
      config.set("blocked.lore", Collections.singletonList("EXAMPLE_BLOCKED_LORE"));
      config.set("blocked.regex", "");

      config.set("action", "crouchrightclick");

      config.set("ranges.sameworld", 10.0);
      config.set("ranges.crossworld", 0.0);
      config.set("ranges.allowcrossworld", false);
      config.set("ranges.blocked-worlds", Arrays.asList("ThisWorldDoesntExistButItsBlocked", "NeitherDoesThisOneButItIsToo"));

      config.set("antiscam.countdown", 10);
      config.set("antiscam.cancelonchange", true);
      config.set("antiscam.preventchangeonaccept", true);

      config.set("gui.title", "Your Items <|     |> Their Items");
      config.set("gui.spectator-title", "Player 1 <|          |> Player 2");
      config.set("gui.head", "&7You are trading with: &3&l%PLAYER%");
      config.set("gui.showhead", !config.contains("showhead") || config.getBoolean("showhead"));
      config.set("gui.accept", "&a&lClick to accept the trade");
      config.set("gui.cancel", "&c&lClick to cancel the trade");
      config.set("gui.showaccept", true);
      config.set("gui.theyaccept", " ");
      config.set("gui.theycancel", " ");
      config.set("gui.acceptid", "green_stained_glass_pane");
      config.set("gui.cancelid", "red_stained_glass_pane");
      config.set("gui.separatorid", "black_stained_glass_pane");
      config.set("gui.force.enabled", true);
      config.set("gui.force.type", "clock");
      config.set("gui.force.name", "&4&lForce Trade");
      config.set("gui.force.lore", Arrays.asList("&cClick here to force", "&cacceptance.", "", "&cThis shows only for admins."));

      config.set("extras.economy.enabled", true);
      config.set("extras.economy.material", "gold_ingot");
      config.set("extras.economy.display", "&7Your current money offer is &e%AMOUNT%");
      config.set("extras.economy.theirdisplay", "&7Their current money offer is &e%AMOUNT%");
      config.set("extras.economy.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
              "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
              "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
              "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
      config.set("extras.economy.increment", 10.0);
      config.set("extras.economy.taxpercent", 0);

      config.set("extras.experience.enabled", true);
      config.set("extras.experience.material", "experience_bottle");
      config.set("extras.experience.display", "&7Your current XP offer is &e%AMOUNT%");
      config.set("extras.experience.theirdisplay", "&7Their current XP offer is &e%AMOUNT%");
      config.set("extras.experience.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
              "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
              "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
              "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
      config.set("extras.experience.increment", 5);
      config.set("extras.experience.taxpercent", 0);

      config.set("extras.playerpoints.enabled", true);
      config.set("extras.playerpoints.material", "diamond");
      config.set("extras.playerpoints.display", "&7Your current PlayerPoints offer is &b%AMOUNT%");
      config.set("extras.playerpoints.theirdisplay", "&7Their current PlayerPoints offer is &b%AMOUNT%");
      config.set("extras.playerpoints.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
              "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
              "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
              "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
      config.set("extras.playerpoints.increment", 5);
      config.set("extras.playerpoints.taxpercent", 0);

      config.set("extras.griefprevention.enabled", true);
      config.set("extras.griefprevention.material", "diamond_pickaxe");
      config.set("extras.griefprevention.display", "&7Your current GriefPrevention offer is &b%AMOUNT%");
      config.set("extras.griefprevention.theirdisplay", "&7Their current GriefPrevention offer is &b%AMOUNT%");
      config.set("extras.griefprevention.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
              "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
              "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
              "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
      config.set("extras.griefprevention.increment", 1);
      config.set("extras.griefprevention.taxperecent", 0);

      config.set("extras.enjinpoints.enabled", false);
      config.set("extras.enjinpoints.material", "emerald");
      config.set("extras.enjinpoints.display", "&7Your current EnjinPoints offer is &b%AMOUNT%");
      config.set("extras.enjinpoints.theirdisplay", "&7Their current EnjinPoints offer is &b%AMOUNT%");
      config.set("extras.enjinpoints.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
              "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
              "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
              "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
      config.set("extras.enjinpoints.increment", 1);
      config.set("extras.enjinpoints.taxpercent", 0);

      config.set("extras.tokenenchant.enabled", true);
      config.set("extras.tokenenchant.material", "enchanted_book");
      config.set("extras.tokenenchant.display", "&7Your current TokenEnchant tokens offer is &b%AMOUNT%");
      config.set("extras.tokenenchant.theirdisplay", "Their current TokenEnchants tokens offer is &b%AMOUNT%");
      config.set("extras.tokenenchant.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
              "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
              "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
              "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
      config.set("extras.tokenenchant.increment", 1);
      config.set("extras.tokenenchant.taxpercent", 0);

      config.set("soundeffects.enabled", true);
      config.set("soundeffects.onchange", true);
      config.set("soundeffects.onaccept", true);
      config.set("soundeffects.oncomplete", true);
      config.set("soundeffects.oncountdown", true);

      config.set("configversion", Double.parseDouble(getDescription().getVersion()));
      try {
        langFile.createNewFile();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      lang.set("sentrequest", "&6&l(!) &r&6You sent a trade request to &e%PLAYER%");
      lang.set("receivedrequest", "&6&l(!) &r&6You received a trade request from &e%PLAYER%%NEWLINE%&6&l(!) &r&6Type &e/trade %PLAYER% &6to begin trading");
      lang.set("receivedrequesthover", "&6&lClick here to trade with &e&l%PLAYER%");
      lang.set("creative", "&4&l(!) &r&4You can't trade in creative mode!");
      lang.set("creativethem", "&4&l(!) &r&4That player is in creative mode!");
      lang.set("withinrange", "&4&l(!) &r&4You must be within %AMOUNT% blocks of a player to trade with them");
      lang.set("withinrangecrossworld", "&4&l(!) &r&4You must be within %AMOUNT% blocks of a player%NEWLINE%&4&l(!) &r&4in a different world to trade with them!");
      lang.set("nocrossworld", "&4&l(!) &r&4You must be in the same world as a player to trade with them!");
      lang.set("senderaccept", "&6&l(!) &r&e%PLAYER% &6accepted your trade request");
      lang.set("receiveraccept", "&6&l(!) &r&6You accepted &e%PLAYER%'s &6trade request");
      lang.set("cancelled", "&4&l(!) &r&4The trade was cancelled");
      lang.set("expired", "&4&l(!) &r&4Your last trade request expired");
      lang.set("waitforexpire", "&4&l(!) &r&4You still have an active trade request%NEWLINE%&4&l(!) &r&4It will expire shortly");
      lang.set("playernotfound", "&4&l(!) &r&4Could not find specified player");
      lang.set("tradewithself", "&4&l(!) &r&4You cannot trade with yourself");
      lang.set("invalidusage", "&4&l(!) &r&4Invalid arguments. Usage: %NEWLINE%" +
              "    &c- /trade <player name>%NEWLINE%" +
              "    &c- /trade deny");
      lang.set("noperms", "&4&l(!) &r&4You do not have permission to trade");
      lang.set("nopermssender", "&4&l(!) &r&4You do not have permission to send a trade");
      lang.set("nopermsreceiver", "&4&l(!) &r&4That player does not have permission to accept a trade");
      lang.set("nopermssenderadmin", "&4&l(!) &r&4You do not have permission to use this command");
      lang.set("tradecomplete", "&6&l(!) &r&6The trade was successful!");
      lang.set("forcedtrade", "&6&l(!) &r&6You've been forced into a trade with &e%PLAYER%");
      lang.set("denied-them", "&4&l(!) &r&4Your trade request to &c%PLAYER% &4was denied");
      lang.set("denied-you", "&4&l(!) &r&4Any recent incoming trade requests have been denied.");
      lang.set("spectate.message", "&6&l(!) &e%PLAYER1% &6and &e%PLAYER2% &6have started a trade %NEWLINE%&6&l(!) &6Type &e/tradeplus spectate %PLAYER1% %PLAYER2% &6to spectate");
      lang.set("spectate.hover", "&6&lClick here to spectate this trade");
    } else {
      double configVersion = config.contains("configversion") && config.isDouble("configversion") ? config.getDouble("configversion") : 0;

      if (configVersion < 1.11) {
        config.set("gui.title", config.contains("guititle") ? config.get("guititle") : "Your Items <|     |> Their Items");
        config.set("guititle", null);
        config.set("gui.showhead", !config.contains("showhead") || config.getBoolean("showhead"));
        config.set("showhead", null);
        config.set("gui.accept", "&a&lClick to accept the trade");
        config.set("gui.cancel", "&c&lClick to cancel the trade");
        config.set("gui.theyaccept", " ");
        config.set("gui.theycancel", " ");
      }

      if (configVersion < 1.12) {
        config.set("messages.tradecomplete", "&6&l(!) &r&6The trade was successful!");
        config.set("gui.headname", "&7You are trading with &3&l%PLAYERNAME%");
      }

      if (configVersion < 1.2) {
        config.set("economy.enabled", true);
        config.set("economy.clear", "&4&lClick to clear your money offer");
        config.set("economy.offer", "&7Your current money offer is &3&l%MONEYAMOUNT% %CURRENCYNAME%");
        config.set("economy.theiroffer", "&7Their current money offer is &3&l%MONEYAMOUNT% %CURRENCYNAME%");
        config.set("economy.higher", "&a&lClick to raise your money offer by %MONEYAMOUNT% %CURRENCYNAME%");
        config.set("economy.lower", "&c&lClick to lower your money offer by %MONEYAMOUNT% %CURRENCYNAME%");
        config.set("economy.higheramount", 5.0);
        config.set("economy.loweramount", 5.0);
        config.set("soundeffects.enabled", true);
        config.set("soundeffects.onchange", true);
        config.set("soundeffects.onaccept", true);
        config.set("soundeffects.oncomplete", true);
        config.set("soundeffects.oncountdown", true);
      }

      if (configVersion < 1.22) {
        config.set("gui.separatorId", "160:15");
      }

      if (configVersion < 1.23) {
        config.set("gui.separatorid", config.getString("gui.separatorId"));
        config.set("gui.separatorId", null);
        config.set("gui.acceptid", "160:13");
        config.set("gui.cancelid", "160:14");
      }

      if (configVersion < 1.24) {
        config.set("messages.withinrange", config.getString("messages.within10"));
        config.set("messages.within10", null);
        config.set("ranges.sameworld", 10.0);
        config.set("ranges.crossworld", 0.0);
        config.set("ranges.allowcrossworld", false);
        config.set("messages.withinrangecrossworld", "&4&l(!) &r&4You must be within %AMOUNT% blocks of a player%NEWLINE%&4&l(!) &r&4in a different world to trade with them!");
        config.set("messages.nocrossworld", "&4&l(!) &r&4You must be in the same world as a player to trade with them!");
      }

      if (configVersion < 1.3) {
        config.set("blocked-items", Arrays.asList("bedrock"));
      }

      if (configVersion < 1.31) {
        config.set("action", "crouchrightclick");
        config.set("economy.increment", config.getDouble("economy.higheramount", 10.0));
        config.set("economy.higheramount", null);
        config.set("economy.loweramount", null);
        config.set("gui.acceptid", "160:14");
        config.set("gui.cancelid", "160:13");
      }

      if (configVersion < 1.34) {
        config.set("hooks.economy.enabled", config.getBoolean("economy.enabled", true));
        config.set("hooks.economy.material", "gold_ingot");
        config.set("hooks.economy.youroffer", config.getString("economy.offer", "&7Your current money offer is &e%MONEYAMOUNT%"));
        config.set("hooks.economy.theiroffer", config.getString("economy.theiroffer", "&7Their current money offer is &e%MONEYAMOUNT%"));
        config.set("hooks.economy.increment", config.getDouble("economy.increment", 10.0));
        config.set("economy", null);
      }

      if (configVersion < 1.5) {
        config.set("hooks.xp.enabled", true);
        config.set("hooks.xp.material", "experience_bottle");
        config.set("hooks.xp.youroffer", "&7Your current XP offer is &e%XPAMOUNT%");
        config.set("hooks.xp.theiroffer", "&7Their current XP offer is &e%XPAMOUNT%");
        config.set("hooks.xp.increment", 5);
      }

      if (configVersion < 2.0) {
        if (config.contains("messages")) {
          for (String key : config.getConfigurationSection("messages").getKeys(false))
            lang.set(key, config.getString("messages." + key).replace("%PLAYERNAME%", "%PLAYER%")
                    .replace("%MONEYAMOUNT%", "%AMOUNT%").replace("%XPAMOUNT%", "%AMOUNT%"));
          config.set("messages", null);
        }
        if (config.contains("hooks")) {
          for (String key : config.getConfigurationSection("hooks.economy").getKeys(false))
            config.set("extras.economy." + key.replace("youroffer", "display").replace("theiroffer", "theirdisplay"), config.get("hooks.economy." + key));
          for (String key : config.getConfigurationSection("hooks.xp").getKeys(false))
            config.set("extras.experience." + key.replace("youroffer", "display").replace("theiroffer", "theirdisplay"), config.get("hooks.experience." + key));
          config.set("hooks", null);
          config.set("extras.economy.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
                  "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
                  "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
                  "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
          config.set("extras.experience.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
                  "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
                  "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
                  "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
          config.set("extras.economy.taxpercent", 0);
          config.set("extras.experience.taxpercent", 0);
        }
        config.set("gui.head", config.getString("gui.headname", "&7You are trading with: &3&l%PLAYER%").replace("%PLAYERNAME%", "%PLAYER%"));
        config.set("gui.headname", null);
      }

      if (configVersion < 2.1) {
        config.set("extras.playerpoints.enabled", true);
        config.set("extras.playerpoints.material", "diamond");
        config.set("extras.playerpoints.display", "&7Your current PlayerPoints offer is &b%AMOUNT%");
        config.set("extras.playerpoints.theirdisplay", "&7Their current PlayerPoints offer is &b%AMOUNT%");
        config.set("extras.playerpoints.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
                "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
                "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
                "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
        config.set("extras.playerpoints.increment", 5);
        config.set("extras.playerpoints.taxpercent", 0);
      }

      if (configVersion < 2.11) {
        config.set("blocked.blacklist", config.getStringList("blocked-items"));
        config.set("blocked.named-items", false);
      }

      if (configVersion < 2.12) {
        config.set("extras.griefprevention.enabled", true);
        config.set("extras.griefprevention.material", "diamond_pickaxe");
        config.set("extras.griefprevention.display", "&7Your current GriefPrevention offer is &b%AMOUNT%");
        config.set("extras.griefprevention.theirdisplay", "&7Their current GriefPrevention offer is &b%AMOUNT%");
        config.set("extras.griefprevention.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
                "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
                "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
                "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
        config.set("extras.griefprevention.increment", 1);
        config.set("extras.griefprevention.taxperecent", 0);
      }

      if (configVersion < 2.14) {
        config.set("requestcooldownseconds", 20);
        lang.set("denied-them", "&4&l(!) &r&4Your trade request to &c%PLAYER% &4was denied");
        lang.set("denied-you", "&4&l(!) &r&4Any recent incoming trade requests have been denied.");
        if (lang.getString("invalidusage", "&4&l(!) &r&4Invalid arguments. Usage: &c/trade <player name>").equals("&4&l(!) &r&4Invalid arguments. Usage: &c/trade <player name>")) {
          lang.set("invalidusage", "&4&l(!) &r&4Invalid arguments. Usage: %NEWLINE%" +
                  "    &c- /trade <player name>%NEWLINE%" +
                  "    &c- /trade deny");
        }
      }

      if (configVersion < 2.20) {
        config.set("gui.spectator-title", "%PLAYER1%              %PLAYER2%");
        config.set("gui.force.enabled", config.getBoolean("gui.showadminforce", true));
        config.set("gui.showadminforce", null);
        config.set("gui.force.type", "clock");
        config.set("gui.force.name", "&4&lForce Trade");
        config.set("gui.force.lore", Arrays.asList("&cClick here to force", "&cacceptance.", "", "&cThis shows only for admins."));
        config.set("gui.showaccept", true);
      }

      if (configVersion < 2.22) {
        if (Sounds.version < 19)
          config.set("gui.title", "Your Items <|     |> Their Items");
      }

      if (configVersion < 2.30) {
        config.set("ranges.blocked-worlds", Arrays.asList("ThisWorldDoesntExistButItsBlocked", "NeitherDoesThisOneButItIsToo"));
      }

      if (configVersion < 2.42) {
        config.set("allow-trade-in-creative", false);
        config.set("blocked.lore", Collections.singletonList("EXAMPLE_BLOCKED_LORE"));
        config.set("blocked.regex", "");
        lang.set("creative", "&4&l(!) &r&4You can't trade in creative mode!");
        lang.set("creativethem", "&4&l(!) &r&4That player is in creative mode!");
      }

      if (configVersion < 2.45) {
        config.set("permissions.required", config.getBoolean("permissionrequired", false));
        config.set("permissions.send", config.getString("permissionnode", "tradeplus.send"));
        config.set("permissions.accept", "tradeplus.accept");
        config.set("permissionrequired", null);
        config.set("permissionnode", null);
      }

      if (configVersion < 2.50) {
        config.set("extras.enjinpoints.enabled", false);
        config.set("extras.enjinpoints.material", "emerald");
        config.set("extras.enjinpoints.display", "&7Your current EnjinPoints offer is &b%AMOUNT%");
        config.set("extras.enjinpoints.theirdisplay", "&7Their current EnjinPoints offer is &b%AMOUNT%");
        config.set("extras.enjinpoints.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
                "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
                "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
                "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
        config.set("extras.enjinpoints.increment", 1);
        config.set("extras.enjinpoints.taxpercent", 0);
      }

      if (configVersion < 2.51) {
        config.set("aliases", Collections.singletonList("trade+"));
      }

      if (configVersion < 2.52) {
        config.set("extras.tokenenchant.enabled", true);
        config.set("extras.tokenenchant.material", "enchanted_book");
        config.set("extras.tokenenchant.display", "&7Your current TokenEnchant tokens offer is &b%AMOUNT%");
        config.set("extras.tokenenchant.theirdisplay", "Their current TokenEnchants tokens offer is &b%AMOUNT%");
        config.set("extras.tokenenchant.lore", Arrays.asList("&aLeft Click to &clower &ayour offer by %PLAYERINCREMENT%",
                "&aRight Click to &braise &ayour offer by %PLAYERINCREMENT%",
                "&aShift + Left Click to &clower &ayour increment by %INCREMENT%",
                "&aShift + Right Click to &braise &ayour increment by %INCREMENT%"));
        config.set("extras.tokenenchant.increment", 1);
        config.set("extras.tokenenchant.taxpercent", 0);
      }

      if (configVersion < 2.53) {
        lang.set("nopermssender", "&4&l(!) &r&4You do not have permission to send a trade");
        lang.set("nopermsreceiver", "&4&l(!) &r&4That player does not have permission to accept a trade");
        lang.set("nopermssenderadmin", "&4&l(!) &r&4You do not have permission to use this command");
      }

      if (configVersion < 2.56) {
        lang.set("spectate.message", "&6&l(!) &e%PLAYER1% &6and &e%PLAYER2% &6have started a trade %NEWLINE%&6&l(!) &6Type &e/tradeplus spectate %PLAYER1% %PLAYER2% &6to spectate");
        lang.set("spectate.hover", "&6&lClick here to spectate this trade");
      }
    }

    // FIX OLD ID SHIT
    List<String> fixList = new ArrayList<>(Arrays.asList("gui.acceptid", "gui.cancelid", "gui.separatorid", "gui.force.type"));
    for (String key : getConfig().getConfigurationSection("extras").getKeys(false)) {
      fixList.add(getConfig().getString("extras." + key + ".material"));
    }
    for (String key : fixList) {
      if (config.contains(key)) {
        String val = config.getString(key);
        if (val.contains(":")) {
          String[] split = val.split(":");
          String val1 = split[1];
          try {
            byte data = Byte.parseByte(val1);
            String name = DyeColor.getByDyeData(data).name();
            if (name != null && !name.isEmpty()) {
              String val2 = split[0];
              Material material = Material.getMaterial(val2);
              if (material == null) {
                getLogger().warning("Unknown material: " + val2 + ". Is it an integer ID? Those are no longer supported in 1.13!");
                continue;
              }
              config.set(key, name + material.name());
            }
          } catch (Exception ignored) {}
        }
      }
    }

    config.set("configversion", Double.parseDouble(getDescription().getVersion()));
    saveConfig();
    saveLang();
    InvUtils.reloadItems(this);
    Sounds.loadSounds();
    if (Sounds.version > 17)
      getServer().getPluginManager().registerEvents(new InteractListener(this), this);
    commandHandler = new CommandHandler(this);
    commandHandler.add(new TradeCommand(this));
    commandHandler.add(new TradePlusCommand(this));
    ongoingTrades = new ConcurrentLinkedQueue<>();
  }

  public void reload() {
    config = YamlConfiguration.loadConfiguration(configFile);
    lang = YamlConfiguration.loadConfiguration(langFile);
    InvUtils.reloadItems(this);
    commandHandler.clear();
    commandHandler.add(new TradeCommand(this));
    commandHandler.add(new TradePlusCommand(this));
  }

  public FileConfiguration getLang() { return lang; }

  private void saveLang() {
    try {
      lang.save(langFile);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

}
