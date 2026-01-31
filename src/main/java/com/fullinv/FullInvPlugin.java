package com.fullinv;

import com.google.inject.Provides;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.Locale;

@Slf4j
@PluginDescriptor(
    name = "Full Inv",
    description = "Highlights the lowest GE value item when inventory becomes full",
    tags = {"inventory", "full", "highlight", "value", "ge", "price"}
)
public class FullInvPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private FullInvConfig config;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private FullInvOverlay overlay;

    private boolean wasInventoryFull = false;

    @Getter
    private int highlightSlot = -1;

    @Getter
    private Instant highlightStartTime;

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
        wasInventoryFull = false;
        highlightSlot = -1;
        highlightStartTime = null;
        log.info("Full Inv plugin started");
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
        wasInventoryFull = false;
        highlightSlot = -1;
        highlightStartTime = null;
        log.info("Full Inv plugin stopped");
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getContainerId() != InventoryID.INVENTORY.getId())
        {
            return;
        }

        ItemContainer inventory = event.getItemContainer();
        if (inventory == null)
        {
            return;
        }

        Item[] items = inventory.getItems();
        int filledSlots = countFilledSlots(items);
        boolean isNowFull = filledSlots >= 28;

        if (!wasInventoryFull && isNowFull)
        {
            onInventoryBecameFull(items);
        }
        else if (wasInventoryFull && !isNowFull)
        {
            highlightSlot = -1;
            highlightStartTime = null;
        }

        wasInventoryFull = isNowFull;
    }

    private int countFilledSlots(Item[] items)
    {
        int count = 0;
        for (Item item : items)
        {
            if (item != null && item.getId() != -1)
            {
                count++;
            }
        }
        return count;
    }

    private void onInventoryBecameFull(Item[] items)
    {
        int lowestSlot = -1;
        long lowestValue = Long.MAX_VALUE;
        String lowestItemName = null;

        boolean useAlchValue = config.ironmanMode();

        for (int i = 0; i < items.length; i++)
        {
            Item item = items[i];
            if (item == null || item.getId() == -1)
            {
                continue;
            }

            int itemId = item.getId();
            int quantity = item.getQuantity();
            ItemComposition itemComp = itemManager.getItemComposition(itemId);
            int price = useAlchValue ? itemComp.getHaPrice() : itemManager.getItemPrice(itemId);

            if (!config.includeUntradeables() && price == 0)
            {
                continue;
            }

            long totalValue = (long) price * quantity;

            if (totalValue < lowestValue)
            {
                lowestValue = totalValue;
                lowestSlot = i;
                lowestItemName = itemComp.getName();
            }
        }

        if (lowestSlot == -1)
        {
            return;
        }

        highlightSlot = lowestSlot;
        highlightStartTime = Instant.now();
        log.info("Highlight set for slot {} item {}", lowestSlot, lowestItemName);

        if (config.showChatMessage() && lowestItemName != null)
        {
            sendChatMessage(lowestItemName, lowestValue, useAlchValue);
        }
    }

    private void sendChatMessage(String itemName, long value, boolean isAlchValue)
    {
        String formattedValue = NumberFormat.getNumberInstance(Locale.US).format(value);
        String valueLabel = isAlchValue ? "HA Value " : "GE Average ";

        final String chatMessage = new ChatMessageBuilder()
            .append(ChatColorType.HIGHLIGHT)
            .append("[Full Inv] ")
            .append(ChatColorType.NORMAL)
            .append("Lowest value: ")
            .append(ChatColorType.HIGHLIGHT)
            .append(itemName)
            .append(ChatColorType.NORMAL)
            .append(" (" + valueLabel + formattedValue + ")")
            .build();

        chatMessageManager.queue(QueuedMessage.builder()
            .type(ChatMessageType.CONSOLE)
            .runeLiteFormattedMessage(chatMessage)
            .build());
    }

    public boolean shouldShowHighlight()
    {
        if (!config.enableHighlight())
        {
            return false;
        }

        if (highlightSlot == -1 || highlightStartTime == null)
        {
            return false;
        }

        int durationSec = config.highlightDurationSec();
        if (durationSec == 0)
        {
            return true;
        }

        long elapsedMs = Instant.now().toEpochMilli() - highlightStartTime.toEpochMilli();
        long durationMs = durationSec * 1000L;
        if (elapsedMs > durationMs)
        {
            highlightSlot = -1;
            highlightStartTime = null;
            return false;
        }

        return true;
    }

    @Provides
    FullInvConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(FullInvConfig.class);
    }
}
