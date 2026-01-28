/**
 * 所属节点：五险二金单位信息变更-列表页面
 * 创建人：liud
 * 创建时间：2020年10月30日
 * 最后修改人：liud
 */
cb.define(process.env.__DOMAINKEY__, [], function () {
    let GT40884AT9_9a33f420List_VM_Extend = {
        doAction: function (name, viewModel) {
            if (this[name])
                this[name](viewModel);
        },
        init: function (viewModel) {
            alert('1234, 啦啦啦啦啦啦');
            console.log('测试调试 - 1');
            console.log('测试调试 - 2');
            viewModel.get('button687ub').on('click', function () {
                console.log('测试脚本是否起作用！！');
            })
        }
    }
    try {
        module.exports = GT40884AT9_9a33f420List_VM_Extend;
    } catch (error) {

    }
    return GT40884AT9_9a33f420List_VM_Extend;
});