$("#report-generate").click(function() {
  $.get("/docs/generate", function(data, status){
    alert("Document: " + data + " updated!");
  });
});

$(document).ready(function() {
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
});
