package com.example.cliniccare.utils;

import com.example.cliniccare.dto.MedicalRecordDTO;
import com.example.cliniccare.model.MedicalRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
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
           "Id", "Patient", "Doctor", "Service", "Date",
            "Description"
    };

    public ExcelGenerator(List<MedicalRecordDTO> medicalRecordList) {
        this.medicalRecordList = medicalRecordList;
        this.workbook = new XSSFWorkbook();
    }

    private XSSFCellStyle createTitleStyle() {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();

        font.setBold(true);
        font.setFontHeight(16);
        font.setColor(IndexedColors.BLACK.getIndex());

        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private void writeTitleRow() {
        sheet = workbook.createSheet("Medical Records");
        XSSFRow titleRow = sheet.createRow(0);
        XSSFCell titleCell = titleRow.createCell(0);
        XSSFCellStyle titleStyle = createTitleStyle();

        titleCell.setCellStyle(titleStyle);
        titleCell.setCellValue("Medical Records Report");

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, HEADERS.length - 1));

        titleRow.setHeight((short) 600);
    }

    private XSSFCellStyle createHeaderStyle() {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();

        font.setBold(true);
        font.setFontHeight(12);
        font.setColor(IndexedColors.WHITE.getIndex());

        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }


    private void writeHeaderRow() {
        XSSFRow row = sheet.createRow(1); // Adjusted to row 1 (after title row)
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

    private XSSFCellStyle createDateStyle() {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        CreationHelper creationHelper = workbook.getCreationHelper();
        style.setDataFormat(creationHelper.createDataFormat().getFormat("dd-MM-yyyy HH:mm:ss"));

        return style;
    }

    private void writeDataRows() {
        XSSFCellStyle dataStyle = createDataStyle();
        XSSFCellStyle dateStyle = createDateStyle();
        int rowNum = 2;

        for (int i = 0; i < medicalRecordList.size(); i++) {
            MedicalRecordDTO record = medicalRecordList.get(i);
            XSSFRow row = sheet.createRow(rowNum++);

            row.setHeight((short) 500);

            XSSFCell sttCell = row.createCell(0);
            sttCell.setCellValue(i + 1);
            sttCell.setCellStyle(dataStyle);

            XSSFCell patientCell = row.createCell(1);
            patientCell.setCellValue(record.getPatientName());
            patientCell.setCellStyle(dataStyle);

            XSSFCell doctorCell = row.createCell(2);
            doctorCell.setCellValue(record.getDoctorName());
            doctorCell.setCellStyle(dataStyle);

            XSSFCell serviceCell = row.createCell(3);
            serviceCell.setCellValue(record.getServiceName());
            serviceCell.setCellStyle(dataStyle);

            XSSFCell dateCell = row.createCell(4);
            dateCell.setCellValue(record.getDate());
            dateCell.setCellStyle(dateStyle);

            XSSFCell descriptionCell = row.createCell(5);
            descriptionCell.setCellValue(record.getDescription());
            descriptionCell.setCellStyle(dataStyle);
        }

        for (int i = 0; i < HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    public ByteArrayInputStream generateExcelFile() throws IOException {
        writeTitleRow();
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
