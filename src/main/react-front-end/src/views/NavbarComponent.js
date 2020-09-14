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
import {AppBar, Button, IconButton, Toolbar, Grid, Hidden, styled, withTheme, ThemeProvider, createMuiTheme, Box, Drawer, makeStyles, List, ListItem} from "@material-ui/core";
import MenuIcon from '@material-ui/icons/Menu';


import { Link } from 'react-router-dom';
import ContactSupportOutlined from '@material-ui/icons/ContactSupportOutlined';
import Tooltip from '@material-ui/core/Tooltip';
import AdminDropdown from "./AdminDropdown";
import { transferPageUrl, queuePageUrl, userPageUrl, userListPageUrl, historyPageUrl, registerPageUrl, newNotifications, signInUrl, endpoint_db } from '../constants';
import { store } from '../App';
import { logout } from '../APICalls/APICalls';
import zIndex from "@material-ui/core/styles/zIndex";

const styles = makeStyles((theme) => ({
	appBar: {
		position: "relative",
		backgroundColor: "black",
		zIndex: 1400
	},
	drawer: {
		flexShrink: 0,
	},
	drawerContainer: {
		backgroundColor: "black",
		overflow: "auto"
	}
}));

const NavbarComponent = () => {
	const [login, setLogin] = useState(store.getState().login);
	const [email, setEmail] = useState(store.getState().email);
	const [admin, setAdmin] = useState(store.getState().admin);
	const [mobileMenu, setMobile] = useState(false);

	const unsubscribe = store.subscribe(() => {
		setLogin(store.getState().login);
		setEmail(store.getState().email);
		setAdmin(store.getState().admin);
	})
	useEffect(() => {
		return () => {
			unsubscribe();
		}
	});

	const toggleMobileMenu = () => {
		console.log(mobileMenu);
		setMobile(!mobileMenu);
	}

	const theme = createMuiTheme();
	const classes = styles();
	const options = [
		{
			id: "NavEmail",
			href: userPageUrl,
			className: "navbarButton",
			label: email,
			visible: login
		}

	];

	return (
		<ThemeProvider theme={theme}>
			<AppBar id="navbar" position="fixed" className={classes.appBar}>
				<Toolbar style={{marginLeft: "1%"}}>
					<Grid className={"leftNav"} alignItems={"center"}>
						<a href={"/"} color={"inherit"} className={"navbarHome"}><h4>OneDataShare</h4></a>

						<Hidden xsDown>
							{!(login) &&

							<Box display="flex" width={"50%"}>
								<a href={transferPageUrl} id="NavTransfer" className={"navbarButton"}>Transfer</a>
								<a href={queuePageUrl} id="NavQueue" className={"navbarButton"}>Queue</a>

								{!admin===true &&
								<AdminDropdown mobile={false}/>
								}


							</Box>
							}
						</Hidden>
					</Grid>
					<Hidden xsDown>
						<Box className={"rightNav"}>
							{login &&
							<a id="NavEmail" href={userPageUrl} className={"navbarButton"}>{email}</a>
							}
							{!login &&
							<a id="NavSignIn" href={signInUrl} className={"navbarButton"}>Sign in</a>
							}
							{!login &&
							<a id="NavRegister" href={registerPageUrl} className={"navbarButton"}>Register</a>
							}
							{login &&
							<p id="NavLogout" onClick={()=>{logout()}} className={"navbarButton"}>
								<span>Log out</span>
							</p>}
							<a href="/support" className={"navbarButton"}>
								Support
							</a>
							{/*<a href={endpoint_db} className={"navbarButton"} id="NavEndpoint">Authorization Database</a>*/}
						</Box>
					</Hidden>
					<Hidden smUp>
						<IconButton onClick={toggleMobileMenu} style={{color: "white", padding: "5px"}}>
							<MenuIcon/>
						</IconButton>
					</Hidden>
				</Toolbar>
			</AppBar>
			<Hidden xsUp>
			<Drawer anchor={"top"}  open={mobileMenu} onClose={toggleMobileMenu} className={classes.drawer}>
				<Toolbar/>
				<div className={classes.drawerContainer}>
					<List>
						{!login &&
						<ListItem>
							<a href={transferPageUrl} id="NavTransfer" className={"navbarButton"}>Transfer</a>
						</ListItem>
						}
						{!login &&
							<ListItem>
								<a href={queuePageUrl} id="NavQueue" className={"navbarButton"}>Queue</a>
							</ListItem>
						}
						{!(login && admin) &&
							<AdminDropdown mobile={true}/>
						}
						{login &&
						<ListItem>
							<a id="NavEmail" href={userPageUrl} className={"navbarButton"}>{email}</a>
						</ListItem>
						}
						{!login &&
						<ListItem>
							<a id="NavSignIn" href={signInUrl} className={"navbarButton"}>Sign in</a>
						</ListItem>
						}
						{!login &&
						<ListItem>
							<a id="NavRegister" href={registerPageUrl} className={"navbarButton"}>Register</a>
						</ListItem>
						}
						{login &&
							<ListItem>
								<p id="NavLogout" onClick={()=>{logout()}} className={"navbarButton"}>
									<span>Log out</span>
								</p>
							</ListItem>
						}
						<ListItem>
							<a href="/support" className={"navbarButton"}>
								Support
							</a>
						</ListItem>

					</List>

				</div>

			</Drawer>
			</Hidden>
		</ThemeProvider>
	);


};

class Navibar extends Component {

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
	}

	Navbar = () => styled(withTheme(AppBar))(props =>({
		backgroundColor: "black",
		zIndex: props.theme.zIndex.drawer + 1,
	}))


	toggleMobileMenu(){
		console.log(this.state.mobileMenu);
		this.setState((prevState) => ({
			mobileMenu: !prevState.mobileMenu
		}));
	}


	render() {
		const Navigation = this.Navbar();
		const theme = createMuiTheme();
		const classes = styles();
		return (
			<ThemeProvider theme={theme}>
				<AppBar id="navbar" position="fixed" className={classes.appBar}>
					<Toolbar style={{marginLeft: "1%"}}>
						<Grid className={"leftNav"} alignItems={"center"}>
							<a href={"/"} color={"inherit"} className={"navbarHome"}><h4>OneDataShare</h4></a>

							<Hidden xsDown>
								{!(this.state.login) &&

								<Box display="flex" width={"50%"}>
									<a href={transferPageUrl} id="NavTransfer" className={"navbarButton"}>Transfer</a>
									<a href={queuePageUrl} id="NavQueue" className={"navbarButton"}>Queue</a>

									{!this.state.admin===true &&
									<AdminDropdown/>
									}


								</Box>
								}
							</Hidden>
						</Grid>
						<Hidden xsDown>
							<Box className={"rightNav"}>
								{this.state.login &&
								<a id="NavEmail" href={userPageUrl} className={"navbarButton"}>{this.state.email}</a>
								}
								{!this.state.login &&
								<a id="NavSignIn" href={signInUrl} className={"navbarButton"}>Sign in</a>
								}
								{!this.state.login &&
								<a id="NavRegister" href={registerPageUrl} className={"navbarButton"}>Register</a>
								}
								{this.state.login &&
								<p id="NavLogout" onClick={()=>{logout()}} className={"navbarButton"}>
									<span>Log out</span>
								</p>}
								<a href="/support" className={"navbarButton"}>
									Support
								</a>
								{/*<a href={endpoint_db} className={"navbarButton"} id="NavEndpoint">Authorization Database</a>*/}
							</Box>
						</Hidden>
						<Hidden smUp>
							<IconButton onClick={() => this.toggleMobileMenu()} style={{color: "white", padding: "5px"}}>
								<MenuIcon/>
							</IconButton>
						</Hidden>
					</Toolbar>
				</AppBar>
				<Drawer variant={"persistent"} open={this.state.mobileMenu} onClose={() => this.toggleMobileMenu()}>
					{this.state.login &&
					<a id="NavEmail" href={userPageUrl} className={"navbarButton"}>{this.state.email}</a>
					}
					{!this.state.login &&
					<a id="NavSignIn" href={signInUrl} className={"navbarButton"}>Sign in</a>
					}
					{!this.state.login &&
					<a id="NavRegister" href={registerPageUrl} className={"navbarButton"}>Register</a>
					}
					{this.state.login &&
					<p id="NavLogout" onClick={()=>{logout()}} className={"navbarButton"}>
						<span>Log out</span>
					</p>}
					<a href="/support" className={"navbarButton"}>
						Support
					</a>
				</Drawer>
			</ThemeProvider>


			// <Navbar inverse collapseOnSelect fixedTop className="navbar_navbar" id="navbar" >
			// 	<Navbar.Header >
			// 		<Navbar.Brand>
			// 			<Link to="/">OneDataShare</Link>
			// 		</Navbar.Brand>
			// 		<Navbar.Toggle />
			// 	</Navbar.Header>
			// 	<Navbar.Collapse>
			// 		{(this.state.login) &&
			// 		<Nav>
			// 			<NavItem componentClass={Link} href={transferPageUrl} to={transferPageUrl} id="NavTransfer">Transfer</NavItem>
			// 			<NavItem componentClass={Link} href={queuePageUrl} to={queuePageUrl} id="NavQueue">Queue</NavItem>
			//
			// 			{this.state.admin===true &&
			// 			<NavDropdown title="Admin" id="NavDropdown">
			// 				<NavItem id="NavAdminClients" componentClass={Link} to={userListPageUrl} href={userListPageUrl}>
			// 					User Information
			// 				</NavItem>
			// 				<NavItem id="NavAdminHistory" componentClass={Link} to={historyPageUrl} href={historyPageUrl}>Transfer History</NavItem>
			// 				<NavItem id="NavAdminSendNotifications" componentClass={Link} to={newNotifications} href={newNotifications}>Send Notifications</NavItem>
			// 			</NavDropdown>
			// 			}
			//
			// 		</Nav>}
			//
			// 		<Nav pullRight>
			// 			{this.state.login &&
			// 			<NavItem id="NavEmail" componentClass={Link} to={userPageUrl} href={userPageUrl} >{this.state.email}</NavItem>
			// 			}
			// 			{!this.state.login &&
			// 			<NavItem id="NavSignIn" componentClass={Link} to={signInUrl} href={signInUrl}>Sign in</NavItem>
			// 			}
			// 			{!this.state.login &&
			// 			<NavItem id="NavRegister" componentClass={Link} to={registerPageUrl} href={registerPageUrl}>Register</NavItem>
			// 			}
			// 			{this.state.login &&
			// 			<NavItem id="NavLogout" onClick={()=>{logout()}}>
			// 				<span>Log out</span>
			// 			</NavItem>}
			// 			<NavItem href="/support">
			// 				Support
			// 			</NavItem>
			// 			{/*<NavItem href={endpoint_db} to={endpoint_db} id="NavEndpoint">Authorization Database</NavItem>*/}
			// 		</Nav>
			// 	</Navbar.Collapse>
			// </Navbar>
		);
	}
}



export default NavbarComponent;