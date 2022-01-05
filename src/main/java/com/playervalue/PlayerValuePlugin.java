package com.playervalue;

import com.google.inject.Provides;

import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;


@Slf4j
@PluginDescriptor(
	name = "PlayerValue"
)
public class PlayerValuePlugin extends Plugin
{
	private String playerValue = "MY PLAYER VALUE";
	private Color textColor = Color.YELLOW;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PlayerValueConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private PlayerValueOverlay overlay;

	@Inject
	private ItemManager itemManager;



	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Nullable
	public String getPlayerValue() {return playerValue;}

	public Color getTextColor() {return textColor;}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		final ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
		final Item[] children = inventoryContainer.getItems();
		long geTotal = 0;

		if(inventoryContainer != null && children != null)
		{
			for (int i = 0; i < inventoryContainer.size(); ++i)
			{
				Item child = children[i];
				if (child != null && child.getId() > -1)
				{
					final int value = itemManager.getItemPrice(child.getId()) * child.getQuantity(); // must be used in subscribed method
					geTotal = geTotal + value;
				}
			}
			String value = formatPlayerValue(geTotal);
			setPlayerValue(value);
		}
	}

	public String formatPlayerValue(long value)
	{
		long f = 0;
		String text="";

		if(value >= 10000000)
		{
			f = value / 1000000;
			text = Long.toString(f) + "M";
			textColor = Color.GREEN;
		}
		else if(value >= 100000)
		{
			f = value / 1000;
			text = Long.toString(f) + "K";
			textColor = Color.WHITE;
		}
		else
		{
			text = Long.toString(value) + "gp";
			textColor = Color.YELLOW;
		}

		return text;
	}

	public void setPlayerValue(String value)
	{
		this.playerValue = "Inventory: " + value;
	}

	@Provides
	PlayerValueConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayerValueConfig.class);
	}
}
