import React, { Component } from 'react';
import {Navbar, Nav, NavItem, NavDropdown} from 'react-bootstrap';
import Button from '@material-ui/core/Button';
import { PropTypes } from 'prop-types';
import { Redirect, Link} from 'react-router-dom';
import ContactSupportOutlined  from '@material-ui/icons/ContactSupportOutlined';
import Tooltip from '@material-ui/core/Tooltip';

import {transferPageUrl,queuePageUrl, userPageUrl, userListPageUrl, historyPageUrl, registerPageUrl, accountPageUrl, managementPageUrl, dataPageUrl} from '../constants';
import {store} from '../App';
import {logoutAction, isAdminAction} from '../model/actions';
import {isAdmin} from '../APICalls/APICalls';

class NavbarComponent extends Component {
	
	constructor(props){
		super(props);
		this.state={
		  login: store.getState().login,
		  email: store.getState().email,
		  admin: store.getState().admin
		};
		if(this.state.login){
			isAdmin(store.getState().email, store.getState().hash, (userIsAdmin)=>{
				if(userIsAdmin){
				    store.dispatch(isAdminAction())
				}else{
					console.log("not admin")
				}
			}, (error)=>{
				console.log(error);
			});
		}

		this.unsubscribe = store.subscribe(()=>{
			this.setState({login: store.getState().login, email : store.getState().email, admin: store.getState().admin});
		});
	}
	componentWillUnmount(){
		this.unsubscribe();
	}
  render() {
    return (
    	<Navbar inverse collapseOnSelect fixedTop className="navbar_navbar" id="navbar">
    		
		    <Navbar.Header >
		      <Navbar.Brand>
		        <Link to="/">OneDataShare</Link>
		      </Navbar.Brand>
		      <Navbar.Toggle/>
		    </Navbar.Header>

	      	
	    	<Navbar.Collapse>
	      	{(this.state.login ) &&
		      <Nav>
				<NavItem componentClass={Link} href={transferPageUrl} to={transferPageUrl}>Transfer</NavItem>
		        <NavItem componentClass={Link} href={queuePageUrl} to={queuePageUrl}>Queue</NavItem>
		      
		      	{this.state.admin &&
			    	<NavDropdown title="Admin" id="Navbar Dropdown">
			        	<NavItem componentClass={Link} to={userListPageUrl} href={userListPageUrl}>
			        		Clients Information
			        	</NavItem>
			        	<NavItem componentClass={Link} to={historyPageUrl} href={historyPageUrl}>History</NavItem>
			        	{/*<NavItem componentClass={Link} to={managementPageUrl} href={managementPageUrl}>Management</NavItem>
			        	<NavItem componentClass={Link} to={dataPageUrl} href={dataPageUrl}>Data</NavItem>*/}
			    	</NavDropdown>
		    	}
		    </Nav>}

		    <Nav pullRight>
		        {this.state.login &&
			        <NavItem componentClass={Link} to={userPageUrl} href={userPageUrl}>{this.state.email}</NavItem>
		    	}
		        {!this.state.login &&
			        <NavItem componentClass={Link} to={accountPageUrl} href={accountPageUrl}>Sign in</NavItem>
			    }
		        {!this.state.login &&
			        <NavItem componentClass={Link} to={registerPageUrl} href={registerPageUrl}>Register</NavItem>
		    	}

		        {this.state.login && 
			        <NavItem onClick={()=>{store.dispatch(logoutAction())}}>
			            <span>Log out</span>
							</NavItem>}
					
						<NavItem component={Link} href="/support">
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
