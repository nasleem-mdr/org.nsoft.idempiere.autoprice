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

package org.nsoft.idempiere.autoprice.validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.compiere.model.MClient;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import java.sql.Timestamp;
import org.compiere.model.MPriceList;
import org.compiere.model.MPriceListVersion;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.nsoft.idempiere.autoprice.model.MXXPriceHistory;
import org.nsoft.idempiere.autoprice.util.PriceRoundingUtil;
import org.nsoft.idempiere.autoprice.util.SalesPriceCalculator;
import org.nsoft.idempiere.autoprice.util.SalesPriceCalculator.PriceResult;
import org.compiere.util.MSysConfig;

public class SalesPriceAutoUpdateValidator implements ModelValidator {

    private static final CLogger log = CLogger.getCLogger(SalesPriceAutoUpdateValidator.class);
    private int m_AD_Client_ID = -1;

    @Override
    public void initialize(ModelValidationEngine engine, MClient client) {
        if (client != null) {
            m_AD_Client_ID = client.getAD_Client_ID();
        }
        engine.addModelChange(MOrder.Table_Name, this);
        log.log(Level.INFO, "[AUTOPRICE] Validator terdaftar untuk table=" + MOrder.Table_Name
                + " m_AD_Client_ID=" + m_AD_Client_ID);
    }

    @Override
    public int getAD_Client_ID() {
        return m_AD_Client_ID;
    }

    @Override
    public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
        return null; // Tidak digunakan
    }

    @Override
    public String modelChange(PO po, int type) throws Exception {
        if (po instanceof MOrder && type == TYPE_AFTER_CHANGE) {
            MOrder order = (MOrder) po;

            if (!order.isSOTrx() && order.is_ValueChanged(MOrder.COLUMNNAME_DocStatus)) {
                String docStatus = order.getDocStatus();
                if (MOrder.DOCSTATUS_Completed.equals(docStatus)) {
                    updateSalesPriceList(order, false);
                    updatePurchasePriceList(order, false);
                } else if (MOrder.DOCSTATUS_Voided.equals(docStatus) || MOrder.DOCSTATUS_Reversed.equals(docStatus)) {
                    updateSalesPriceList(order, true);
                    updatePurchasePriceList(order, true);
                }
            }
        }
        return null;
    }

    @Override
    public String docValidate(PO po, int timing) {
        return null; 
    }
    
    // Ganti pemanggilannya di dalam updatePurchasePriceList(), sebelum loop produk:
    int adClientIDForConfig = order.getAD_Client_ID();
    BigDecimal spikeThresholdPercent = new BigDecimal(
            MSysConfig.getIntValue("AUTOPRICE_SPIKE_THRESHOLD_PCT", 10, adClientIDForConfig));
    
    private void updatePurchasePriceList(MOrder order, boolean isRollback) {
        String sqlPLV = "SELECT plv.M_PriceList_Version_ID FROM M_PriceList_Version plv "
                + "JOIN M_PriceList pl ON pl.M_PriceList_ID = plv.M_PriceList_ID "
                + "WHERE plv.IsAutoUpdateFromPO='Y' AND plv.IsActive='Y' AND pl.IsSOPriceList='N'";
        PreparedStatement pstmtPLV = null;
        ResultSet rsPLV = null;

        try {
            pstmtPLV = DB.prepareStatement(sqlPLV, order.get_TrxName());
            rsPLV = pstmtPLV.executeQuery();

            while (rsPLV.next()) {
            int plvID = rsPLV.getInt("M_PriceList_Version_ID");

            MPriceListVersion plv = new MPriceListVersion(order.getCtx(), plvID, order.get_TrxName());
            MPriceList priceList = (MPriceList) plv.getM_PriceList();
            int targetCurrencyID = priceList.getC_Currency_ID();
            int adClientID = plv.getAD_Client_ID();
            int adOrgID = plv.getAD_Org_ID();
            int conversionTypeID = 0;

            for (MOrderLine line : order.getLines()) {
                int productID = line.getM_Product_ID();
                if (productID <= 0) continue;

                    MProduct product = line.getProduct();
                    if (!product.isPurchased()) continue; // hanya produk yang memang dibeli

                    BigDecimal poPrice = line.getPriceActual();
                    if (poPrice == null || poPrice.compareTo(BigDecimal.ZERO) <= 0) continue;

                    BigDecimal poPriceConverted = SalesPriceCalculator.convertToTargetCurrency(
                            poPrice, order.getC_Currency_ID(), targetCurrencyID, order.getDateOrdered(),
                            conversionTypeID, adClientID, adOrgID);
                    if (poPriceConverted == null) {
                        log.warning("[AUTOPRICE] Tidak ada conversion rate untuk Purchase Price update, M_Product_ID=" + productID);
                        continue;
                    }

                    MProductPrice pp = MProductPrice.get(order.getCtx(), plvID, productID, order.get_TrxName());
                    boolean isNewRecord = (pp == null);
                    BigDecimal oldPrice = isNewRecord ? null : pp.getPriceStd();

                    SalesPriceCalculator.VarianceResult variance =
                            SalesPriceCalculator.evaluateVariance(oldPrice, poPriceConverted, SPIKE_THRESHOLD_PERCENT);

                    if (isNewRecord) {
                        pp = new MProductPrice(order.getCtx(), plvID, productID, order.get_TrxName());
                    }
                    // Purchase price: harga langsung dari PO, tanpa markup/rounding
                    pp.setPriceList(poPriceConverted);
                    pp.setPriceStd(poPriceConverted);
                    pp.setPriceLimit(poPriceConverted);
                    pp.saveEx();

                    String desc = isRollback
                            ? "Rollback Purchase Price dari Void/Reverse PO " + order.getDocumentNo()
                            : "Auto update Purchase Price dari Complete PO " + order.getDocumentNo();
                    if (variance.isSpike) {
                        desc = "[HARGA MELONJAK " + variance.variancePercent + "%] " + desc;
                        log.warning("[AUTOPRICE] Purchase price spike product ID=" + productID
                                + " variance=" + variance.variancePercent + "% oldPrice=" + oldPrice
                                + " newPrice=" + poPriceConverted);
                    }

                    try {
                        MXXPriceHistory history = new MXXPriceHistory(order.getCtx(), productID, plvID,
                                order.getC_Order_ID(), order.get_TrxName());
                        history.setPriceOld(oldPrice == null ? BigDecimal.ZERO : oldPrice);
                        history.setPriceNew(poPriceConverted);
                        history.setDescription(desc);
                        history.saveEx();
                    } catch (Exception histEx) {
                        log.severe("[AUTOPRICE] Gagal mencatat Purchase Price History product ID=" + productID + ": " + histEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.severe("Gagal memproses update Purchase Price List: " + e.getMessage());
        } finally {
            DB.close(rsPLV, pstmtPLV);
        }
    }
    
    private void updateSalesPriceList(MOrder order, boolean isRollback) {
    String sqlPLV = "SELECT M_PriceList_Version_ID FROM M_PriceList_Version WHERE IsAutoUpdateFromPO='Y' AND IsActive='Y'";
    PreparedStatement pstmtPLV = null;
    ResultSet rsPLV = null;

    try {
        pstmtPLV = DB.prepareStatement(sqlPLV, order.get_TrxName());
        rsPLV = pstmtPLV.executeQuery();

        while (rsPLV.next()) {
            int plvID = rsPLV.getInt("M_PriceList_Version_ID");

            MPriceListVersion plv = new MPriceListVersion(order.getCtx(), plvID, order.get_TrxName());
            MPriceList priceList = (MPriceList) plv.getM_PriceList();
            int targetCurrencyID = priceList.getC_Currency_ID();
            int adClientID = plv.getAD_Client_ID();
            int adOrgID = plv.getAD_Org_ID();
            int conversionTypeID = 0;

            for (MOrderLine line : order.getLines()) {
                int productID = line.getM_Product_ID();
                if (productID <= 0) continue;

                MProduct product = line.getProduct();
                BigDecimal markup = (BigDecimal) product.get_Value("MarkupPercent");
                if (markup == null || markup.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal targetPOPrice;
                int poCurrencyID;
                Timestamp convDate;

                if (!isRollback) {
                    targetPOPrice = line.getPriceActual();
                    poCurrencyID = order.getC_Currency_ID();
                    convDate = order.getDateOrdered();
                } else {
                    String sqlPrev = "SELECT ol.PriceActual, o.C_Currency_ID, o.DateOrdered FROM C_OrderLine ol "
                            + "JOIN C_Order o ON o.C_Order_ID = ol.C_Order_ID "
                            + "WHERE o.IsSOTrx = 'N' AND o.DocStatus IN ('CO','CL') "
                            + "AND ol.M_Product_ID = ? AND o.C_Order_ID <> ? "
                            + "ORDER BY o.DateOrdered DESC, ol.C_OrderLine_ID DESC";

                    PreparedStatement pstmtPrev = null;
                    ResultSet rsPrev = null;
                    try {
                        pstmtPrev = DB.prepareStatement(sqlPrev, order.get_TrxName());
                        pstmtPrev.setInt(1, productID);
                        pstmtPrev.setInt(2, order.getC_Order_ID());
                        rsPrev = pstmtPrev.executeQuery();
                        if (!rsPrev.next()) continue; 
                        targetPOPrice = rsPrev.getBigDecimal("PriceActual");
                        poCurrencyID = rsPrev.getInt("C_Currency_ID");
                        convDate = rsPrev.getTimestamp("DateOrdered");
                    } finally {
                        DB.close(rsPrev, pstmtPrev);
                    }
                }

                if (targetPOPrice == null || targetPOPrice.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal poPriceConverted = SalesPriceCalculator.convertToTargetCurrency(
                        targetPOPrice, poCurrencyID, targetCurrencyID, convDate,
                        conversionTypeID, adClientID, adOrgID);

                if (poPriceConverted == null) {
                    log.warning("[AUTOPRICE] No conversion rate for M_Product_ID=" + productID + ". Skipping.");
                    continue;
                }

                String roundingType = product.get_ValueAsString("RoundingType");
                PriceResult result = SalesPriceCalculator.calculate(poPriceConverted, markup, roundingType);

                MProductPrice pp = MProductPrice.get(order.getCtx(), plvID, productID, order.get_TrxName());
                boolean isNewRecord = (pp == null);
                BigDecimal oldSalesPrice = isNewRecord ? BigDecimal.ZERO : pp.getPriceStd();

                if (isNewRecord) {
                    pp = new MProductPrice(order.getCtx(), plvID, productID, order.get_TrxName());
                }
                pp.setPriceList(result.salesPrice);
                pp.setPriceStd(result.salesPrice);
                pp.setPriceLimit(result.priceLimit);
                pp.saveEx();

                log.log(Level.INFO, "[AUTOPRICE] product ID=" + productID
                        + " newSalesPrice=" + result.salesPrice + " priceLimit=" + result.priceLimit);

                try {
                    MXXPriceHistory history = new MXXPriceHistory(order.getCtx(), productID, plvID,
                            order.getC_Order_ID(), order.get_TrxName());
                    history.setPriceOld(oldSalesPrice);
                    history.setPriceNew(result.salesPrice);
                    history.setMarkupPercent(markup);
                    history.setDescription(isRollback
                            ? "Rollback from Void/Reverse PO " + order.getDocumentNo()
                            : "Auto update from Complete PO " + order.getDocumentNo());
                    history.saveEx();
                } catch (Exception histEx) {
                    log.severe("[AUTOPRICE] Failed to log XX_PriceHistory product ID=" + productID + ": " + histEx.getMessage());
                }
            }
        }
    } catch (Exception e) {
        log.severe("Failed to process price rollback/update: " + e.getMessage());
    } finally {
        DB.close(rsPLV, pstmtPLV);
    }
  }
}
