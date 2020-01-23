$.ajax({url: "/bridge/status"}).then(function(data) {
  if (data) {
    $("#bridge-enable").attr("disabled", true);
    $("#bridge-disable").attr("disabled", false);
  }
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
