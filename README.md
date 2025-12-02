# Hiscores Watch

This plugin notifies you in chat when a high-ranking player or a player with 200m XP in a skill is detected. It is designed to be informative but not intrusive, with intelligent alerts and multiple detection sources.

## Features

*   **Multi-Source Detection:** Automatically checks players who appear nearby, join your Friends Chat, or join your Clan Channel.
*   **Priority Queue:** Notifications for players joining your Friends Chat or Clan Channel are prioritized to ensure you get immediate alerts, even in crowded areas.
*   **Intelligent Alerts:** Achievements are sorted by impressiveness (Overall first, then by rank). Multiple achievements from one player are collapsed into a single, clean message.
*   **Rank-Based & 200m XP Alerts:** Get notified for players with a notable rank (configurable threshold) or those who have achieved the maximum 200,000,000 XP in any skill.
*   **Responsible API Usage:** Uses a throttled queue system to send hiscores requests at a safe, controlled rate, preventing API spam.
*   **Customizable Alerts:** Configure the alert color, rank threshold, and which detection sources are active.

## Configuration

The plugin offers several options to tailor the alerts to your liking:

*   **Rank Threshold:** Sets the rank at or below which a player will trigger an alert.
*   **Alert for 200m XP:** Toggles whether to alert for players with 200m XP in a skill.
*   **Check Nearby/Friends Chat/Clan Chat:** Individually toggle which player sources you want to monitor.
*   **Ignore List:** A comma-separated list of player names to ignore.
*   **Alert Color:** Opens a color picker to set the color of the chat message alert.
