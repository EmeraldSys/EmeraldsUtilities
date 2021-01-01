package me.elementemerald.EmeraldsUtilities.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandCheck {
	public static boolean isUser(CommandSender s)
	{
		if (s instanceof Player)
		{
			return true;
		}
		return false;
	}
}
