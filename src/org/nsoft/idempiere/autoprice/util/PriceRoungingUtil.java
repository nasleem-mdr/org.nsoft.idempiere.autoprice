package org.nsoft.idempiere.autoprice.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceRoundingUtil {

    /**
     * Melakukan pembulatan ke atas (Round Up) berdasarkan aturan pembulatan produk.
     * Jika roundingType null/kosong, tidak dilakukan pembulatan khusus (kembali ke harga dasar).
     */
    public static BigDecimal applyRounding(BigDecimal price, String roundingType) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }

        // Jika roundingType NULL / kosong / tidak diisi -> Tidak ada pembulatan khusus
        if (roundingType == null || roundingType.trim().isEmpty()) {
            return price.setScale(2, RoundingMode.HALF_UP);
        }

        try {
            BigDecimal increment = new BigDecimal(roundingType);
            if (increment.compareTo(BigDecimal.ZERO) <= 0) {
                return price;
            }

            // Rumus Round Up Kelipatan: CEIL(Price / Increment) * Increment
            // Contoh: 100.455 / 1000 = 100.455 -> CEIL = 101 -> 101 * 1000 = 101.000
            BigDecimal divided = price.divide(increment, 0, RoundingMode.CEILING);
            return divided.multiply(increment).setScale(0, RoundingMode.UNNECESSARY);

        } catch (NumberFormatException e) {
            // Jika format angka pada reference bernilai invalid
            return price.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
