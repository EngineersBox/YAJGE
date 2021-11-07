package com.engineersbox.yajge.resources.loader.assimp;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;

public class SyncCache<K, T> {

    private final long timeToLive;
    private final LRUMap<K, CacheEntry> cacheMap;

    protected class CacheEntry {
        private long lastAccessed;
        public final T value;

        protected CacheEntry(final T value) {
            this.lastAccessed = System.currentTimeMillis();
            this.value = value;
        }

        public void setLastAccessed() {
            this.lastAccessed = System.currentTimeMillis();
        }
    }

    public SyncCache(final long ttl,
                     final long timerInterval,
                     final int maxItems) {
        this.timeToLive = ttl * 1000;
        this.cacheMap = new LRUMap<>(maxItems);

        if (this.timeToLive > 0 && timerInterval > 0) {
            final Thread t = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(timerInterval * 1000);
                    } catch (final InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    cleanup();
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    public void put(final K key, final T value) {
        synchronized (this.cacheMap) {
            this.cacheMap.put(key, new CacheEntry(value));
        }
    }

    public T get(final K key) {
        synchronized (this.cacheMap) {
            final CacheEntry c;
            c = this.cacheMap.get(key);

            if (c == null) {
                return null;
            }
            c.setLastAccessed();
            return c.value;
        }
    }

    public void remove(final K key) {
        synchronized (this.cacheMap) {
            this.cacheMap.remove(key);
        }
    }

    public int size() {
        synchronized (this.cacheMap) {
            return this.cacheMap.size();
        }
    }

    public T computeIfAbsent(final K key,
                             final Function<? super K, ? extends T> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        final T value;
        if ((value = get(key)) == null) {
            final T newValue;
            if ((newValue = mappingFunction.apply(key)) != null) {
                put(key, newValue);
                return newValue;
            }
        }

        return value;
    }

    public void cleanup() {
        final long now = System.currentTimeMillis();
        final ArrayList<K> deleteKey;

        synchronized (this.cacheMap) {
            final MapIterator<K, CacheEntry> itr = this.cacheMap.mapIterator();
            deleteKey = new ArrayList<>((this.cacheMap.size() / 2) + 1);
            K key = null;
            CacheEntry c = null;

            while (itr.hasNext()) {
                key = itr.next();
                c = itr.getValue();
                if (c != null && (now > (this.timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }

        for (final K key : deleteKey) {
            synchronized (this.cacheMap) {
                this.cacheMap.remove(key);
            }
            Thread.yield();
        }
    }
}
