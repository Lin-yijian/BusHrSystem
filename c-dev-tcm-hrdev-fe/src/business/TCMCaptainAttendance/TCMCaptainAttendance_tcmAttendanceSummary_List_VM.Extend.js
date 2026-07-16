cb.define(process.env.__DOMAINKEY__, ['common/common_VM.Extend.js'], function (common) {
  var XXX_VM = {
    doAction: function (name, viewModel) { if (this[name]) this[name](viewModel); },
    init: function (viewModel) {
        alert('脚本已加载');
      common.bindJumpToCorrection(viewModel, 'button104mj');
      common.bindBackButton(viewModel, 'button73yi');
    }
  };
  try { module.exports = XXX_VM; } catch (e) {}
  return XXX_VM;
});