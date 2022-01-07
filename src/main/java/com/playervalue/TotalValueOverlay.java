package com.playervalue;

import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

public class TotalValueOverlay extends OverlayPanel {
    private PlayerValuePlugin plugin;
    private PlayerValueConfig config;
    private Client client;
    private ItemManager itemManager;

    @Inject
    private TotalValueOverlay(PlayerValuePlugin plugin, PlayerValueConfig config)
    {
        super(plugin);
        setPosition(OverlayPosition.BOTTOM_RIGHT);
        this.plugin = plugin;
        this.config = config;
        // not sure what this is for
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Value overlay"));
    }

    // needed to render and configure overlay
    public Dimension render(Graphics2D graphics)
    {
        String playerValue = plugin.getTotalValue();
        Color textColor = plugin.getTotalColor();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text(playerValue)
                .color(textColor)
                .build());

        panelComponent.setPreferredSize(new Dimension(
                graphics.getFontMetrics().stringWidth(playerValue) + 10,
                0));

        return super.render(graphics);
    }

}
