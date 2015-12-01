/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var wsUri = "ws://" + document.location.host + document.location.pathname + "whiteboardendpoint";
var websocket = new WebSocket(wsUri);
var xlatitude = 39.50;
var ylongitude = -98.35;
websocket.onerror = function(evt) { onError(evt) };

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}
websocket.onmessage = function(evt) { onMessage(evt) };

function sendText(json) {
    console.log("sending text: " + json);
    window.alert("sending");
    websocket.send(json);
   // websocket.send("WASSSUP");
}
              
function onMessage(evt) {
    console.log("received: " + evt.data);
    window.alert("received");
    window.alert(evt.data); 
}
function get_coordinates(){
    
    return {
        x: xlatitude,
        y: ylongitude,
    };
}

function initMap() {


    var sai = new google.maps.LatLng(39.50,-98.35);
    var map = new google.maps.Map(document.getElementById('map'), {
            center: sai,
            zoom: 5
    });

    var marker = null;
    var coordInfoWindow = null;
    google.maps.event.addListener(map, 'click', function (event) {

            xlatitude = event.latLng.lat();
            ylongitude = event.latLng.lng();

            coordinates = new google.maps.LatLng(xlatitude, ylongitude);

            if(marker)
            {
                    marker.setMap(null);
            }

            marker = new google.maps.Marker({position: event.latLng, map: map});

            if (coordInfoWindow) {
                    coordInfoWindow.close();
            }

            coordInfoWindow = new google.maps.InfoWindow();
            coordInfoWindow.setContent(createInfoWindowContent(coordinates, map.getZoom()));
            coordInfoWindow.setPosition(coordinates);
            coordInfoWindow.open(map, marker);

            map.addListener('zoom_changed', function() {
            coordInfoWindow.setContent(createInfoWindowContent(coordinates, map.getZoom()));
            coordInfoWindow.open(map, marker);
            });

    });

}


function createInfoWindowContent(latLng, zoom) {

    return [
            'LatLng: ' + latLng,
    ].join('<br>');
}

