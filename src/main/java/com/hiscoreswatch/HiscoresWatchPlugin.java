package com.hiscoreswatch;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Provides;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
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

	private Cache<String, Boolean> checkedPlayers;

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
				// Use a try-with-resources block to ensure the response is always closed
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

					// --- CORE LOGIC CHANGE ---
					// Loop through every hiscore category instead of just one.
					for (Hiscores hiscore : Hiscores.values())
					{
						int apiIndex = hiscore.getApiIndex();

						// Ensure the response is long enough for the current category
						if (stats.length <= apiIndex)
						{
							// All subsequent categories will also be out of bounds, so we can stop.
							log.warn("Hiscores response for {} was too short. Stopping check at {}.", playerName, hiscore.getName());
							break;
						}

						String hiscoreData = stats[apiIndex];
						if (hiscoreData.isEmpty() || !hiscoreData.contains(","))
						{
							// Skip malformed lines
							continue;
						}

						try
						{
							String[] hiscoreStats = hiscoreData.split(",");
							int rank = Integer.parseInt(hiscoreStats[0]);
							int score = Integer.parseInt(hiscoreStats[1]);

							// Check if the player is ranked and meets the threshold
							if (rank > 0 && score > 0 && rank <= rankThreshold)
							{
								log.info("High-ranking player found! {} is rank {} in {}", playerName, rank, hiscore.getName());

								// Final variables for use in the lambda
								final int finalRank = rank;
								final Hiscores finalHiscore = hiscore;

								clientThread.invoke(() -> {
									String message = String.format("%s is nearby and is rank %d in %s!",
											playerName, finalRank, finalHiscore.getName());
									client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
									notifier.notify(message);
								});
							}
						}
						catch (NumberFormatException | ArrayIndexOutOfBoundsException e)
						{
							log.warn("Failed to parse line for {} in category {}: {}", playerName, hiscore.getName(), e.getMessage());
						}
					}
				}
				catch (Exception e)
				{
					log.error("Failed to process hiscores response for player {}: {}", playerName, e.getMessage());
				}
			}
		});
	}
}
