$(document).ready(function() {
  $.ajax({url: "/bridge/status"}).then(function(data) {
    var enabled = JSON.parse(data);
    if (enabled) {
      $("#bridge-enable").attr("disabled", true);
      $("#bridge-disable").attr("disabled", false);
    } else {
      $("#bridge-enable").attr("disabled", false);
      $("#bridge-disable").attr("disabled", true);
    }
  });

  $.ajax({url: "/bridge/source-repos"}).then(function(data) {
    $.each(data, function( index, val ) {
      $("#source-repo-list").append("<li class='list-group-item'>" + val + "</li>");
    });
  });

  $("#bridge-enable").click(function() {
    $.get("/bridge/enable", function(data, status) {
      $("#bridge-enable").attr("disabled", true);
      $("#bridge-disable").attr("disabled", false);
    });
  });

  $("#bridge-disable").click(function() {
    $.get("/bridge/disable", function(data, status) {
      $("#bridge-enable").attr("disabled", false);
      $("#bridge-disable").attr("disabled", true);
    });
  });

  $("#bridge-now").click(function() {
    $.get("/bridge/now", function(data, status) {
    });
  });

});
