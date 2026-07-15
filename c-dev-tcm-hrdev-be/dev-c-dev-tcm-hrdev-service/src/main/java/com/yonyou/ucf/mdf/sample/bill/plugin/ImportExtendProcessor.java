package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.ImportEventExtendProcessor;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * 导入处理器统一入口
 * @author aze
 * @date
 */
@Component
public class ImportExtendProcessor implements ImportEventExtendProcessor {

    private final Map<String, BiConsumer<ImportBatchData, ImportEventBO>> strategyMap = new HashMap<>();

    @Autowired
    public void initStrategies(TCMDriverServicePlugin p1, BusFaultDataRecord p2, DriverOvertimeRecordPlugin p3,
                               TcmAbsenceDataRecordPlugin p4, TcmControlCenterLogPlugin p5, TCMDriverSafePlugin p6,
                               TCMTrafficAccidentPlugin p7) {
        strategyMap.put("TCMDriverService", p1::beforeTranslate);
        strategyMap.put("tcmBusFaultDataRecord", p2::beforeTranslate);
        strategyMap.put("TCMSafety", p3::beforeTranslate);
        strategyMap.put("tcmAbsenceDataRecord", p4::beforeTranslate);
        strategyMap.put("tcmControlCenterLog", p5::beforeTranslate);
        strategyMap.put("TCMDriverSafe", p6::processBeforeImport);
        strategyMap.put("TCMTrafficAccident", p7::processBeforeImport);
    }

    @Override
    public void beforeTranslate(@NotNull ImportBatchData importBatchData, @NotNull ImportEventBO importEventBO) {
        ImportEventExtendProcessor.super.beforeTranslate(importBatchData, importEventBO);
        String busiObjCode = importEventBO.getImportContextDto().getBusiObj();
        BiConsumer<ImportBatchData, ImportEventBO> handler = strategyMap.get(busiObjCode);
        if (handler != null) {
            handler.accept(importBatchData, importEventBO);
        }
    }
}
