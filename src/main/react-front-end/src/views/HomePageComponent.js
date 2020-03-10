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
				<div className='homePage underNav'>

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