package com.hiscoreswatch;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provides;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanChannel;
import net.runelite.api.events.ClanChannelChanged;
import net.runelite.api.events.FriendsChatMemberJoined;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ColorUtil;
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
		name = "HiscoresWatch",
		description = "Notifies you when high-ranking players or players with 200m XP are nearby.",
		tags = {"hiscores", "rank", "level", "xp", "player", "alert", "notification", "clan", "friends chat"}
)
public class HiscoresWatchPlugin extends Plugin
{
	// --- Constants ---
	private static final String HISCORES_API_URL = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=";
	private static final long MAX_XP = 200_000_000L;
	private static final int API_REQUEST_DELAY_MS = 500;
	private static final int MAX_LISTED_ACHIEVEMENTS = 5;

	// Constants for configuration management
	private static final String CONFIG_GROUP = "hiscoreswatch";
	private static final String IGNORE_LIST_KEY = "ignoreList";

	/**
	 * An enum representing the source of a player detection event.
	 */
	@Getter
	@RequiredArgsConstructor
	private enum DetectionSource
	{
		NEARBY("is nearby and is notable for: ", false),
		FRIENDS_CHAT("joined your friends chat and is notable for: ", true),
		CLAN_CHAT("joined your clan and is notable for: ", true);

		private final String message;
		private final boolean isPriority;
	}

	/**
	 * A class to hold structured data about a player's achievement for sorting.
	 */
	@Getter
	@Setter
	@AllArgsConstructor
	private static class PlayerAchievement
	{
		private Hiscores hiscore;
		private int rank;
		private boolean has200mXp;

		/**
		 * Converts the achievement object into its final display string.
		 */
		public String toDisplayString()
		{
			// If the rank is notable (not -1), build the rank string.
			if (rank != -1)
			{
				String base = "rank " + rank + " in " + hiscore.getName();
				// If they also have 200m XP, append that.
				if (has200mXp)
				{
					return base + " (200m XP)";
				}
				// Otherwise, just return the rank string.
				return base;
			}
			else
			{
				// If the rank is -1, it means only the 200m XP was notable.
				return "200m XP in " + hiscore.getName();
			}
		}
	}

	/**
	 * A class to hold a player check request for the processing queue.
	 * This replaces the Java 16+ 'record' for Java 11 compatibility.
	 */
	@RequiredArgsConstructor
	@Getter
	private static final class PlayerCheck
	{
		private final String playerName;
		private final DetectionSource source;
	}

	private Cache<String, Boolean> checkedPlayers;
	private Set<String> ignoredPlayers;
	// Keep track of current clan members to detect new joiners
	private final Set<String> clanMembers = new HashSet<>();

	// --- API Throttling Components ---
	private ScheduledExecutorService executor;
	// This is now a Deque to allow adding to the front (high-priority) or back (low-priority)
	private final Deque<PlayerCheck> playerCheckQueue = new ConcurrentLinkedDeque<>();


	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

	@Inject
	private OkHttpClient okHttpClient;

	@Inject
	private ConfigManager configManager;

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

		// --- Start the API Throttling Worker ---
		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(this::processQueue, 2000, API_REQUEST_DELAY_MS, TimeUnit.MILLISECONDS);

		log.info("Hiscores Watch started!");
		clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Hiscores Watch has started.", null));
	}

	@Override
	protected void shutDown() throws Exception
	{
		// --- Stop the API Throttling Worker ---
		if (executor != null)
		{
			executor.shutdown();
			executor = null;
		}
		playerCheckQueue.clear();

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
		clanMembers.clear();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals(CONFIG_GROUP))
		{
			if (event.getKey().equals(IGNORE_LIST_KEY))
			{
				updateIgnoredPlayers();
				log.debug("Hiscores Watch ignore list has been updated.");
			}
		}
	}

	private void updateIgnoredPlayers()
	{
		this.ignoredPlayers = getIgnoredPlayerListFromString(config.ignoreList());
	}

	@Subscribe
	public void onPlayerSpawned(PlayerSpawned event)
	{
		if (!config.checkNearbyPlayers())
		{
			return;
		}

		Player player = event.getPlayer();
		if (player == client.getLocalPlayer())
		{
			return;
		}
		checkPlayer(player.getName(), DetectionSource.NEARBY);
	}

	@Subscribe
	public void onFriendsChatMemberJoined(FriendsChatMemberJoined event)
	{
		if (!config.checkFriendsChat())
		{
			return;
		}

		String playerName = event.getMember().getName();
		if (client.getLocalPlayer() != null && playerName.equals(client.getLocalPlayer().getName()))
		{
			return;
		}
		checkPlayer(playerName, DetectionSource.FRIENDS_CHAT);
	}

	@Subscribe
	public void onClanChannelChanged(ClanChannelChanged event)
	{
		if (!config.checkClanChat())
		{
			return;
		}

		ClanChannel clanChannel = event.getClanChannel();
		if (clanChannel == null)
		{
			clanMembers.clear();
			return;
		}

		// Get the current members from the event
		Set<String> currentMembers = clanChannel.getMembers().stream()
				.map(member -> Text.toJagexName(member.getName()))
				.collect(Collectors.toSet());

		// Use set operations to find who is new
		Set<String> newMembers = new HashSet<>(currentMembers);
		newMembers.removeAll(clanMembers);

		// Check the new members
		for (String newMemberName : newMembers)
		{
			checkPlayer(newMemberName, DetectionSource.CLAN_CHAT);
		}

		// Update the tracked list for the next event
		clanMembers.clear();
		clanMembers.addAll(currentMembers);
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded event)
	{
		// We only care about right-clicking on other players
		int type = event.getType();
		if (type >= MenuAction.PLAYER_FIRST_OPTION.getId() && type <= MenuAction.PLAYER_EIGHTH_OPTION.getId())
		{
			final String targetName = Text.toJagexName(event.getTarget());
			final boolean isIgnored = ignoredPlayers.contains(targetName.toLowerCase());

			// Add the "Ignore" or "Un-ignore" menu entry
			client.createMenuEntry(-1)
					.setOption(isIgnored ? "Un-ignore" : "Ignore")
					.setTarget(ColorUtil.wrapWithColorTag(targetName, config.chatColor()))
					.setType(MenuAction.RUNELITE)
					.onClick(e -> togglePlayerIgnore(targetName, isIgnored));
		}
	}

	/**
	 * Adds a player to the lookup queue if they are not ignored or recently checked.
	 * FC/CC members are prioritized by being added to the front of the queue.
	 *
	 * @param playerName The name of the player to check.
	 * @param source     The source from which the player was detected.
	 */
	private void checkPlayer(String playerName, DetectionSource source)
	{
		if (playerName == null)
		{
			return;
		}

		String sanitizedName = Text.toJagexName(playerName);
		if (ignoredPlayers.contains(sanitizedName.toLowerCase()))
		{
			return;
		}

		// Check if we've already processed this player recently
		if (checkedPlayers.getIfPresent(sanitizedName) != null)
		{
			return;
		}
		// Add to cache immediately to prevent duplicate queue entries
		checkedPlayers.put(sanitizedName, true);

		PlayerCheck playerCheck = new PlayerCheck(sanitizedName, source);

		// Prioritize social notifications over nearby players by asking the source
		if (source.isPriority())
		{
			playerCheckQueue.addFirst(playerCheck);
		}
		else
		{
			playerCheckQueue.addLast(playerCheck);
		}
	}

	/**
	 * Processes one player from the queue, called by the scheduled executor.
	 */
	private void processQueue()
	{
		PlayerCheck playerCheck = playerCheckQueue.poll();
		if (playerCheck != null)
		{
			fetchHiscores(playerCheck.getPlayerName(), playerCheck.getSource());
		}
	}

	private void fetchHiscores(String playerName, DetectionSource source)
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

					// Use a map to easily combine rank and 200m XP for the same skill
					final Map<Hiscores, PlayerAchievement> achievementMap = new LinkedHashMap<>();

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
								achievementMap.put(hiscore, new PlayerAchievement(hiscore, rank, false));
							}

							// 200M XP Check - Using the robust isSkill() method from the enum
							if (alertFor200m && hiscore.isSkill() && hiscore != Hiscores.OVERALL && hiscoreStats.length > 2)
							{
								long xp = Long.parseLong(hiscoreStats[2]);
								if (xp >= MAX_XP)
								{
									PlayerAchievement existing = achievementMap.get(hiscore);
									if (existing != null)
									{
										existing.setHas200mXp(true);
									}
									else
									{
										// Player only has 200m xp, not a notable rank.
										// Use -1 rank to signify this special case.
										achievementMap.put(hiscore, new PlayerAchievement(hiscore, -1, true));
									}
								}
							}
						}
						catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
						{
							log.warn("Failed to parse line for {} in category {}: {}", playerName, hiscore.getName(), e.getMessage());
						}
					}

					if (!achievementMap.isEmpty())
					{
						// Convert map values to a list for sorting
						List<PlayerAchievement> achievements = new ArrayList<>(achievementMap.values());

						// --- ENHANCED SORTING LOGIC ---
						achievements.sort(Comparator
								// 1. Prioritize "Overall" rank above all else.
								.comparing((PlayerAchievement a) -> a.getHiscore() != Hiscores.OVERALL)
								// 2. Prioritize achievements with a valid rank over 200m-only ones.
								.thenComparing((PlayerAchievement a) -> a.getRank() == -1)
								// 3. Finally, sort by rank number ascending (lower is better).
								.thenComparingInt(PlayerAchievement::getRank)
						);

						sendCollapsedAlert(playerName, achievements, source);
					}
				}
				catch (Exception e)
				{
					log.error("Failed to process hiscores response for player {}: {}", playerName, e.getMessage());
				}
			}
		});
	}

	private void sendCollapsedAlert(String playerName, List<PlayerAchievement> achievements, DetectionSource source)
	{
		// Convert the sorted list of objects to a list of display strings
		List<String> achievementStrings = achievements.stream()
				.map(PlayerAchievement::toDisplayString)
				.collect(Collectors.toList());

		int achievementCount = achievementStrings.size();

		String logMessage = playerName + " " + source.getMessage() + String.join(", ", achievementStrings) + ".";
		log.info("High-ranking player found! {}", logMessage);

		clientThread.invoke(() -> {
			final Color alertColor = config.chatColor();

			ChatMessageBuilder chatMessageBuilder = new ChatMessageBuilder();
			chatMessageBuilder.append(alertColor, playerName)
					.append(alertColor, " " + source.getMessage());

			if (achievementCount <= MAX_LISTED_ACHIEVEMENTS)
			{
				// Simplified message joining logic
				if (achievementCount > 1)
				{
					String mostAchievements = String.join(", ", achievementStrings.subList(0, achievementCount - 1));
					chatMessageBuilder.append(alertColor, mostAchievements)
							.append(alertColor, " and ")
							.append(alertColor, achievementStrings.get(achievementCount - 1));
				}
				else
				{
					chatMessageBuilder.append(alertColor, achievementStrings.get(0));
				}
			}
			else
			{
				int moreCount = achievementCount - (MAX_LISTED_ACHIEVEMENTS - 1);
				String listedAchievements = String.join(", ", achievementStrings.subList(0, MAX_LISTED_ACHIEVEMENTS - 1));
				chatMessageBuilder.append(alertColor, listedAchievements)
						.append(alertColor, ", ... and " + moreCount + " more");
			}

			chatMessageBuilder.append(alertColor, ".");
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", chatMessageBuilder.build(), null);
		});
	}

	/**
	 * A helper method to handle the logic for ignoring or un-ignoring a player.
	 *
	 * @param playerName The name of the player to toggle.
	 * @param isIgnored  True if the player is currently on the ignore list.
	 */
	private void togglePlayerIgnore(String playerName, boolean isIgnored)
	{
		Set<String> ignoreSet = getIgnoredPlayerListFromString(config.ignoreList());

		if (isIgnored)
		{
			ignoreSet.remove(playerName.toLowerCase());
		}
		else
		{
			ignoreSet.add(playerName.toLowerCase());
		}

		// Save the updated list back to the config, which will trigger onConfigChanged
		String newIgnoreList = String.join(",", ignoreSet);
		configManager.setConfiguration(CONFIG_GROUP, IGNORE_LIST_KEY, newIgnoreList);
	}

	/**
	 * Parses the comma-separated config string into a modifiable set of player names.
	 *
	 * @param ignoreListString The string from the config.
	 * @return A modifiable set of lower-case player names.
	 */
	private Set<String> getIgnoredPlayerListFromString(String ignoreListString)
	{
		return new HashSet<>(Arrays.asList(ignoreListString.toLowerCase().split(",")))
				.stream()
				.map(String::trim)
				.filter(name -> !name.isEmpty())
				.collect(Collectors.toSet());
	}
}
