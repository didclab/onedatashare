/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team,
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ##
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ##
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ##
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */

 import React, { useEffect } from "react";
 import L from "leaflet";
 import "leaflet/dist/leaflet.css";
 import marker from "./assets/marker-icon.svg"
 
 const CarbonMapComponent = () => {

  useEffect(() => {
    const map = L.map('map').setView([42.8864, -78.8784], 13);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      worldCopyJump: false,
      minZoom: 5,
    }).addTo(map);

    // map.setMaxBounds(map.getBounds());
    // console.log(map.getBounds())

    const waypoint1 = [42.8864, -78.8784];
    const waypoint2 = [42.9000, -78.8700];
    const waypoint3 = [42.9000, -78.8720];
    const waypoint4 = [40.7128, -74.0060];
    const waypoint5 = [36.7783, -119.4179];
    const waypoint6 = [27.6648, -81.5158];

    var markerIcon = L.icon({
      iconUrl: marker,
      iconSize: [38, 95],
      iconAnchor: [19, 60], 
      popupAnchor: [0, -30]
    });

    L.marker(waypoint1, { icon: markerIcon })
      .addTo(map)
      .bindPopup('Example IP: 111.111.1111', { className: 'popup' });

    L.marker(waypoint2, { icon: markerIcon })
      .addTo(map)
      .bindPopup('Example IP: 111.111.1111', { className: 'popup' });

    L.marker(waypoint3, { icon: markerIcon })
      .addTo(map)
      .bindPopup('Example IP: 111.111.1111', { className: 'popup' });

    L.marker(waypoint4, { icon: markerIcon })
      .addTo(map)
      .bindPopup('Example IP: 111.111.1111', { className: 'popup' });

    L.marker(waypoint5, { icon: markerIcon })
      .addTo(map)
      .bindPopup('Example IP: 111.111.1111', { className: 'popup' });
    
    L.marker(waypoint6, { icon: markerIcon })
      .addTo(map)
      .bindPopup('Example IP: 111.111.1111', { className: 'popup' });

    const latlngs = [waypoint1, waypoint2];
    const latlngs1 = [waypoint2, waypoint3];
    const latlngs2 = [waypoint1, waypoint4];
    const latlngs3 = [waypoint1, waypoint5];
    const latlngs4 = [waypoint1, waypoint6];

    const polyline = L.polyline(latlngs, { color: 'red', weight: 6 })
      .addTo(map).bindPopup('Carbon Intensity: 0.92', { className: 'popup' });
    const polyline2 = L.polyline(latlngs1, { color: 'blue', weight: 6})
      .addTo(map).bindPopup('Carbon Intensity: 0.95', { className: 'popup' });
    const polyline3 = L.polyline(latlngs2, { color: 'black', weight: 6})
      .addTo(map).bindPopup('Carbon Intensity: 0.95', { className: 'popup' });
    const polyline4 = L.polyline(latlngs3, { color: 'green', weight: 6})
      .addTo(map).bindPopup('Carbon Intensity: 0.95', { className: 'popup' });
    const polyline5 = L.polyline(latlngs4, { color: 'red', weight: 6})
      .addTo(map).bindPopup('Carbon Intensity: 0.95', { className: 'popup' });


    const group = new L.featureGroup([polyline, polyline2, polyline3, polyline4]);
    map.fitBounds(group.getBounds());

    const initialBounds = map.getBounds();
    const southWest = initialBounds.getSouthWest();
    const northEast = initialBounds.getNorthEast();
    
    const adjustedSouthWest = L.latLng(-350, southWest.lng - 340);
    const adjustedNorthEast = L.latLng(350, northEast.lng + 340);
    
    map.setMaxBounds(L.latLngBounds(adjustedSouthWest, adjustedNorthEast));
  }, []);

   return (
    <div className="map-page-container">
        <div id="map"></div>
    </div>
   );
 };
 
 export default CarbonMapComponent;
 