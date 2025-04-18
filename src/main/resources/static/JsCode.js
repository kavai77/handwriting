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
        ctx.strokeStyle = 'blue';
        ctx.lineWidth = 18;
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
}

function send() {
    var img = document.getElementById("myCanvas").toDataURL("image/png");
    var showLoadingTimer = setTimeout(function () {
        $( "#loader" ).show();
        $( "#predictionTable" ).hide();
    }, 1000);
    $.ajax({
      url: "/service/prediction",
      method: "POST",
      contentType: "text/plain; charset=UTF-8",
      dataType: "json",
      data: img
    })
    .done(function( data ) {
      window.clearTimeout(showLoadingTimer);
      $( "#loader" ).hide();
      $( "#predictionTable" ).show();
      for (var i = 0; i < data.length; i++) {
          $('#method' + i).text(data[i].method);
          if (data[i].confidence == null) {
              $('#prediction' + i).text(data[i].prediction);
              $('#confidence' + i).text("Confidence value not available");
          } else if (data[i].confidence >= 0.5) {
              $('#prediction' + i).text(data[i].prediction);
              $('#confidence' + i).text((Math.round(data[i].confidence * 10000) / 100) + "%");
          } else {
              $('#prediction' + i).text("Not sure");
              $('#confidence' + i).text("Too low confidence rate from each output unit.");
          }
      }
    });
}

