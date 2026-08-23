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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.model.I_Persistent;
import org.compiere.model.PO;
import org.compiere.model.POInfo;
import org.compiere.model.MTable;

/**
 * Generated Model for XX_PriceHistory
 * @author iDempiere Custom Generator
 */
public class X_XX_PriceHistory extends PO implements I_Persistent {

    private static final long serialVersionUID = 20260820L;

    /** TableName=XX_PriceHistory */
    public static final String Table_Name = "XX_PriceHistory";

    /** AD_Table_ID */
    public static int Table_ID;
    static {
        Table_ID = MTable.getTable_ID(Table_Name);
    }

    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Standard Constructor */
    public X_XX_PriceHistory(Properties ctx, int XX_PriceHistory_ID, String trxName) {
        super(ctx, XX_PriceHistory_ID, trxName);
    }

    /** Load Constructor */
    public X_XX_PriceHistory(Properties ctx, ResultSet rs, String trxName) {
        super(ctx, rs, trxName);
    }

    /** AccessLevel = 3 - Client - Org */
    @Override
    protected int get_AccessLevel() {
        return accessLevel.intValue();
    }

    @Override
    protected POInfo initPO(Properties ctx) {
        POInfo poi = POInfo.getPOInfo(ctx, Table_ID, get_TrxName());
        return poi;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("X_XX_PriceHistory[").append(get_ID()).append("]");
        return sb.toString();
    }

    /** Column name XX_PriceHistory_ID */
    public static final String COLUMNNAME_XX_PriceHistory_ID = "XX_PriceHistory_ID";

    /** Set Primary Key: XX_PriceHistory_ID */
    public void setXX_PriceHistory_ID(int XX_PriceHistory_ID) {
        if (XX_PriceHistory_ID < 1)
            set_ValueNoCheck(COLUMNNAME_XX_PriceHistory_ID, null);
        else
            set_ValueNoCheck(COLUMNNAME_XX_PriceHistory_ID, Integer.valueOf(XX_PriceHistory_ID));
    }

    /** Get Primary Key: XX_PriceHistory_ID */
    public int getXX_PriceHistory_ID() {
        Integer ii = (Integer) get_Value(COLUMNNAME_XX_PriceHistory_ID);
        if (ii == null) return 0;
        return ii.intValue();
    }

    /** Column name M_Product_ID */
    public static final String COLUMNNAME_M_Product_ID = "M_Product_ID";

    /** Set Product.
     * @param M_Product_ID Product, Service, Item */
    public void setM_Product_ID(int M_Product_ID) {
        if (M_Product_ID < 1)
            set_Value(COLUMNNAME_M_Product_ID, null);
        else
            set_Value(COLUMNNAME_M_Product_ID, Integer.valueOf(M_Product_ID));
    }

    /** Get Product.
     * @return Product, Service, Item */
    public int getM_Product_ID() {
        Integer ii = (Integer) get_Value(COLUMNNAME_M_Product_ID);
        if (ii == null) return 0;
        return ii.intValue();
    }

    /** Column name M_PriceList_Version_ID */
    public static final String COLUMNNAME_M_PriceList_Version_ID = "M_PriceList_Version_ID";

    /** Set Price List Version.
     * @param M_PriceList_Version_ID Identifies a particular Version of a Price List */
    public void setM_PriceList_Version_ID(int M_PriceList_Version_ID) {
        if (M_PriceList_Version_ID < 1)
            set_Value(COLUMNNAME_M_PriceList_Version_ID, null);
        else
            set_Value(COLUMNNAME_M_PriceList_Version_ID, Integer.valueOf(M_PriceList_Version_ID));
    }

    /** Get Price List Version.
     * @return Identifies a particular Version of a Price List */
    public int getM_PriceList_Version_ID() {
        Integer ii = (Integer) get_Value(COLUMNNAME_M_PriceList_Version_ID);
        if (ii == null) return 0;
        return ii.intValue();
    }

    /** Column name C_Order_ID */
    public static final String COLUMNNAME_C_Order_ID = "C_Order_ID";

    /** Set Order.
     * @param C_Order_ID Order */
    public void setC_Order_ID(int C_Order_ID) {
        if (C_Order_ID < 1)
            set_Value(COLUMNNAME_C_Order_ID, null);
        else
            set_Value(COLUMNNAME_C_Order_ID, Integer.valueOf(C_Order_ID));
    }

    /** Get Order.
     * @return Order */
    public int getC_Order_ID() {
        Integer ii = (Integer) get_Value(COLUMNNAME_C_Order_ID);
        if (ii == null) return 0;
        return ii.intValue();
    }

    /** Column name PriceOld */
    public static final String COLUMNNAME_PriceOld = "PriceOld";

    /** Set Price Old.
     * @param PriceOld Price Old */
    public void setPriceOld(BigDecimal PriceOld) {
        set_Value(COLUMNNAME_PriceOld, PriceOld);
    }

    /** Get Price Old.
     * @return Price Old */
    public BigDecimal getPriceOld() {
        BigDecimal bd = (BigDecimal) get_Value(COLUMNNAME_PriceOld);
        if (bd == null) return BigDecimal.ZERO;
        return bd;
    }

    /** Column name PriceNew */
    public static final String COLUMNNAME_PriceNew = "PriceNew";

    /** Set Price New.
     * @param PriceNew Price New */
    public void setPriceNew(BigDecimal PriceNew) {
        set_Value(COLUMNNAME_PriceNew, PriceNew);
    }

    /** Get Price New.
     * @return Price New */
    public BigDecimal getPriceNew() {
        BigDecimal bd = (BigDecimal) get_Value(COLUMNNAME_PriceNew);
        if (bd == null) return BigDecimal.ZERO;
        return bd;
    }

    /** Column name MarkupPercent */
    public static final String COLUMNNAME_MarkupPercent = "MarkupPercent";

    /** Set Markup Percent.
     * @param MarkupPercent Percentage mark up */
    public void setMarkupPercent(BigDecimal MarkupPercent) {
        set_Value(COLUMNNAME_MarkupPercent, MarkupPercent);
    }

    /** Get Markup Percent.
     * @return Percentage mark up */
    public BigDecimal getMarkupPercent() {
        BigDecimal bd = (BigDecimal) get_Value(COLUMNNAME_MarkupPercent);
        if (bd == null) return BigDecimal.ZERO;
        return bd;
    }

    /** Column name IsPriceSpike */
    public static final String COLUMNNAME_IsPriceSpike = "IsPriceSpike";

    /** Set Price Spike.
     * @param IsPriceSpike Indicates the price change exceeded the spike threshold */
    public void setIsPriceSpike(boolean IsPriceSpike) {
        set_Value(COLUMNNAME_IsPriceSpike, Boolean.valueOf(IsPriceSpike));
    }

    /** Get Price Spike.
     * @return Indicates the price change exceeded the spike threshold */
    public boolean isPriceSpike() {
        Object oo = get_Value(COLUMNNAME_IsPriceSpike);
        if (oo != null)
            return ((Boolean) oo).booleanValue();
        return false;
    }

    /** Column name VariancePercent */
    public static final String COLUMNNAME_VariancePercent = "VariancePercent";

    /** Set Variance Percent.
     * @param VariancePercent Percentage variance from the previous price */
    public void setVariancePercent(BigDecimal VariancePercent) {
        set_Value(COLUMNNAME_VariancePercent, VariancePercent);
    }

    /** Get Variance Percent.
     * @return Percentage variance from the previous price */
    public BigDecimal getVariancePercent() {
        BigDecimal bd = (BigDecimal) get_Value(COLUMNNAME_VariancePercent);
        if (bd == null) return BigDecimal.ZERO;
        return bd;
    }

    /** Column name Description */
    public static final String COLUMNNAME_Description = "Description";

    /** Set Description.
     * @param Description Optional short description of the record */
    public void setDescription(String Description) {
        set_Value(COLUMNNAME_Description, Description);
    }

    /** Get Description.
     * @return Optional short description of the record */
    public String getDescription() {
        return (String) get_Value(COLUMNNAME_Description);
    }
}
