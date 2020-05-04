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
import PropTypes from 'prop-types';

import Paper from '@material-ui/core/Paper';
import InputBase from '@material-ui/core/InputBase';
import IconButton from '@material-ui/core/IconButton';
import SearchIcon from '@material-ui/icons/Search';

import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItem';
import ListItemText from '@material-ui/core/ListItemText';

import LinearProgress from '@material-ui/core/LinearProgress';

import { globusListEndpoints, globusAddEndpoint } from "../../APICalls/globusAPICalls";
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
				(resp) => {
					this.setState({data: resp["DATA"]});
					this.setState({loading: false});
				}, (error) => {
					eventEmitter.emit("errorOccured", error);
					this.setState({loading: false});
				}
			)
		}else{
			eventEmitter.emit("errorOccured", "Search Text should not be empty.");
		}
	}

	render(){
		const { search_text, loading, data } = this.state;
		const greatDiscoveryList = data.map((v) =>
    		<ListItem button key={v.canonical_name} style={{background: "white", height: 40}} onClick={() => {
    			globusAddEndpoint(v, (resp) => {
    				this.props.endpointAdded(v);
    			}, (error) => {
    				eventEmitter.emit("errorOccured", "Error Saving Your endpoint.");
    			});
			}}>
	          <ListItemText primary={v.name} secondary={v.canonical_name} />
			</ListItem>
		);

		return (
		<div style={{width: 300, height: 400, backgroundColor: "white", borderWidth: '1px', borderColor: '#005bbb', borderRadius: 2,borderStyle: 'solid', overflow: "hidden"}}>
			{loading && <LinearProgress/>}
			<Paper style={styles.root} elevation={0}>
		      <InputBase style={styles.input} placeholder="Search for Endpoints" value={search_text} onChange={(event) => {
		      	this.setState({search_text: event.target.value})
		      }}/>
		      <IconButton style={styles.iconButton} aria-label="Search" onClick={(e) => {
		      	this.getEndpointListAsData();
		      }}>
		        <SearchIcon/>
		      </IconButton>
		    </Paper>
		    {(data.length === 0) && <h3 style={{margin: "100px"}}> No Result </h3>}
		    <List component="nav" style={{overflow: 'auto', height: "100%"}}>
		    	{data.length > 0 &&
			    	greatDiscoveryList
			    }
		    </List>
		</div>);
	}
} 