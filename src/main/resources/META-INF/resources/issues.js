$(document).ready(function() {

  $("#report-enable").click(function() {
    $.get("/reporting/enable", function(data, status) {
      $("#report-enable").attr("disabled", true);
      $("#report-disable").attr("disabled", false);
    });
  });

  $("#report-disable").click(function() {
    $.get("/reporting/disable", function(data, status) {
      $("#report-enable").attr("disabled", false);
      $("#report-disable").attr("disabled", true);
    });
  });

  $("#report-issues-collect").click(function() {
    $.get("/reporting/collect/issue", function(data, status) {
    });
  });

  $.ajax({url: "/reporting/status"}).then(function(data) {
    var enabled = JSON.parse(data);
    if (enabled) {
      $("#report-enable").attr("disabled", true);
      $("#report-disable").attr("disabled", false);
    } else {
      $("#report-enable").attr("disabled", false);
      $("#report-disable").attr("disabled", true);
    }
  });

  $.ajax({url: "/reporting/start-time"}).then(function(data) {
    if (data) {
      var d = new Date(data);
      d.setHours(12)
      d.setMinutes(00)
      var dateText =  d.getDate() + '/' + (d.getMonth() + 1) + '/' + d.getFullYear() + " 12:00 AM";
      $('#start-date').datetimepicker({
        defaultDate: d,
        sideBySide: true,
      });
    }
  });

  $.ajax({url: "/reporting/end-time"}).then(function(data) {
    if (data) {
      var d = new Date(data);
      d.setHours(12)
      d.setMinutes(00)
      var dateText =  d.getDate() + '/' + (d.getMonth() + 1) + '/' + d.getFullYear() + "12:00 AM";
      $('#end-date').datetimepicker({
        defaultDate: d,
        sideBySide: true,
      });
    }
  });

  $('#issues-table').DataTable( {
    processing: true,
    dom: 'Bfrtip',
    buttons: [
      'copy', 'csv', 'excel', 'pdf'
    ],
    columns: [
      {data: 'assignee', type: 'string' },
      {data: 'repository', type: 'string' },
      {data: 'url', type : 'num', render: dataTablesRenderNumber},
      {data: 'title', type: 'string' },
      {data: 'open', type: 'boolean' },
      {data: 'createdAt', type: 'string', render: dataTablesRenderDate},
      {data: 'updatedAt', type: 'string', render: dataTablesRenderDate},
      {data: 'closedAt', type: 'string', render: dataTablesRenderDate}
    ],
    order: [[3, "desc"]],
    ajax: "/reporting/data/issues"
  });
});
