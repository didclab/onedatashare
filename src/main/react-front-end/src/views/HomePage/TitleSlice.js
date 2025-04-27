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


import React, {Component} from "react";
import {Hidden} from "@material-ui/core";
import Button from "@material-ui/core/Button";
import {siteURLS} from "../../constants";
import Grid from "@material-ui/core/Grid";
import Logo from "../../assets/images/logo.png";

export default class TitleClass extends Component {

    render() {
        return(
            <div className="title_container">
                <div className='socialIcons'>
                    <div className="socialFiller"></div>
					<img src="https://img.icons8.com/?size=100&id=106562&format=png&color=000000" onClick={() => {window.location.href = "https://github.com/didclab/onedatashare"}}></img>
                    <div className="socialFiller"></div>
                    
				</div>
                <div className="TitleSlice">
                    <h1>OneDataShare</h1>
                    <h2> Fast and secure file transfers made easy!  </h2>
                    <div className="TitleSliceBottom">
                        <a href={siteURLS.registerPageUrl}>
                            <button className='getStartedBtn'>Get Started</button>
                        </a>
                    </div>
                    <img className='homeImg' src={Logo} alt="OneDataShare Logo" />
                </div>
            </div>
        )
    }
}