import React, { Component } from 'react';
import { Navbar, Nav, NavItem, NavDropdown } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import ContactSupportOutlined from '@material-ui/icons/ContactSupportOutlined';
import Tooltip from '@material-ui/core/Tooltip';
import { transferPageUrl, queuePageUrl, userPageUrl, userListPageUrl, historyPageUrl, registerPageUrl, newNotifications, signInUrl } from '../constants';
import { store } from '../App';
import { logoutAction, isAdminAction } from '../model/actions';
import { isAdmin } from '../APICalls/APICalls';

class NavbarComponent extends Component {

	constructor(props) {
		super(props);
		this.state = {
			login: store.getState().login,
			email: store.getState().email,
			admin: store.getState().admin
		};
		if (this.state.login) {
			isAdmin(store.getState().email, store.getState().hash, (userIsAdmin) => {
				if (userIsAdmin) {
					store.dispatch(isAdminAction())
				} else {
					console.log("not admin")
				}
			}, (error) => {
				console.log(error);
			});
		}
		this.unsubscribe = store.subscribe(() => {
			this.setState({ login: store.getState().login, email: store.getState().email, admin: store.getState().admin });
		});
	}
	componentWillUnmount() {
		this.unsubscribe();
	}
	render() {
		return (
			<Navbar inverse collapseOnSelect fixedTop className="navbar_navbar" id="navbar" >
				<Navbar.Header >
					<Navbar.Brand>
						<Link to="/">OneDataShare</Link>
					</Navbar.Brand>
					<Navbar.Toggle />
				</Navbar.Header>
				<Navbar.Collapse>
					{(this.state.login) &&
						<Nav>
							<NavItem componentClass={Link} href={transferPageUrl} to={transferPageUrl} id="NavTransfer">Transfer</NavItem>
							<NavItem componentClass={Link} href={queuePageUrl} to={queuePageUrl} id="NavQueue">Queue</NavItem>

		      	{this.state.admin &&
			    	<NavDropdown title="Admin" id="NavDropdown">
			        	<NavItem id="NavAdminClients" componentClass={Link} to={userListPageUrl} href={userListPageUrl}>
			        		User Information
			        	</NavItem>
			        	<NavItem id="NavAdminHistory" componentClass={Link} to={historyPageUrl} href={historyPageUrl}>Transfer History</NavItem>
						<NavItem id="NavAdminSendNotifications" componentClass={Link} to={newNotifications} href={newNotifications}>Send Notifications</NavItem>
			    	</NavDropdown>
		    	}
		    </Nav>}

		    <Nav pullRight>
		        {this.state.login &&
			        <NavItem id="NavEmail" componentClass={Link} to={userPageUrl} href={userPageUrl} >{this.state.email}</NavItem>
		    	}
		        {!this.state.login &&
			        <NavItem id="NavSignIn" componentClass={Link} to={signInUrl} href={signInUrl}>Sign in</NavItem>
			    }
		        {!this.state.login &&
			        <NavItem id="NavRegister" componentClass={Link} to={registerPageUrl} href={registerPageUrl}>Register</NavItem>
		    	}
		        {this.state.login &&
			        <NavItem id="NavLogout" onClick={()=>{store.dispatch(logoutAction())}}>
			            <span>Log out</span>
					</NavItem>}
					<NavItem href="/support">
						<Tooltip title="Report an issue" placement="top">
							<ContactSupportOutlined />
						</Tooltip>
					</NavItem>
					</Nav>
				</Navbar.Collapse>
			</Navbar>
		);
	}
}



export default NavbarComponent;
