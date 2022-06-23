package org.apache.wiki.cache;

import static java.util.Collections.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.wiki.util.CheckedSupplier;

public class SimpleInMemoryCachingManager
implements CachingManager
{
	protected Map<String, CachePair> caches;
	protected long maxElementsAllowedPerCache;
	
	public SimpleInMemoryCachingManager()
	{
		this(1024);  //TODO this should really be configured..not here X'D   (when we redo the entire configuration system to be outside of the core! and not use dynamic class loading and newInstance()!  X'D )
	}
	
	public SimpleInMemoryCachingManager(long maxElementsAllowedPerCache)
	{
		this.caches = new HashMap<>(7);
		this.maxElementsAllowedPerCache = maxElementsAllowedPerCache;
		registerCache(CACHE_ATTACHMENTS);
		registerCache(CACHE_ATTACHMENTS_COLLECTION);
		registerCache(CACHE_ATTACHMENTS_DYNAMIC);
		registerCache(CACHE_DOCUMENTS);
		registerCache(CACHE_PAGES);
		registerCache(CACHE_PAGES_HISTORY);
		registerCache(CACHE_PAGES_TEXT);
		registerCache(CACHE_RSS);
	}
	
	protected void registerCache(String cacheName)
	{
		if (caches.containsKey(cacheName))  throw new Error("Duplicates!!: "+cacheName);
		
		CachePair p = new CachePair();
		p.cache = new HashMap<>();
		p.info = new CacheInfo(cacheName, maxElementsAllowedPerCache, () -> p.cache.size());
		caches.put(cacheName, p);
	}
	
	protected static class CachePair
	{
		protected Map<String, Object> cache;
		protected CacheInfo info;
	}
	
	
	
	
	@Override
	public boolean enabled(String cacheName)
	{
		return caches.containsKey(cacheName);
	}
	
	@Override
	public CacheInfo info(String cacheName)
	{
		CachePair p = caches.get(cacheName);
		return p == null ? null : p.info;
	}
	
	
	
	@Override
	public Set<String> keys(String cacheName)
	{
		CachePair p = caches.get(cacheName);
		return p == null ? emptySet() : p.cache.keySet();
	}
	
	
	@Override
	public <T, E extends Exception> T get(String cacheName, String key, CheckedSupplier<T, E> supplier) throws E
	{
		CachePair p = caches.get(cacheName);
		
		if (p == null)
			return null;
		else
		{
			Map<String, Object> cache = p.cache;
			
			Object v = cache.get(key);
			
			if (v == null && !cache.containsKey(key))
			{
				p.info.miss();
				v = supplier.get();
				putIntoGiven(cache, key, v, false);
			}
			else
			{
				p.info.hit();
			}
			
			return (T)v;
		}
	}
	
	@Override
	public void put(String cacheName, String key, Object val)
	{
		CachePair p = caches.get(cacheName);
		if (p != null)
			putIntoGiven(p.cache, key, val, true);
	}
	
	
	protected void putIntoGiven(Map<String, Object> cache, String key, Object val, boolean maybePresent)
	{
		if (maxElementsAllowedPerCache > 0)
		{
			if (cache.size() >= maxElementsAllowedPerCache)
			{
				boolean has = maybePresent ? cache.containsKey(key) : false;
				
				if (!has)  //if we'll overwrite an existing one, then we don't need to worry about this! XD
					cache.remove(getArbitraryElementThrowing(cache.keySet()));
			}
			
			cache.put(key, val);  //overwrite if there like we're supposed to!
		}
	}
	
	
	@Override
	public void remove(String cacheName, String key)
	{
		CachePair p = caches.get(cacheName);
		if (p != null)
			p.cache.remove(key);  //no worries if it's not there :3
	}
	
	
	
	
	
	
	@Override
	public void shutdown()
	{
	}
}
