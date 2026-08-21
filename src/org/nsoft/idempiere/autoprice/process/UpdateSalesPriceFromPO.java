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

package org.nsoft.idempiere.autoprice.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.nsoft.idempiere.autoprice.util.PriceRoundingUtil;

public class UpdateSalesPriceFromPO extends SvrProcess {

    @Override
    protected void prepare() {
    	// Where to get parameters if the process is run with a window filter
    }

    @Override
    protected String doIt() throws Exception {
    	// 1. Get all active Sales Price List Versions marked with IsAutoUpdateFromPO = 'Y'
        String sqlPLV = "SELECT M_PriceList_Version_ID FROM M_PriceList_Version WHERE IsAutoUpdateFromPO='Y' AND IsActive='Y'";
        
        int updatedCount = 0;
        PreparedStatement pstmtPLV = null;
        ResultSet rsPLV = null;

        try {
            pstmtPLV = DB.prepareStatement(sqlPLV, get_TrxName());
            rsPLV = pstmtPLV.executeQuery();

            while (rsPLV.next()) {
                int plvID = rsPLV.getInt("M_PriceList_Version_ID");

                // 2. Query the last PO per product that has MarkupPercent
                String sqlPO = "SELECT p.M_Product_ID, p.MarkupPercent, ol.PriceActual "
                        + "FROM M_Product p "
                        + "JOIN C_OrderLine ol ON ol.M_Product_ID = p.M_Product_ID "
                        + "JOIN C_Order o ON o.C_Order_ID = ol.C_Order_ID "
                        + "WHERE o.IsSOTrx = 'N' AND o.DocStatus IN ('CO','CL') "
                        + "AND p.MarkupPercent IS NOT NULL AND p.MarkupPercent > 0 "
                        + "AND ol.C_OrderLine_ID = ("
                        + "    SELECT ol2.C_OrderLine_ID FROM C_OrderLine ol2 "
                        + "    JOIN C_Order o2 ON o2.C_Order_ID = ol2.C_Order_ID "
                        + "    WHERE ol2.M_Product_ID = p.M_Product_ID AND o2.IsSOTrx = 'N' AND o2.DocStatus IN ('CO','CL') "
                        + "    ORDER BY o2.DateOrdered DESC, ol2.C_OrderLine_ID DESC LIMIT 1"
                        + ")";

                PreparedStatement pstmt = DB.prepareStatement(sqlPO, get_TrxName());
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    int productID = rs.getInt("M_Product_ID");
                    BigDecimal markup = rs.getBigDecimal("MarkupPercent");
                    BigDecimal lastPOPrice = rs.getBigDecimal("PriceActual");

                    // Formulas: Sales Price = PO Price * (1 + (Markup / 100))
                    BigDecimal multiplier = BigDecimal.ONE.add(markup.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    BigDecimal rawPrice = lastPOPrice.multiply(multiplier);

                    // Get the rounding rule (List reference) from M_Product, then apply it
                    MProduct product = new MProduct(Env.getCtx(), productID, get_TrxName());
                    String roundingType = product.get_ValueAsString("RoundingType");
                    BigDecimal newSalesPrice = PriceRoundingUtil.applyRounding(rawPrice, roundingType);

                    // 3. Upsert data to M_ProductPrice
                    MProductPrice pp = MProductPrice.get(getCtx(), plvID, productID, get_TrxName());
                    if (pp == null) {
                        pp = new MProductPrice(getCtx(), plvID, productID, get_TrxName());
                    }
                    pp.setPriceList(newSalesPrice);
                    pp.setPriceStd(newSalesPrice);
                    pp.setPriceLimit(lastPOPrice);   // batas bawah = harga beli terakhir, bukan harga jual
                    pp.saveEx();
                    updatedCount++;
                }
                DB.close(rs, pstmt);
            }
        } finally {
            DB.close(rsPLV, pstmtPLV);
        }

        return "Update Successfull" + updatedCount + " product price record at Sales Pricelist.";
    }
}
