package com.example.basketballsupervisor.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.util.Log;

public class FileUtil {
	/**
	 * 把一个文件转化为字节
	 * 
	 * @param file
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] getByte(File file) {
		byte[] bytes = null;
		FileInputStream os = null;
		if (file != null) {

			try {
				os = new FileInputStream(file);
				int length = (int) file.length();
				if (length > Integer.MAX_VALUE) {
					return null;
				}
				bytes = new byte[length];
				int offset = 0;
				int numRead = 0;
				while (offset < bytes.length && (numRead = os.read(bytes, offset, bytes.length - offset)) >= 0) {
					offset += numRead;
				}
				// 如果得到的字节长度和file实际的长度不一致就可能出错了
				if (offset < bytes.length) {
					System.out.println("file length is error");
					return null;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}
		return bytes;
	}

	/**
	 * 此类实现了一个输出流，其中的数据被写入一个 byte 数组。缓冲区会随着数据的不断写入而自动增长。 可使用 toByteArray() 和
	 * toString() 获取数据。 关闭 ByteArrayOutputStream 无效。此类中的方法在关闭此流后仍可被调用，而不会产生任何
	 * IOException。
	 * 
	 * @param filename
	 *            文件路径名称
	 * @param buffSize
	 *            文件读取流ByteArrayOutputStream， 缓冲区大小
	 * @return
	 */
	public static ByteArrayOutputStream getOutStreamByte(String filename, int buffSize) {
		BufferedInputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(filename));
			out = new ByteArrayOutputStream(buffSize);
			byte[] temp = new byte[buffSize];
			int size = 0;
			while ((size = in.read(temp)) != -1) {
				out.write(temp, 0, size);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return out;
	}

	/**
	 * 删除指定文件
	 * 
	 * @param filename
	 */
	public static void deleteFile(String filename) {
		File tmp = new File(filename);
		boolean flag = false;
		if (tmp.exists()) {
			flag = tmp.delete();
		}
		if (!flag) {
			Log.i("文件删除失败：", filename);
		}

	}

}