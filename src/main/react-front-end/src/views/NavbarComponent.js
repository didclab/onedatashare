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


import React, { Component,  } from 'react';
import {AppBar, IconButton, Toolbar, Grid, Hidden, styled, Box, Drawer, List, ListItem, Divider} from "@material-ui/core";
import MenuIcon from '@material-ui/icons/Menu';
import AccountCircle from '@material-ui/icons/esm/AccountCircle';
import '../hamburgers.css';

import Logo from "../assets/images/logo.png";
import { ReactComponent as TransferIcon } from "../assets/images/transfer.svg";
import { ReactComponent as ScheduleIcon } from "../assets/images/schedule.svg";
import { ReactComponent as HistoryIcon}  from "../assets/images/history.svg";
import { ReactComponent as AccountIcon} from "../assets/images/account.svg";
import { ReactComponent as PolicyIcon} from "../assets/images/policy.svg";
import { ReactComponent as TermsIcon} from "../assets/images/terms.svg";
import { ReactComponent as SupportIcon} from "../assets/images/support.svg";
import { ReactComponent as LogoutIcon} from "../assets/images/logout.svg";


import { Link } from 'react-router-dom';
// import { endpoint_db } from '../constants';
import { siteURLS } from "../constants";
import { store } from '../App';
import { logout } from '../APICalls/APICalls';
import { isMobile } from 'react-device-detect';


class NavbarComponent extends Component {

	constructor(props) {
		super(props);
		this.state = {
			login: store.getState().login,
			email: store.getState().email,
			mobileMenu: false,
			userMenu: false,
			isMobile: false,
		};

		this.unsubscribe = store.subscribe(()=>{
			this.setState({login: store.getState().login, email : store.getState().email});
		});

		this.setMobileView = this.setMobileView.bind(this)
		this.toggleMobileMenu = this.toggleMobileMenu.bind(this)

	}

	componentDidMount() {
		this.setMobileView();
		window.addEventListener("resize", () => {
			this.setMobileView();
			if (window.innerWidth >= 960 && this.state.mobileMenu) {
				this.toggleMobileMenu();
			}
		})

		return () => {
			window.removeListener("resize", () => {
				this.setMobileView();
				if (window.innerWidth >= 960 && this.state.mobileMenu) {
					this.toggleMobileMenu()
				}
			})
		}
	}

	componentWillUnmount() {
		this.unsubscribe();
	}

	setMobileView() {
		if (window.innerWidth >= 960) {
			this.setState(({isMobile: false}))
		}
		else {
			this.setState(({isMobile: true}))
		}
	}

	toggleMobileMenu(){
		if (this.state.userMenu) {
			this.toggleUserMenu()
		}
		this.setState((prevState) => ({
			mobileMenu: !prevState.mobileMenu
		}));
	}

	toggleUserMenu(){
		if (this.state.mobileMenu) {
			this.toggleMobileMenu()
		}
		this.setState((prevState) => ({
			userMenu: !prevState.userMenu
		}));
	}

	closeUserMenu() {
		if (this.state.userMenu) {
			this.toggleUserMenu()
		}
	}

	renderMobileMenu() {
		return (
				<Drawer anchor={"top"} open={this.state.mobileMenu} onClose={() => this.toggleMobileMenu()} BackdropProps={{style: { zIndex: 1000}}}>
					<div className={`drawerContainer${this.state.isMobile ? "-mobile": ""}`}>
						{this.state.login && (	
							<List className={`drawerContainer${this.state.isMobile ? "-mobile": ""}`} style={{width:"100%"}}>
								<ListItem onClick={() => this.toggleMobileMenu()}>
									<Link to={siteURLS.transferPageUrl} id="NavTransfer" className={`navbarButton-mobile${window.location.pathname === "/transfer" ? "-active": ""}`} style={{ textDecoration: 'none'}}>
										<TransferIcon className='icon'/>
										{"Transfer"}
									</Link>
								</ListItem>

								<ListItem onClick={() => this.toggleMobileMenu()}>
									<Link to={siteURLS.queuePageUrl} id="NavQueue" className={`navbarButton-mobile${window.location.pathname === "/queue" ? "-active": ""}`} style={{ textDecoration: 'none', gap:"2%" }}>
										<ScheduleIcon className="icon"/>
										{"Queue"}
									</Link>
								</ListItem>

								<ListItem onClick={() => this.toggleMobileMenu()}>
									<Link to={siteURLS.historyPageUrl} id="NavHistory" className={`navbarButton-mobile${window.location.pathname === "/history" ? "-active": ""}`} style={{ textDecoration: 'none' }}>
										<HistoryIcon className="icon"/>
										{"History"}
									</Link>
								</ListItem>

							</List>
						)}
					</div>
				</Drawer>
		)
	}

	renderUserMenu() {
		return (
			<Drawer anchor={"top"} open={this.state.userMenu} onClose={() => this.toggleUserMenu()} PaperProps={this.state.isMobile ? {style: { backgroundColor: 'transparent' }} : {style: { maxWidth: "375px", minWidth: "300px", marginLeft: "auto", backgroundColor: 'transparent' }}} BackdropProps={{style: { backgroundColor: "transparent", zIndex: 1000}}}>
				<div className={`drawerContainer${this.state.isMobile ? "-mobile": "-menu"}`}>
					<List className={`drawerContainer${this.state.isMobile ? "-mobile": "-menu"}`}>
						{this.state.login &&
							<React.Fragment>
								<ListItem onClick={() => this.toggleUserMenu()}>
									<Link to={siteURLS.userPageUrl} id="NavEmail" href={siteURLS.userPageUrl} className={`navbarButton${this.state.isMobile ? "-mobile": "-menu"}`}>
										{!this.state.isMobile ? (
											<React.Fragment>
											<div className='icon'>
												<AccountIcon style={{color: "white", width: "30px", height: "30px"}}/>
											</div>
											{"Account Details"}
											</React.Fragment>
											) 
											: 
											(this.state.email)}
									</Link>
								</ListItem>
								<Hidden mdUp>
									<ListItem onClick={() => this.closeUserMenu()}>
										<Link id="NavLogout" to={"/"} onClick={()=>{logout()}} className={`navbarButton${this.state.isMobile ? "-mobile": "-menu"}`}>
											<LogoutIcon className="icon"/>
											<span style={{textwrap: "none"}}>Log out</span>
										</Link>
									</ListItem>
								</Hidden>
							</React.Fragment>
						}

						<ListItem onClick={() => this.toggleUserMenu()}>
							<Link to={siteURLS.termsUrl} href={siteURLS.supportPageUrl} className={`navbarButton${this.state.isMobile ? "-mobile": "-menu"}`}>
								<TermsIcon className="icon"/>
								Terms
							</Link>
						</ListItem>
						
						<ListItem onClick={() => this.toggleUserMenu()}>
							<Link to={siteURLS.policyUrl} href={siteURLS.supportPageUrl} className={`navbarButton${this.state.isMobile ? "-mobile": "-menu"}`}>
								<PolicyIcon className="icon"/>
								Policy
							</Link>
						</ListItem>

						<ListItem onClick={() => this.toggleUserMenu()}>
							<Link to={siteURLS.supportPageUrl} href={siteURLS.supportPageUrl} className={`navbarButton${this.state.isMobile ? "-mobile": "-menu"}`}>
								<div className='icon'>
									<SupportIcon style={{color: "white", width: "30px", height: "30px"}}/>
								</div>
								Support
							</Link>
						</ListItem>
					</List>
				</div>
			</Drawer>
			
		)
	}

	renderRightNavBar() {
		return (
			<Grid className={"rightNav"}>
				{this.state.login &&
					<Box display="flex" whiteSpace={"nowrap"}>
						<IconButton onClick={() => this.toggleUserMenu()} >
							<AccountCircle style={{color: "white", fontSize: "35px"}}/>
							<div className={`navbarButton`}>{this.state.email}</div>
						</IconButton>
							<Link id="NavLogout" to={"/"} onClick={()=>{logout()}} className={"navbarButton"}>
								<LogoutIcon className="icon" style={{width: "30px"}}/>
								<span style={{textwrap: "none"}}>Log out</span>
							</Link>
					</Box>
				}
				{!this.state.login &&
					<React.Fragment>
						<Link to={siteURLS.signInPageUrl} id="NavSignIn" href={siteURLS.signInPageUrl} className={"navbarSignIn"}>Sign in</Link>
						<Link to={siteURLS.registerPageUrl} id="NavRegister" href={siteURLS.registerPageUrl} className={"navbarButton"}>Register</Link>
					</React.Fragment>
				}
			</Grid>

		)
	}


	renderLeftNavbar() {
		return (
			<Box display="flex" width={"50%"} justifyContent={"flex-start"} alignItems={"center"}>
				{(this.state.login) &&
					<React.Fragment>
						<Link to={siteURLS.transferPageUrl} id="NavTransfer" className={`navbarButton${window.location.pathname === "/transfer" ? "-active": ""}`} onClick={() => {this.closeUserMenu()}} style={{ textDecoration: 'none'}}>
							<TransferIcon className='icon'/>
							{"Transfer"}
						</Link>
						<Link to={siteURLS.queuePageUrl} id="NavQueue" className={`navbarButton${window.location.pathname === "/queue" ? "-active": ""}`} onClick={() => {this.closeUserMenu()}} style={{ textDecoration: 'none' }}>
							<ScheduleIcon className="icon"/>
							{"Queue"}
						</Link>
						<Link to={siteURLS.historyPageUrl} id="NavHistory" className={`navbarButton${window.location.pathname === "/history" ? "-active": ""}`} onClick={() => {this.closeUserMenu()}} style={{ textDecoration: 'none' }}>
							<HistoryIcon className="icon"/>
							{"History"}
						</Link>
					</React.Fragment>
				}
			</Box>
		)
	}

	


	render() {
		return (
			<div className="navbar-root">
				<AppBar className='navbar-container' style={{position:"relative", backgroundColor: "#172753", zIndex: 1400, maxWidth: "2048px",}}>
					<Toolbar style={{padding: "24px"}}>
						<Grid container className={"leftNav"} alignItems={"center"}>

							{/*Home Button and OneDatashare logo */}
							<Link to={"/"} className={"navbarHome"} onClick={() => {this.closeUserMenu(); if (this.state.mobileMenu) {this.toggleMobileMenu();}}}>
								<img className="navbarLogo" src={Logo} alt="OneDataShare Logo" />
								<h4 className="navbarName">OneDataShare</h4>
							</Link>

							{/* Left side of the navbar hidden on small screen*/}
							<Hidden smDown>
								{this.renderLeftNavbar()}
							</Hidden>
						</Grid>

						<Hidden smDown>
							{this.renderRightNavBar()}
						</Hidden>


						<Hidden mdUp>
							<Box display={"flex"} >
								{this.state.login &&
									<React.Fragment>
										<IconButton onClick={() => this.toggleUserMenu()} >
											<AccountCircle style={{color: "white", fontSize: "40px"}}/>
										</IconButton>
										<IconButton onClick={() => this.toggleMobileMenu()} >
											<button class={`hamburger hamburger--slider ${this.state.mobileMenu ? "is-active" : ""}`} type="button">
												<span class="hamburger-box">
													<span class="hamburger-inner"></span>
												</span>
											</button>

											{/* {!this.state.mobileMenu &&
												<MenuIcon style={{color: "white", fontSize: "40px"}}/>

											}
											{this.state.mobileMenu &&
												<CloseIcon style={{color: "white", fontSize: "40px"}}/>

											} */}
										</IconButton>
									</React.Fragment>
								}
								{!this.state.login &&
									<Box display={"flex"} alignItems={"center"} justifyContent={"center"}>
										<Link to={siteURLS.signInPageUrl} id="NavSignIn" href={siteURLS.signInPageUrl} className={"navbarSignIn"}>
											<h4 style={{fontWeight: "bold", fontSize: "15px"}}>Sign in</h4>
										</Link>
										<Link to={siteURLS.registerPageUrl} id="NavRegister" href={siteURLS.registerPageUrl} className={"navbarButton"}>Register</Link>
									</Box>
								}
							</Box>
						</Hidden>


					</Toolbar>
					{this.renderMobileMenu()}
					{this.renderUserMenu()}
				</AppBar>
			</div>

		);
	}
}


export default NavbarComponent;
