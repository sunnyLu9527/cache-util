package com.htt.app.cache.utils.ehcache;

import java.io.Serializable;

import net.sf.ehcache.Cache;

/**
 * Cache构建类 从zk读取属性进行构建
 * @see #toCache()
 * @see EhcacheUtils#EhcacheUtils(String)
 */
public class EhcacheCacheConfig implements Serializable {
	private static final long serialVersionUID = -1334731656734993971L;
	private String name;
	private int maxElementsInMemory;
	private boolean eternal;
	private long timeToIdleSeconds;
	private long timeToLiveSeconds;
	private boolean overflowToDisk;
	@Override
	public String toString() {
		return "EhcacheCacheConfig [name=" + name + ", maxElementsInMemory=" + maxElementsInMemory + ", eternal=" + eternal
				+ ", timeToIdleSeconds=" + timeToIdleSeconds + ", timeToLiveSeconds=" + timeToLiveSeconds
				+ ", overflowToDisk=" + overflowToDisk + "]";
	}
	public String toJson() {
		return "{\"name\":\"" + name + "\", \"maxElementsInMemory\":" + maxElementsInMemory + ", \"eternal\":" + eternal
				+ ", \"timeToIdleSeconds\":" + timeToIdleSeconds + ", \"timeToLiveSeconds\":" + timeToLiveSeconds
				+ ", \"overflowToDisk\"" + overflowToDisk + "}";
	}
	public Cache toCache() {
		Cache result = new Cache(this.getName(), this.getMaxElementsInMemory(), this.isOverflowToDisk(), this.isEternal(), this.getTimeToLiveSeconds(), this.getTimeToIdleSeconds());
		return result;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getMaxElementsInMemory() {
		return maxElementsInMemory;
	}
	public void setMaxElementsInMemory(int maxElementsInMemory) {
		this.maxElementsInMemory = maxElementsInMemory;
	}
	public boolean isEternal() {
		return eternal;
	}
	public void setEternal(boolean eternal) {
		this.eternal = eternal;
	}
	public long getTimeToIdleSeconds() {
		return timeToIdleSeconds;
	}
	public void setTimeToIdleSeconds(long timeToIdleSeconds) {
		this.timeToIdleSeconds = timeToIdleSeconds;
	}
	public long getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}
	public void setTimeToLiveSeconds(long timeToLiveSeconds) {
		this.timeToLiveSeconds = timeToLiveSeconds;
	}
	public boolean isOverflowToDisk() {
		return overflowToDisk;
	}
	public void setOverflowToDisk(boolean overflowToDisk) {
		this.overflowToDisk = overflowToDisk;
	}
}
