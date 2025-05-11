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


import React, { Component } from 'react';
import {updateGAPageView} from "../analytics/ga";
import {nsfImage, ubImage} from "../constants";
import TitleSlice from "./HomePage/TitleSlice";
import WhySection from "./HomePage/WhySection";
import WhoSection from './HomePage/WhoSection';
import Platforms from './HomePage/Platforms';
import Grid from "@material-ui/core/Grid";
import RecognitionSlice from './HomePage/RecognitionSlice';
import Logo from "../assets/images/logo.png";

export default class HomePageComponent extends Component {

	constructor(props){
		super(props);
		updateGAPageView();
	}

	componentDidMount(){
		document.title = "OneDataShare";
	}

	
	render() {
		return(
			<div className='homePage'>
				<div className='hero_section'>
					<TitleSlice/>	
				</div>
				<WhoSection/>
				<Platforms/>
				<WhySection/>
				<div className='footer_section'>
						<h1>Our Sponsors: </h1>
						<RecognitionSlice/>
				</div>
			</div>
		);
	}
}