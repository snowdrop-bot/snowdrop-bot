var forksEnabled=true;
var reposEnabled=true;

$(document).ready(function() {

  $("#repositories-toggle").attr("checked", reposEnabled);
  $("#forks-toggle").attr("checked", forksEnabled);

  refreshDataTable();
 
  new EventSource("/reporting/fork/status").addEventListener('message', function(m) {
    var data = JSON.parse(m.data)
    $("#fork-progress").attr("style", "width: " + data.progress + "%");
    $("#fork-progress").text(data.message);
  });

  new EventSource("/reporting/repositories/status").addEventListener('message', function(m) {
    var data = JSON.parse(m.data)
    $("#repository-progress").attr("style", "width: " + data.progress + "%");
    $("#repository-progress").text(data.message);
  });

  $("#repositories-toggle").click(function() {
    reposEnabled = !reposEnabled
    $("#repositories-toggle").attr("checked", reposEnabled)
    refreshDataTable();
  });

  $("#forks-toggle").click(function() {
    console.log("forks-toggle: " + forksEnabled)
    forksEnabled = !forksEnabled
    $("#forks-toggle").attr("checked", forksEnabled)
    refreshDataTable()
  });

  $("#repositories-enable").click(function() {
    $.get("/reporting/enable", function(data, status) {
      $("#repositories-enable").attr("disabled", true);
      $("#repositories-disable").attr("disabled", false);
    });
  });

  $("#repositories-disable").click(function() {

    $.get("/reporting/disable", function(data, status) {
      $("#repositories-enable").attr("disabled", false);
      $("#repositories-disable").attr("disabled", true);
    });
  });

  $("#repositories-collect").click(function() {
    $.get("/reporting/collect/repositories", function(data, status) {
    });
  });

  $.ajax({url: "/reporting/status"}).then(function(data) {
    var enabled = JSON.parse(data);
    if (enabled) {
      $("#repositories-enable").attr("disabled", true);
      $("#repositories-disable").attr("disabled", false);
    } else {
      $("#repositories-enable").attr("disabled", false);
      $("#repositories-disable").attr("disabled", true);
    }
  });

  function refreshDataTable() {

    $('#repositories-table').DataTable( {
      destroy: true,
      processing: true,
      dom: 'Bfrtip',
      buttons: [
        'copy', 'csv', 'excel', 'pdf'
      ],
      columns: [
        {data: 'owner', type: 'string' },
        {data: 'name', type: 'string' },
        {data: 'url', type: 'string' },
      ],
      order: [[1, "desc"]],
      ajax: "/reporting/data/all-repositories?forksEnabled=" + forksEnabled + "&reposEnabled=" + reposEnabled
    });
  }
});
