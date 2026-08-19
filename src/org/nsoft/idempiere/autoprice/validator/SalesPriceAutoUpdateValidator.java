package org.nsoft.idempiere.autoprice.validator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.MClient;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

public class SalesPriceAutoUpdateValidator implements ModelValidator {

    private static final CLogger log = CLogger.getCLogger(SalesPriceAutoUpdateValidator.class);
    private int m_AD_Client_ID = -1;

    @Override
    public void initialize(ModelValidationEngine engine, MClient client) {
        if (client != null) {
            m_AD_Client_ID = client.getAD_Client_ID();
        }
        // Register listener untuk tabel C_Order
        engine.addModelChange(MOrder.Table_Name, this);
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
            
            // 1. Jika PO Complete -> Hitung harga dari PO ini
            if (MOrder.DOCSTATUS_Completed.equals(docStatus)) {
                updateSalesPriceList(order, false);
            } 
            // 2. Jika PO di-Void/Reversed -> Rollback ke PO sebelumnya
            else if (MOrder.DOCSTATUS_Voided.equals(docStatus) 
                  || MOrder.DOCSTATUS_Reversed.equals(docStatus)) {
                updateSalesPriceList(order, true);
            }
        }
    }
    return null;
}

    @Override
    public String docValidate(PO po, int timing) {
        return null; // Tidak digunakan
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

            for (MOrderLine line : order.getLines()) {
                int productID = line.getM_Product_ID();
                if (productID <= 0) continue;

                MProduct product = line.getProduct();
                BigDecimal markup = (BigDecimal) product.get_Value("MarkupPercent");
                if (markup == null || markup.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal targetPOPrice = null;

                if (!isRollback) {
                    // Kasus normal: Gunakan harga dari PO saat ini
                    targetPOPrice = line.getPriceActual();
                } else {
                    // Kasus Rollback: Cari harga PO terakhir yang masah VALIID (DocStatus CO/CL, mengabaikan PO ini)
                    String sqlPreviousPO = "SELECT ol.PriceActual FROM C_OrderLine ol "
                            + "JOIN C_Order o ON o.C_Order_ID = ol.C_Order_ID "
                            + "WHERE o.IsSOTrx = 'N' AND o.DocStatus IN ('CO','CL') "
                            + "AND ol.M_Product_ID = ? AND o.C_Order_ID <> ? "
                            + "ORDER BY o.DateOrdered DESC, ol.C_OrderLine_ID DESC";

                    targetPOPrice = DB.getSQLValueBDEx(order.get_TrxName(), sqlPreviousPO, productID, order.getC_Order_ID());
                }

                // Jika ditemukan harga PO acuan (baik dari PO baru atau PO sebelumnya)
                if (targetPOPrice != null && targetPOPrice.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal multiplier = BigDecimal.ONE.add(markup.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    BigDecimal newSalesPrice = targetPOPrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

                    MProductPrice pp = MProductPrice.get(order.getCtx(), plvID, productID, order.get_TrxName());
                    if (pp == null) {
                        pp = new MProductPrice(order.getCtx(), plvID, productID, order.get_TrxName());
                    }
                    pp.setPriceList(newSalesPrice);
                    pp.setPriceStd(newSalesPrice);
                    pp.setPriceLimit(newSalesPrice);
                    pp.saveEx();
                }
            }
        }
    } catch (Exception e) {
        log.severe("Gagal memproses rollback/update harga: " + e.getMessage());
    } finally {
        DB.close(rsPLV, pstmtPLV);
    }
}
}
