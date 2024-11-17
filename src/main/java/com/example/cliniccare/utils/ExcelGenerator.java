package com.example.cliniccare.utils;

import com.example.cliniccare.dto.MedicalRecordDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelGenerator {
    private List<MedicalRecordDTO> medicalRecordList;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;

    private static final String[] HEADERS = {
            "Patient Name", "Doctor", "Service", "Date",
            "Description"
    };

    public ExcelGenerator(List<MedicalRecordDTO> medicalRecordList) {
        this.medicalRecordList = medicalRecordList;
        this.workbook = new XSSFWorkbook();
    }

    private XSSFCellStyle createHeaderStyle() {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();

        font.setBold(true);
        font.setFontHeight(12);
        font.setColor(IndexedColors.WHITE.getIndex());

        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private void writeHeaderRow() {
        sheet = workbook.createSheet("Medical Records");
        XSSFRow row = sheet.createRow(0);
        XSSFCellStyle headerStyle = createHeaderStyle();

        for (int i = 0; i < HEADERS.length; i++) {
            XSSFCell cell = row.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(HEADERS[i]);
            sheet.autoSizeColumn(i);
        }

        row.setHeight((short) 500);
    }

    private XSSFCellStyle createDataStyle() {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void writeDataRows() {
        XSSFCellStyle dataStyle = createDataStyle();
        int rowNum = 1;

        for (MedicalRecordDTO record : medicalRecordList) {
            XSSFRow row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(record.getPatientName());
            row.createCell(1).setCellValue(record.getDoctorName());
            row.createCell(2).setCellValue(record.getServiceName());
            row.createCell(3).setCellValue(record.getDate().toString());
            row.createCell(4).setCellValue(record.getDescription());

            for (int i = 0; i < HEADERS.length; i++) {
                row.getCell(i).setCellStyle(dataStyle);
            }
        }

        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public ByteArrayInputStream generateExcelFile() throws IOException {
        writeHeaderRow();
        writeDataRows();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        } finally {
            workbook.close();
        }
    }
}
