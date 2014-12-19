var tripPath = [];
var infoWindow;
var activeWindow = null;
var activeWindowId = null;
var isTripMap = false;

function initialize(latitude, longitude, mapId, isTripMap) {
    if (!latitude || !longitude) {
        return;
    }   
    this.isTripMap = isTripMap;
    
    var mapOptions = {
        center: { lat: latitude, lng: longitude},
        zoom: 8
    };
    var map = new google.maps.Map(document.getElementById(mapId),mapOptions);
    
    return map;
}

function drawMarkers(tripMap, coordinates) {
    tripPath = [];
    coordinates.forEach(function(coordinate) {
        drawNewMarker(tripMap, coordinate[0], coordinate[1], coordinate[2]);
        tripPath.push(new google.maps.LatLng(coordinate[0], coordinate[1]));
    });
}

function drawNewMarker(map, lat, lng, id){
    var marker = new google.maps.Marker({
        position: { lat: lat, lng: lng},
        _id: id
    });
    
    if (id > 0){
        google.maps.event.addListener(marker, 'click', onClickMarker);
        //google.maps.event.addListener(marker, 'rightclick', onRightClickMarker);
    }
    
    marker.setMap(map);
}

var onClickMarker = function(event) {
    if(activeWindow !== null) activeWindow.close(); 
    if (activeWindowId !== this._id){
        infoWindow  = new google.maps.InfoWindow({
            title: "Image"
        });
        //img.width = 180;
        //img.height = 180;
        //var iWC = infoWindow.getContent();
        //iWC = "<div style='wdith: 500px;'><img id='user-image' src=/images/" + this._id + "/gallerythumbnail/></div>"
        if (isTripMap){
            var img = new Image();
            img.src = "/images/" + this._id + "/gallerythumbnail/"        
            infoWindow.setContent(img);
            activeWindow = infoWindow;
            activeWindowId = this._id;
            infoWindow.open(this.map, this);
        }
    } else {
        activeWindow = null;
        activeWindowId = null;        
    }
};

/*
 * Added function for context menu etc.
 */
var onRightClickMarker = function(event) {
    
};

function drawPolyLinePath(map){
    if (isTripMap){
        new google.maps.Polyline({                    
            path:tripPath,
            strokeColor:"#0000FF",
            strokeOpacity:0.8,
            strokeWeight:2
        }).setMap(map); 
    }    
}
/**
 * Created by Omistaja on 14.11.2014.
 */
