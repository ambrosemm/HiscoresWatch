package com.hiscoreswatch;

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
		return 25; // Default to front page ranks
	}
}
