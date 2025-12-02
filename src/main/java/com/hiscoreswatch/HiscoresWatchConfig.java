package com.hiscoreswatch;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("hiscoreswatch")
public interface HiscoresWatchConfig extends Config
{
	@Range(
			min = 1,
			max = 10000
	)
	@ConfigItem(
			keyName = "rankThreshold",
			name = "Rank Threshold",
			description = "The rank at or below which an alert will be triggered for any hiscore category.",
			position = 1
	)
	default int rankThreshold()
	{
		return 1000;
	}

	@ConfigItem(
			keyName = "alertFor200mXp",
			name = "Alert for 200m XP",
			description = "Notifies you when a player with 200m XP in a skill is nearby.",
			position = 2
	)
	default boolean alertFor200mXp()
	{
		return true;
	}

	@ConfigItem(
			keyName = "ignoreList",
			name = "Ignore List",
			description = "Comma-separated list of player names to ignore. Not case-sensitive.",
			position = 3
	)
	default String ignoreList()
	{
		return "";
	}

	@ConfigItem(
			keyName = "chatColor",
			name = "Alert Color",
			description = "The color of the chat message alert.",
			position = 4
	)
	default Color chatColor()
	{
		return Color.RED;
	}

	// --- NEW CONFIGURATION ITEMS ---
	@ConfigItem(
			keyName = "checkNearbyPlayers",
			name = "Check Nearby Players",
			description = "Checks players who appear in your viewport.",
			position = 5
	)
	default boolean checkNearbyPlayers()
	{
		return true;
	}

	@ConfigItem(
			keyName = "checkFriendsChat",
			name = "Check Friends Chat",
			description = "Checks players who join your friends chat channel.",
			position = 6
	)
	default boolean checkFriendsChat()
	{
		return true;
	}

	@ConfigItem(
			keyName = "checkClanChat",
			name = "Check Clan Chat",
			description = "Checks players who join your clan chat.",
			position = 7
	)
	default boolean checkClanChat()
	{
		return true;
	}
}
