/***********************************************************************
* This file is part of iDempiere ERP Open Source                     *
* http://www.idempiere.org                                            *
*                                                                       *
* Copyright (C) Contributors                                          *
*                                                                       *
* This program is free software; you can redistribute it and/or      *
* modify it under the terms of the GNU General Public License        *
* as published by the Free Software Foundation; either version 2     *
* of the License, or (at your option) any later version.             *
*                                                                       *
* This program is distributed in the hope that it will be useful,    *
* but WITHOUT ANY WARRANTY; without even the implied warranty of     *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the       *
* GNU General Public License for more details.                       *
*                                                                       *
* You should have received a copy of the GNU General Public License  *
* along with this program; if not, write to the Free Software        *
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,         *
* MA 02110-1301, USA.                                                 *
*                                                                       *
* Contributors:                                                       *
* - Nasleem Mdr - Nsoft                                               *
**********************************************************************/

package org.nsoft.idempiere.autoprice.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceRoundingUtil {

	/**
	* Rounds up based on the product's rounding rules.
	* If roundingType is null, no special rounding is performed (returns to the base price, 2 decimal places).
	*
	* Example: increment=1000 -> price 100.455 rounded up to the nearest multiple of 1000 = 1000
	* increment=100 -> price 1450 rounded up to the nearest multiple of 100 = 1500
	*/
    public static BigDecimal applyRounding(BigDecimal price, String roundingType) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }

        // If roundingType is NULL / empty / not filled -> No special rounding
        if (roundingType == null || roundingType.trim().isEmpty()) {
            return price.setScale(2, RoundingMode.HALF_UP);
        }

        try {
            BigDecimal increment = new BigDecimal(roundingType.trim());
            if (increment.compareTo(BigDecimal.ZERO) <= 0) {
                return price.setScale(2, RoundingMode.HALF_UP);
            }

            // Multiple Round Up Formula: CEIL(Price / Increment) * Increment
            BigDecimal divided = price.divide(increment, 0, RoundingMode.CEILING);
            return divided.multiply(increment).setScale(2, RoundingMode.HALF_UP);

        } catch (NumberFormatException e) {
        	// If the number format in the reference is invalid
            return price.setScale(2, RoundingMode.HALF_UP);
        }
    }
}