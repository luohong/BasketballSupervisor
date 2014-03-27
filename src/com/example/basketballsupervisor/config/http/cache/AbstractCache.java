/* Copyright (c) 2009 Matthias Kaeppler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.basketballsupervisor.config.http.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.example.basketballsupervisor.config.Library;
import com.example.basketballsupervisor.config.Library.OnLowMemoryListener;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.MapMaker;

/**
 * <p>
 * A simple 2-level cache consisting of a small and fast in-memory cache (1st
 * level cache) and an (optional) slower but bigger disk cache (2nd level
 * cache). For disk caching, either the application's cache directory or the SD
 * card can be used. Please note that in the case of the app cache dir, Android
 * may at any point decide to wipe that entire directory if it runs low on
 * internal storage. The SD card cache <i>must</i> be managed by the
 * application, e.g. by calling {@link #wipe} whenever the app quits.
 * </p>
 * <p>
 * When pulling from the cache, it will first attempt to load the data from
 * memory. If that fails, it will try to load it from disk (assuming disk
 * caching is enabled). If that succeeds, the data will be put in the in-memory
 * cache and returned (read-through). Otherwise it's a cache miss.
 * </p>
 * <p>
 * Pushes to the cache are always write-through (i.e. the data will be stored
 * both on disk, if disk caching is enabled, and in memory).
 * </p>
 * 
 * @author Matthias Kaeppler
 */
public abstract class AbstractCache<KeyT, ValT> implements Map<KeyT, ValT>,
		OnLowMemoryListener {

	public static final int DISK_CACHE_INTERNAL = 0;
	public static final int DISK_CACHE_SDCARD = 1;

	private static final String LOG_TAG = "Droid-Fu[CacheFu]";

	private ConcurrentMap<KeyT, ValT> cache;

	private String name;

	private long expirationInMinutes;

	/**
	 * Creates a new cache instance.
	 * 
	 * @param name
	 *            a human readable identifier for this cache. Note that this
	 *            value will be used to derive a directory name if the disk
	 *            cache is enabled, so don't get too creative here (camel case
	 *            names work great)
	 * @param initialCapacity
	 *            the initial element size of the cache
	 * @param expirationInMinutes
	 *            time in minutes after which elements will be purged from the
	 *            cache
	 * @param maxConcurrentThreads
	 *            how many threads you think may at once access the cache; this
	 *            need not be an exact number, but it helps in fragmenting the
	 *            cache properly
	 */
	public AbstractCache(String name, int initialCapacity,
			long expirationInMinutes, int maxConcurrentThreads) {

		Library.Instance().registerOnLowMemoryListener(this);

		this.name = name;
		this.expirationInMinutes = expirationInMinutes;

		MapMaker mapMaker = new MapMaker();
		mapMaker.initialCapacity(initialCapacity);
		mapMaker.expiration(expirationInMinutes * 60, TimeUnit.SECONDS);
		mapMaker.concurrencyLevel(maxConcurrentThreads);
		mapMaker.softValues();
		this.cache = mapMaker.makeMap();
		if (getCacheDirectory() != null) {
			sanitizeDiskCache();
		}
	}

	/**
	 * Sanitize disk cache. Remove files which are older than
	 * expirationInMinutes.
	 */
	private void sanitizeDiskCache() {
		List<File> cachedFiles = getCachedFiles();
		for (File f : cachedFiles) {
			// if file older than expirationInMinutes, remove it
			long lastModified = f.lastModified();
			Date now = new Date();
			long ageInMinutes = ((now.getTime() - lastModified) / (1000 * 60));

			if (ageInMinutes >= expirationInMinutes) {
				Log.d(name, "DISK cache expiration for file " + f.toString());
				f.delete();
			}
		}
	}

	/**
	 * Only meaningful if disk caching is enabled. See {@link #enableDiskCache}.
	 * 
	 * @return the full absolute path to the directory where files are cached,
	 *         if the disk cache is enabled, otherwise null
	 */
	public abstract String getCacheDirectory();

	/**
	 * Only meaningful if disk caching is enabled. See {@link #enableDiskCache}.
	 * Turns a cache key into the file name that will be used to persist the
	 * value to disk. Subclasses must implement this.
	 * 
	 * @param key
	 *            the cache key
	 * @return the file name
	 */
	public abstract String getFileNameForKey(KeyT key);

	/**
	 * Only meaningful if disk caching is enabled. See {@link #enableDiskCache}.
	 * Restores a value previously persisted to the disk cache.
	 * 
	 * @param file
	 *            the file holding the cached value
	 * @return the cached value
	 * @throws IOException
	 */
	protected abstract ValT readValueFromDisk(File file) throws IOException;

	/**
	 * Only meaningful if disk caching is enabled. See {@link #enableDiskCache}.
	 * Persists a value to the disk cache.
	 * 
	 * @param ostream
	 *            the file output stream (buffered).
	 * @param value
	 *            the cache value to persist
	 * @throws IOException
	 */
	protected abstract void writeValueToDisk(File file, ValT value)
			throws IOException;

	private void cacheToDisk(KeyT key, ValT value) {
		File file = getFileForKey(key);
		if (file == null) {
			return;
		}
		try {
			file.createNewFile();
			file.deleteOnExit();

			writeValueToDisk(file, value);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getFileForKey(KeyT key) {
		String dir = getCacheDirectory();
		if (dir == null) {
			return null;
		}
		return new File(dir + "/" + getFileNameForKey(key));
	}

	/**
	 * Reads a value from the cache by first probing the in-memory cache. If not
	 * found, the the disk cache will be probed. If it's a hit, the entry is
	 * written back to memory and returned.
	 * 
	 * @param elementKey
	 *            the cache key
	 * @return the cached value, or null if element was not cached
	 */
	@Override
    @SuppressWarnings("unchecked")
	public synchronized ValT get(Object elementKey) {
		KeyT key = (KeyT) elementKey;
		ValT value = cache.get(key);
		if (value != null) {
			// memory hit
			Log.d(name, "MEM cache hit for " + key.toString());
			return value;
		}

		// memory miss, try reading from disk
		File file = getFileForKey(key);
		if (file != null && file.exists()) {
			// if file older than expirationInMinutes, remove it
			long lastModified = file.lastModified();
			Date now = new Date();
			long ageInMinutes = ((now.getTime() - lastModified) / (1000 * 60));

			if (ageInMinutes >= expirationInMinutes) {
				Log.d(name,
						"DISK cache expiration for file " + file.toString());
				file.delete();
				return null;
			}

			// disk hit
			Log.d(name, "DISK cache hit for " + key.toString());
			try {
				value = readValueFromDisk(file);
			} catch (IOException e) {
				// treat decoding errors as a cache miss
				e.printStackTrace();
				return null;
			}
			if (value == null) {
				return null;
			}
			cache.put(key, value);
			return value;
		}

		// cache miss
		return null;
	}

	/**
	 * Writes an element to the cache. NOTE: If disk caching is enabled, this
	 * will write through to the disk, which may introduce a performance
	 * penalty.
	 */
	@Override
    public synchronized ValT put(KeyT key, ValT value) {
		cacheToDisk(key, value);

		return cache.put(key, value);
	}

	@Override
    public synchronized void putAll(Map<? extends KeyT, ? extends ValT> t) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks if a value is present in the cache. If the disk cached is enabled,
	 * this will also check whether the value has been persisted to disk.
	 * 
	 * @param key
	 *            the cache key
	 * @return true if the value is cached in memory or on disk, false otherwise
	 */
	@Override
    public synchronized boolean containsKey(Object key) {
		return cache.containsKey(key) || containsKeyOnDisk(key);
	}

	/**
	 * Checks if a value is present in the in-memory cache. This method ignores
	 * the disk cache.
	 * 
	 * @param key
	 *            the cache key
	 * @return true if the value is currently hold in memory, false otherwise
	 */
	public synchronized boolean containsKeyInMemory(Object key) {
		return cache.containsKey(key);
	}

	/**
	 * Checks if a value is present in the disk cache. This method ignores the
	 * memory cache.
	 * 
	 * @param key
	 *            the cache key
	 * @return true if the value is currently hold on disk, false otherwise.
	 *         Always false if disk cache is disabled.
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean containsKeyOnDisk(Object key) {
		File file = getFileForKey((KeyT) key);
		if (file == null) {
			return false;
		}
		return file.exists();
	}

	/**
	 * Checks if the given value is currently held in memory. For performance
	 * reasons, this method does NOT probe the disk cache.
	 */
	@Override
    public synchronized boolean containsValue(Object value) {
		return cache.containsValue(value);
	}

	/**
	 * Removes an entry from both memory and disk.
	 */
	@Override
    @SuppressWarnings("unchecked")
	public synchronized ValT remove(Object key) {
		ValT value = removeKey(key);

		File cachedValue = getFileForKey((KeyT) key);
		if (cachedValue != null && cachedValue.exists()) {
			cachedValue.delete();
		}

		return value;
	}

	/**
	 * Removes an entry from memory.
	 * 
	 * @param key
	 *            the cache key
	 * @return the element removed or null
	 */
	public ValT removeKey(Object key) {
		return cache.remove(key);
	}

	@Override
    public Set<KeyT> keySet() {
		return cache.keySet();
	}

	@Override
    public Set<Map.Entry<KeyT, ValT>> entrySet() {
		return cache.entrySet();
	}

	@Override
    public synchronized int size() {
		return cache.size();
	}

	@Override
    public synchronized boolean isEmpty() {
		return cache.isEmpty();
	}

	/**
	 * Retrieves the list of files that are currently cached to disk. Guarantees
	 * to never return null.
	 * 
	 * @return the list of files on disk
	 */
	public List<File> getCachedFiles() {
		String dir = getCacheDirectory();
		if (dir == null) {
			return Collections.emptyList();
		}
		File[] cachedFiles = new File(dir).listFiles();
		if (cachedFiles == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(cachedFiles);
		}
	}

	/**
	 * Clears the entire cache (memory and disk).
	 */
	@Override
    public synchronized void clear() {
		clear(true);
	}

	/**
	 * Clears the memory cache, as well as the disk cache if it's enabled and
	 * <code>removeFromDisk</code> is <code>true</code>.
	 * 
	 * @param removeFromDisk
	 *            whether or not to wipe the disk cache, too
	 */
	public synchronized void clear(boolean removeFromDisk) {
		cache.clear();
		String dir = getCacheDirectory();
		if (removeFromDisk && dir != null) {
			File[] cachedFiles = new File(dir).listFiles();
			if (cachedFiles == null) {
				return;
			}
			for (File f : cachedFiles) {
				f.delete();
			}
		}

		Log.d(LOG_TAG, "Cache cleared");
	}

	@Override
    public Collection<ValT> values() {
		return cache.values();
	}

	@Override
	public void onLowMemoryReceived() {
		clear(false);
	}

	/**
	 * 不需要延迟处理(泛型的方式封装)
	 * 
	 * @return
	 */
	public static <K, V> LoadingCache<K, V> cached(CacheLoader<K, V> cacheLoader) {
		LoadingCache<K, V> cache = CacheBuilder.newBuilder().maximumSize(10000)
				.weakKeys().softValues()
				.refreshAfterWrite(120, TimeUnit.SECONDS)
				.expireAfterWrite(10, TimeUnit.MILLISECONDS)
				// .removalListener(RemovalListeners.asynchronous(listener,executor))
				// .removalListener(MY_LISTENER)
				.build(cacheLoader);
		return cache;
	}

	/**
	 * 通过key获取value 调用方式 commonCache.get(key) ; return String
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static LoadingCache<String, String> commonCache(final String key)
			throws Exception {
		return cached(new CacheLoader<String, String>() {
			@Override
			public String load(String key) throws Exception {
				return null;
			}
		});
	}
}
