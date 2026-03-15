package net.md_5.bungee.balancer;

import java.util.List;
import java.util.Objects;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.LoadBalancer;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Selects the server with the fewest online players from the priority list.
 */
public class LowestPlayerBalancer implements LoadBalancer
{

    @Override
    public ServerInfo select(List<String> priorities, ServerInfo currentServer)
    {
        ServerInfo best = null;
        int lowestCount = Integer.MAX_VALUE;

        for ( String name : priorities )
        {
            ServerInfo candidate = ProxyServer.getInstance().getServerInfo( name );
            if ( candidate != null && !Objects.equals( currentServer, candidate ) )
            {
                int count = candidate.getPlayers().size();
                if ( count < lowestCount )
                {
                    lowestCount = count;
                    best = candidate;
                }
            }
        }
        return best;
    }
}
