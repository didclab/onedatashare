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
import FreeIcon from "../assets/images/free.png";
import FastIcon from "../assets/images/fast.png";
import SecureIcon from "../assets/images/secure.png";
import EasyIcon from "../assets/images/ui.png";
import EcofriendlyIcon from "../assets/images/ecofriendly.svg";
import InteroperableIcon from "../assets/images/interoperable.png";
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
				<div className='description_section'>
					<h1 id="whyods">Why OneDataShare?</h1>
					<div className='description_card_container'>
						<div className='description_card'>
							<h1>Fast</h1>
							<img src={FastIcon} className='description_icon' alt="Fast Icon"/>
							<p>OneDataShare delivers your data in the fastest way possible via its state-of-the-art optimization mechanisms.</p>
						</div>
						<div className='description_card'>
							<h1>Secure</h1>
							<img src={SecureIcon} className='description_icon' alt="Secure Icon"/>
							<p>User privacy is of utmost importance for us. OneDataShare encrypts and protects your user credentials.</p>
						</div>
						<div className='description_card'>
							<h1>Easy</h1>
							<img src={EasyIcon} className='description_icon' alt="Easy Icon"/>
							<p>Our intuitive web interface makes file transfer and monitoring very easy from any device and location.</p>
						</div>
						<div className='description_card'>
							<h1>Interoperable</h1>
							<img src={InteroperableIcon} className='description_icon' alt="Interoperable Icon"/>
							<p>We provide support for most popular cloud storage providers and data transfer end-points.</p>
						</div>
						<div className='description_card'>
							<h1>Free</h1>
							<img src={FreeIcon} className='description_icon' alt="Free Icon"/>
							<p>OneDataShare is a free service to the community. You don’t need to pay to transfer your data!</p>
						</div>
						<div className='description_card'>
							<h1>Ecofriendly</h1>
							<img src={EcofriendlyIcon} className='description_icon' alt="Free Icon" style={{"width": "150px"}}/>
							<p>OneDataShare is committed to sustainability, leveraging optimized networking to actively reduce its carbon footprint.</p>
						</div>
					</div>
				</div>
				<div className='platform_section'>
					<h1>Platforms We Support</h1>
					<div className='platforms'>
						<img src="https://img.icons8.com/?size=100&id=JF6kPfhVzeVz&format=png&color=000000" alt="Platform Icon 1"/>
						<img src="https://img.icons8.com/?size=100&id=11106&format=png&color=000000" alt="Platform Icon 2"/>
						<img src="https://img.icons8.com/?size=100&id=11107&format=png&color=000000" alt="Platform Icon 3"/>
						<img src="https://img.icons8.com/?size=100&id=17990&format=png&color=000000" alt="Platform Icon 4"/>
						<img src="https://img.icons8.com/?size=100&id=5fzjhSdcssrn&format=png&color=000000" alt="Platform Icon 5"/>
						<img src="https://img.icons8.com/?size=100&id=PDDAw6sv9exf&format=png&color=000000" alt="Platform Icon 6"/>
						<img src="https://img.icons8.com/?size=100&id=25728&format=png&color=000000" alt="Platform Icon 7"/>
					</div>
				</div>

				<div className='footer_section'>
						<h1>Our Sponsors: </h1>
						<RecognitionSlice/>
				</div>
			</div>
		);
	}
}