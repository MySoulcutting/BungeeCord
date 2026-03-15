package net.md_5.bungee.api.event;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Event;

/**
 * Called when a server is added to the proxy.
 */
@Getter
@ToString(callSuper = false)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ServerAddEvent extends Event
{

    /**
     * The server that was added.
     */
    private final ServerInfo server;
}
