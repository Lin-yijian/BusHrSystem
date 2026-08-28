package com.yonyou.ucf.mdf.sample.service.impl;

import com.yonyou.yonbip.iuap.xport.importing.data.ImportBatchData;
import com.yonyou.yonbip.iuap.xport.importing.processor.importRuleCheck.ImportDataSaveStrategyExtend;
import com.yonyou.yonbip.iuap.xport.importing.processor.pojo.ImportEventBO;

/**
 * 注意事项
 * 版本兼容性：请注意文档中提到的“20240920 修改了默认逻辑”，确保您的平台版本与此机制匹配。在新逻辑下，如果 importDataSaveStrategyExtend 扩展为 null 或 accept 方法返回 false，平台将直接走默认导入流程。
 * 性能影响：在 doImportDataSaveStrategyCheck 方法中执行的校验逻辑应尽可能高效，避免复杂的数据库查询或远程调用，以免影响大批量导入的性能。
 * 错误处理：校验失败时，应提供清晰、准确的错误信息，方便用户定位和修改导入数据。确保异常信息能正确反馈到导入结果中。
 * 多单据支持：若需支持多种单据，请在 accept 方法中设计好判断逻辑（如使用 Set 存储支持的单据URI），确保扩展点能正确路由。
 * Bean 冲突：确保系统中只存在一个有效的 ImportDataSaveStrategyExtend 类型的 Bean。如果存在多个，需明确指定主 Bean 或通过 @Primary 注解标记。
 */
public class DefaultImportDataSaveStrategyExtendImpl implements ImportDataSaveStrategyExtend {
    @Override
    public boolean accept(ImportBatchData batchData, ImportEventBO event) {
        // 根据单据类型决定是否执行此扩展
        return false;
    }

    @Override
    public void doImportDataSaveStrategyCheck(ImportBatchData batchData, ImportEventBO event) {
        // 执行针对“采购订单”的特定校验逻辑
        // 1. 获取导入数据
        // List<BizObject> dataList = batchData.getDataList();
        // 2. 遍历数据进行业务规则校验
        // 3. 如果校验失败，可以：
        //    - 抛出 RuntimeException 并附带错误信息。
        //    - 或在 batchData 中设置错误信息（具体方式需参考平台API）。
        // 示例：校验数量必须大于0
    }
}