package com.example.basketballsupervisor.config.http.cache;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import com.android.framework.core.util.Md5Util;

public class CacheHelper {

	public static String getFileNameFromUrl(String url) {
		/*
		 * // replace all special URI characters with a single + symbol return
		 * url.replaceAll("[.:/,%?&=]", "+").replaceAll("[+]+", "+");
		 */
		// 使用上面的命名规则有时会导致文件名过长
		// return url;

		return Md5Util.MD5Encode(url);
	}

	public static void removeAllWithStringPrefix(
			AbstractCache<String, ?> cache, String urlPrefix) {
		Set<String> keys = cache.keySet();

		for (String key : keys) {
			if (key.startsWith(urlPrefix)) {
				cache.remove(key);
			}
		}

		removeExpiredCache(cache, urlPrefix);
	}

	private static void removeExpiredCache(
			final AbstractCache<String, ?> cache, final String urlPrefix) {
		String dir = cache.getCacheDirectory();
		if (dir == null) {
			return;
		}
		final File cacheDir = new File(dir);

		if (!cacheDir.exists()) {
			return;
		}

		File[] list = cacheDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				return dir.equals(cacheDir)
						&& filename.startsWith(cache
								.getFileNameForKey(urlPrefix));
			}
		});

		if (list == null || list.length == 0) {
			return;
		}

		for (File file : list) {
			file.delete();
		}
	}

}
