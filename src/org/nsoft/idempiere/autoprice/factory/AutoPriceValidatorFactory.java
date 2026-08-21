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

import org.adempiere.base.IModelValidatorFactory;
import org.compiere.model.ModelValidator;
import org.nsoft.idempiere.autoprice.validator.SalesPriceAutoUpdateValidator;

/**
* Factory connecting ModelValidationEngine (via Core.getModelValidator)
* with the custom ModelValidator implementation in this plugin.
*
* Required for the custom ModelValidator class to be loaded from the OSGi bundle;
* registering the class as an OSGi ModelValidator service directly is not sufficient, 
* as ModelValidationEngine performs lookup through * IModelValidatorFactory, not a direct 
* service lookup.
*/
public class AutoPriceValidatorFactory implements IModelValidatorFactory {

    @Override
    public ModelValidator newModelValidatorInstance(String className) {
        if (SalesPriceAutoUpdateValidator.class.getName().equals(className)) {
            return new SalesPriceAutoUpdateValidator();
        }
        return null;
    }
}