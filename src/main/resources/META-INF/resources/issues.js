var dataTablesRenderDate = function (data, type, row) {
    // If display or filter data is requested, format the date
    if (data == null) {
        return "--"
    }
    if ( type === 'display' || type === 'filter' ) {
        var d = new Date( data );
        return d.getDay() + '/' + (d.getMonth()+1) + '/' + d.getFullYear();
    }

    // Otherwise the data type requested (`type`) is type detection or
    // sorting data, for which we want to use the integer, so just return
    // that, unaltered
    return data;
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
            {data: 'number', type : 'num'},
            {data: 'title', type: 'string' },
            {data: 'open', type: 'boolean' },
            {data: 'createdAt', type: 'string', render: dataTablesRenderDate},
            {data: 'closedAt', type: 'string', render: dataTablesRenderDate}
        ],
        order: [[3, "desc"]],
        ajax: "/reporting/data/issues"
    });
});
