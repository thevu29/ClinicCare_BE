package com.example.cliniccare.utils;
import java.text.DecimalFormat;

public class NumberToWords {
    private static final String[] UNITS = {"", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"};
    private static final String[] TENS = {"", "mười", "hai mươi", "ba mươi", "bốn mươi", "năm mươi", "sáu mươi", "bảy mươi", "tám mươi", "chín mươi"};
    private static final String[] SCALES = {"", "nghìn", "triệu", "tỷ"};

    public static String convert(double number) {
        if (number == 0) {
            return "không";
        }

        String result = "";
        int scaleIndex = 0;

        while (number > 0) {
            int group = (int) (number % 1000); // Lấy nhóm 3 chữ số
            if (group != 0) {
                String groupInWords = convertGroup(group);
                result = groupInWords + " " + SCALES[scaleIndex] + " " + result;
            }
            number /= 1000;
            scaleIndex++;
        }

        return result.trim().replaceAll("\\s+", " ");
    }

    private static String convertGroup(int number) {
        String result = "";

        int hundreds = number / 100;
        int tens = (number % 100) / 10;
        int units = number % 10;

        if (hundreds > 0) {
            result += UNITS[hundreds] + " trăm ";
        } else if (tens > 0 || units > 0) {
            result += "lẻ ";
        }

        if (tens > 0) {
            result += TENS[tens] + " ";
        } else if (units > 0 && hundreds > 0) {
            result += "lẻ ";
        }

        if (units > 0) {
            if (tens == 0 || units != 5) {
                result += UNITS[units];
            } else {
                result += "lăm";
            }
        }

        return result.trim();
    }

}
