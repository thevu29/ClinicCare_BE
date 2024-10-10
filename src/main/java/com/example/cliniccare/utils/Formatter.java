package com.example.cliniccare.utils;

import java.util.UUID;

public class Formatter {
    public static UUID fromHexString(String hex) {
        if (hex.startsWith("0x")) {
            hex = hex.substring(2);
        }
        long mostSigBits = Long.parseUnsignedLong(hex.substring(0, 16), 16);
        long leastSigBits = Long.parseUnsignedLong(hex.substring(16), 16);
        return new UUID(mostSigBits, leastSigBits);
    }
}
