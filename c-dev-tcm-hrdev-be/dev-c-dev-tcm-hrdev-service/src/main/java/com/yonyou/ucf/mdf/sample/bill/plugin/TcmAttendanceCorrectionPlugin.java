package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.ucf.mdf.sample.enums.BaseEnum;
import com.yonyou.ypd.bill.annotation.BillPlugin;
import com.yonyou.ypd.bill.context.BillQueryContext;
import com.yonyou.ypd.bill.plugin.AbstractBillPlugin;
import lombok.extern.slf4j.Slf4j;
import org.imeta.orm.schema.QueryCondition;
import org.imeta.orm.schema.QuerySchema;

@BillPlugin(busiObj = "tcmAttendanceCorrection")
@Slf4j
public class TcmAttendanceCorrectionPlugin extends AbstractBillPlugin {

    @Override
    public void beforeBillListQuery(BillQueryContext billContext, QuerySchema querySchema) throws Exception {
        super.beforeBillListQuery(billContext, querySchema);
//        if ("bocInfoCash".equals(billContext.getBaseBillContext().getServiceCode())){
//            querySchema.appendQueryCondition(QueryCondition.name("terminal_id").is_null());
//        }
    }


}
