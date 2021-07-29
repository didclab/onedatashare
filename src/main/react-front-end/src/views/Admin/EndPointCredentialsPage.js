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

 import {DROPBOX_TYPE, GOOGLEDRIVE_TYPE,S3_TYPE, BOX_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE, GRIDFTP_NAME, DROPBOX_NAME, GOOGLEDRIVE_NAME, BOX_NAME, getType,getDefaultPortFromUri} from "../../constants";
import PropTypes from "prop-types";

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
import Grid from '@material-ui/core/Grid'
import {eventEmitter} from "../../App";
import DeleteIcon from '@material-ui/icons/Delete';

import { deleteHistory, deleteCredentialFromServer, history, savedCredList } from "../../APICalls/APICalls";
export default class EndPointCredentialsPage extends Component{
    static propTypes = {
		endpoint : PropTypes.object,
		history : PropTypes.array,
	}
     constructor(props){
         super(props);
         this.state = {
            historyList:"",
            selectType: "default",
            creds : [],
            open : false,
            email: store.getState().email,
         };
     }

     labelcreds = (number, url) =>{
        return { number, url};
      }

      _handleError = (msg) => {
        eventEmitter.emit("errorOccured", msg);
    }
	createListOfCreds = (historyList)=>{
		let values = Object.values(historyList);
		let creds = [];
        let num = 1;
		values[0].forEach(ele=>{
                creds.push(this.labelcreds(num,ele));
                num += 1;
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
  generateCert=(id)=>{
    
    alert(id);
  }
    changeFunction = (event) =>{
      
        this.setState({selectType:event.target.value});

        savedCredList(event.target.value, (data) =>{
        let newCreds = this.createListOfCreds(data);
        
        this.setState({creds:newCreds});
          // this.setState({historyList: data});
        },(error)=>{
          this._handleError("Unable to retrieve data from backend. Try log out or wait for few minutes.");
        });
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
            <form>
            <TextField style={styles.textField} disabled id="outlined-search" label="Email" type="Email" value={this.state.email} variant="outlined" />
            <TextField style={styles.textField} id="outlined-search" label="Reason" type="Reason" variant="outlined" />
            <TextField style={styles.textField} id="outlined-search" label="Assoc" type="Assoc" variant="outlined" />
            <br></br>
            <Button style={styles.formBtn} onClick={this.handleClose} variant="contained" color="primary">
            Send Request
            </Button>
            </form>
          </div>
        );

         return(
             // Currently using indexes in a to show multiple credentials. Need to dynamically code.
             <div style={styles.screen}>
                
      <Modal
  open={this.state.open}
  onClose={this.handleClose}
  aria-labelledby="simple-modal-title"
  aria-describedby="simple-modal-description"
>
  {body}
</Modal>
<Grid style={styles.wholeContainer} container>
     <Grid style={styles.wholeContainer1} item xs={6}> 
     <TableContainer style={styles.table} component={Paper}>
           <Select style={styles.selectBar} onChange={this.changeFunction} value={this.state.selectType} >
             <MenuItem style={styles.selectBarItems} disabled value="default">Select Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="Dropbox">Dropbox Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="FTP">FTP Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="DRIVE">Google Drive Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="Box">Box Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="GridFTP">GridFTP Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="HTTP">HTTP/HTTPS Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="SFTP">SFTP Credentials</MenuItem>
             <MenuItem style={styles.selectBarItems} value="S3">S3 Credentials</MenuItem>
           </Select>

{/* modal form to accept the requests */}
           
      <Table  aria-label="simple table">
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
                {/* This button currently does nothing. Need to set onClick = code */}
                <Button style={styles.deleteBtn} variant="contained" color="primary">delete</Button>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
     </Grid>
     <Grid style={styles.secondDiv} item xs={6}>
         <div style={styles.secondDivHeading}>Certificates Generated</div>
         <TableContainer style={styles.table} component={Paper}>
      <Table  aria-label="simple table">
        <TableHead>
        {this.state.creds.length>0 &&
          <TableRow>
            <TableCell style={styles.head}>No:</TableCell>
            <TableCell style={styles.head} align="right">Link:</TableCell>
            <TableCell style={styles.head} align="right"></TableCell>
          </TableRow>
     }
     {this.state.creds.length==0 &&
          <TableRow>
            <TableCell style={styles.error}>No Credentials</TableCell>
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
              <TableCell style={styles.cell} align="right">
              <Button style={styles.certGen} onClick={()=>this.generateCert(row.url)} variant="contained" color="primary">
     Generate
      </Button>
              </TableCell>
              
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
     </Grid>
     <Button style={styles.modalBtn} onClick={this.handleOpen} variant="contained" color="primary">
     Request Connector
      </Button>
</Grid>
           
    
            </div>
         );
     }
 }
 const styles = {
    screen:{
       margin:"2%",
       width:"80%"
    },
    selectBar:{
        width:"100%",
        fontSize:'21px'
    },
    selectBarItems:{
        fontSize:'21px',
        zIndex: 2
    },
    table:{
      padding:"5%",
    },
    wholeContainer:{
      marginLeft:"10%",
    },
    wholeContainer1:{
      border: '1px solid black'
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
      border: '2px solid white',
      fontWeight:"bold",
      fontSize:"14px"
    },
    certGen:{
      border: '1px solid white',
      backgroundColor:"green",
      fontWeight:"bold",
      fontSize:"10px"
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
      marginLeft:"40%",
      alignSelf:"center"
    },
    secondDiv:{
      backgroundColor:'white',
      border: '1px solid black'
    },
    secondDivHeading:{
      textAlign:"center",
      fontSize:'21px',
      fontWeight:"bold",
      marginTop:"1%"
    },
    deleteBtn:{
        marginTop:"12%",
        marginLeft:"40%",
        alignSelf:"center"
    }
};