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


import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import CreateAccountComponent from './CreateAccountComponent';
import {resendVerificationCode} from '../../APICalls/APICalls'
import LinearProgress from '@material-ui/core/LinearProgress';
import {eventEmitter} from '../../App';
import { updateGAPageView } from "../../analytics/ga";


export default class ValidateEmailComponent extends Component {
	static propTypes = {
		backToSignin: PropTypes.func,
		email: PropTypes.string
	}
	constructor(props){
	    super(props);
	    this.state = {
				email: props.email,
				loadVerifyCode:false,
        loading: false,
			}
		this.next = this.next.bind(this);
		updateGAPageView();
	}
	next(){
		this.setState({loading:true})
		resendVerificationCode(this.state.email).then((response) =>{
			if(response.data.status === 200){
			this.setState({loading:false, loadVerifyCode:true})
			}
			else{
				this.setState({loading:false, loadVerifyCode:false})
				eventEmitter.emit("errorOccured", response.data.response);
			}	
		});
	}

	render(){
		const { backToSignin } = this.props;
		const properties = this.props
		const { email } = this.state;
		const loadVerifyCode = this.state.loadVerifyCode;
		const handleChange = name => event => {
		    this.setState({
		      [name]: event.target.value,
		    });
		};
		
		if(loadVerifyCode){		
			return(
				<CreateAccountComponent {...properties} loadVerifyCode = {this.state.loadVerifyCode} email = {this.state.email} ></CreateAccountComponent>
			);
		}
		else{
			return (	
				<div className="enter-from-right slide-in">
					{this.state.loading && <LinearProgress></LinearProgress>}
					<Typography style={{fontSize: "1.6em", marginBottom: "0.4em"}}>
						Find your OneDataShare Account
					</Typography>
					<TextField
						id="Email"
						label="Enter Your Email"
						value={email}
						style={{width: '100%', marginBottom: '50px', borderRadius: '25px'}}
						onChange={ handleChange('email') }
					/>

					<CardActions style={{ justifyContent: 'space-between'}}>						
						<Button size="small" variant="outlined" color="primary" onClick={backToSignin}>
							Back to Sign in Page
						</Button>
						<Button size="large" variant="contained" color="primary" onClick={() => this.next()} style={{marginLeft: '4vw'}}>
							Next
						</Button>
					</CardActions>
				</div>
			);
		}
	}
} 