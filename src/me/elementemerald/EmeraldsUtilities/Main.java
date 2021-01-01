package me.elementemerald.EmeraldsUtilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
//import java.util.Set;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.io.IOException;

//import org.bukkit.GameRule;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.entity.*;
import org.bukkit.OfflinePlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.NamespacedKey;

import me.elementemerald.EmeraldsUtilities.utils.CommandCheck;

public class Main extends JavaPlugin implements Listener, TabCompleter {
	// file configs
	private File configf;
	private FileConfiguration config;
	private ArrayList<String> godmode = new ArrayList<String>();
	
	boolean entitycleanup = false;
	int entitylimit = 0;
	boolean randomspawn = false;
	boolean logevents = true;
	
	String prefix = "[" + ChatColor.AQUA + "Emerald's Utilities" + ChatColor.WHITE + "]";
	String prefix_d = "[" + ChatColor.AQUA + "Emerald's Utilities - Debug" + ChatColor.WHITE + "]";
	
	public FileConfiguration getConfig()
	{
		return this.config;
	}
	
	public FileConfiguration createConfig()
	{
		configf = new File(getDataFolder(), "config.yml");
		config = YamlConfiguration.loadConfiguration(configf);
		if (!configf.exists())
		{
			configf.getParentFile().mkdirs();
			//ConfigurationSection spawning = config.createSection("spawning");
			ConfigurationSection cleanup = config.createSection("cleanup");
			ConfigurationSection autorule = config.createSection("autorule");
			ConfigurationSection powert = config.createSection("powertools");
			ConfigurationSection warps = config.createSection("warps");
			//config.set("randomspawn", false);
			//spawning.set("randomspawn", false);
			cleanup.set("enabled", false);
			cleanup.set("entitylimit", 500);
			config.set("logevents", true);
			
			try
			{
				config.save(configf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return config;
	}
	
	public void reloadConfig()
	{
		config = YamlConfiguration.loadConfiguration(configf);
		ConfigurationSection spawning = config.getConfigurationSection("spawning");
		ConfigurationSection cleanup = config.getConfigurationSection("cleanup");
		entitycleanup = cleanup.getBoolean("enabled");
		entitylimit = cleanup.getInt("entitylimit");
		logevents = config.getBoolean("logevents");
		//randomspawn = spawning.getBoolean("randomspawn");
	}
	
	public void saveConfig()
	{
		try
		{
			config.save(configf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onEnable()
	{
		System.out.println("[Emeralds Utilities] Enabled!");
		FileConfiguration sconfig = createConfig();
		ConfigurationSection cleanup = sconfig.getConfigurationSection("cleanup");
		entitycleanup = cleanup.getBoolean("enabled");
		entitylimit = cleanup.getInt("entitylimit");
		logevents = config.getBoolean("logevents");
		//randomspawn = sconfig.getBoolean("randomspawn");
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
	}
	
	public void onDisable()
	{
		System.out.println("[Emeralds Utilities] Disabled!");
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e)
	{
		Player p = e.getPlayer();
		p.sendMessage(ChatColor.AQUA + "This server is running Emerald's Custom Utility plugin.");
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e)
	{
		if (e.isBedSpawn()) return;
		if (randomspawn)
		{
			Random r = new Random();
			double x = 10 + (10000 - 10) * r.nextDouble();
			double y = 75.0;
			double z = 10 + (10000 - 10) * r.nextDouble();
			Player p = e.getPlayer();
			// not sure if this is the default world, might read server.properties
			//World defWorld = Bukkit.getServer().getWorlds().get(0);
			Location randompos = new Location(null, x, y, z);
			p.teleport(randompos);
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e)
	{
		Entity en = e.getEntity();
		if (en.getType() == EntityType.PLAYER)
		{
			String n = en.getName();
			if (godmode.contains(n))
			{
				e.setCancelled(true);
			}
		}
		/* else if (en.getType() == EntityType.ZOMBIE || en.getType() == EntityType.SKELETON || en.getType() == EntityType.CREEPER && e.getCause() == EntityDamageEvent.DamageCause.FALL)
		{
			e.setCancelled(true); // spawner extra damage when falling
		} */
		else
		{
			if (logevents)
			{
				System.out.println("[Emerald's Utilities] EntityDamageEvent - EntityType: " + e.getEntityType().name() + " - Cause: " + e.getCause().name());
			}
		}
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e)
	{
		if (entitycleanup)
		{
			Location loc = e.getLocation();
			World a = loc.getWorld();
			List<LivingEntity> l_ent = a.getLivingEntities();
			if (l_ent.size() >= entitylimit)
			{
				for (LivingEntity en : l_ent)
				{
					if (en.getType() != EntityType.PLAYER)
					{
						en.remove();
					}
				}
			}
		}
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e)
	{
		Player p = e.getPlayer();
		EquipmentSlot slot = e.getHand();
		//System.out.println("Interact Event - " + p.getName() + " - " + slot.name());
		if (slot == EquipmentSlot.HAND && e.hasItem() || e.hasBlock() && e.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			//System.out.println("Interact Event (has item or block) - " + p.getName() + " - " + slot.name());
			String n = p.getName();
			ConfigurationSection s = config.getConfigurationSection("powertools");
			ConfigurationSection s2 = s.getConfigurationSection(n);
			if (s2 != null)
			{
				for (String k : s2.getKeys(false))
				{
					ItemStack item = e.getItem();
					Material m = item.getType();
					String mtype = m.toString().toLowerCase();
					if (k.equals(mtype))
					{
						String cmd = s2.getString(k);
						p.performCommand(cmd);
						System.out.println("[Emerald's Utilities] Powertool Executed - User: " + n + " - Item type: " + m.toString() + " - Command: " + cmd);
					}
				}
			}
		}
	}
	
	public EntityType getEntityByString(String entity)
	{
		for (EntityType etype : EntityType.values())
		{
			if (etype.name().equalsIgnoreCase(entity))
			{
				return etype;
			}
		}
		return null;
	}
	
	public Material getMaterialByString(String mat)
	{
		for (Material m : Material.values())
		{
			if (m.name().equalsIgnoreCase(mat))
			{
				return m;
			}
		}
		return null;
	}
	
	public Enchantment getEnchantByString(String ench)
	{
		return Enchantment.getByKey(NamespacedKey.minecraft(ench));
	}
	
	public PotionEffectType getPotionTypeByString(String effect)
	{
		for (PotionEffectType pt : PotionEffectType.values())
		{
			if (pt.getName().equalsIgnoreCase(effect))
			{
				return pt;
			}
		}
		return null;
	}
	
	public String userLastPlayed(OfflinePlayer p)
	{
		Date lastplayed = new Date(p.getLastPlayed());
		return lastplayed.toString();
	}

	public OfflinePlayer getOfflinePlayer2(String name)
	{
		for (OfflinePlayer op : Bukkit.getOfflinePlayers())
		{
			if (op.getName().equalsIgnoreCase(name))
			{
				return op;
			}
		}
		return null;
	}
	
	/* public void findAndKillEntities(World w, EntityType etype)
	{
		List<Entity> ents = w.getEntities();
		for (Entity ent : ents)
		{
			if (ent.getType() == etype)
			{
				ent.remove();
			}
		}
	} */
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
	{
		if (label.equalsIgnoreCase("eureload"))
		{
			if (s.hasPermission("EUtilities.config"))
			{
				reloadConfig();
				s.sendMessage(prefix + " Reloaded config.");
			}
			else
			{
				s.sendMessage(prefix + " You do not have access to this command.");
			}
		}
		if (label.equalsIgnoreCase("eucustomentity"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.entity"))
				{
					try
					{
						//Block b = p.getTargetBlock(null, 50);
						String entype = args[0].toUpperCase();
						//int encount = Integer.parseInt(args[1]);
						int encount = 0;
						
						try
						{
							encount = Integer.parseInt(args[1]);
						}
						catch (NumberFormatException ex)
						{
							encount = 1;
						}
						
						EntityType t = getEntityByString(entype);
						if (t == null)
						{
							s.sendMessage(prefix + " Invalid entity.");
							return true;
						}
						//Location eloc = new Location(null, b.getX(), b.getY(), b.getZ());
						Location eloc = p.getLocation();
						//p.getWorld().spawnEntity(((Player) s).getLocation(), t);
						
						for (int i = 0; i < encount; i++)
						{
							Entity ent = p.getWorld().spawnEntity(eloc, t);
							if (ent instanceof LivingEntity)
							{
								((LivingEntity) ent).setRemoveWhenFarAway(false);
							}
						}
						
						if (!args[0].contains("minecraft:"))
						{
							s.sendMessage(prefix + " minecraft:" + args[0].toLowerCase() + " successfully spawned " + Integer.toString(encount) + " time(s).");
						}
						else
						{
							s.sendMessage(prefix + args[0].toLowerCase() + " successfully spawned " + Integer.toString(encount) + " time(s).");
						}
					}
					catch (IndexOutOfBoundsException ex)
					{
						s.sendMessage(prefix + " Invalid syntax. Usage: /eucustomentity <entity> [count]");
					}
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("euflymode"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.fly"))
				{
					boolean enabled = !p.getAllowFlight();
					p.setAllowFlight(enabled);
					p.setFlying(true);
					if (enabled)
					{
						p.setFlying(false);
					}
				}
			}
		}
		if (label.equalsIgnoreCase("eugetfspeed"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.fly"))
				{
					float speed = p.getFlySpeed() * 10;
					s.sendMessage(prefix_d + " Fly speed is currently " + Integer.toString((int)speed) + ".");
				}
			}
		}
		if (label.equalsIgnoreCase("eusetfspeed"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.fly"))
				{
					try
					{
						int speed = Integer.parseInt(args[0]);
						if (speed < 0 || speed > 10)
						{
							s.sendMessage(prefix + " Invalid speed value.");
							return true;
						}
						float speed2 = (float)speed/10;
						p.setFlySpeed(speed2);
						s.sendMessage(prefix + " Set fly speed to " + args[0] + ".");
					}
					catch (IndexOutOfBoundsException ex)
					{
						s.sendMessage(prefix + " Invalid syntax. Usage: /eusetfspeed <speed>");
					}
				}
			}
		}
		if (label.equalsIgnoreCase("eudebug"))
		{
			Player p = (Player)s;
			try
			{
				if (args[0].equals("iteminhand"))
				{
					ItemStack i = p.getInventory().getItemInMainHand();
					Material itype = i.getType();
					s.sendMessage(prefix_d + " Current item in hand: " + itype.toString());
				}
				else if (args[0].equals("effects"))
				{
					Collection<PotionEffect> effects = p.getActivePotionEffects();
					for (PotionEffect po : effects)
					{
						s.sendMessage(prefix_d + " Found potion type: " + po.getType().getName() + " - Amplifier: " + Integer.toString(po.getAmplifier()) + " - Duration: " + Integer.toString(po.getDuration()));
					}
				}
				else if (args[0].equals("testcmd1"))
				{
					Location loc = p.getLocation();
					LivingEntity e1 = (LivingEntity)p.getWorld().spawnEntity(loc, EntityType.ZOMBIE);
					LivingEntity e2 = (LivingEntity)p.getWorld().spawnEntity(loc, EntityType.SKELETON);
					
					e1.attack(e2);
				}
				else
				{
					s.sendMessage(prefix_d + " Invalid debug option.");
				}
			}
			catch (IndexOutOfBoundsException ex)
			{
				s.sendMessage(prefix_d + " Invalid syntax. Usage: /eudebug <option>");
			}
		}
		if (label.equalsIgnoreCase("euaddpt"))
		{	
			Player p = (Player)s;
			if (p.hasPermission("EUtilities.item"))
			{
				ItemStack i = p.getInventory().getItemInMainHand();
				if (i.getType() == Material.AIR)
				{
					s.sendMessage(prefix + " Cannot powertool nothing.");
					return true;
				}
				
				ConfigurationSection cs = config.getConfigurationSection("powertools");
				ConfigurationSection ps = cs.getConfigurationSection(p.getName());
				if (ps == null)
				{
					ps = cs.createSection(p.getName());
				}
				
				ps.set(i.getType().toString().toLowerCase(), String.join(" ", args));
				
				try
				{
					config.save(configf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				s.sendMessage(prefix + " Command set to " + i.getType().toString().toLowerCase() + ".");
			}
			else
			{
				s.sendMessage(prefix + " You do not have access to this command.");
			}
		}
		if (label.equalsIgnoreCase("euremovept"))
		{
			Player p = (Player)s;
			if (p.hasPermission("EUtilities.item"))
			{
				ItemStack i = p.getInventory().getItemInMainHand();
				if (i.getType() == Material.AIR)
				{
					s.sendMessage(prefix + " Cannot powertool nothing.");
					return true;
				}
				
				ConfigurationSection cs = config.getConfigurationSection("powertools");
				ConfigurationSection ps = cs.getConfigurationSection(p.getName());
				
				if (ps == null)
				{
					s.sendMessage(prefix + " No powertool history.");
					return true;
				}
				
				ps.set(i.getType().toString().toLowerCase(), null);
				
				try
				{
					config.save(configf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				s.sendMessage(prefix + " Removed powertool.");
			}
			else
			{
				s.sendMessage(prefix + " You do not have access to this command.");
			}
		}
		if (label.equalsIgnoreCase("eugod"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.god"))
				{
					String n = p.getName();
					if (!godmode.contains(n))
					{
						godmode.add(n);
						p.sendMessage(prefix + " God mode turned on.");
					}
					else
					{
						godmode.remove(n);
						p.sendMessage(prefix + " God mode turned off.");
					}
				}
				else
				{
					p.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("eusmite"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				Block b = p.getTargetBlock(null, 50);
				p.getWorld().strikeLightning(new Location(null, b.getX(), b.getY(), b.getZ()));
			}
		}
		if (label.equalsIgnoreCase("eucreatewarp"))
		{
			//if (s instanceof Player)
			if (CommandCheck.isUser(s))
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.warp"))
				{
					try
					{
						Location ploc = p.getLocation();
						int x = ploc.getBlockX();
						int y = ploc.getBlockY();
						int z = ploc.getBlockZ();
						String[] loc = {Integer.toString(x), Integer.toString(y), Integer.toString(z)};
						
						ConfigurationSection warps = config.getConfigurationSection("warps");
						/* if (warps.getConfigurationSection(args[0]) != null)
						{
							s.sendMessage(prefix + " Warp already exists.");
							return true;
						} */
						
						ConfigurationSection warp = warps.createSection(args[0]);
						warp.set("loc", String.join(" ", loc));
						warp.set("world", p.getWorld().getName());
						
						saveConfig();
						
						s.sendMessage(prefix + " Warp created.");
					}
					catch (IndexOutOfBoundsException ex)
					{
						p.sendMessage(prefix + " Invalid syntax. Usage: /eucreatewarp <name>");
					}
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("euwarp"))
		{
			//if (s instanceof Player)
			if (CommandCheck.isUser(s))
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.warp"))
				{
					try
					{
						ConfigurationSection warps = config.getConfigurationSection("warps");
						ConfigurationSection wworld = warps.getConfigurationSection(args[0]);
						
						if (wworld != null)
						{
							String loc = wworld.getString("loc");
							String world = wworld.getString("world");
							World rworld = Bukkit.getWorld(world);
							if (loc != null)
							{
								String[] loc2 = loc.split(" ");
								Location wloc = new Location(rworld, Integer.parseInt(loc2[0]), Integer.parseInt(loc2[1]), Integer.parseInt(loc2[2]));
								p.teleport(wloc);
								p.sendMessage(prefix + " Warped to " + ChatColor.AQUA + args[0] + ChatColor.WHITE + ".");
							}
						}
						else
						{
							p.sendMessage(prefix + " Warp doesn't exist.");
						}
					}
					catch (IndexOutOfBoundsException ex)
					{
						p.sendMessage(prefix + " Invalid syntax. Usage: /euwarp <name>");
					}
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("eudelwarp"))
		{
			if (CommandCheck.isUser(s))
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.warp"))
				{
					try
					{
						ConfigurationSection warps = config.getConfigurationSection("warps");
						if (warps.getConfigurationSection(args[0]) == null)
						{
							p.sendMessage(prefix + " Warp doesn't exist.");
							return true;
						}
						
						warps.set(args[0], null);
						
						saveConfig();
						
						p.sendMessage(prefix + " Warp deleted.");
					}
					catch (IndexOutOfBoundsException ex)
					{
						p.sendMessage(prefix + " Invalid syntax. Usage: /eudelwarp <name>");
					}
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("euget32k"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.item"))
				{
					ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
					sword.addUnsafeEnchantment(Enchantment.FIRE_ASPECT, 32000);
					sword.addUnsafeEnchantment(Enchantment.KNOCKBACK, 32000);
					sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, 32000);
					p.getInventory().addItem(sword);
				}
			}
		}
		if (label.equalsIgnoreCase("eurename"))
		{
			if (s instanceof Player)
			{	
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.item"))
				{
					try
					{
						ItemStack i = p.getInventory().getItemInMainHand();
						if (i.getType() == Material.AIR)
						{
							return true;
						}
						
						ItemMeta im = i.getItemMeta();
						im.setDisplayName(String.join(" ", args));
						i.setItemMeta(im);
					}
					catch (IndexOutOfBoundsException ex)
					{
						s.sendMessage(prefix + " Invalid syntax. Usage: /eurename <name>");
					}
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("eucenchant"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.item"))
				{
					try
					{
						ItemStack i = p.getInventory().getItemInMainHand();
						
						if (i.getType() == Material.AIR)
						{
							return true;
						}
						
						if (args[0].contains(","))
						{
							String[] enchs_array = args[0].split(",");
							
							for (String ench_b : enchs_array)
							{
								Enchantment ench = getEnchantByString(ench_b.toLowerCase());
								if (ench == null)
								{
									p.sendMessage(prefix + " Invalid enchantment.");
									return true;
								}
								
								i.addUnsafeEnchantment(ench, Integer.parseInt(args[1]));
								p.sendMessage(prefix + " Added " + ChatColor.AQUA + ench_b.toUpperCase() + ChatColor.WHITE + " to " + ChatColor.AQUA + i.getType().name() + ChatColor.WHITE + ".");
							}
						}
						else
						{
							Enchantment ench = getEnchantByString(args[0].toLowerCase());
							if (ench == null)
							{
								p.sendMessage(prefix + " Invalid enchantment.");
								return true;
							}
							
							i.addUnsafeEnchantment(ench, Integer.parseInt(args[1]));
							p.sendMessage(prefix + " Added " + ChatColor.AQUA + args[0].toUpperCase() + ChatColor.WHITE + " to " + ChatColor.AQUA + i.getType().name() + ChatColor.WHITE + ".");
						}
					}
					catch (IndexOutOfBoundsException ex)
					{
						s.sendMessage(prefix + " Invalid syntax. Usage: /eucenchant <enchantment> <level>");
					}
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("eurenchant"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.item"))
				{
					try
					{
						ItemStack i = p.getInventory().getItemInMainHand();
						
						if (i.getType() == Material.AIR)
						{
							return true;
						}
						
						Enchantment ench = getEnchantByString(args[0]);
						if (ench == null)
						{
							p.sendMessage(prefix + " Invalid enchantment.");
							return true;
						}
						
						ItemMeta im = i.getItemMeta();
						if (!im.hasEnchant(ench))
						{
							p.sendMessage(prefix + " Item does not have that enchantment.");
							return true;
						}
						
						i.removeEnchantment(ench);
						p.sendMessage(prefix + " Removed " + ChatColor.AQUA + args[0].toUpperCase() + ChatColor.WHITE + " from " + ChatColor.AQUA + i.getType().name() + ChatColor.WHITE + ".");
					}
					catch (IndexOutOfBoundsException ex)
					{
						s.sendMessage(prefix + " Invalid syntax. Usage: /eurenchant <enchantment>");
					}
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("eurenchants"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.item"))
				{
					ItemStack i = p.getInventory().getItemInMainHand();
					
					if (i.getType() == Material.AIR)
					{
						return true;
					}
					
					Map<Enchantment, Integer> enchs = i.getEnchantments();
					
					/* for (Entry<Enchantment, Integer> entry : enchs.entrySet())
					{
						i.removeEnchantment(entry.getKey());
					} */
					for (Enchantment ench : enchs.keySet())
					{
						i.removeEnchantment(ench);
					}
					
					p.sendMessage(prefix + " All enchantments removed.");
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("euechest"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				Inventory echest = p.getEnderChest();
				p.openInventory(echest);
			}
		}
		if (label.equalsIgnoreCase("euinvsee"))
		{
			if (s instanceof Player)
			{
				if (s.hasPermission("EUtilities.view"))
				{
					try
					{
						Player p = Bukkit.getPlayer(args[0]);
						if (p == null)
						{
							s.sendMessage(prefix + " Player not found.");
							return true;
						}
						((Player) s).openInventory(p.getInventory());
					}
					catch (IndexOutOfBoundsException ex)
					{
						s.sendMessage(prefix + " Invalid syntax. Usage: /euinvsee <player>");
					}
				}
			}
		}
		if (label.equalsIgnoreCase("euclearinv"))
		{
			if (s instanceof Player)
			{
				try
				{
					if (args[0].equals("all"))
					{
						Player p = (Player)s;
						Inventory pinv = p.getInventory();
						pinv.clear();
						p.sendMessage(prefix + " Inventory cleared.");
					}
					else if (args[0].equals("hand"))
					{
						Player p = (Player)s;
						PlayerInventory pinv = p.getInventory();
						ItemStack i = pinv.getItemInMainHand();
						if (i.getType() == Material.AIR)
						{
							return true;
						}
						
						pinv.remove(i);
						p.sendMessage(prefix + " Item in main hand cleared.");
					}
					else
					{
						Player p = (Player)s;
						Inventory pinv = p.getInventory();
						Material m = getMaterialByString(args[0]);
						if (m == null)
						{
							p.sendMessage(prefix + " Not a valid item type.");
							return true;
						}
						
						HashMap<Integer, ? extends ItemStack> items = pinv.all(m);
						for (ItemStack i : items.values())
						{
							pinv.remove(i);
						}
						p.sendMessage(prefix + " Type of item cleared.");
					}
				}
				catch (IndexOutOfBoundsException ex)
				{
					s.sendMessage(prefix + " Invalid syntax. Usage: /euclearinv <all/hand/itemtype>");
				}
			}
		}
		if (label.equalsIgnoreCase("euwhois"))
		{
			if (s.hasPermission("EUtilities.view"))
			{
				try
				{
					OfflinePlayer op = getOfflinePlayer2(args[0]);
					if (op != null)
					{
						boolean banned = op.isBanned();
						boolean online = op.isOnline();
						String lastplayed = userLastPlayed(op);
						s.sendMessage("----- Player Whois -----");
						String[] msgs = {String.format("User banned?: %s", banned ? "Yes" : "No"), String.format("User online?: %s", online ? "Yes" : "No"), String.format("User last played: %s", lastplayed)};
						s.sendMessage(msgs);
					}
					else {
						s.sendMessage(prefix + " User has never played on this server.");
					}
				}
				catch (IndexOutOfBoundsException ex)
				{
					s.sendMessage(prefix + " Invalid syntax. Usage: /euwhois <player>");
				}
			}
		}
		
		// custom potions lo
		if (label.equalsIgnoreCase("eucpotion"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.item"))
				{
					try
					{
						if (args[1].equalsIgnoreCase("splash"))
						{
							ItemStack i = new ItemStack(Material.SPLASH_POTION, 1);
							PotionMeta pm = (PotionMeta)i.getItemMeta();
							
							PotionEffectType pt = getPotionTypeByString(args[0]);
							if (pt == null)
							{
								p.sendMessage(prefix + " Not a valid effect.");
								return true;
							}
							
							PotionEffect pf = new PotionEffect(pt, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
							pm.addCustomEffect(pf, true);
							i.setItemMeta((ItemMeta)pm);
							p.getInventory().addItem(i);
						}
						else if (args[1].equalsIgnoreCase("normal"))
						{
							ItemStack i = new ItemStack(Material.POTION, 1);
							PotionMeta pm = (PotionMeta)i.getItemMeta();
							
							PotionEffectType pt = getPotionTypeByString(args[0]);
							if (pt == null)
							{
								p.sendMessage(prefix + " Not a valid effect.");
								return true;
							}
							
							PotionEffect pf = new PotionEffect(pt, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
							pm.addCustomEffect(pf, true);
							i.setItemMeta((ItemMeta)pm);
							p.getInventory().addItem(i);
						}
						else if (args[1].equalsIgnoreCase("lingering"))
						{
							ItemStack i = new ItemStack(Material.LINGERING_POTION, 1);
							PotionMeta pm = (PotionMeta)i.getItemMeta();
							
							PotionEffectType pt = getPotionTypeByString(args[0]);
							if (pt == null)
							{
								p.sendMessage(prefix + " Not a valid effect.");
								return true;
							}
							
							PotionEffect pf = new PotionEffect(pt, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
							pm.addCustomEffect(pf, true);
							i.setItemMeta((ItemMeta)pm);
							p.getInventory().addItem(i);
						}
					}
					catch (IndexOutOfBoundsException ex)
					{
						s.sendMessage(prefix + " Invalid syntax. Usage: /eucpotion <effect> <type> <duration> <amplifier>");
					}
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		if (label.equalsIgnoreCase("eukillentity"))
		{
			if (s instanceof Player)
			{
				Player p = (Player)s;
				if (p.hasPermission("EUtilities.entity"))
				{
					try
					{						
						if (args[0].equalsIgnoreCase("all"))
						{
							World w = null;
							
							if (args.length >= 2)
							{
								w = Bukkit.getWorld(args[1]);
								if (w == null)
								{
									p.sendMessage(prefix + " Not a valid world.");
									return true;
								}
							}
							else
							{
								w = p.getWorld();
							}
							
							int count = 0;
							List<Entity> ents = w.getEntities();
							for (Entity ent : ents)
							{
								if (ent.getType() != EntityType.PLAYER)
								{
									ent.remove();
									count++;
								}
							}
							p.sendMessage(prefix + " Removed " + Integer.toString(count) + " entities.");
						}
						else if (args[0].equalsIgnoreCase("hostile"))
						{
							World w = null;
							
							if (args.length >= 2)
							{
								w = Bukkit.getWorld(args[1]);
								if (w == null)
								{
									p.sendMessage(prefix + " Not a valid world.");
									return true;
								}
							}
							else
							{
								w = p.getWorld();
							}
							
							int count = 0;
							List<LivingEntity> ents = w.getLivingEntities();
							for (LivingEntity ent : ents)
							{
								if (ent instanceof Monster)
								{
									ent.remove();
									count++;
								}
							}
							p.sendMessage(prefix + " Removed " + Integer.toString(count) + " entities.");
						}
						else
						{
							World w = null;
							
							if (args.length >= 2)
							{
								w = Bukkit.getWorld(args[1]);
								if (w == null)
								{
									p.sendMessage(prefix + " Invalid world.");
									return true;
								}
							}
							else
							{
								w = p.getWorld();
							}
							
							int count = 0;
							List<Entity> ents = w.getEntities();
							EntityType etype = getEntityByString(args[0]);
							if (etype == null)
							{
								p.sendMessage(prefix + " Invalid entity type.");
								return true;
							}
							
							for (Entity ent : ents)
							{
								if (ent.getType() == etype)
								{
									ent.remove();
									count++;
								}
							}
							p.sendMessage(prefix + " Removed " + Integer.toString(count) + " entities.");
						}
					}
					catch (IndexOutOfBoundsException ex)
					{
						s.sendMessage(prefix + " Invalid syntax. Usage: /eukillentity <all/hostile/type>");
					}
				}
				else
				{
					s.sendMessage(prefix + " You do not have access to this command.");
				}
			}
		}
		return true;
	}
	
	public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] args) 
	{
		if (cmd.getName().equalsIgnoreCase("eucustomentity"))
		{
			if (args.length == 1)
			{
				List<String> entities = new ArrayList<>();
				for (EntityType e : EntityType.values())
				{
					String n = e.name().toLowerCase();
					entities.add(n);
				}
				return entities;
			}
		}
		else if (cmd.getName().equalsIgnoreCase("eudebug"))
		{
			if (args.length == 1)
			{
				List<String> opts = new ArrayList<>();
				opts.add("iteminhand");
				opts.add("effects");
				
				return opts;
			}
		}
		else if (cmd.getName().equalsIgnoreCase("eucreatewarp"))
		{
			if (args.length == 1)
			{
				List<String> empty = new ArrayList<>();
				return empty;
			}
		}
		else if (cmd.getName().equalsIgnoreCase("euwarp") || cmd.getName().equalsIgnoreCase("eudelwarp"))
		{
			if (args.length == 1)
			{
				List<String> warps = new ArrayList<>();
				ConfigurationSection warp_s = config.getConfigurationSection("warps");
				Map<String, Object> warpmap = warp_s.getValues(false);
				for (String w : warpmap.keySet())
				{
					warps.add(w);
				}
				
				return warps;
			}
		}
		else if (cmd.getName().equalsIgnoreCase("eucenchant"))
		{
			if (args.length == 1)
			{
				List<String> enchs = new ArrayList<>();
				for (Enchantment ench : Enchantment.values())
				{
					String ench2 = ench.getKey().getKey();
					enchs.add(ench2);
				}
				return enchs;
			}
		}
		else if (cmd.getName().equalsIgnoreCase("eurenchant"))
		{
			if (args.length == 1)
			{
				if (s instanceof Player)
				{
					List<String> enchs = new ArrayList<>();
					Player p = (Player)s;
					ItemStack i = p.getInventory().getItemInMainHand();
					Map<Enchantment, Integer> enchmap = i.getEnchantments();
					for (Enchantment ench : enchmap.keySet())
					{
						String n = ench.getKey().getKey();
						enchs.add(n);
					}
					return enchs;
				}
			}
		}
		else if (cmd.getName().equalsIgnoreCase("euclearinv"))
		{
			if (args.length == 1)
			{
				List<String> types = new ArrayList<>();
				types.add("all");
				types.add("hand");
				for (Material m : Material.values())
				{
					String n = m.name().toLowerCase();
					types.add(n);
				}
				return types;
			}
		}
		else if (cmd.getName().equalsIgnoreCase("eucpotion"))
		{
			if (args.length == 1)
			{
				List<String> pts = new ArrayList<>();
				for (PotionEffectType pt : PotionEffectType.values())
				{
					String n = pt.getName().toLowerCase();
					pts.add(n);
				}
				return pts;
			}
			else if (args.length == 2)
			{
				List<String> types = new ArrayList<>();
				types.add("normal");
				types.add("splash");
				types.add("lingering");
				return types;
			}
		}
		else if (cmd.getName().equalsIgnoreCase("eukillentity"))
		{
			if (args.length == 1)
			{
				List<String> ents = new ArrayList<>();
				ents.add("all");
				ents.add("hostile");
				for (EntityType ent : EntityType.values())
				{
					String n = ent.name().toLowerCase();
					ents.add(n);
				}
				return ents;
			}
		}
		return null;
	}
}