package com.fullinv;

import net.runelite.api.Client;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class FullInvOverlay extends Overlay
{
    private final Client client;
    private final FullInvPlugin plugin;

    @Inject
    FullInvOverlay(Client client, FullInvPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.shouldShowHighlight())
        {
            return null;
        }

        int slot = plugin.getHighlightSlot();
        if (slot < 0 || slot >= 28)
        {
            return null;
        }

        Widget inventoryWidget = client.getWidget(ComponentID.INVENTORY_CONTAINER);
        if (inventoryWidget == null || inventoryWidget.isHidden())
        {
            return null;
        }

        Widget[] children = inventoryWidget.getDynamicChildren();
        if (children == null || slot >= children.length)
        {
            return null;
        }

        Widget itemWidget = children[slot];
        if (itemWidget == null)
        {
            return null;
        }

        Rectangle bounds = itemWidget.getBounds();
        if (bounds == null)
        {
            return null;
        }

        graphics.setColor(new Color(255, 0, 0, 200));
        graphics.setStroke(new BasicStroke(2));
        graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

        return null;
    }
}
