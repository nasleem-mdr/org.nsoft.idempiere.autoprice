package org.custom.idempiere.autoprice.model;

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
