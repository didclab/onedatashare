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

 import {DROPBOX_TYPE, GOOGLEDRIVE_TYPE, BOX_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE, GRIDFTP_NAME, DROPBOX_NAME, GOOGLEDRIVE_NAME, BOX_NAME, getType,getDefaultPortFromUri} from "../../constants";
import PropTypes from "prop-types";
import './EndPointCreds.css';

import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import Button from '@material-ui/core/Button';
import Modal from '@material-ui/core/Modal';
import { store } from '../../App';
import TextField from '@material-ui/core/TextField';

import { deleteHistory, deleteCredentialFromServer, history, savedCredList } from "../../APICalls/APICalls";
export default class EndPointCredentialsPage extends Component{
    static propTypes = {
		endpoint : PropTypes.object,
		history : PropTypes.array,
	}
     constructor(props){
         super(props);
         this.state = {
            historyListFtp:"",
            historyListDropBox:"",
            historyListBox:"",
            historyListGoogle:"",
            historyListSftp:"",
            historyListGridftp:"",
            historyListHttp:"",
            mycar: "Select Credentials",
            creds : [],
            open : false,
            email: store.getState().email,
         };
		    this.historyListUpdateFromBackend();
     }
     historyListUpdateFromBackend = () => {
		// this.props.setLoading(true);
		history("",-1, (data) =>{
			this.setState({historyListFtp: data.filter((v) => { return v.indexOf(/*this.props.endpoint.uri*/FTP_TYPE) === 0 })});
			this.setState({historyListSftp: data.filter((v) => { return v.indexOf(/*this.props.endpoint.uri*/SFTP_TYPE) === 0 })});
			this.setState({historyListGoogle: data.filter((v) => { return v.indexOf(/*this.props.endpoint.uri*/GOOGLEDRIVE_TYPE) === 0 })});
			//this.props.setLoading(false);
		}, (error) => {
			this._handleError("Unable to retrieve data from backend. Try log out or wait for few minutes.");
			// this.props.setLoading(false);
		});
	}

     labelcreds = (number, url) =>{
        return { number, url};
      }
	createListOfCreds = (historyList,inputType)=>{
		let values = Object.values(historyList);
		let creds = [];
        let num = 1;
		values.forEach(ele=>{
            if(typeof(ele)=="string" && ele.indexOf(inputType)==0){
                //creds.push(<div>{ele}</div>)
                creds.push(this.labelcreds(num,ele));
                num += 1;
            }
        })
       
        // Return entire list of credentials
		return creds;
	}

  
 handleOpen = () => {
    this.setState({open:true});
  };

   handleClose = () => {
    this.setState({open:false});

  };

    changeFunction = (event) =>{
        this.setState({mycar:event.target.value});
        let newCreds = "";
        if(event.target.value==FTP_TYPE){
           newCreds = this.createListOfCreds(this.state.historyListFtp,event.target.value)
        }
        else if(event.target.value==SFTP_TYPE){
           newCreds = this.createListOfCreds(this.state.historyListSftp,event.target.value)
        }
        else if(event.target.value==GOOGLEDRIVE_TYPE){
           newCreds = this.createListOfCreds(this.state.historyListGoogle,event.target.value)
        }
        this.setState({creds:newCreds});
    }
     render(){
        
         // Create new consts to use different font sizes
         const smallFont = {
             fontSize: 16,
         }
         const mediumFont = {
             fontSize: 24,
         }

         const body = (
          <div style={styles.modalStyle}>
            <h2 style={styles.textField} id="simple-modal-title">Request Connector</h2>
            <TextField style={styles.textField} disabled id="outlined-search" label="Email" type="Email" value={this.state.email} variant="outlined" />
            <TextField style={styles.textField} id="outlined-search" label="Reason" type="Reason" variant="outlined" />
            <TextField style={styles.textField} id="outlined-search" label="Assoc" type="Assoc" variant="outlined" />
            <br></br>
            <Button style={styles.formBtn} onClick={this.handleClose} variant="contained" color="primary">
            Send Request
            </Button>
          </div>
        );

         return(
             // Currently using indexes in a to show multiple credentials. Need to dynamically code.
             <div style={styles.screen}>
                 <Select style={styles.selectBar} onChange={this.changeFunction} defaultValue="Select Credentials" >
             <MenuItem style={styles.selectBarItems} value="Dropbox">Dropbox Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value={FTP_TYPE}>FTP Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value={GOOGLEDRIVE_NAME}>Google Drive Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="Box">Box Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="GridFTP">GridFTP Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="HTTP">HTTP/HTTPS Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value={SFTP_TYPE}>SFTP Credentials</MenuItem>

           </Select>

{/* modal form to accept the requests */}
           <Button style={styles.modalBtn} onClick={this.handleOpen} variant="contained" color="primary">
        Request
      </Button>
      <Modal
  open={this.state.open}
  onClose={this.handleClose}
  aria-labelledby="simple-modal-title"
  aria-describedby="simple-modal-description"
>
  {body}
</Modal>

           <TableContainer component={Paper}>
      <Table style={styles.table} aria-label="simple table">
        <TableHead>
        {this.state.creds.length>0 &&
          <TableRow>
            <TableCell style={styles.head}>No:</TableCell>
            <TableCell style={styles.head} align="right">Link:</TableCell>
          </TableRow>
     }
     {this.state.creds.length==0 &&
          <TableRow>
            <TableCell style={styles.error}>No Credentials </TableCell>
          </TableRow>
     }
        </TableHead>
        <TableBody>
          {this.state.creds.length>0 && this.state.creds.map((row) => (
            <TableRow key={row.number}>
              <TableCell style={styles.cell} component="th" scope="row">
                {row.number}
              </TableCell>
              <TableCell style={styles.cell} align="right">{row.url}</TableCell>
              
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
                 {/* <mediumFont style={mediumFont}> Dropbox Credentials</mediumFont><br></br>
                 <mediumFont style={mediumFont}> FTP Credentials </mediumFont><br></br>
				 <smallFont style={smallFont}>
                 

                 </smallFont>
                 <mediumFont style={mediumFont}> Google Drive Credentials</mediumFont><br></br>
                 <mediumFont style={mediumFont}> Box Credentials</mediumFont><br></br>
                 <mediumFont style={mediumFont}> GridFTP Credentials</mediumFont><br></br>
                 <mediumFont style={mediumFont}> HTTP/HTTPS Credentials</mediumFont><br></br>
                 <mediumFont style={mediumFont}> SFTP Credentials</mediumFont><br></br> */}
            </div>
         );
     }
 }
 const styles = {
    screen:{
       margin:"2%" 
    },
    selectBar:{
        width:"50%",
        fontSize:'21px'
    },
    selectBarItems:{
        fontSize:'21px'
    },
    table:{
      marginLeft:"20",
    },
    cell:{
      fontSize:'17px',
    },
    head:{
      fontSize:'17px',
      fontWeight: "bold"
    },
    error:{
      fontSize:'17px',
      fontWeight: "bold", 
      color:"red",
      margin:10
    },
    modalStyle:{
      backgroundColor:"white",
      margin:"20%",
      padding:"3%",
    },
    modalBtn:{
      marginLeft:"4vh",
    },
    textField:{
      width:"93%",
      height:"5%",
      margin:"2%",
      textAlign:"center"
    },
    formBtn:{
      height:"5%",
      margin:"2%",
      marginLeft:"35%",
      alignSelf:"center"
    }
};