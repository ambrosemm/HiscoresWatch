package com.hiscoreswatch;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import net.runelite.client.eventbus.Subscribe; // <-- The new missing import
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@Slf4j
@PluginDescriptor(
		name = "Hiscores Watch"
)
public class HiscoresWatchPlugin extends Plugin
{
	private static final String HISCORES_API_URL = "https://secure.runescape.com/m=hiscore_oldschool/index_lite.ws?player=";
	private static final int MIN_LEVEL_THRESHOLD = 2000;

	// A cache to avoid looking up the same player repeatedly.
	// It will store player names for 5 minutes.
	private Cache<String, Boolean> checkedPlayers;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private Notifier notifier;

	@Inject
	private OkHttpClient okHttpClient;

	@Override
	protected void startUp() throws Exception
	{
		// Initialize the cache on startup
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
			checkedPlayers.invalidateAll(); // Clear the cache
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

		// Sanitize the player name for caching and API calls
		String sanitizedName = Text.toJagexName(playerName);

		// If we have already checked this player recently, don't do it again.
		if (checkedPlayers.getIfPresent(sanitizedName) != null)
		{
			return;
		}

		// Add the player to the cache immediately to prevent duplicate requests
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
				try
				{
					if (!response.isSuccessful())
					{
						// This is expected for unranked players (HTTP 404)
						log.debug("Unsuccessful hiscores response for {}. Code: {}", playerName, response.code());
						return;
					}

					final String body = response.body().string();
					String[] stats = body.split("\n");

					if (stats.length < 1 || stats[0].isEmpty() || !stats[0].contains(","))
					{
						log.warn("Hiscores response for {} was malformed.", playerName);
						return;
					}

					String[] overallStats = stats[0].split(",");
					int overallLevel = Integer.parseInt(overallStats[1]);

					log.debug("Parsed total level for {}: {}", playerName, overallLevel);

					if (overallLevel > MIN_LEVEL_THRESHOLD)
					{
						log.info("High-level player found! {}: Total Level {}", playerName, overallLevel);
						String message = String.format("%s is nearby with a total level of %d!", playerName, overallLevel);

						// Schedule the chat message and notification to run on the main game thread
						clientThread.invoke(() -> {
							client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
							notifier.notify(message);
						});
					}
				}
				catch (Exception e)
				{
					log.error("Failed to parse hiscores for player {}: {}", playerName, e.getMessage());
				}
				finally
				{
					// Always close the response to free resources
					response.close();
				}
			}
		});
	}
}
