package org.nsoft.idempiere.autoprice.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import org.compiere.model.MConversionRate;

public class SalesPriceCalculator {

    public static class PriceResult {
        public final BigDecimal salesPrice;
        public final BigDecimal priceLimit; // = harga PO (converted), floor price

        public PriceResult(BigDecimal salesPrice, BigDecimal priceLimit) {
            this.salesPrice = salesPrice;
            this.priceLimit = priceLimit;
        }
    }

    /** Konversi harga PO ke currency target bila berbeda. Return null jika rate tidak ditemukan. */
    public static BigDecimal convertToTargetCurrency(BigDecimal poPrice, int poCurrencyID, int targetCurrencyID,
            Timestamp convDate, int conversionTypeID, int adClientID, int adOrgID) {
        if (poCurrencyID == targetCurrencyID) {
            return poPrice;
        }
        BigDecimal rate = MConversionRate.getRate(poCurrencyID, targetCurrencyID, convDate,
                conversionTypeID, adClientID, adOrgID);
        if (rate == null) {
            return null;
        }
        return poPrice.multiply(rate).setScale(4, RoundingMode.HALF_UP);
    }

    /** poPriceConverted HARUS sudah dalam currency price list target. */
    public static PriceResult calculate(BigDecimal poPriceConverted, BigDecimal markupPercent, String roundingType) {
        BigDecimal multiplier = BigDecimal.ONE.add(
                markupPercent.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        BigDecimal rawPrice = poPriceConverted.multiply(multiplier);
        BigDecimal salesPrice = PriceRoundingUtil.applyRounding(rawPrice, roundingType);
        return new PriceResult(salesPrice, poPriceConverted);
    }
}
