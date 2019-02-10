import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';

import listEndpoints from '../../APICalls/APICalls';

export default class GlobusEndpointListingComponent extends Component {
	static propTypes = {
		close: PropTypes.func,
		endpointAdd: PropTypes.func
	}
	constructor(props){
	    super(props);
	    this.state={
	    	search_text = "",
	    	data:[],
	    }
	    listEndpoint()
	}

	render(){
		const { close } = this.props;
		const { search_text } = this.state;
		const handleChange = name => event => {
		    
		};
		return (
		<div>
			{search_text === "" && <LinearProgress/>}
		</div>);
	}
} 