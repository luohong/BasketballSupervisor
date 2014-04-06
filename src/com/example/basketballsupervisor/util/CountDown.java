package com.example.basketballsupervisor.util;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;

public class CountDown {

	private final static int TAG_COUNT_DOWN_INTERVAL_REACH = 0x100;
	private final static int TAG_COUNT_DOWN_TIMEOUT = 0x101;

	private int mTimeout;
	private int mInterval;
	private int mCount;
	private CountDownHandler mHandler;
	private CountDownThread mThread;
	private OnCountDownListener mListener;
	private boolean mToQuit = false;
	
	private boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

	public CountDown(int timeout, int interval) {
		mTimeout = timeout;
		mInterval = interval <= 0 ? 10 : interval;
		mCount = 0;
		mHandler = new CountDownHandler(this);
	}

	public void setOnCountDownListener(OnCountDownListener listener) {
		mListener = listener;
	}

	public void start() {
		mToQuit = false;
		mCount = 0;
		mThread = new CountDownThread();
		mThread.start();
	}

	public void stop() {
		mToQuit = true;
		if (mThread != null)
			mThread.interrupt();
	}
	
	public void setPauseWork(boolean pauseWork) {
		synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
	}

	private static class CountDownHandler extends Handler {
		WeakReference<CountDown> mRef;

		CountDownHandler(CountDown countdown) {
			mRef = new WeakReference<CountDown>(countdown);
		}

		public void handleMessage(Message msg) {
			CountDown countDown = mRef.get();
			switch (msg.what) {
			case TAG_COUNT_DOWN_INTERVAL_REACH:
				if (countDown.mListener != null)
					countDown.mListener
							.onCountDownIntervalReach((Integer) msg.obj);
				break;
			case TAG_COUNT_DOWN_TIMEOUT:
				if (countDown.mListener != null)
					countDown.mListener.OnCountDownTimeout();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	}

	private class CountDownThread extends Thread {
		public void run() {
			while (!isInterrupted()) {
				try {
					Thread.sleep(mInterval);
					
					// Wait here if work is paused and the task is not cancelled
		            synchronized (mPauseWorkLock) {
		                while (mPauseWork) {
		                    try {
		                        mPauseWorkLock.wait();
		                    } catch (InterruptedException e) {}
		                }
		            }
					
					mCount++;

					Message message = new Message();
					if (mCount * mInterval > mTimeout) {
						message.what = TAG_COUNT_DOWN_TIMEOUT;
						mHandler.sendMessage(message);
						break;
					}

					message.what = TAG_COUNT_DOWN_INTERVAL_REACH;
					message.obj = mCount * mInterval;
					mHandler.sendMessage(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
					if (mToQuit)
						break;
				}
			}
		}
	}

	public interface OnCountDownListener {
		void OnCountDownTimeout();

		void onCountDownIntervalReach(int last);
	}
}