package com.example.cliniccare.utils;

public class Validation {
    public static boolean isPhoneValid(String phone) {
        return phone.matches("^(\\+84|0)(3[2-9]|7[0-9]|8[0-9]|9[0-9]|1[2-9]|5[0-9]|4[0-9])[0-9]{7}$");
    }
}
