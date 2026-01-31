package com.fullinv;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("fullinv")
public interface FullInvConfig extends Config
{
    @ConfigItem(
        keyName = "enableHighlight",
        name = "Enable Highlight",
        description = "Toggle the red box highlight on/off",
        position = 1
    )
    default boolean enableHighlight()
    {
        return true;
    }

    @ConfigItem(
        keyName = "highlightDurationSec",
        name = "Highlight Duration (sec)",
        description = "How long the highlight lasts in seconds. Set to 0 for persistent highlight.",
        position = 2
    )
    default int highlightDurationSec()
    {
        return 3;
    }

    @ConfigItem(
        keyName = "showChatMessage",
        name = "Show Chat Message",
        description = "Toggle the chat notification on/off",
        position = 3
    )
    default boolean showChatMessage()
    {
        return true;
    }

    @ConfigItem(
        keyName = "includeUntradeables",
        name = "Include Untradeables",
        description = "Include 0 GP items in lowest value check",
        position = 4
    )
    default boolean includeUntradeables()
    {
        return true;
    }

    @ConfigItem(
        keyName = "ironmanMode",
        name = "Ironman Mode",
        description = "Use high alch value instead of GE value",
        position = 5
    )
    default boolean ironmanMode()
    {
        return false;
    }
}
