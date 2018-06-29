var mousePressed = false;
var lastX, lastY;
var ctx;

function InitThis() {
    canvas = document.getElementById('myCanvas');
    ctx = canvas.getContext("2d");

    $('#myCanvas').mousedown(downEvent);
    $('#myCanvas').mousemove(moveEvent);
    $('#myCanvas').mouseup(upEvent);
	$('#myCanvas').mouseleave(upEvent);
	canvas.addEventListener("touchstart", downEvent);
	canvas.addEventListener("touchend", upEvent);
	canvas.addEventListener("touchmove", moveEvent);

    $('#weightTable').hide();
}

function downEvent(e) {
    mousePressed = true;
    Draw(e.pageX - $(this).offset().left, e.pageY - $(this).offset().top, false);
}

function upEvent(e) {
    mousePressed = false;
}

function moveEvent(e) {
    if (mousePressed) {
        Draw(e.pageX - $(this).offset().left, e.pageY - $(this).offset().top, true);
    }
}


function Draw(x, y, isDown) {
    if (isDown) {
        ctx.beginPath();
        ctx.strokeStyle = $('#selColor').val();
        ctx.lineWidth = $('#selWidth').val();
        ctx.lineJoin = "round";
        ctx.moveTo(lastX, lastY);
        ctx.lineTo(x, y);
        ctx.closePath();
        ctx.stroke();
    }
    lastX = x; lastY = y;
}

function clearArea() {
    // Use the identity matrix while clearing the canvas
    ctx.setTransform(1, 0, 0, 1, 0, 0);
    ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
    $('#prediction').text("-");
    $('#confidence').text("-");
    $('#weightTable').hide();
}

function send() {
    var img = document.getElementById("myCanvas").toDataURL("image/png");
    $.ajax({
      url: "/service/prediction",
      method: "POST",
      contentType: "text/plain; charset=UTF-8",
      dataType: "json",
      data: img
    })
    .done(function( data ) {
      var confidence = Math.round(data.weights[data.prediction] * 10000) / 100;
      if (confidence >= 50) {
          $('#prediction').text(data.prediction);
          $('#confidence').text(confidence + "%");
      } else {
          $('#prediction').text("Not sure");
          $('#confidence').text("Too low confidence rate from each output unit.");
      }
      for (i = 0; i <= 9; i++) {
        $('#w' + i).text(data.weights[i]);
      }
//      $('#weightTable').show();
    });
}
