import React, {Component} from 'react';
import Typography from '@material-ui/core/Typography';
import TextField from '@material-ui/core/TextField';
import CardActions from '@material-ui/core/CardActions';
import Button from '@material-ui/core/Button';
import PropTypes from 'prop-types';



import Paper from '@material-ui/core/Paper';
import InputBase from '@material-ui/core/InputBase';
import Divider from '@material-ui/core/Divider';
import IconButton from '@material-ui/core/IconButton';
import MenuIcon from '@material-ui/icons/Menu';
import SearchIcon from '@material-ui/icons/Search';
import DirectionsIcon from '@material-ui/icons/Directions';

import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';

import LinearProgress from '@material-ui/core/LinearProgress';

import {globusListEndpoints, globusEndpointIds, globusEndpointDetail} from '../../APICalls/APICalls';
import {eventEmitter} from "../../App";

const styles = {
  root: {
    padding: '2px 4px',
    display: 'flex',
    alignItems: 'center',
    paddingLeft: 10,
    paddingRight: 10,
  },
  input: {
    marginLeft: 8,
    flex: 1,
    width: "90%"
  },
  iconButton: {
    padding: 10,
  },
  divider: {
    width: 1,
    height: 28,
    margin: 4,
  },
};

export default class GlobusEndpointListingComponent extends Component {
	static propTypes = {
		close: PropTypes.func,
		endpointAdded: PropTypes.func
	}
	constructor(props){
	    super(props);
	    this.state={
	    	search_text: "",
	    	data:[],
	    	loading: false,
	    }

	}

	getEndpointListAsData(){
		const { search_text } = this.state;
		this.setState({loading: true});
		if(search_text !== ""){
			globusListEndpoints(
				search_text,
				(resp)=>{
					this.setState({data: resp["DATA"]});
					this.setState({loading: false});
				}, (error)=>{
					eventEmitter.emit("errorOccured", error);
					this.setState({loading: false});
				}
			)
		}else{
			eventEmitter.emit("errorOccured", "Search Text should not be empty.");
		}
	}

	render(){
		const { close } = this.props;
		const { search_text, loading, data } = this.state;
		console.log(data)
		const greatDiscoveryList = data.map((v)=>
    		<ListItem button key={v.canonical_name} style={{background: "white", height: 40}} onClick={() => {
    			console.log("endpoint without detail", v)
    			globusEndpointIds(v, (resp)=>{
    				this.props.endpointAdded(v);
    			}, (error)=>{
    				eventEmitter.emit("errorOccured", "Error Saving Your endpoint.");
    			});
			}}>
	          <ListItemText primary={v.name} secondary={v.canonical_name} />
			</ListItem>
		);

		return (
		<div style={{  width: 300, height: 400, backgroundColor: "white", borderWidth: '1px', borderColor: '#005bbb', borderRadius: 2,borderStyle: 'solid', overflow: "hidden"}}>
			{loading && <LinearProgress/>}
			<Paper style={styles.root} elevation={0}>
		      <InputBase style={styles.input} placeholder="Search for Endpoints" value={search_text} onChange={(event)=>{
		      	this.setState({search_text: event.target.value})
		      }}/>
		      <IconButton style={styles.iconButton} aria-label="Search" onClick={(e)=>{
		      	this.getEndpointListAsData();
		      }}>
		        <SearchIcon/>
		      </IconButton>
		    </Paper>

		    {(data.length === 0) &&
		    	<h3 style={{margin: "100px"}}> No Result </h3>
			}

			
		    <List component="nav" style={{overflow: 'auto', height: "100%"}}>
		    	{data.length > 0 &&
			    	greatDiscoveryList
			    }
		    </List>
			
		</div>);
	}
} 