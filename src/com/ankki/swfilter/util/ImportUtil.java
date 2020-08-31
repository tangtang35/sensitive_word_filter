package com.ankki.swfilter.util;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;

public class ImportUtil {
    private static String importFromTxt(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
        String line;
        StringBuilder str = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            String[] words = line.split("\\s+");
            str.append(words[0]).append(":").append(words[1]).append(",");
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    private static String importFromExcel(String path) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(path)));
        XSSFSheet sheet = workbook.getSheetAt(0);
        int rowNum = sheet.getPhysicalNumberOfRows();
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < rowNum; i++) {
            XSSFRow row = sheet.getRow(i);
            String sensitiveWord = row.getCell(0).getStringCellValue();
            String replaceWord = row.getCell(1).getStringCellValue();
            if (!"".equals(sensitiveWord) && !"".equals(replaceWord)) {
                str.append(sensitiveWord).append(":").append(replaceWord).append(",");
            } else {
                break;
            }
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    public static void main(String[] args) {
        try {
            long start = System.currentTimeMillis();
            String s = importFromExcel("d:\\test.xlsx");
            long end = System.currentTimeMillis();
            System.out.println(s);
            System.out.println("导入耗时：" + (end - start));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
