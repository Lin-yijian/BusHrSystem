package com.yonyou.ucf.mdf.sample.bill.plugin;

import com.yonyou.yonbip.iuap.xport.importing.processor.importRuleCheck.DefaultImportDataSaveStrategyExtendImpl;
import com.yonyou.yonbip.iuap.xport.importing.processor.importRuleCheck.ImportDataSaveStrategyExtend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 系统注册Bean
 * 自 2024-09-20 起平台要求显式注册本扩展才会覆盖默认导入保存策略。
 * </p>
 */
@Configuration
public class ImportExtendConfig {

    /**
     * 导入规则数据校验领域扩展
     */
    @Bean
    public ImportDataSaveStrategyExtend importDataSaveStrategyExtend() {
        return new DefaultImportDataSaveStrategyExtendImpl();
    }

}
