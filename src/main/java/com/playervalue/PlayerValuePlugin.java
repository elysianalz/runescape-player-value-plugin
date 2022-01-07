package com.playervalue;

import com.google.inject.Provides;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

@Slf4j
@PluginDescriptor(
	name = "PlayerValue"
)
public class PlayerValuePlugin extends Plugin
{
	private String inventoryValue = "MY INV VALUE";
	private String equipmentValue = "MY EQUIP VALUE";
	private String riskValue = "MY RISK VALUE";
	private String totalValue = "MY TOTAL VALUE";
	private Color inventoryColor = Color.YELLOW;
	private Color equipmentColor = Color.WHITE;
	private Color riskColor = Color.GREEN;
	private ArrayList<Long> combinedValues = new ArrayList<Long>();

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private PlayerValueConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InventoryValueOverlay overlay;

	@Inject
	private EquipmentValueOverlay eOverlay;

	@Inject
	private RiskValueOverlay rOverlay;

	@Inject
	private ItemManager itemManager;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(eOverlay);
		overlayManager.add(rOverlay);
	}

	@Nullable
	public String getInventoryValue() {return inventoryValue;}

	@Nullable
	public String getEquipmentValue() {return equipmentValue;}

	@Nullable
	public String getRiskValue() {return riskValue;}

	public Color getInventoryColor() {return inventoryColor;}

	public Color getEquipmentColor() {return equipmentColor;}

	public Color getRiskColor() {return riskColor;}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		updateInventoryValue();
		updateEquipmentValue();
		updateRiskValue();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if(event.getGameState() == GameState.LOGGED_IN)
		{
			updateInventoryValue();
			updateEquipmentValue();
			updateRiskValue();
		}
	}

	public void updateInventoryValue()
	{
		long geTotal = 0;
		final ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);

		if (inventoryContainer.size() > 0 && inventoryContainer != null && inventoryContainer.getItems().length != 0);
		{
			final Item[] children = inventoryContainer.getItems();

			for (int i = 0; i < inventoryContainer.size(); ++i)
			{
				Item child = children[i];
				if (child != null && child.getId() > -1)
				{
					final long value = itemManager.getItemPrice(child.getId()) * child.getQuantity(); // must be used in subscribed method
					geTotal = geTotal + value;
					combinedValues.add(value);
				}
			}
		}
		String value = formatInventoryValue(geTotal);
		setInventoryValue(value);
	}

	public void updateEquipmentValue()
	{
		long geTotal = 0;
		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);

		if (equipmentContainer != null && equipmentContainer.size() > 0 && equipmentContainer.getItems().length != 0)
		{
			final Item[] children = equipmentContainer.getItems();

			for (int i = 0; i < equipmentContainer.size(); ++i)
			{
				Item child = children[i];
				if (child != null && child.getId() > -1)
				{
					final long value = itemManager.getItemPrice(child.getId()) * child.getQuantity();
					geTotal += value;
					combinedValues.add(value);
				}
			}
		}
		String value = formatEquipmentValue(geTotal);
		setEquipmentValue(value);
	}

	public void updateRiskValue()
	{
		long riskTotal = 0;
		long risk = 0;

		if(combinedValues.size() >= 3)
		{
			Collections.sort(combinedValues, Collections.reverseOrder());

			for (Long val : combinedValues)
			{
				riskTotal += val;
			}

			risk = combinedValues.get(0) + combinedValues.get(1) + combinedValues.get(2);
			riskTotal -= risk;
		}

		String value = formatRiskValue(riskTotal);
		setRiskValue(value);
		combinedValues.clear();
	}

	public void updateTotalValue()
	{

	}

	public String formatInventoryValue(long value)
	{
		long f = 0;
		String text="";

		if(value >= 10000000)
		{
			f = value / 1000000;
			text = Long.toString(f) + "M";
			inventoryColor = Color.GREEN;
		}
		else if(value >= 100000)
		{
			f = value / 1000;
			text = Long.toString(f) + "K";
			inventoryColor = Color.WHITE;
		}
		else
		{
			text = Long.toString(value) + "gp";
			inventoryColor = Color.YELLOW;
		}

		return text;
	}

	public String formatEquipmentValue(long value)
	{
		long f = 0;
		String text="";

		if(value >= 10000000)
		{
			f = value / 1000000;
			text = Long.toString(f) + "M";
			equipmentColor = Color.GREEN;
		}
		else if(value >= 100000)
		{
			f = value / 1000;
			text = Long.toString(f) + "K";
			equipmentColor = Color.WHITE;
		}
		else
		{
			text = Long.toString(value) + "gp";
			equipmentColor = Color.YELLOW;
		}

		return text;
	}

	public String formatRiskValue(long value)
	{
		long f = 0;
		String text="";

		if(value >= 10000000)
		{
			f = value / 1000000;
			text = Long.toString(f) + "M";
			riskColor = Color.GREEN;
		}
		else if(value >= 100000)
		{
			f = value / 1000;
			text = Long.toString(f) + "K";
			riskColor = Color.WHITE;
		}
		else
		{
			text = Long.toString(value) + "gp";
			riskColor = Color.YELLOW;
		}

		return text;
	}

	public void setInventoryValue(String value)
	{
		this.inventoryValue = "Inventory: " + value;
	}

	public void setEquipmentValue(String value)
	{
		this.equipmentValue = "Equip: " + value;
	}

	public void setRiskValue(String value)
	{
		this.riskValue = "Risk: " + value;
	}

	@Provides
	PlayerValueConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayerValueConfig.class);
	}
}
