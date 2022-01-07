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
import java.util.ArrayList;
import java.util.Collections;

@Slf4j
@PluginDescriptor(
	name = "PlayerValue"
)
public class PlayerValuePlugin extends Plugin
{
	// constants
	private final String SPACE = " ";
	private final String INVENTORY = "Inventory:";
	private final String EQUIP = "Equip:";
	private final String TOTAL = "Total:";
	private final String RISK = "Risk:";
	private final int EMPTY = 0;
	private final int ZERO = 0;
	private final int ITEM_ONE = 0;
	private final int ITEM_TWO = 1;
	private final int ITEM_THREE = 2;
	private final int RISK_BUFFER = 3;
	private final int BAD_ID = -1;

	private String inventoryValue = "MY INV VALUE";
	private String equipmentValue = "MY EQUIP VALUE";
	private String riskValue = "MY RISK VALUE";
	private String totalValue = "MY TOTAL VALUE";
	private Color inventoryColor = Color.YELLOW;
	private Color equipmentColor = Color.WHITE;
	private Color riskColor = Color.GREEN;
	private Color totalColor = Color.YELLOW;
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
	private TotalValueOverlay tOverlay;

	@Inject
	private ItemManager itemManager;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		overlayManager.add(eOverlay);
		overlayManager.add(rOverlay);
		overlayManager.add(tOverlay);
	}

	@Nullable
	public String getInventoryValue() { return inventoryValue; }

	@Nullable
	public String getEquipmentValue() { return equipmentValue; }

	@Nullable
	public String getRiskValue() { return riskValue; }

	@Nullable
	public String getTotalValue() { return totalValue; }

	public Color getInventoryColor() { return inventoryColor; }

	public Color getEquipmentColor() { return equipmentColor; }

	public Color getRiskColor() { return riskColor; }

	public Color getTotalColor() { return totalColor; }

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
		updateTotalValue();
		updateRiskValue();
	}

	public void updateInventoryValue()
	{
		long geTotal = ZERO;

		try {
			final ItemContainer inventoryContainer = client.getItemContainer(InventoryID.INVENTORY);
			final Item[] children = inventoryContainer.getItems();

			if (inventoryContainer != null && children != null);
			{
				for (int i = 0; i < inventoryContainer.size(); ++i)
				{
					Item child = children[i];
					if (child != null && child.getId() > BAD_ID)
					{
						final long value = itemManager.getItemPrice(child.getId()) * child.getQuantity(); // must be used in subscribed method
						geTotal = geTotal + value;
						combinedValues.add(value);
					}
				}
			}
		} catch (NullPointerException e){}

		ValueFormatter value = formatValue(geTotal);
		setInventoryValue(value.formattedString);
		this.inventoryColor = value.color;
	}

	public void updateEquipmentValue()
	{
		long geTotal = ZERO;
		ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);

		if (equipmentContainer != null)
		{
			final Item[] children = equipmentContainer.getItems();

			for (int i = 0; i < equipmentContainer.size(); ++i)
			{
				Item child = children[i];
				if (child != null && child.getId() > BAD_ID)
				{
					final long value = itemManager.getItemPrice(child.getId()) * child.getQuantity();
					geTotal += value;
					combinedValues.add(value);
				}
			}
		}
		ValueFormatter value = formatValue(geTotal);
		setEquipmentValue(value.formattedString);
		this.equipmentColor= value.color;
	}

	public void updateRiskValue()
	{
		long riskTotal = ZERO;
		long risk = ZERO;

		if(combinedValues.size() >= RISK_BUFFER)
		{
			Collections.sort(combinedValues, Collections.reverseOrder());

			for (Long val : combinedValues)
			{
				riskTotal += val;
			}

			risk = combinedValues.get(ITEM_ONE) + combinedValues.get(ITEM_TWO) + combinedValues.get(ITEM_THREE);
			riskTotal -= risk;
		}

		ValueFormatter value = formatValue(riskTotal);
		setRiskValue(value.formattedString);
		this.riskColor = value.color;
		combinedValues.clear(); // temporary fix needs refactor
	}

	public void updateTotalValue()
	{
		long val = ZERO;
		if(combinedValues.size() > EMPTY)
		{
			for (long v : combinedValues)
			{
				val += v;
			}
		}

		ValueFormatter value = formatValue(val);
		setTotalValue(value.formattedString);
		this.totalColor = value.color;
	}

	public ValueFormatter formatValue(long value)
	{
		ValueFormatter vf = new ValueFormatter(value);
		return vf;
	}

	public void setInventoryValue(String value)
	{
		this.inventoryValue = INVENTORY + SPACE + value;
	}

	public void setEquipmentValue(String value)
	{
		this.equipmentValue = EQUIP + SPACE + value;
	}

	public void setRiskValue(String value)
	{
		this.riskValue = RISK + SPACE + value;
	}

	public void setTotalValue(String value)
	{
		this.totalValue = TOTAL + SPACE + value;
	}

	@Provides
	PlayerValueConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(PlayerValueConfig.class);
	}
}
