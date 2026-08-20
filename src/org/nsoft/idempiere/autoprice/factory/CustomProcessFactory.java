package org.nsoft.idempiere.autoprice.factory;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;
import org.custom.idempiere.autoprice.process.UpdateSalesPriceFromPO;

public class CustomProcessFactory implements IProcessFactory {

    @Override
    public ProcessCall newProcessInstance(String className) {
        // Cek apakah classname yang dipanggil dari Application Dictionary cocok dengan class custom
        if (className != null && className.equals(UpdateSalesPriceFromPO.class.getName())) {
            return new UpdateSalesPriceFromPO();
        }
        return null;
    }
}
