package com.example.basketballsupervisor.config.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.CharArrayBuffer;

import android.text.TextUtils;

/***
 * Static helpers for dealing with {@link HttpEntity entities}.
 * 
 * @author <a href="mailto:oleg at ural.ru">Oleg Kalnichevski</a>
 * 
 * @version $Revision: 569637 $
 * 
 * @since 4.0
 */
public final class EntityUtils {

	/*** Disabled default constructor. */
	private EntityUtils() {
	}

	public static byte[] toByteArray(final HttpEntity entity)
			throws IOException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
		if (instream == null) {
			return new byte[] {};
		}
		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"HTTP entity too large to be buffered in memory");
		}

		try {
			if (entity.getContentEncoding() != null) {
				String encoding = entity.getContentEncoding().getValue();
				if (!TextUtils.isEmpty(encoding)
						&& (encoding.contains("gzip")
								|| encoding.contains("GZIP") || encoding
									.equalsIgnoreCase("gzip"))) {
					instream = new GZIPInputStream(instream);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();// 构造gzip失败，忽略
		}

		int i = (int) entity.getContentLength();
		if (i < 0) {
			i = 4096;
		}
		ByteArrayBuffer buffer = new ByteArrayBuffer(i);
		try {
			byte[] tmp = new byte[4096];
			int l;
			while ((l = instream.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
			buffer.clear();
		} finally {
			instream.close();
		}
		return buffer.toByteArray();
	}

	public static String getContentCharSet(final HttpEntity entity)
			throws ParseException {

		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		String charset = null;
		if (entity.getContentType() != null) {
			HeaderElement values[] = entity.getContentType().getElements();
			if (values.length > 0) {
				NameValuePair param = values[0].getParameterByName("charset");
				if (param != null) {
					charset = param.getValue();
				}
			}
		}
		return charset;
	}

	public static String toString(final HttpEntity entity,
			final String defaultCharset) throws IOException, ParseException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = entity.getContent();
		if (instream == null) {
			return "";
		}
		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException(
					"HTTP entity too large to be buffered in memory");
		}

		try {
		    if(entity.getContentEncoding() != null){
    			String encoding = entity.getContentEncoding().getValue();
    			if (encoding.contains("gzip") || encoding.contains("GZIP")
    					|| encoding.equalsIgnoreCase("gzip")) {
    				instream = new GZIPInputStream(instream);
    			}
		    }
		} catch (Exception e) {
			e.printStackTrace();// 构造gzip失败，忽略
		}

		int i = (int) entity.getContentLength();
		if (i < 0) {
			i = 4096;
		}
		String charset = getContentCharSet(entity);
		if (charset == null) {
			charset = defaultCharset;
		}
		if (charset == null) {
			charset = HTTP.DEFAULT_CONTENT_CHARSET;
		}
		Reader reader = new InputStreamReader(instream, charset);
		CharArrayBuffer buffer = null;
		try {
			buffer = new CharArrayBuffer(i);
			char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
		} finally {
			reader.close();
		}
		return buffer.toString();
	}

	public static String toString(final HttpEntity entity) throws IOException,
			ParseException {
		return toString(entity, null);
	}

}
