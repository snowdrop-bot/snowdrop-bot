$(document).ready(function () {

    console.log("naviation started")
    $(function () {
        $("#includedContent").load("home.html");
    });

    $("#home").click(function () {
        $("#includedContent").load("home.html");
    });
    $("#issues").click(function () {
        console.log("naviation started")
        $("#includedContent").load("issues.html");
    });
    $("#pull-requests").click(function () {
        console.log("naviation started")
        $("#includedContent").load("pull-requests.html");
    });
    $("#bridge").click(function () {
        $("#includedContent").load("bridge.html");
    });

    // Make selected tab active on click
    $(".nav a").on("click", function () {
        $(".nav").find(".active").removeClass("active");
        $(this).parent().addClass("active");
    });

});
