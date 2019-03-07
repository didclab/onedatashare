import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';

export default class ValidateEmailComponent extends Component {
	static propTypes = {
		back: PropTypes.func,
		email: PropTypes.string
	}
	constructor(props){
	    super(props);
	    this.state = {
	    	email: props.email
	    }
	}

	render(){
		const { back } = this.props;
		const { email } = this.state;
		const handleChange = name => event => {
		    this.setState({
		      [name]: event.target.value,
		    });
		};
		return (
		<div className="enter-from-right slide-in">
	        <Typography style={{fontSize: "1.6em", marginBottom: "0.4em"}}>
	          Find your Stork Account
	        </Typography>
	        <TextField
	          id="Email"
	          label="Enter Your Email"
	          value={email}
	          style={{width: '100%', marginBottom: '50px', borderRadius: '25px'}}
	          onChange={ handleChange('email') }
	        />
	        <CardActions className="flexSpaceBetween">
		        {"\n"}
		        
		        <Button size="small" variant="outlined" color="primary" onClick={back}>
		          Back to Sign in Page
		        </Button>
		        <Button size="large" variant="contained" color="primary" style={{marginLeft: '4vw'}}>
		          Next
		        </Button>
		    </CardActions>
		</div>);
	}
} 