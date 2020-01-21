import React, { Component } from 'react';
import {updateGAPageView} from "../analytics/ga";
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import { isSafari } from 'react-device-detect';
import { fastImage, easyImage, eteImage, precImage, intopImage, nsfImage, ubImage, ODS_S3_BUCKET } from "../constants.js"
import './HomePageComponent.css';
import Grid from "@material-ui/core/Grid";

const textStyle = {color:'white', fontSize: '1.2em', textAlign: 'left'};
const sideStyle = {...textStyle, fontSize: '1.5em'};
const rowStyle = {background: '#579', padding: '4vw', width: '100%', margin: 0};
const homeStyle = {background: '#579', padding: '10vw'};
const subHeaderStyle = {...textStyle, fontSize: '2em'};
const subTextStyle = {...textStyle, fontSize: '1.1em'};


export default class HomePageComponent extends Component {

	constructor(props){
		super(props);
		updateGAPageView();
	}

	componentDidMount(){
		document.title = "OneDataShare";
	}

	render() {

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
