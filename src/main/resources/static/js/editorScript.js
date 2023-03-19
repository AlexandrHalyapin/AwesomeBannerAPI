$(document).ready(function () {
    console.log("jquery is work")

    $('#send').click(function () {
        console.log("catch click!")

        $.ajax({
            url: document.URL + "/createNewGroup",
            type: 'POST',
            data: {
                name: $('#name').value,
                text: $('#name').value,
                price: $('#name').value,
            }
        }).done(function () {

        }).fail(function () {

        });
    });
})