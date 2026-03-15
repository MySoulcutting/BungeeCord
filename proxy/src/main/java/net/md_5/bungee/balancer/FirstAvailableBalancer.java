package net.md_5.bungee.balancer;

import java.util.List;
import java.util.Objects;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.LoadBalancer;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Default strategy: pick the first available server in priority order.
 * This matches the original BungeeCord behavior.
 */
public class FirstAvailableBalancer implements LoadBalancer
{

    @Override
    public ServerInfo select(List<String> priorities, ServerInfo currentServer)
    {
        for ( String name : priorities )
        {
            ServerInfo candidate = ProxyServer.getInstance().getServerInfo( name );
            if ( candidate != null && !Objects.equals( currentServer, candidate ) )
            {
                return candidate;
            }
        }
        return null;
    }
}
