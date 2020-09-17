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


import React, { Component, useState, useEffect } from 'react';
import { Navbar, Nav, NavItem, NavDropdown } from 'react-bootstrap';
import {AppBar, Button, IconButton, Toolbar, Grid, Hidden, styled, Box, Drawer, List, ListItem} from "@material-ui/core";
import MenuIcon from '@material-ui/icons/Menu';


import { Link } from 'react-router-dom';
import ContactSupportOutlined from '@material-ui/icons/ContactSupportOutlined';
import Tooltip from '@material-ui/core/Tooltip';
import AdminDropdown from "./AdminDropdown";
import { transferPageUrl, queuePageUrl, userPageUrl, userListPageUrl, historyPageUrl, registerPageUrl, newNotifications, signInUrl, endpoint_db } from '../constants';
import { store } from '../App';
import { logout } from '../APICalls/APICalls';
import zIndex from "@material-ui/core/styles/zIndex";


class NavbarComponent extends Component {

	constructor(props) {
		super(props);
		this.state = {
			login: store.getState().login,
			// login: true,
			email: store.getState().email,
			admin: store.getState().admin,
			mobileMenu: false
		};

		this.unsubscribe = store.subscribe(()=>{
			this.setState({login: store.getState().login, email : store.getState().email, admin: store.getState().admin});
		});

	}

	componentWillUnmount() {
		this.unsubscribe();
		// console.log(store);
	}

	Navbar = () => styled(AppBar)(props =>({
		position: "relative",
		backgroundColor: "#323840",
		zIndex: 1400
	}))


	toggleMobileMenu(){
		console.log(this.state.mobileMenu);
		this.setState((prevState) => ({
			mobileMenu: !prevState.mobileMenu
		}));
	}


	render() {
		const Navigation = this.Navbar();
		return (
			<React.Fragment>
				<Navigation>
					<Toolbar style={{marginLeft: "1%"}}>
						<Grid className={"leftNav"} alignItems={"center"}>
							<Link to={"/"} href={"/"} color={"inherit"} className={"navbarHome"}><h4>OneDataShare</h4></Link>

							<Hidden xsDown>
								{(this.state.login) &&

								<Box display="flex" width={"50%"}>
									<Link to={transferPageUrl} href={transferPageUrl} id="NavTransfer" className={"navbarButton"}>Transfer</Link>
									<Link to={queuePageUrl} href={queuePageUrl} id="NavQueue" className={"navbarButton"}>Queue</Link>

									{this.state.admin===true &&
									<AdminDropdown/>
									}


								</Box>
								}
							</Hidden>
						</Grid>
						<Hidden xsDown>
							<Box className={"rightNav"}>
								{this.state.login &&
								<Link to={userPageUrl} id="NavEmail" href={userPageUrl} className={"navbarButton"}>{this.state.email}</Link>
								}
								{!this.state.login &&
								<Link to={signInUrl} id="NavSignIn" href={signInUrl} className={"navbarButton"}>Sign in</Link>
								}
								{!this.state.login &&
								<Link to={registerPageUrl} id="NavRegister" href={registerPageUrl} className={"navbarButton"}>Register</Link>
								}
								{this.state.login &&
								<p id="NavLogout" onClick={()=>{logout()}} className={"navbarButton"}>
									<span>Log out</span>
								</p>}
								<Link to={"/support"} href="/support" className={"navbarButton"}>
									Support
								</Link>
								{/*<a href={endpoint_db} className={"navbarButton"} id="NavEndpoint">Authorization Database</a>*/}
							</Box>
						</Hidden>
						<Hidden smUp>
							<IconButton onClick={() => this.toggleMobileMenu()} >
								<MenuIcon style={{color: "white", fontSize: "20px"}}/>
							</IconButton>
						</Hidden>
					</Toolbar>
				</Navigation>
				<Hidden smUp>
					<Drawer anchor={"top"} open={this.state.mobileMenu} onClose={() => this.toggleMobileMenu()} style={{flexShrink: 0}}>
						<Toolbar/>
						<div className={"drawerContainer"}>
							<List>
								{this.state.login &&
								<ListItem>
									<Link to={transferPageUrl} href={transferPageUrl} id="NavTransfer" className={"navbarButton"}>Transfer</Link>
								</ListItem>
								}
								{this.state.login &&
								<ListItem>
									<Link to={queuePageUrl} href={queuePageUrl} id="NavQueue" className={"navbarButton"}>Queue</Link>
								</ListItem>
								}
								{(this.state.login && this.state.admin) &&
								<AdminDropdown mobile={true}/>
								}
								{this.state.login &&
								<ListItem>
									<Link to={userPageUrl} id="NavEmail" href={userPageUrl} className={"navbarButton"}>{this.state.email}</Link>
								</ListItem>
								}
								{!this.state.login &&
								<ListItem>
									<Link to={signInUrl} id="NavSignIn" href={signInUrl} className={"navbarButton"}>Sign in</Link>
								</ListItem>
								}
								{!this.state.login &&
								<ListItem>
									<Link to={registerPageUrl} id="NavRegister" href={registerPageUrl} className={"navbarButton"}>Register</Link>
								</ListItem>
								}
								{this.state.login &&
								<ListItem>
									<p id="NavLogout" onClick={()=>{logout()}} className={"navbarButton"}>
										<span>Log out</span>
									</p>
								</ListItem>
								}
								<ListItem>
									<Link to={"/support"} href="/support" className={"navbarButton"}>
										Support
									</Link>
								</ListItem>

							</List>

						</div>

					</Drawer>
				</Hidden>
			</React.Fragment>

		);
	}
}



export default NavbarComponent;