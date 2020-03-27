import React, { Component } from 'react';
//import { makeStyles } from '@material-ui/core/styles';
import axios from 'axios';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Button from '@material-ui/core/Button';
//import IconButton from '@material-ui/core/IconButton';
//import MenuIcon from '@material-ui/icons/Menu';
import Grid from '@material-ui/core/Grid';
import {CliInterface} from '../APICalls/EndpointAPICalls';
import {eventEmitter} from "../App";
export default class Terminal extends Component{

  constructor(props)
  {
  super(props)
  this.state={show:false};
  this.state = {
        value:'',response:'',
        loading: false,
      };
  this.toggleDiv=this.toggleDiv.bind(this);
  this.KeyPress = this.KeyPress.bind(this);
  this.textChange = this.textChange.bind(this);
  }

  toggleDiv=()=>
  {
  const{show}=this.state;
  this.setState({show:!show,value:'',response:''});
  }

  textChange(event) {
      this.setState({
        value: event.target.value,
      })
    }

      KeyPress(e) {
      if(e.keyCode === 13) {
           var inputelement=e.target.value;
           if(inputelement==="help"){
           this.response='welcome to OneDataShare, Please enter commands to submit';
           }
           else if(inputelement==="clear"){
                     console.log('clear');
                     var myNode = document.getElementById("terminalOutput");
                       while (myNode.firstChild) {
                          myNode.removeChild(myNode.firstChild);
                       }
                       this.response="";
                   }
           else
           {
         		 CliInterface(
                         				inputelement,
                         				(resp) => {
                         					this.setState({response: resp.data});

                         					console.log(this.state.response);
                         					this.setState({loading: false});
                         				}, (error) => {
                         					eventEmitter.emit("errorOccured", error);
                         					this.setState({loading: false});
                         				}
                         			)
                         		//console.log(x);
            }



            this.setState({
                      value:''
                    });
                          var terminalOutput=document.getElementById('terminalOutput');
                           var element = document.createElement("div");
                           element.appendChild(document.createTextNode('>>'+e.target.value));
                           var breakStatement= document.createElement("br");
                           element.appendChild(breakStatement);
                           //console.log(this.response);
                           var answer_node=document.createTextNode( this.response );
                           answer_node.id="answer-node";
                           element.appendChild(answer_node);
                           terminalOutput.append(element);
                           terminalOutput.scrollIntoView(false);
      }

  }
   render() {

           return (
           <div>
           <AppBar position="static" style={{backgroundColor:'#337ab7'}}>
                   <Toolbar>
                   <Grid xs="11">
                     <Typography variant="h6">
                       CLI
                     </Typography>
                     </Grid>
                     <Grid>
                     <Button onClick={this.toggleDiv} color="inherit" style={{float:"right"}}> >> </Button>
                     </Grid>
                   </Toolbar>
           </AppBar>
           {(this.state.show)?
           <div id = "show" >
                <div style={{height:'300px', backgroundColor:"black",color:"white",fontFamily: "courier",overflow: "scroll"}}>
                <div style={{fontFamily: "courier"}}>Welcome to OneDataShare, Please enter your commands. Enter help for more information</div>
                <div id="terminalOutput"></div>
                >>
                <input type="text" style ={{backgroundColor:"black",border:"none",background:"black",outline:"none"}}  onChange={this.textChange} value={this.state.value} onKeyDown={this.KeyPress} id="terminalInput"/>
                </div>
           </div>:null
           }
   </div>

           );
       };
}
