package net.md_5.bungee;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionThrottle
{

    private final LoadingCache<InetAddress, AtomicInteger> throttle;
    private final LoadingCache<String, AtomicInteger> cidrThrottle;
    private final int throttleLimit;
    private final int cidrLimit;
    private final int cidrSize;

    public ConnectionThrottle(int throttleTime, int throttleLimit)
    {
        this( Ticker.systemTicker(), throttleTime, throttleLimit, -1, 24 );
    }

    public ConnectionThrottle(int throttleTime, int throttleLimit, int cidrLimit, int cidrSize)
    {
        this( Ticker.systemTicker(), throttleTime, throttleLimit, cidrLimit, cidrSize );
    }

    @VisibleForTesting
    ConnectionThrottle(Ticker ticker, int throttleTime, int throttleLimit, int cidrLimit, int cidrSize)
    {
        this.throttleLimit = throttleLimit;
        this.cidrLimit = cidrLimit;
        this.cidrSize = cidrSize;

        CacheLoader<Object, AtomicInteger> loader = new CacheLoader<Object, AtomicInteger>()
        {
            @Override
            public AtomicInteger load(Object key) throws Exception
            {
                return new AtomicInteger();
            }
        };

        this.throttle = CacheBuilder.newBuilder()
                .ticker( ticker )
                .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
                .initialCapacity( 100 )
                .expireAfterWrite( throttleTime, TimeUnit.MILLISECONDS )
                .build( (CacheLoader<InetAddress, AtomicInteger>) (CacheLoader) loader );

        this.cidrThrottle = CacheBuilder.newBuilder()
                .ticker( ticker )
                .concurrencyLevel( Runtime.getRuntime().availableProcessors() )
                .initialCapacity( 100 )
                .expireAfterWrite( throttleTime, TimeUnit.MILLISECONDS )
                .build( (CacheLoader<String, AtomicInteger>) (CacheLoader) loader );
    }

    public void unthrottle(SocketAddress socketAddress)
    {
        if ( !( socketAddress instanceof InetSocketAddress ) )
        {
            return;
        }

        InetAddress address = ( (InetSocketAddress) socketAddress ).getAddress();
        AtomicInteger throttleCount = throttle.getIfPresent( address );
        if ( throttleCount != null )
        {
            throttleCount.decrementAndGet();
        }

        if ( cidrLimit > 0 )
        {
            String subnet = getSubnet( address );
            if ( subnet != null )
            {
                AtomicInteger cidrCount = cidrThrottle.getIfPresent( subnet );
                if ( cidrCount != null )
                {
                    cidrCount.decrementAndGet();
                }
            }
        }
    }

    public boolean throttle(SocketAddress socketAddress)
    {
        if ( !( socketAddress instanceof InetSocketAddress ) )
        {
            return false;
        }

        InetAddress address = ( (InetSocketAddress) socketAddress ).getAddress();
        int throttleCount = throttle.getUnchecked( address ).incrementAndGet();

        if ( throttleCount > throttleLimit )
        {
            return true;
        }

        if ( cidrLimit > 0 )
        {
            String subnet = getSubnet( address );
            if ( subnet != null )
            {
                int cidrCount = cidrThrottle.getUnchecked( subnet ).incrementAndGet();
                return cidrCount > cidrLimit;
            }
        }

        return false;
    }

    private String getSubnet(InetAddress address)
    {
        byte[] addr = address.getAddress();
        if ( addr.length != 4 )
        {
            return null; // IPv6 not supported for CIDR throttle
        }

        int maskBits = cidrSize;
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < 4; i++ )
        {
            int bits = Math.min( maskBits, 8 );
            int mask = ( bits == 8 ) ? 0xFF : ( 0xFF << ( 8 - bits ) ) & 0xFF;
            sb.append( addr[i] & mask );
            if ( i < 3 )
            {
                sb.append( '.' );
            }
            maskBits -= bits;
        }
        return sb.toString();
    }
}
