/***********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Nasleem Mdr - Nsoft                                               *
 **********************************************************************/

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
    public static class VarianceResult {
    public final BigDecimal variancePercent;
    public final boolean isSpike;

    public VarianceResult(BigDecimal variancePercent, boolean isSpike) {
        this.variancePercent = variancePercent;
        this.isSpike = isSpike;
    }
}

/** oldPrice null/0 dianggap belum ada baseline -> bukan spike (baru pertama kali). */
public static VarianceResult evaluateVariance(BigDecimal oldPrice, BigDecimal newPrice, BigDecimal thresholdPercent) {
    if (oldPrice == null || oldPrice.compareTo(BigDecimal.ZERO) == 0) {
        return new VarianceResult(BigDecimal.ZERO, false);
    }
    BigDecimal variancePercent = newPrice.subtract(oldPrice)
            .divide(oldPrice, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    boolean isSpike = variancePercent.abs().compareTo(thresholdPercent) > 0;
    return new VarianceResult(variancePercent, isSpike);
}
}
