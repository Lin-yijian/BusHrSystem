/**
 * 新增editPlus，导出的action移动端和PC端可共用
 * 如果导出的action名称和系统action相同时，则覆盖系统action
 * 如果导出的action名称和系统action不相同时，则新增该action
 *
 * @param {*} billNo 单据编码
 * @param {*} viewmodel 单据模型
 * @param {*} params 参照
 * @param {*} beforeAct 前置事件
 * @param {*} afterAct 后置事件
 */
const editPlus = (billNo, viewmodel, params, beforeAct, afterAct) => {
  // 组装params参数
  beforeAct(params, () => {
    console.log('editPlus action的逻辑代码', billNo, viewmodel, params);
    afterAct({
      // 参数
    });
  })
}

const actions = {
  editplus: editPlus
}
export default actions
