# Plugin PriceList Auto Update 
IDempiere Plugin to Generate Auto Sales Pricelist and Purchase Pricelist itself based on Purchase Order transaction.

For businesses—particularly trading businesses dealing with fluctuating market prices—a system is needed that can automatically update selling prices based on purchase costs. We can apply a specific percentage or a markup to the purchase price.

This plugin applies price updates at the product level, allowing for flexible markup adjustments for each individual product. Not all of sales price list can be auto update, especially sales price list based on Contract, so we can choose price list version that can be updated by Mark isAutoUdatefromPO. 
This plugin also comes with a feature for rounding off irregular numbers, ensuring that prices do not end up looking odd or awkward—particularly important for the Indonesian market.

Changes of sales price list are recorded or stored in the Price History table, which can be used for analysis or as evidence of the changes.

Pack-in
1. Add new column (isAutoUdatefromPO) on m_pricelist_version
2. Add Column (MarkupPercent, RoundingType) on M_Product
3. Add process org.nsoft.idempiere.autoprice.process.updateAutopriceFromPO.
4. Add validator org.nsoft.idempiere.autoprice.validator.SalesPriceAutoUpdateValidator
   
