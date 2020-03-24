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
import './HomePageComponent.css';
import {HomeInfo} from "./HomePage/HomePageInfo";
import HomeInfoSlice from "./HomePage/HomeInfoSlice";
import RecognitionSlice from "./HomePage/RecognitionSlice";
import TitleSlice from "./HomePage/TitleSlice";
import TripleIconSlice from "./HomePage/TripleIconSlice";
import {TripleIconInfo} from "./HomePage/TripleIconInfo";
import Grid from "@material-ui/core/Grid";

export default class HomePageComponent extends Component {

	constructor(props){
		super(props);
		updateGAPageView();
	}

	componentDidMount(){
		document.title = "OneDataShare";
	}

	makeInfoSlices(){
		let retVal = [];
		for (let i=0; i<HomeInfo.length; i+=1) {
			let data = HomeInfo[i];
			let left = i % 2;
			let name = 'evenSlice';
			if (!left) {
				name = 'oddSlice';
			}
			retVal.push( <HomeInfoSlice className={name}
										imgOnLeft={left}
										title={data.title}
										text ={data.text}
										img={require('./HomePage' + data.img)}
										imgAltTxt={data.imgAltTxt} /> );
		}
		return retVal;
	}

	makeTriple() {
		let retVal = [];
		for (let i=0; i<TripleIconInfo.length; i+=1) {
			let data = TripleIconInfo[i];
			retVal.push( <TripleIconSlice img={require('./HomePage' + data.img)}
										  title={data.title}
										  imgAltTxt={data.imgAlt}/> );
		}
		return retVal;
	}

	render() {

		let homeInfo = this.makeInfoSlices();
		let tripleIcon = this.makeTriple();

		return(
			<div>
				<div className='homePage'>

					<TitleSlice />

					<Grid className='TripleIconSlice' container direction='row'>
					{tripleIcon}
					</Grid>

					{homeInfo}
				</div>

				<RecognitionSlice />

			</div>
		);
	}
}