$("#report-generate").click(function() {
  $.get("/docs/generate", function(data, status){
    alert("Document: " + data + " updated!");
  });
});
