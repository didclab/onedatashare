import React, { Component } from 'react';
import {updateGAPageView} from "../analytics/ga";
import Button from '@material-ui/core/Button';
import { isSafari } from 'react-device-detect';
import { nsfImage, ubImage, ODS_S3_BUCKET } from "../constants.js"
import './HomePageComponent.css';
import Grid from "@material-ui/core/Grid";
import {HomeInfo} from "./HomePage/HomePageInfo";
import HomeInfoSlice from "./HomePage/HomeInfoSlice";

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
			retVal.push( <HomeInfoSlice imgOnLeft={left}
										title={data.title}
										text ={data.text}
										img={require('./HomePage' + data.img)}
										imgAltTxt={data.imgAltTxt} /> );
		}
		return retVal;
	}

	render() {

		let homeInfo = this.makeInfoSlices();

		let bgImgS1 = ODS_S3_BUCKET + 's1.webp';
		let bgImgS2 = ODS_S3_BUCKET + 's2.webp';
		let bgImgS3 = ODS_S3_BUCKET + 's3.webp';
		let bgImgS4 = ODS_S3_BUCKET + 's4.webp';
		let bgImgS5 = ODS_S3_BUCKET + 's5.webp';
		let bgImgS6 = ODS_S3_BUCKET + 's6.webp';
		if(isSafari){
			// Since Safari does not support WebP images, we load PNG
			bgImgS1 = ODS_S3_BUCKET + 's1.png';
			bgImgS2 = ODS_S3_BUCKET + 's2.png';
			bgImgS3 = ODS_S3_BUCKET + 's3.png';
			bgImgS4 = ODS_S3_BUCKET + 's4.png';
			bgImgS5 = ODS_S3_BUCKET + 's5.png';
			bgImgS6 = ODS_S3_BUCKET + 's6.png';
		}

		return(
			<div className='homePage'>

				<h1>OneDataShare</h1>
				<p> Reliable and fast data movement in the cloud </p>

				<div className='homeSlices'>
					{homeInfo}
				</div>

				<Grid container
					  direction='row'
					  justify='center'
					  alignItems='center'
					  className='credits'>
					<Grid container alignItems='center' xs={12} md={6}>
						<img width={'100px'} src={nsfImage} style={{float: 'left'}} alt="NSF Logo" />
					</Grid>
					<Grid container alignItems='center' xs={12} md={6}>
						<img width={'100px'} src={ubImage} style={{float: 'left'}} alt="UB Logo" />
					</Grid>
				</Grid>

			</div>
		);
	}
}
