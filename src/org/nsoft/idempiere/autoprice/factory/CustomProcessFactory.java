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
package org.nsoft.idempiere.autoprice.factory;

import org.adempiere.base.IProcessFactory;
import org.compiere.process.ProcessCall;
import org.nsoft.idempiere.autoprice.process.UpdateSalesPriceFromPO;

public class CustomProcessFactory implements IProcessFactory {

    @Override
    public ProcessCall newProcessInstance(String className) {
    	// Check if the classname called from the Application Dictionary matches the custom class
        if (className != null && className.equals(UpdateSalesPriceFromPO.class.getName())) {
            return new UpdateSalesPriceFromPO();
        }
        return null;
    }
}
