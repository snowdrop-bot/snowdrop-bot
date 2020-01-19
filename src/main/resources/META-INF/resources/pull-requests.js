$(document).ready(function() {
    $('#pull-requests-table').DataTable( {
        processing: true,
        dom: 'Bfrtip',
        buttons: [
            'copy', 'csv', 'excel', 'pdf'
        ],
        columns: [
            {data: 'creator', type: 'string' },
            {data: 'repository', type: 'string' },
            {data: 'number', type : 'num'},
            {data: 'title', type: 'string' },
            {data: 'open', type: 'boolean' },
            {data: 'createdAt', type: 'string'},
            {data: 'closedAt', type: 'string'}
        ],
        order: [[3, "desc"]],
        ajax: "/reporting/data/pr"
    });
});
