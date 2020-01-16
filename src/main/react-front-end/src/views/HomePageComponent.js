import React, { Component } from 'react';
import {Glyphicon, Jumbotron, Row, Col} from 'react-bootstrap';
import Slider from "react-slick";
import {updateGAPageView} from "../analytics/ga";
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';
import { isSafari } from 'react-device-detect';
import { fastImage, easyImage, eteImage, precImage, intopImage, nsfImage, ubImage, ODS_S3_BUCKET } from "../constants.js"
import './HomePageComponent.css';
import HomeInfoSlice from "./HomePage/HomeInfoSlice";
import {homeInfo} from "./HomePage/HomePageInfo.js";

const textStyle = {color:'white', fontSize: '1.2em', textAlign: 'left'};
const sideStyle = {...textStyle, fontSize: '1.5em'};
const rowStyle = {background: '#579', padding: '4vw', width: '100%', margin: 0};
const subHeaderStyle = {...textStyle, fontSize: '2em'}
const subTextStyle = {...textStyle, fontSize: '1.1em'}

function CustomNextArrow(props) {
  const { style, onClick } = props;
  return (
    <div
      className="slick-next"
      style={{ ...style}}
      onClick={onClick}
    >
    	<Glyphicon glyph="chevron-right" style={{fontSize:"50px", color: "white"}}/>
    </div>
  );
}

function CustomPrevArrow(props) {
  const { style, onClick } = props;
  return (
    <div
      className="slick-prev"
      style={{ ...style}}
      onClick={onClick}
    >
    	<Glyphicon glyph="chevron-left" style={{fontSize:"50px", color: "white"}}/>
    </div>
  );
}


export default class HomePageComponent extends Component {

	constructor(props){
		super(props);
		updateGAPageView();
		this.state = {
			infoSlices: []
		};
	}

	componentDidMount(){
		document.title = "OneDataShare";
		this.setState({
				infoSlices: homeInfo
		});
	}

	render(){
		var settings = {
			dots: true,
			infinite: true,
			speed: 500,
			slidesToShow: 1,
			slidesToScroll: 1,
			appendDots: dots => <ul style={{bottom: "8%", zIndex: 1}}>{dots}</ul>,
			nextArrow: <CustomNextArrow/>,
			prevArrow: <CustomPrevArrow/>,
			autoplay: false,
			responsive: [{breakpoint: 500, settings: {arrows: false}}]
		};

		let getStartedButton = (
			<div style={{ marginTop : '5%', marginLeft : '2%'}}>
				<Button variant="contained" color="primary" style={{ backgroundColor : 'rgb(45, 46, 48)', fontSize: '1.5rem'}}>
					<Link to="/get-started" style={{ color:'white'  }}>
						Get Started
					</Link>
				</Button>
			</div>
		);
		
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
			<div className="adjustTop">

					<Row style={rowStyle}>
						<img width={'300px'} className="image-center" src={fastImage} style={{float: 'left'}} alt="Fast, Scalable, and Flexible Data Sharing Made Easy"></img>
						<h3 style={subHeaderStyle}>Fast, Scalable, and Flexible Data Sharing Made Easy</h3>
						<p style={subTextStyle}>OneDataShare harnesses the power of the cloud and implements state-of-the-art frameworks to optimize data transfer scheduling. It enhances the speed of data transfer via multiple protocols by enabling faster data transfer across the network. OneDataShare utilizes the available bandwidth to the fullest by using many optimization mechanisms, thereby speeding up data transfer and lowering cost.</p>
					</Row>

					<Row style={rowStyle}>
						<img width={'300px'} className="image-center" src={easyImage} style={{float: 'left'}} alt="Reduced Time to the Delivery of Data"></img>
						<h3 style={subHeaderStyle}>Reduced Time to the Delivery of Data</h3>
						<p style={subTextStyle}>OneDataShare reduces the time to the delivery of data and drastically increases the end-to-end performance of data-intensive applications relying on remote data sources. It provides an end-to-end data transfer architecture which is reliable, fast and effective. The users are capable of reducing the burden of data transfer time through the use of OneDataShare.</p>
					</Row>

					<Row style={rowStyle}>
						<img width={'300px'} className="image-center" src={intopImage} style={{float: 'left'}} alt="Interoperation Across Heterogeneous Data Resources"></img>
						<h3 style={subHeaderStyle}>Interoperation Across Heterogeneous Data Resources</h3>
						<p style={subTextStyle}>By using OneDatashare, users can transfer their data across multiple protocols and platforms without having to worry about protocol translation. OneDataShare takes care of the protocol translation across multiple users ends and it provides a user-friendly field to translate data across multiple instances and ensures effective compatibility and platform independence as data gets transferred from the newest to the oldest protocols and vice versa.</p>
					</Row>

					<Row style={rowStyle}>
						<img width={'300px'} className="image-center" src={precImage} style={{float: 'left'}} alt="Decreased Uncertainty in Real-time Decision-Making Processes"></img>
						<h3 style={subHeaderStyle}>Decreased Uncertainty in Real-time Decision-Making Processes.</h3>
						<p style={subTextStyle}>Delays over transmission of large data sets are eliminated by using OneDataShare. The desirable data delivery throughput of OneDataShare will alleviate the issue of a long time of data transfer. Hence users will be able to experience faster data deposits across long distances which will, in turn, speed up their other processes regarding usage of the data. Also, it will provide the users with upfront information regarding their data transfers and arrivals, thereby enabling them to prepare in advance for data transfer and scheduling.</p>
					</Row>

					<Row style={rowStyle}>
						<img width={'300px'} className="image-center" src={eteImage} style={{float: 'left'}} alt="End-to-end Data Sharing Solution"></img>
						<h3 style={subHeaderStyle}>End-to-end Data Sharing Solution</h3>
						<p style={subTextStyle}>OneDataShare is capable of transferring high-volume data of data like real-time weather predictions and conditions and natural disasters to sharing genomic maps and consumer behavior statistics for business intelligence, climate change data or geographic data from any Management Information System (MIS) from anywhere in the world.</p>
					</Row>

					<Row className="imagecentercontainer" style={{...rowStyle, backgroundColor: 'white' }}>
						<img width={'100px'} src={nsfImage} style={{float: 'left'}} alt="NSF Logo"></img>
						<img width={'100px'} src={ubImage} style={{float: 'left'}} alt="UB Logo"></img>
					</Row>
		</div>
			);
		;
	}
}
