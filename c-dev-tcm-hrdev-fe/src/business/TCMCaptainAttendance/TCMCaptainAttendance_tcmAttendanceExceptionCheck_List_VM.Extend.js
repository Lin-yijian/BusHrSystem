cb.define(process.env.__DOMAINKEY__, ['common/common_VM.Extend.js'], function (common) {
  var XXX_VM = {
    doAction: function (name, viewModel) { if (this[name]) this[name](viewModel); },
    init: function (viewModel) {
      common.bindJumpToCorrection(viewModel, 'button7cj');
      common.bindBackButton(viewModel, 'button46cb');

      viewModel.on('afterLoadMeta', function () {
        hideOvertimeHours(viewModel);
        showExceptionFields(viewModel);
      });

      viewModel.on('afterQuery', function () {
        dedupeExceptionRows(viewModel);
      });

      var grid = viewModel.getGridModel && viewModel.getGridModel();
      if (grid && grid.on) {
        grid.on('afterSetDataSource', function () {
          dedupeExceptionRows(viewModel);
        });
      }
    }
  };

  var HIDE_FIELDS = ['overtime_hours', 'overtime_time'];
  var SHOW_FIELDS = [
    'overtime_duration',
    'bus_fault',
    'control_center_log',
    'absence_category',
    'absence_reason',
    'absence_record_count'
  ];

  function hideOvertimeHours(viewModel) {
    setFieldsVisible(viewModel, HIDE_FIELDS, false);
  }

  function showExceptionFields(viewModel) {
    setFieldsVisible(viewModel, SHOW_FIELDS, true);
  }

  function setFieldsVisible(viewModel, fields, visible) {
    var grid = viewModel.getGridModel && viewModel.getGridModel();
    fields.forEach(function (name) {
      var field = viewModel.get(name);
      if (field && field.setState) {
        field.setState('visible', visible);
      }
      if (grid && grid.setColumnState) {
        grid.setColumnState(name, 'bShowIt', visible);
        grid.setColumnState(name, 'bHidden', !visible);
        grid.setColumnState(name, 'visible', visible);
      }
    });
  }

  function rowDate(row) {
    var d = row && row.date;
    if (!d) return '';
    return String(d).substring(0, 10);
  }

  function rowEmp(row) {
    var e = row && row.employee_id;
    if (!e) return '';
    if (typeof e === 'object') {
      return e.id || e.value || e.code || '';
    }
    return String(e);
  }

  function dedupeExceptionRows(viewModel) {
    var grid = viewModel.getGridModel && viewModel.getGridModel();
    if (!grid || grid.__tcmDeduping) {
      return;
    }
    var rows = (grid.getRows && grid.getRows()) || (grid.getData && grid.getData()) || [];
    if (!rows.length) {
      return;
    }
    var seen = {};
    var unique = [];
    for (var i = 0; i < rows.length; i++) {
      var key = rowDate(rows[i]) + '|' + rowEmp(rows[i]);
      if (!seen[key]) {
        seen[key] = true;
        unique.push(rows[i]);
      }
    }
    if (unique.length === rows.length) {
      return;
    }
    grid.__tcmDeduping = true;
    try {
      if (grid.setDataSource) {
        grid.setDataSource(unique);
      } else if (grid.setData) {
        grid.setData(unique);
      } else if (grid.setRows) {
        grid.setRows(unique);
      }
    } finally {
      grid.__tcmDeduping = false;
    }
  }

  try { module.exports = XXX_VM; } catch (e) {}
  return XXX_VM;
});
