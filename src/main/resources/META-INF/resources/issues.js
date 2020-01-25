$.ajax({url: "/reporting/status"}).then(function(data) {
  if (data) {
    $("#report-enable").attr("disabled", true);
    $("#report-disable").attr("disabled", false);
  }
});

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

//DataTables
var dataTablesRenderDate = function (data, type, row) {
    // If display or filter data is requested, format the date
    if (data == null) {
        return "--"
    }
    if ( type === 'display' || type === 'filter' ) {
        var d = new Date( data );
        return d.getDate() + '/' + (d.getMonth()+1) + '/' + d.getFullYear();
    }

    // Otherwise the data type requested (`type`) is type detection or
    // sorting data, for which we want to use the integer, so just return
    // that, unaltered
    return data;
}

var dataTablesRenderNumber = function (data, type, row) {
    var url = data;
    var num = data.substring(data.lastIndexOf('/') + 1);
    return "<a href='" + url +"'>" +num +"</a>";
}

$(document).ready(function() {

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
