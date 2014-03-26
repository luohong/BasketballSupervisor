/* Copyright (c) 2009-2011 Matthias Kaeppler
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

package com.example.basketballsupervisor.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

public class SystemUtil {

	public static final int SCREEN_DENSITY_LOW = 120;
	public static final int SCREEN_DENSITY_MEDIUM = 160;
	public static final int SCREEN_DENSITY_HIGH = 240;

	private static int screenDensity = -1;

	public static int dipToPx(Context context, int dip) {
		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		return (int) (dip * displayMetrics.density + 0.5f);
	}

	public static Drawable scaleDrawable(Context context,
			int drawableResourceId, int width, int height) {
		Bitmap sourceBitmap = BitmapFactory.decodeResource(
				context.getResources(), drawableResourceId);
		return new BitmapDrawable(Bitmap.createScaledBitmap(sourceBitmap,
				width, height, true));
	}

	public static int getScreenDensity(Context context) {
		if (screenDensity == -1) {
			DisplayMetrics displayMetrics = context.getResources()
					.getDisplayMetrics();
			try {
				screenDensity = DisplayMetrics.class.getField("densityDpi")
						.getInt(displayMetrics);
			} catch (Exception e) {
				screenDensity = SCREEN_DENSITY_MEDIUM;
			}
		}
		return screenDensity;
	}

	/**
	 * @return ip地址
	 */
	public static String getIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			// Ignore
		}
		return "";
	}

	/**
	 * @return 系统版本
	 */
	public static String getOS() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * @return 手机型号
	 */
	public static String getMachine() {
		return android.os.Build.MODEL;
	}

	/**
	 * @return SDK版本
	 */
	public static String getSDKVersion() {
		return android.os.Build.VERSION.SDK;
	}

	/**
	 * @param context
	 * @return 当前VersionName
	 */
	public static String getAppVersion(Context context) {
		String versionName = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionName = pi.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return versionName;
	}

	/**
	 * @param context
	 * @return 当前VersionCode
	 */
	public static int getAppVersionCode(Context context) {
		int versionCode = 0;
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			versionCode = pi.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 * 
	 * @param 地址
	 * @param context
	 */
	public static void openSystemUrl(String url, Context context) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(intent);
	}

	/**
	 * 检测是否有活动网络
	 */
	public static boolean contactNet(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);// 获取系统的连接服务
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();// 获取网络的连接情况
		if (activeNetInfo != null && activeNetInfo.isConnected()
				&& activeNetInfo.isAvailable()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isConnectionType(Context context, int type) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo active = cm.getActiveNetworkInfo();
		return (active != null && active.getType() == type);
	}

	public static boolean isWIFI(Context context) {
		return isConnectionType(context, ConnectivityManager.TYPE_WIFI);
	}

	public static boolean is2GNetWork(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo net = cm.getActiveNetworkInfo();
			if (net.getState() == NetworkInfo.State.CONNECTED) {
				int type = net.getType();
				int subtype = net.getSubtype();

				return !isConnectionFast(type, subtype);
			}
		}
		return false;
	}

	public static boolean isConnectionFast(int type, int subType) {
		if (type == ConnectivityManager.TYPE_WIFI) {
			return true;
		} else if (type == ConnectivityManager.TYPE_MOBILE) {
			switch (subType) {
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return false; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return false; // ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return false; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return true; // ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return true; // ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return false; // ~ 100 kbps
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return true; // ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return true; // ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return true; // ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return true; // ~ 400-7000 kbps
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				return false;
			default:
				return false;
			}
		} else {
			return false;
		}
	}
	
	public static int getNetStatus(Context context) {
//		[网络状况.0:未定义网络状况，1:2G,2:3G,3:WIFI,4:4G]
		int netStatus = 0;// 默认未定义
		
		ConnectivityManager cm =  
	            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	  
	    NetworkInfo activeInfo = cm.getActiveNetworkInfo();  
	    if (activeInfo != null && activeInfo.isConnected()) {  
	    	int type = activeInfo.getType();
	    	
	    	switch (type) {
			case ConnectivityManager.TYPE_WIFI:
			case ConnectivityManager.TYPE_ETHERNET:
				netStatus = 3;
				break;
			case ConnectivityManager.TYPE_MOBILE:
				switch (activeInfo.getSubtype()) 
		        {
		            case TelephonyManager.NETWORK_TYPE_GPRS:
		            case TelephonyManager.NETWORK_TYPE_EDGE:
		            case TelephonyManager.NETWORK_TYPE_CDMA:
		            case TelephonyManager.NETWORK_TYPE_1xRTT:
		            case TelephonyManager.NETWORK_TYPE_IDEN:
		            	netStatus = 1;
		            case TelephonyManager.NETWORK_TYPE_UMTS:
		            case TelephonyManager.NETWORK_TYPE_EVDO_0:
		            case TelephonyManager.NETWORK_TYPE_EVDO_A:
		            case TelephonyManager.NETWORK_TYPE_HSDPA:
		            case TelephonyManager.NETWORK_TYPE_HSUPA:
		            case TelephonyManager.NETWORK_TYPE_HSPA:
		            case TelephonyManager.NETWORK_TYPE_EVDO_B:
		            case TelephonyManager.NETWORK_TYPE_EHRPD:
		            case TelephonyManager.NETWORK_TYPE_HSPAP:
		            	netStatus = 2;
		            case TelephonyManager.NETWORK_TYPE_LTE:
		            	netStatus = 4;
		            default:
		            	netStatus = 2;
		        }
				break;
			case ConnectivityManager.TYPE_WIMAX:
            	netStatus = 4;
				break;
			default:
				netStatus = 0;// 默认未定义
				break;
			}
	    } 
	    return netStatus;
	}

	public static String getUUID(Activity activity) {
		String uuid = "";
		try {
			final TelephonyManager tm = (TelephonyManager) activity
					.getBaseContext().getSystemService(
							Context.TELEPHONY_SERVICE);
			final String tmDevice, tmSerial, tmPhone, androidId;
			tmDevice = "" + tm.getDeviceId();
			tmSerial = "" + tm.getSimSerialNumber();
			androidId = ""
					+ android.provider.Settings.Secure.getString(
							activity.getContentResolver(),
							android.provider.Settings.Secure.ANDROID_ID);
			UUID deviceUuid = new UUID(androidId.hashCode(),
					((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());

			uuid = "android:" + deviceUuid.toString();
		} catch (Exception e) {

		}

		return uuid;
	}
	
	public static String getDeviceId(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}
	
	public static String getChannelNo(Context context) {
		try {
			ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),
				 PackageManager.GET_META_DATA);
			return ai.metaData.get("UMENG_CHANNEL").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String formatNumber2KW(int number) {
		String result = number + "";
		if (number > 10000) {
			result = (int) number / 10000 + "W+";
		} else if (number > 1000) {
			result = (int) number / 1000 + "K+";
		}
		return result;
	}

}