$(document).ready(function () {
    $(function () {
        $("#includedContent").load("home.html");
    });

    $("#home").click(function () {
        $("#includedContent").load("home.html");
    });
    $("#repositories").click(function () {
        $("#includedContent").load("repositories.html");
    });
    $("#issues").click(function () {
        $("#includedContent").load("issues.html");
    });
    $("#pull-requests").click(function () {
        $("#includedContent").load("pull-requests.html");
    });
    $("#bridge").click(function () {
        $("#includedContent").load("bridge.html");
    });
    $("#report").click(function () {
        $("#includedContent").load("report.html");
    });

    // Make selected tab active on click
    $(".nav a").on("click", function () {
        $(".nav").find(".active").removeClass("active");
        $(this).parent().addClass("active");
    });

});
