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

 import React, {useState} from "react"
 import { Tooltip } from 'react-tooltip'
 import MapData from "./assets/world.json"
 import {
    ComposableMap,
    Geographies,
    Geography,
    ZoomableGroup,
    Marker
  } from "react-simple-maps";
 
 const geoUrl =
   "https://cdn.jsdelivr.net/npm/world-atlas@2/countries-110m.json"
 
 const CarbonMapComponent = () => {
    const [hoverContent, setHoverContent] = useState("Hello")
    
    return (
        <div className="map-background">
            <Tooltip id="my-tooltip" />
            <ComposableMap>
                <ZoomableGroup center={[0, 0]} zoom={9}>
                    <Geographies geography={geoUrl}>
                        {({ geographies }) =>
                        geographies.map((geo) => (
                            <Geography 
                                key={geo.rsmKey} geography={geo}
                                data-tooltip-id="my-tooltip"
                                data-tooltip-content={hoverContent}
                                data-tooltip-place="top"
                                onMouseEnter={() => {
                                    const { name } = geo.properties;
                                    console.log(geo.properties)
                                    setHoverContent(`${name}`)
                                }}
                                onMouseLeave={() => {
                                    setHoverContent("")
                                }}
                                style={{
                                    color: "#FFF",
                                    hover: {
                                        fill: "#F53",
                                        outline: "none"
                                    }
                                }} 
                            />
                        ))
                        }
                    </Geographies>
                </ZoomableGroup>
        </ComposableMap>
        </div>
    );
 };
 
 export default CarbonMapComponent;