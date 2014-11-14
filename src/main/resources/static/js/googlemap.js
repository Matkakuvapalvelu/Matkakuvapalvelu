
var infoWindow  = new google.maps.InfoWindow({
    content:"You were here!",
    maxWidth: 300,
    maxHeight: 300,
    width: 150,
    height: 150
});

function initialize(latitude, longitude) {
    if (latitude == null || longitude == null) {
        return;
    }

    var mapOptions = {
        center: { lat: latitude, lng: longitude},
        zoom: 8
    };
    var map = new google.maps.Map(document.getElementById('googleMap'),mapOptions);
    drawNewMarker(map, latitude, longitude)
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
}/**
 * Created by Omistaja on 14.11.2014.
 */
