cb.define(process.env.__DOMAINKEY__, [], function () {
  var common_VM = {
    bindJumpToCorrection: function (viewModel, buttonCode) {
      var btn = viewModel.get(buttonCode);
      if (!btn) return;
      btn.on('click', function () {
        var rows = viewModel.getSelectedRows ? viewModel.getSelectedRows() : [];
        if (!rows || rows.length === 0) {
          cb.utils.alert('请先选择一条记录', 'error');
          return;
        }
        var row = rows[0];
        cb.rest.invokeFunction(
          'TCMCaptainAttendance.backDesignerFunction.queryAttendanceCorrectionByKey',
          { date: row.date, employee_id: row.employee_id, task_code: row.task_code },
          function (err, res) {
            if (err || !res || !res.success || !res.id) {
              cb.utils.alert('目标考勤补签记录不存在', 'error');
              return;
            }
            cb.loader.runCommandLine('bill', {
              billtype: 'voucher',
              billno: 'tcmAttendanceCorrection',
              domainKey: 'c-dev-tcm-hrdev',
              params: {
                mode: 'edit',
                id: res.id,
                query: { busiObj: 'tcmAttendanceCorrection', serviceCode: 'tcmAttendanceCorrection' }
              }
            }, viewModel);
          }
        );
      });
    },
    bindBackButton: function (viewModel, buttonCode) {
  var btn = viewModel.get(buttonCode);
  if (!btn) return;
  btn.on('click', function () {
    cb.loader.runCommandLine('bill', {
      billtype: 'voucherlist',
      billno: 'tcmAttendanceCheckRecordList',
      domainKey: 'c-dev-tcm-hrdev',
      params: {
        query: { busiObj: 'tcmAttendanceCheckRecord', serviceCode: 'tcmAttendanceCheckRecordList' }
      }
    }, viewModel);
  });
}

    
  };

  
  try { module.exports = common_VM; } catch (e) {}
  return common_VM;
});