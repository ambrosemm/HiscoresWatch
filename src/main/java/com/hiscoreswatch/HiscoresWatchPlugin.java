package com.hiscoreswatch;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provides;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
// New Imports for the improved logic
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
// This is the correct import path for the ChatMessageBuilder utility
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.util.Text;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

@Slf4j
@PluginDescriptor(
		name = "Hiscores Watch"
)

public class HiscoresWatchPlugin extends Plugin
{
	private static final String HISCORES_API_URL = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=";
	private static final long MAX_XP = 200_000_000L;

	private Cache<String, Boolean> checkedPlayers;
	private Set<String> ignoredPlayers;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private HiscoresWatchConfig config;

	@Provides
	HiscoresWatchConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HiscoresWatchConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		checkedPlayers = CacheBuilder.newBuilder()
				.expireAfterWrite(5, TimeUnit.MINUTES)
				.build();
		// Initialize the ignore list on startup
		updateIgnoredPlayers();
		log.info("Hiscores Watch started!");
		clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Hiscores Watch has started.", null));
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Hiscores Watch stopped!");
		clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Hiscores Watch has stopped.", null));
		if (checkedPlayers != null)
		{
			checkedPlayers.invalidateAll();
			checkedPlayers = null;
		}
		if (ignoredPlayers != null)
		{
			ignoredPlayers.clear();
			ignoredPlayers = null;
		}
	}

	/**
	 * Parses the comma-separated ignore list from the config into a Set for efficient lookups.
	 */
	private void updateIgnoredPlayers()
	{
		this.ignoredPlayers = Arrays.stream(config.ignoreList().toLowerCase().split(","))
				.map(String::trim)
				.filter(name -> !name.isEmpty())
				.collect(Collectors.toSet());
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned event)
	{
		Player player = event.getPlayer();
		String playerName = player.getName();

		if (player == client.getLocalPlayer() || playerName == null)
		{
			return;
		}

		// --- IGNORE LIST LOGIC ---
		// Check against the cached ignore list.
		if (ignoredPlayers.contains(playerName.toLowerCase()))
		{
			return;
		}

		String sanitizedName = Text.toJagexName(playerName);

		if (checkedPlayers.getIfPresent(sanitizedName) != null)
		{
			return;
		}

		checkedPlayers.put(sanitizedName, true);
		fetchHiscores(sanitizedName);
	}

	private void fetchHiscores(String playerName)
	{
		log.debug("Attempting to fetch hiscores for: {}", playerName);

		HttpUrl url = HttpUrl.parse(HISCORES_API_URL + playerName);
		if (url == null)
		{
			log.error("Failed to create a valid URL for player: {}", playerName);
			return;
		}

		Request request = new Request.Builder().url(url).build();

		okHttpClient.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(Call call, IOException e)
			{
				log.warn("Network failure when fetching hiscores for {}: {}", playerName, e.getMessage());
			}

			@Override
			public void onResponse(Call call, Response response)
			{
				try (ResponseBody responseBody = response.body())
				{
					if (!response.isSuccessful())
					{
						log.debug("Unsuccessful hiscores response for {}. Code: {}", playerName, response.code());
						return;
					}

					if (responseBody == null)
					{
						return;
					}

					final String body = responseBody.string();
					final String[] stats = body.split("\n");
					final int rankThreshold = config.rankThreshold();
					final boolean alertFor200m = config.alertFor200mXp();

					// --- REFACTORED LOGIC ---
					// Use a Map to store achievements, allowing us to combine them.
					// LinkedHashMap preserves the insertion order.
					final Map<Hiscores, String> achievementsMap = new LinkedHashMap<>();

					for (Hiscores hiscore : Hiscores.values())
					{
						int apiIndex = hiscore.getApiIndex();

						if (stats.length <= apiIndex)
						{
							log.warn("Hiscores response for {} was too short. Stopping check at {}.", playerName, hiscore.getName());
							break;
						}

						String hiscoreData = stats[apiIndex];
						if (hiscoreData.isEmpty() || !hiscoreData.contains(","))
						{
							continue;
						}

						try
						{
							String[] hiscoreStats = hiscoreData.split(",");
							int rank = Integer.parseInt(hiscoreStats[0]);
							int score = Integer.parseInt(hiscoreStats[1]);

							// Rank Check
							if (rank > 0 && score > 0 && rank <= rankThreshold)
							{
								achievementsMap.put(hiscore, "rank " + rank + " in " + hiscore.getName());
							}

							// 200M XP Check
							boolean isSkill = hiscore.getApiIndex() <= Hiscores.CONSTRUCTION.getApiIndex();
							if (alertFor200m && isSkill && hiscore != Hiscores.OVERALL && hiscoreStats.length > 2)
							{
								long xp = Long.parseLong(hiscoreStats[2]);
								if (xp >= MAX_XP)
								{
									if (achievementsMap.containsKey(hiscore))
									{
										// Player is high-ranked AND has 200m xp, append to the existing string.
										String existing = achievementsMap.get(hiscore);
										achievementsMap.put(hiscore, existing + " (200m XP)");
									}
									else
									{
										// Player only has 200m xp, not a notable rank.
										achievementsMap.put(hiscore, "200m XP in " + hiscore.getName());
									}
								}
							}
						}
						catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
						{
							log.warn("Failed to parse line for {} in category {}: {}", playerName, hiscore.getName(), e.getMessage());
						}
					}

					// Process the collected achievements after the loop
					if (!achievementsMap.isEmpty())
					{
						// Pass the values from the map to the alert method
						sendCollapsedAlert(playerName, new ArrayList<>(achievementsMap.values()));
					}
				}
				catch (Exception e)
				{
					log.error("Failed to process hiscores response for player {}: {}", playerName, e.getMessage());
				}
			}
		});
	}

	/**
	 * Sends a single, consolidated alert for a player with one or more achievements.
	 * @param playerName The name of the player.
	 * @param achievements A list of their notable achievements.
	 */
	private void sendCollapsedAlert(String playerName, List<String> achievements)
	{
		// Define a limit for how many achievements to list before summarizing.
		final int MAX_LISTED_ACHIEVEMENTS = 5;
		int achievementCount = achievements.size();

		// Build the simple string for logging
		String logMessage = playerName + " is nearby and is notable for: " + String.join(", ", achievements) + ".";
		log.info("High-ranking player found! {} with achievements: {}", playerName, logMessage);

		clientThread.invoke(() -> {
			// --- COLOR IS NOW CONFIGURABLE ---
			// Get the color from the config at the start of the method.
			final Color alertColor = config.chatColor();

			ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder();
			chatMessageBuilder.append(alertColor, playerName)
					.append(alertColor, " is nearby and is notable for: ");

			if (achievementCount <= MAX_LISTED_ACHIEVEMENTS)
			{
				// List all achievements with proper grammar
				for (int i = 0; i < achievementCount; i++)
				{
					chatMessageBuilder.append(alertColor, achievements.get(i));
					if (i < achievementCount - 2)
					{
						// Comma for items that are not the last two
						chatMessageBuilder.append(alertColor, ", ");
					}
					else if (i == achievementCount - 2)
					{
						// " and " before the last item
						chatMessageBuilder.append(alertColor, " and ");
					}
				}
			}
			else
			{
				// List the first few, then summarize the rest
				int moreCount = achievementCount - (MAX_LISTED_ACHIEVEMENTS - 1);
				for (int i = 0; i < (MAX_LISTED_ACHIEVEMENTS - 1); i++)
				{
					chatMessageBuilder.append(alertColor, achievements.get(i))
							.append(alertColor, ", ");
				}
				chatMessageBuilder.append(alertColor, "... and " + moreCount + " more");
			}

			chatMessageBuilder.append(alertColor, ".");
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMessageBuilder.build(), null);
		});
	}
}
