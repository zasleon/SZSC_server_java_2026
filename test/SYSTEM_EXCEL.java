package test;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class SYSTEM_EXCEL implements AutoCloseable{
	public SYSTEM_EXCEL(InputStream is, int sheet_No) {
        try {
            //fis = new FileInputStream(is);
            workbook = new XSSFWorkbook(is);

            sheet = workbook.getSheetAt(sheet_No-offset);//获取第1个sheet表

        } catch (Exception e) {
            show("创建excel连接出错 "+"InputStream方式\n"+e.getMessage());
        }
    }
	public SYSTEM_EXCEL(InputStream is, String sheet_name) {
        try {
            //fis = new FileInputStream(is);
            workbook = new XSSFWorkbook(is);

            int sheet_No=code_none;
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                if (workbook.getSheetName(i).equals(sheet_name)) {
                	sheet_No=i;
                    break;
                }
            }
			if(sheet_No==code_none)
			{
				show("没找到文件路径"+file_path+"里名为【"+sheet_name+"】的表格");
				//throw new IllegalArgumentException("Sheet不存在");
				sheet = workbook.getSheetAt(0);//获取第1个sheet表
			}
			else
				sheet = workbook.getSheetAt(sheet_No);//获取第sheet_No个sheet表

        } catch (Exception e) {
            show("创建excel连接出错 "+"InputStream方式\n"+e.getMessage());
        }
    }
	private static void show(String msg) {
		core_main.show(msg);
		
	}
	private int code_none=-999;
	private static final int offset=1;
	
	private FileInputStream fis;
	private XSSFWorkbook workbook;
	private XSSFSheet sheet;
	String file_path;
	
	public SYSTEM_EXCEL(String file_path,String sheet_name) {
		this.file_path=file_path;
		try {
			fis = new FileInputStream(file_path);
			workbook = new XSSFWorkbook(fis);
			
			int sheet_No=code_none;
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                if (workbook.getSheetName(i).equals(sheet_name)) {
                	sheet_No=i;
                    break;
                }
            }
			if(sheet_No==code_none)
			{
				show("没找到文件路径"+file_path+"里名为【"+sheet_name+"】的表格");
				//throw new IllegalArgumentException("Sheet不存在");
				sheet = workbook.getSheetAt(0);//获取第1个sheet表
			}
			else
				sheet = workbook.getSheetAt(sheet_No);//获取第sheet_No个sheet表
 	    } catch (Exception e) {
 	           show("创建excel连接出错 "+file_path+"\n"+e.getMessage());
 	           }
	}
	
	public void change_sheet(String sheet_name) {
		try {
			int sheet_No=code_none;
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                if (workbook.getSheetName(i).equals(sheet_name)) {
                	sheet_No=i;
                    break;
                }
            }
			if(sheet_No==code_none)
			{
				show("没找到文件路径"+file_path+"里名为【"+sheet_name+"】的表格");
				throw new IllegalArgumentException("Sheet不存在");
			}
			
			sheet = workbook.getSheetAt(sheet_No);//获取第1个sheet表
		}catch (Exception e) {
	           show("创建excel连接出错 "+file_path+"\n"+e.getMessage()); 
	        }
	}
	
	
	public void change_sheet(int sheet_No) {
		
		sheet = workbook.getSheetAt(sheet_No-offset);//获取第1个sheet表
	}
	
	
	public int getLastRowNum() {
		int result=code_none;
		try {
			result=sheet.getLastRowNum();
		} catch (Exception e) {
			show("excel获取行数出错 \n"+e.getMessage()); 
		}
		return result;
		
	}
	
	//统计总列数
	public int getLastColNum() {
		int result=code_none;
		try {
			Row firstRow=sheet.getRow(0);
			result=firstRow.getLastCellNum();
		} catch (Exception e) {
			show("excel获取行数出错 \n"+e.getMessage()); 
		}
		return result;
		
	}
	
	
	//确认在哪一行,列数、对应值
	public int getrow(int column,int value) {
		
		int result = code_none;
		int row_total_mount = getLastRowNum();
	    for (int row = 1; row <=row_total_mount; row++) {
	    	if (value == getInt(row, column)) {
	    		result= row;
	    		break;
	    	}
	    }
		return result;
	}
	public int getrow(int column,String value) {
		int result = code_none;
		int row_total_mount = getLastRowNum();
		
	    for (int row = 1; row <=row_total_mount; row++) {
	    	String row_value=getString(row, column);
	    	if (value.equals(row_value)) {
	    		result= row;
	    		break;
	    	}
	    	
	    }
		return result;
	}
	
	public int getrow(String column_name,int value) {
		
		int result = code_none;
		if(column_name==null||column_name.length()<=0) {
			show("column错误!   "+column_name);
			return result;
		}
		int column=get_column(column_name);
		if(column==code_none) {
			show("未找到对应列名: "+column_name);
			return result;
		}
		
		result=getrow(column, value);
		return result;
	}
	public int getrow(String column_name,String value) {
		int result = code_none;
		if(column_name==null||column_name.length()<=0) {
			show("column错误!   "+column_name);
			return result;
		}
		int column=get_column(column_name);
		if(column==code_none) {
			show("未找到对应列名: "+column_name);
			return result;
		}
		result=getrow(column, value);
		return result;
	}
	
	//如果是公式，获取公式计算后的值
	public int getInt(int rowNumber, int columnNumber) {
        int result = code_none;
        if (rowNumber <0 || columnNumber <0) {
            return result;
        }
        XSSFRow row = sheet.getRow(rowNumber);
        if (row != null) {
        	org.apache.poi.ss.usermodel.Cell cell = row.getCell(columnNumber);
            if (cell != null) {
            	switch(cell.getCellType()) // 兼容公式和纯数值
            	{
            		case org.apache.poi.ss.usermodel.CellType.FORMULA:
            		{
            			
            			org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            			org.apache.poi.ss.usermodel.CellValue evalResult = evaluator.evaluate(cell);
            			
            			switch (evalResult.getCellType()) {
							case org.apache.poi.ss.usermodel.CellType.NUMERIC: 
								result = (int) evalResult.getNumberValue();
								break;
							case org.apache.poi.ss.usermodel.CellType.STRING:
								if(evalResult.getStringValue().equals(""))
									break;
								result = Integer.parseInt(evalResult.getStringValue());
								break;
							case org.apache.poi.ss.usermodel.CellType.BLANK:
								break;
							case org.apache.poi.ss.usermodel.CellType.ERROR:
							
								show("公式错误: " + evalResult.getStringValue());
								break;
							default:
								break;
						
						}

            		}
                        break;
            		case org.apache.poi.ss.usermodel.CellType.NUMERIC:
            			result = (int) cell.getNumericCellValue();
            			break;
            		case org.apache.poi.ss.usermodel.CellType.STRING:
            			try {
                            result = Integer.parseInt(cell.getStringCellValue());
                        } catch (NumberFormatException e) {
                            // 忽略非整数字符串
                        }
            			break;
            		default:
            			break;
            	}
            }
        }
        return result;
    }
	
	public int get_column(String column_name) {
		int result=code_none;
		
		XSSFRow row = sheet.getRow(0);
		int column_number=row.getLastCellNum();
		for(int column=0;column<column_number;column++)
		{
			XSSFCell cell = row.getCell(column); // 序号列
			
			String column_result_name=getCellStringValue(cell);
				
			if(column_name.equals(column_result_name)) {//如果该列名称为对应字符，说明为该列，已经定位到该列
				result=column;
				break;
			}
		
			
		}
		return result;
	}
	
	/*
	public String getString(int row_number,int column_number) {
		String result="";
		if(row_number<0) {
			return result;
		}
			
		XSSFRow row = sheet.getRow(row_number);
		if (row != null) {
              XSSFCell cell = row.getCell(column_number); // 序号列
              if(cell!=null)
            	  result=getCellStringValue(cell);
		}
		return result;
	}*/
	public String getString(int rowNumber, int columnNumber) {
		String result="";
        if (rowNumber <0 || columnNumber <0) {
            return result;
        }
        XSSFRow row = sheet.getRow(rowNumber);
        if (row != null) {
        	//org.apache.poi.ss.usermodel.Cell cell = row.getCell(columnNumber);
        	XSSFCell cell = row.getCell(columnNumber); // 序号列
            if (cell != null) {
            	switch(cell.getCellType()) // 兼容公式和纯数值
            	{
            		case org.apache.poi.ss.usermodel.CellType.FORMULA:
            		{
            			org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            			org.apache.poi.ss.usermodel.CellValue evalResult = evaluator.evaluate(cell);
            			
            			switch (evalResult.getCellType()) {
							case org.apache.poi.ss.usermodel.CellType.NUMERIC: 
								result =String.valueOf(evalResult.getNumberValue());
								break;
							case org.apache.poi.ss.usermodel.CellType.STRING:
								if(evalResult.getStringValue().equals(""))
									break;
								result = evalResult.getStringValue();
								break;
							case org.apache.poi.ss.usermodel.CellType.BLANK:
								break;
							case org.apache.poi.ss.usermodel.CellType.ERROR:
							
								show("公式错误: " + evalResult.getStringValue());
								break;
							default:
								break;
						
						}

            		}
                        break;
            		case org.apache.poi.ss.usermodel.CellType.NUMERIC:
            			result = String.valueOf(cell.getNumericCellValue());
            			break;
            		case org.apache.poi.ss.usermodel.CellType.STRING:
            			try {
                            result = cell.getStringCellValue();
                        } catch (NumberFormatException e) {
                            // 忽略非整数字符串
                        }
            			break;
            		default:
            			break;
            	}
            }
        }
        return result;
    }
	
	
	// 获取单元格字符串值
    public static String getCellStringValue(XSSFCell cell) {
    	String default_value="";
        if (cell == null) return default_value;
        
        switch (cell.getCellType()) {
            //case org.apache.poi.ss.usermodel.CellType.STRING: return cell.getStringCellValue();
        	case STRING: return cell.getStringCellValue();
        	//case org.apache.poi.ss.usermodel.CellType.NUMERIC: return String.valueOf(cell.getNumericCellValue());
        	case NUMERIC: return String.valueOf(cell.getNumericCellValue());
            default: ;
        }
        return default_value;
    }
    @Override
	public void close() {
    	try {
			if (workbook != null) {
                workbook.close();
            }
            if (fis != null) {
                fis.close();
            }
		} catch (Exception e) {
			show("关闭excel连接异常\n"+e.getMessage());
		}
		
	}
	
    
    
    
	
}
