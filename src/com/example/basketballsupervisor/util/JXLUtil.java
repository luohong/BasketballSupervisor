package com.example.basketballsupervisor.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import android.os.Environment;

import com.android.framework.core.util.SDcardUtil;
import com.example.basketballsupervisor.model.DataStat;
import com.example.basketballsupervisor.widget.DataStatDialog;

public class JXLUtil {
	
	public static WritableFont arial14font = null;
	public static WritableCellFormat arial14format = null;
	
	public static WritableFont arial10font = null;
	public static WritableCellFormat arial10format = null;
	
	public static WritableFont arial12font = null;
	public static WritableCellFormat arial12format = null;

	/**
	 * 格式定义
	 */
	public static void format() {
		try {
			arial14font = new WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD);
			arial14font.setColour(jxl.format.Colour.LIGHT_BLUE);
			
			arial14format = new WritableCellFormat(arial14font);
			arial14format.setAlignment(jxl.format.Alignment.CENTRE);
			arial14format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
			arial14format.setBackground(jxl.format.Colour.VERY_LIGHT_YELLOW);
			
			arial10font = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
			arial10format = new WritableCellFormat(arial10font);
			arial10format.setAlignment(jxl.format.Alignment.CENTRE);
			arial10format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
			arial10format.setBackground(jxl.format.Colour.LIGHT_BLUE);
			
			arial12font = new WritableFont(WritableFont.ARIAL, 12);
			arial12format = new WritableCellFormat(arial12font);
			arial12format.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
	
	public static String write(String filename, List<DataStat> dataStatList) {
		if (!SDcardUtil.checkSdCardEnable()) {
			return "SD卡不存在";
		}
		
		String error = "";
		
		String sdDir = Environment.getExternalStorageDirectory().toString();
		String dirName = sdDir + "/BasketballSupervisor/";
		File dir = new File(dirName);

		boolean dirExist = true;
		if (!dir.isDirectory()) {
			dirExist = dir.mkdirs();
		}
		if (!dirExist) {
			error = "SD卡保存目录创建失败: " + dir.getAbsolutePath();
			return error;
		}
		
		File file = new File(dir.getAbsolutePath() + "/" + filename + ".xls");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		String filePath = file.getAbsolutePath();
		
		format();// 先设置格式
		
		WritableWorkbook workbook = null;
		try {
			workbook = Workbook.createWorkbook(file);

			String title = "比赛数据统计列表";
			
			int columnLength = Constants.MEMBER_DATA_STAT_COLUMNS.length - 1;
			
			WritableSheet sheet = workbook.createSheet(title, 0);// 建立sheet
			sheet.mergeCells(0, 0, columnLength, 0);
			sheet.addCell((WritableCell) new Label(0, 0, title, arial14format));// 表头设置完成
			
//			sheet.mergeCells(0, 1, columnLength, 1);
//			addPictureToExcel(sheet, dataStatList.get(0).dataList.get(0), 1, 0);
			
			for (int i = 0; i < dataStatList.size(); i++) {
				DataStat dataStat = dataStatList.get(i);
				
				List<String> dataList = dataStat.dataList;
				
				int row = i + 1;
				
				switch (dataStat.type) {
				case DataStatDialog.TYPE_COURT_POINT:
					sheet.mergeCells(0, row, columnLength, row);
					addPictureToExcel(sheet, dataList.get(0), dataStatList.size() + 5, 0);
					break;
				case DataStatDialog.TYPE_TITLE:
					sheet.mergeCells(0, row, columnLength, row);
					sheet.addCell((WritableCell) new Label(0, row, dataList.get(0), arial14format));// 表头设置完成
					break;
				case DataStatDialog.TYPE_COLUMN:
					// 总得分 总出手命中次数（不含罚球） 总出手次数（不含罚球） 总命中率（总命中率中不含罚球命中率） 2分球命中次数 2分球出手次数 2分球命中率 3分球命中次数 3分球出手次数 3分球命中率 罚球命中次数 罚球出手次数 罚球命中率 前场篮板 后场篮板 总篮板 助攻 抢断 封盖 被犯规 犯规 失误 上场时间
					for (int col = 0; col < dataList.size(); col++) {
						sheet.addCell(new Label(col, row, dataList.get(col), arial10format));// 写入column名称
					}
					break;
				case DataStatDialog.TYPE_CONTENT:
					// 一条龙，超远三分，绝杀，最后三秒得分，晃倒，2+1,3+1，扣篮，快攻，2罚不中，三罚不中，被晃倒
					for (int col = 0; col < dataList.size(); col++) {
						sheet.addCell(new Label(col, row, dataList.get(col), arial12format));
					}
					break;
				}
			}
			
			workbook.write();// 写入数据
			
			error = "导入成功: " + filePath; 
		} catch (RowsExceededException e) {
			e.printStackTrace();
			error = e.getMessage();
		} catch (WriteException e) {
			e.printStackTrace();
			error = e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			error = e.getMessage();
		} catch (Exception e) {
			e.printStackTrace();
			error = e.getMessage();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (WriteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return error;
	}

    /**
    * 插入图片到EXCEL
    * 
    * @param picSheet sheet
    * @param pictureFile 图片file对象
    * @param cellRow 行数
    * @param cellCol 列数
    * @throws Exception 例外
    */
   private static void addPictureToExcel(WritableSheet picSheet, String filename, int row, int col)
       throws Exception {

       // 读入图片
	   int index = filename.lastIndexOf("_");
	   String filePath = filename.substring(0, index);
       String ext = filename.substring(index + 1);
	   File pictureFile = new File(filePath);
	   
       // 开始位置
       double picBeginCol = col;
       double picBeginRow = row;
       // 图片时间的高度，宽度
       double picCellWidth = 0.0;
       double picCellHeight = 0.0;
       // 取得图片的像素高度，宽度
       String[] params = ext.split("x");
       int picWidth = Integer.valueOf(params[0]);
       int picHeight = Integer.valueOf(params[1]);
       
       // 计算图片的实际宽度
       int picWidth_t = picWidth * 32;  //具体的实验值，原理不清楚。
       for (int x = 0; x < 1234; x++) {
           int bc = (int) Math.floor(picBeginCol + x);
           // 得到单元格的宽度
           int v = picSheet.getColumnView(bc).getSize();
           double offset0_t = 0.0;
           if (0 == x)
               offset0_t = (picBeginCol - bc) * v;
           if (0.0 + offset0_t + picWidth_t > v) {
               // 剩余宽度超过一个单元格的宽度
               double ratio_t = 1.0;
               if (0 == x) {
                   ratio_t = (0.0 + v - offset0_t) / v;
               }
              picCellWidth += ratio_t;
               picWidth_t -= (int) (0.0 + v - offset0_t);
           } else { //剩余宽度不足一个单元格的宽度
               double ratio_r = 0.0;
               if (v != 0)
                   ratio_r = (0.0 + picWidth_t) / v;
               picCellWidth += ratio_r;
               break;
           }
       }        
       // 计算图片的实际高度
       int picHeight_t = picHeight * 15;
       for (int x = 0; x < 1234; x++) {
           int bc = (int) Math.floor(picBeginRow + x);
           // 得到单元格的高度
           int v = picSheet.getRowView(bc).getSize();
           double offset0_r = 0.0;
           if (0 == x)
               offset0_r = (picBeginRow - bc) * v;
           if (0.0 + offset0_r + picHeight_t > v) {
               // 剩余高度超过一个单元格的高度
               double ratio_q = 1.0;
               if (0 == x)
                   ratio_q = (0.0 + v - offset0_r) / v;
               picCellHeight += ratio_q;
               picHeight_t -= (int) (0.0 + v - offset0_r);
           } else {//剩余高度不足一个单元格的高度
               double ratio_m = 0.0;
               if (v != 0)
                   ratio_m = (0.0 + picHeight_t) / v;
               picCellHeight += ratio_m;
               break;
           }
       }
       //生成一个图片对象。
       WritableImage image = new WritableImage(picBeginCol, picBeginRow,
               picCellWidth, picCellHeight, pictureFile);
       // 把图片插入到sheet
       picSheet.addImage(image);
   }

}
