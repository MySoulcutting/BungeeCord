package net.md_5.bungee.balancer;

import java.util.Locale;
import net.md_5.bungee.api.config.LoadBalancer;

/**
 * Factory for creating load balancer instances from configuration.
 */
public final class LoadBalancerFactory
{

    private LoadBalancerFactory()
    {
    }

    /**
     * Create a load balancer by strategy name.
     *
     * @param strategy the strategy name (FIRST, LOWEST, RANDOM)
     * @return the load balancer instance
     */
    public static LoadBalancer create(String strategy)
    {
        switch ( strategy.toUpperCase( Locale.ROOT ) )
        {
            case "LOWEST":
                return new LowestPlayerBalancer();
            case "RANDOM":
                return new RandomBalancer();
            case "FIRST":
            default:
                return new FirstAvailableBalancer();
        }
    }
}
