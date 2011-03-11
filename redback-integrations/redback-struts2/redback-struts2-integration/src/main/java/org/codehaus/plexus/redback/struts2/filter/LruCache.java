package org.codehaus.plexus.redback.struts2.filter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copied from Tomcat
 * 
 * @param <T>
 */
public class LruCache<T>
{
    // Although the internal implementation uses a Map, this cache
    // implementation is only concerned with the keys.
    private final Map<T, T> cache;

    public LruCache( final int cacheSize )
    {
        cache = new LinkedHashMap<T, T>()
        {
            private static final long serialVersionUID = 1L;

            @Override
            protected boolean removeEldestEntry( Map.Entry<T, T> eldest )
            {
                if ( size() > cacheSize )
                {
                    return true;
                }

                return false;
            }
        };
    }

    public void add( T key )
    {
        cache.put( key, null );
    }

    public boolean contains( T key )
    {
        return cache.containsKey( key );
    }

    public int getSize()
    {
        return cache.size();
    }

    public Set<T> getKeys()
    {
        return cache.keySet();       
    }
}