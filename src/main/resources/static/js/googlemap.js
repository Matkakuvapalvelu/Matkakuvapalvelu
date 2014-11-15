var tripPath = [];
var infoWindow  = new google.maps.InfoWindow({
    content:"You were here!",
    maxWidth: 300,
    maxHeight: 300,
    width: 150,
    height: 150
});

function initialize(latitude, longitude) {
    if (!latitude || !longitude) {
        return;
    }

    var mapOptions = {
        center: { lat: latitude, lng: longitude},
        zoom: 8
    };
    var map = new google.maps.Map(document.getElementById('googleMap'),mapOptions);
    
    return map;
}

function drawMarkers(tripMap, coordinates) {
    coordinates.forEach(function(coordinate) {
        drawNewMarker(tripMap, coordinate[0], coordinate[1]);
        tripPath.push(new google.maps.LatLng(coordinate[0], coordinate[1]));
    });
}

function drawNewMarker(map, lat, lng){
    var marker = new google.maps.Marker({
        position: { lat: lat, lng: lng}
    });
    google.maps.event.addListener(marker, 'click', onClickMarker);
    marker.setMap(map);
}

var onClickMarker = function(event) {
    infoWindow.open(this.map, this);
};

function drawPolyLinePath(map){
    new google.maps.Polyline({                    
        path:tripPath,
        strokeColor:"#0000FF",
        strokeOpacity:0.8,
        strokeWeight:2
    }).setMap(map); 
}
/**
 * Created by Omistaja on 14.11.2014.
 */
