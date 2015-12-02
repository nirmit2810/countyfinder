/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var wsUri = "ws://" + document.location.host + document.location.pathname + "whiteboardendpoint";
var websocket = new WebSocket(wsUri);
var xlatitude = 39.50;
var ylongitude = -98.35;
var mapStuff = {
    //this is the "global" vars of this code sample as I have love for namespacing my globals
    theMap : null, // the global var holding the reference to the map
    markerList : [] // to be an array holding the list of markers
};
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
    console.log(evt.data); 
    var obj = JSON.parse(evt.data);
    var i;
    for(i=0; i<obj.latitude.length;i++)
    {    
       // console.log(obj.latitude[i]);
       // console.log(obj.longitude[i]);
        var myLatLng = {lat: parseFloat(obj.latitude[i]), lng: parseFloat(obj.longitude[i])};
        var marker = new google.maps.Marker({
           position: myLatLng,
           map: mapStuff.theMap,
           title: obj.latitude[i] + obj.longitude[i] ,
        });
         window.alert("created");
    }
}
function get_coordinates(){
    
    return {
        x: xlatitude,
        y: ylongitude
    };
}

function initMap() {


    var sai = new google.maps.LatLng(39.50,-98.35);
    mapStuff.theMap = new google.maps.Map(document.getElementById('map'), {
            center: sai,
            zoom: 5
    });

    var marker = null;
    var coordInfoWindow = null;
    google.maps.event.addListener( mapStuff.theMap, 'click', function (event) {
   
            xlatitude = event.latLng.lat();
            ylongitude = event.latLng.lng();

            coordinates = new google.maps.LatLng(xlatitude, ylongitude);

            if(marker)
            {
                    marker.setMap(null);
            }

            marker = new google.maps.Marker({position: event.latLng, map:  mapStuff.theMap});

            if (coordInfoWindow) {
                    coordInfoWindow.close();
            }

            coordInfoWindow = new google.maps.InfoWindow();
            coordInfoWindow.setContent(createInfoWindowContent(coordinates,  mapStuff.theMap.getZoom()));
            coordInfoWindow.setPosition(coordinates);
            coordInfoWindow.open( mapStuff.theMap, marker);

            map.addListener('zoom_changed', function() {
            coordInfoWindow.setContent(createInfoWindowContent(coordinates,  mapStuff.theMap.getZoom()));
            coordInfoWindow.open( mapStuff.theMap, marker);
            });

    });

}


function createInfoWindowContent(latLng, zoom) {

    return [
            'LatLng: ' + latLng
    ].join('<br>');
}

