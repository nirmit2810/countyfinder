/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var canvas = document.getElementById("myCanvas");
var context = canvas.getContext("2d");
canvas.addEventListener("click", defineImage, false);

function send_data() {
    var coordinates = get_coordinates();
    
    var json = JSON.stringify({
        "coords": {
            "Latitude": coordinates.x,
            "Longitude": coordinates.y,
        }
    });

    if( coordinates.x != null && coordinates.y != null )
    {
        sendText(json);
    } else
    {
        window.alert("Point not found");
    }
}

function rect_data() {
    var coordinates = get_rectcoordinates();
  
    var json = JSON.stringify({
        "coords": {
            "North_east": coordinates.max,
            "South_west": coordinates.min,
        }
    });
    if( coordinates.max != null && coordinates.min != null )
    {
        sendText(json);
    } else
    {
        window.alert("Rectangle not found");
    }
}