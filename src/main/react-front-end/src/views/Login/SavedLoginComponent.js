import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';
import ListSubheader from '@material-ui/core/ListSubheader';
import Divider from '@material-ui/core/Divider';
import ListItemSecondaryAction from '@material-ui/core/ListItemSecondaryAction';

import IconButton from '@material-ui/core/IconButton';
import DeleteIcon from '@material-ui/icons/Delete';

const spaceBetweenStyle = {display: 'flex', justifyContent:"space-between"};

export default class SavedLoginComponent extends Component {

	static propTypes = {
	  	accounts : PropTypes.object,
	  	useAnotherAccount : PropTypes.func, 
	  	removedAccount : PropTypes.func,
	  	isLoading : PropTypes.func,
	  	login : PropTypes.func,
	}
	constructor(props){
	    super(props);
	    this.state = {
	    	accounts: this.props.accounts,
	    }
	}

	render(){
		const { accounts } = this.state;
		const {useAnotherAccount, isLoading, login} = this.props;
		if(Object.keys(accounts).length == 0){
			useAnotherAccount();
		}
		const handleLogin = (email) => {
			login(email);
		}
		const removeAccount = (email) => {
			isLoading(true);
			delete accounts[email];
			this.setState({accounts: accounts});
			this.props.removedAccount(accounts);
		}
		const addAccount = () => {
			useAnotherAccount();
		}
		return (
		<div className="enter-from-right slide-in">
			
	      	<Typography style={{fontSize: "1.6em", marginBottom: "0.4em", textAlign: 'center'}}>
	          Choose an account
	        </Typography>
	        <List>
	        	{Object.keys(accounts).map(email => (
	        		<ListItem
		              key={email}
		              role={undefined}
		          
		              button
		              onClick={()=>handleLogin(email)}
		            >
		            	<ListItemText primary={email} secondary="Signed out" />
		            	<ListItemSecondaryAction>
			                <IconButton 
			                 onClick={()=>removeAccount(email)}
			                aria-label="Delete">
		                        <DeleteIcon />
		                      </IconButton>
			              </ListItemSecondaryAction>

		            </ListItem>
	        	))}
	        	
	        			
    			<Divider 
		          	  style={{marginTop: '30px'}}/>

	            <ListItem
		              key='Add Another'
		              button
		              onClick={useAnotherAccount}
		            >
	            	<ListItemText primary='Use another account' />
	            </ListItem>
	        </List>
		</div>);
	}
} 