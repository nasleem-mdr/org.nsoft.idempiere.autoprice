package org.nsoft.idempiere.autoprice.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.compiere.model.MProductPrice;
import org.compiere.process.SVR_Process;
import org.compiere.util.DB;

public class UpdateSalesPriceFromPO extends SVR_Process {

    @Override
    protected void prepare() {
        // Tempat mengambil parameter jika process dijalankan dengan window filter
    }

    @Override
    protected String doIt() throws Exception {
        // 1. Ambil semua Sales Price List Version aktif yang bertanda IsAutoUpdateFromPO = 'Y'
        String sqlPLV = "SELECT M_PriceList_Version_ID FROM M_PriceList_Version WHERE IsAutoUpdateFromPO='Y' AND IsActive='Y'";
        
        int updatedCount = 0;
        PreparedStatement pstmtPLV = null;
        ResultSet rsPLV = null;

        try {
            pstmtPLV = DB.prepareStatement(sqlPLV, get_TrxName());
            rsPLV = pstmtPLV.executeQuery();

            while (rsPLV.next()) {
                int plvID = rsPLV.getInt("M_PriceList_Version_ID");

                // 2. Query PO terakhir per produk yang memiliki MarkupPercent
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

                    // Formulasi: Sales Price = PO Price * (1 + (Markup / 100))
                    BigDecimal multiplier = BigDecimal.ONE.add(markup.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                    BigDecimal newSalesPrice = lastPOPrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

                    // 3. Upsert data ke M_ProductPrice
                    MProductPrice pp = MProductPrice.get(getCtx(), plvID, productID, get_TrxName());
                    if (pp == null) {
                        pp = new MProductPrice(getCtx(), plvID, productID, get_TrxName());
                    }
                    pp.setPriceList(newSalesPrice);
                    pp.setPriceStd(newSalesPrice);
                    pp.setPriceLimit(newSalesPrice);
                    pp.saveEx();

                    updatedCount++;
                }
                DB.close(rs, pstmt);
            }
        } finally {
            DB.close(rsPLV, pstmtPLV);
        }

        return "Berhasil memperbarui " + updatedCount + " record harga produk pada Sales Pricelist.";
    }
}
