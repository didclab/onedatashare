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
import {HomeInfo} from "./HomePage/HomePageInfo";
import HomeInfoSlice from "./HomePage/HomeInfoSlice";
import {nsfImage, ubImage} from "../constants";
import TitleSlice from "./HomePage/TitleSlice";
import TripleIconSlice from "./HomePage/TripleIconSlice";
import {TripleIconInfo} from "./HomePage/TripleIconInfo";
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
				<div className='hero_section_bottom'>
						<img src="https://img.icons8.com/?size=100&id=JF6kPfhVzeVz&format=png&color=000000"/>
						<img src="https://img.icons8.com/?size=100&id=11106&format=png&color=000000"/>
						<img src="https://img.icons8.com/?size=100&id=11107&format=png&color=000000"/>
						<img src="https://img.icons8.com/?size=100&id=17990&format=png&color=000000"/>
						<img src="https://img.icons8.com/?size=100&id=5fzjhSdcssrn&format=png&color=000000"/>
						<img src="https://img.icons8.com/?size=100&id=PDDAw6sv9exf&format=png&color=000000"/>
						<img src="https://img.icons8.com/?size=100&id=25728&format=png&color=000000"/>
				</div>
				<div className='description_section'>
					Fast, Easy, Free, Secure
				</div>
				
				<RecognitionSlice/>

			</div>
		);
	}
}