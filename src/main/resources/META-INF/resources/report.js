var startTime
var endTime

$("#report-generate").click(function() {
  if (startTime != null && endTime != null) {
    $.get("/docs/generate?startTime=" + formatDate(startTime) + "&endTime=" + formatDate(endTime), function(data, status){
      alert("Document: " + data + " updated!")
    })
  } else {
      alert("Please define the period!")
  }
})

$("#report-generate-md").click(function() {
  if (strStartTime != null && strEndTime != null) {
    $.get("/weeklyreport/generate?startTime=" + formatDate(new Date(strStartTime.value)) + "&endTime=" + formatDate(new Date(strEndTime.value)), function(data, status){
      alert("Document: updated!")
    })
  } else {
      alert("Please define the period!")
  }
})

$("#report-publish-md").click(function() {
  if (strStartTime != null && strEndTime != null) {
    $.get("/weeklyreport/publish?startTime=" + formatDate(new Date(strStartTime.value)) + "&endTime=" + formatDate(new Date(strEndTime.value)), function(data, status){
      alert("Document: updated!")
    })
  } else {
      alert("Please define the period!")
  }
})

$(document).ready(function() {
  $.ajax({url: "/reporting/start-time"}).then(function(data) {
    if (data) {
      startTime = new Date(data)
      startTime.setHours(12)
      startTime.setMinutes(00)
      var dateText =  formatDate(startTime) + " 12:00 AM"
      $('#start-date').datetimepicker({
        defaultDate: startTime,
        sideBySide: true,
      })
    }
  })

  $.ajax({url: "/reporting/end-time"}).then(function(data) {
    if (data) {
      endTime = new Date(data)
      endTime.setHours(12)
      endTime.setMinutes(00)
      var dateText =  formatDate(endTime) + "12:00 AM"
      $('#end-date').datetimepicker({
        defaultDate: endTime,
        sideBySide: true,
      })
    }
  })
})
