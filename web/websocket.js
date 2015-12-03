/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


var wsUri = "ws://" + document.location.host + document.location.pathname + "whiteboardendpoint";
var websocket = new WebSocket(wsUri);
var xlatitude = null;
var ylongitude = null;
var north_east = null;
var south_west = null;
var mapStuff = {
    //this is the "global" vars of this code sample as I have love for namespacing my globals
    theMap : null, // the global var holding the reference to the map
    markerList : [], // to be an array holding the list of markers
};
websocket.onerror = function(evt) { onError(evt) };

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}
websocket.onmessage = function(evt) { onMessage(evt) };

function sendText(json) {
    console.log("sending text: " + json);
    websocket.send(json);
}
              
function onMessage(evt) {
    console.log("received");
    var obj = JSON.parse(evt.data);
    var i;
    for(i=0; i < mapStuff.markerList.length;i++)
    {
        if(mapStuff.markerList[i])
        {
            mapStuff.markerList[i].setMap(null);
        }
    }    
    for(i=0; i<obj.latitude.length;i++)
    {    
       // console.log(obj.latitude[i]);
       // console.log(obj.longitude[i]);
        var myLatLng = {lat: parseFloat(obj.latitude[i]), lng: parseFloat(obj.longitude[i])};
        mapStuff.markerList[i] = new google.maps.Marker({
           position: myLatLng,
           map: mapStuff.theMap
        });
    }
}
function get_coordinates(){
    
    return {
        x: xlatitude,
        y: ylongitude
    };
}

function get_rectcoordinates(){
    
    return {
        max: north_east,
        min: south_west
    };
}
function initMap() {

    var sai = new google.maps.LatLng(39.50,-98.35);
    mapStuff.theMap = new google.maps.Map(document.getElementById('map'), {
            center: sai,
            zoom: 5
    });
    
    var rectangle = null;
    var marker = null;
    var coordInfoWindow = null;
    google.maps.event.addListener( mapStuff.theMap, 'click', function (event) {

            xlatitude = event.latLng.lat();
            ylongitude = event.latLng.lng();
            north_east = null;
            coordinates = new google.maps.LatLng(xlatitude, ylongitude);

            if(marker)
            {
                    marker.setMap(null);
            }

            marker = new google.maps.Marker({position: event.latLng, map:  mapStuff.theMap});

            if (coordInfoWindow) {
                    coordInfoWindow.close();
            }
            if(rectangle != null)
                rectangle.setMap(null);
            
            coordInfoWindow = new google.maps.InfoWindow();
            coordInfoWindow.setContent(createInfoWindowContent(coordinates,  mapStuff.theMap.getZoom()));
            coordInfoWindow.setPosition(coordinates);
            coordInfoWindow.open( mapStuff.theMap, marker);

            mapStuff.theMap.addListener('zoom_changed', function() {
            coordInfoWindow.setContent(createInfoWindowContent(coordinates,  mapStuff.theMap.getZoom()));
            coordInfoWindow.open( mapStuff.theMap, marker);
            });

    });
    
    var drawingManager = new google.maps.drawing.DrawingManager({
            drawingMode: google.maps.drawing.OverlayType.MARKER,
            drawingControl: true,
            drawingControlOptions: {
              position: google.maps.ControlPosition.TOP_CENTER,
              drawingModes: [
                google.maps.drawing.OverlayType.RECTANGLE
              ]
            },
            markerOptions: {icon: 'images/beachflag.png'},
            circleOptions: {
              fillColor: '#ffff00',
              fillOpacity: 1,
              strokeWeight: 5,
              clickable: false,
              editable: true,
              zIndex: 1
            }
    });
    drawingManager.setMap(mapStuff.theMap);

    google.maps.event.addListener(drawingManager, 'overlaycomplete', function (e) {
            if(marker != null)
                marker.setMap(null);
            xlatitude = null;
            if(rectangle != null)
                rectangle.setMap(null);

            rectangle = e.overlay;
            var bounds = e.overlay.getBounds();
            north_east = bounds.getNorthEast();
            south_west = bounds.getSouthWest();
            var contentString = '<b>Rectangle moved.</b><br>' +
                  'New north-east corner: ' + north_east.lat() + ', ' + north_east.lng() + '<br>' +
                  'New south-west corner: ' + south_west.lat() + ', ' + south_west.lng();
            console.log(contentString);
    });

}


function createInfoWindowContent(latLng, zoom) {

    return [
            'LatLng: ' + latLng
    ].join('<br>');
}

