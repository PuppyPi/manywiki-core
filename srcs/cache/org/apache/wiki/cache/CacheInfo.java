/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */

package org.apache.wiki.cache;

import rebound.util.functional.FunctionInterfaces.NullaryFunctionToLong;

/**
 * Simple pojo that holds cache information.
 */
public class CacheInfo
{
	private final String name;
	private final long maxElementsAllowed;
	private final NullaryFunctionToLong getCurrentNumberOfElementsCached;
	private long misses;
	private long hits;
	
	public CacheInfo(final String name, final long maxElementsAllowed, final NullaryFunctionToLong getCurrentNumberOfElementsCached)
	{
		this.name = name;
		this.maxElementsAllowed = maxElementsAllowed;
		this.hits = 0l;
		this.misses = 0l;
		this.getCurrentNumberOfElementsCached = getCurrentNumberOfElementsCached;
	}
	
	public void hit()
	{
		hits++;
	}
	
	public void miss()
	{
		misses++;
	}
	
	public String getName()
	{
		return name;
	}
	
	public long getMisses()
	{
		return misses;
	}
	
	public long getHits()
	{
		return hits;
	}
	
	public long getMaxElementsAllowed()
	{
		return maxElementsAllowed;
	}
	
	public long getCurrentNumberOfElementsCached()
	{
		return getCurrentNumberOfElementsCached.f();
	}
}
