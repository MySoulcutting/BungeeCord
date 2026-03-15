package net.md_5.bungee.api.config;

import java.util.List;

/**
 * Strategy for selecting a server from a priority list.
 */
public interface LoadBalancer
{

    /**
     * Select a server from the given priority list, excluding the current
     * server.
     *
     * @param priorities the ordered list of server names to choose from
     * @param currentServer the server to exclude (may be null)
     * @return the selected server info, or null if no suitable server found
     */
    ServerInfo select(List<String> priorities, ServerInfo currentServer);
}
