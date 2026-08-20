package org.nsoft.idempiere.autoprice.factory;

import java.sql.ResultSet;
import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;
import org.custom.idempiere.autoprice.model.MXXPriceHistory;
import org.custom.idempiere.autoprice.model.X_XX_PriceHistory;

public class CustomModelFactory implements IModelFactory {

    @Override
    public Class<?> getClass(String tableName) {
        if (X_XX_PriceHistory.Table_Name.equals(tableName)) {
            return MXXPriceHistory.class;
        }
        return null;
    }

    @Override
    public PO getPO(String tableName, int Record_ID, String trxName) {
        if (X_XX_PriceHistory.Table_Name.equals(tableName)) {
            return new MXXPriceHistory(Env.getCtx(), Record_ID, trxName);
        }
        return null;
    }

    @Override
    public PO getPO(String tableName, ResultSet rs, String trxName) {
        if (X_XX_PriceHistory.Table_Name.equals(tableName)) {
            return new MXXPriceHistory(Env.getCtx(), rs, trxName);
        }
        return null;
    }
}
