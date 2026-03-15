package net.md_5.bungee.balancer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.LoadBalancer;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Selects a random server from the priority list.
 */
public class RandomBalancer implements LoadBalancer
{

    @Override
    public ServerInfo select(List<String> priorities, ServerInfo currentServer)
    {
        List<ServerInfo> candidates = new ArrayList<>();
        for ( String name : priorities )
        {
            ServerInfo candidate = ProxyServer.getInstance().getServerInfo( name );
            if ( candidate != null && !Objects.equals( currentServer, candidate ) )
            {
                candidates.add( candidate );
            }
        }

        if ( candidates.isEmpty() )
        {
            return null;
        }
        return candidates.get( ThreadLocalRandom.current().nextInt( candidates.size() ) );
    }
}
