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

package org.nsoft.idempiere.autoprice.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MXXPriceHistory extends X_XX_PriceHistory {

    private static final long serialVersionUID = 1L;

    // Constructor Standar 1
    public MXXPriceHistory(Properties ctx, int XX_PriceHistory_ID, String trxName) {
        super(ctx, XX_PriceHistory_ID, trxName);
    }

    // Constructor Standar 2 (Dari ResultSet)
    public MXXPriceHistory(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }

    // Helper Constructor Khusus untuk Memudahkan Pencatatan History
    public MXXPriceHistory(Properties ctx, int productID, int priceListVersionID, int orderID, String trxName) {
        this(ctx, 0, trxName);
        setM_Product_ID(productID);
        setM_PriceList_Version_ID(priceListVersionID);
        if (orderID > 0) {
            setC_Order_ID(orderID);
        }
    }
}
