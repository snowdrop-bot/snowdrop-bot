$("#report-generate").click(function() {
  $.get("/docs/generate", function(data, status){
    alert("Document: " + data + " updated!");
  });
});

$.extend(true, $.fn.datetimepicker.defaults, {
    icons: {
      time: 'fa fa-clock',
      date: 'fa fa-calendar',
      up: 'fa fa-angle-up',
      down: 'fa fa-angle-down',
      previous: 'fa fa-angle-left',
      next: 'fa fa-angle-right',
      today: 'fas fa-calendar-check',
      clear: 'far fa-trash-alt',
      close: 'far fa-times-circle'
    }
});

$(document).ready(function() {
  $('#start-date').datetimepicker({
    useCurrent: false,
    inline: true,
    sideBySide: true
  });
  $('#end-date').datetimepicker({
    useCurrent: false,
    inline: true,
    sideBySide: true
  });
  $.ajax({url: "/reporting/start-time"}).then(function(data) {
    if (data) {
      var d = new Date(data);
      var dateText =  d.getDate() + '/' + (d.getMonth() + 1) + '/' + d.getFullYear() + " 12:00 AM";
      $("#start-date").val(dateText);
    }
  });

  $.ajax({url: "/reporting/end-time"}).then(function(data) {
    if (data) {
      var d = new Date(data);
      var dateText =  d.getDate() + '/' + (d.getMonth() + 1) + '/' + d.getFullYear() + "12:00 AM";
      $("#end-date").val(dateText);
    }
  });
});
