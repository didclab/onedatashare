import React, { Component } from 'react';
import { Bounce } from 'react-activity';
import 'react-activity/dist/react-activity.css';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import {CliInterface} from '../APICalls/EndpointAPICalls';
import {eventEmitter} from "../App";
import Button from '@material-ui/core/Button';
export default class Terminal extends Component{
  constructor(props)
  {
  super(props)
  this.state={show:false};
  this.state = {
        value:'',
        history:[],
        isLoading:false
      };
  this.toggleDiv=this.toggleDiv.bind(this);
  this.KeyPress = this.KeyPress.bind(this);
  this.textChange = this.textChange.bind(this);
  }

checkEndpointlogin = () =>
{

 if(this.props.endpoint.login===true)
 {
    if(this.props.endpoint.uri.startsWith("sftp://"))
    {
       return true;
    }
 }
return false;

}
  toggleDiv=()=>
  {
  const{show}=this.state;
  this.setState({show:!show,value:'',history:[]});
  }

  textChange(event) {
      this.setState({
        value: event.target.value,
      })
    }
      KeyPress(e) {
      if(e.keyCode === 13) {
           var inputelement=e.target.value;
           if(inputelement==="clear"){
                     var myNode = document.getElementById("terminalOutput");
                       while (myNode.firstChild) {
                          myNode.removeChild(myNode.firstChild);
                       }
                       this.setState({value:'' });
                   }

           else
           {
         		var his=this.state.history;
         		his.push(inputelement);
         		this.setState({isLoading:true});
         		 CliInterface(
                         				inputelement,this.props.endpoint.uri.slice(7),this.props.endpoint.credential.username,this.props.endpoint.credential.password,this.props.endpoint.portNumber,
                         				(resp) => {

                         				     this.setState({isLoading:false});
                         				     var op_len=Object.keys(resp[0]).length;
                         				     var parseArr=[];
                         				     if(op_len===1)
                         				     {
                         				      parseArr = resp[0]['output'].split("\n");
                         				      parseArr.pop();
                         				     }
                         				    if(op_len===2)
                         				    {
                         				       parseArr=resp[0]['error'].split("\n");
                         				       parseArr.pop();
                         				    }
                                            his.push(parseArr);
                                           this.setState({

                                                    history:his,
                                                    value:'',
                                                  })

                         				}, (error) => {
                         					eventEmitter.emit("errorOccured", error);
                         				}
                         			)
           }


      }
  }
   render() {
           return (
           <React.Fragment>
           { !this.checkEndpointlogin() ?(<div>
           <AppBar position="relative" style={{backgroundColor:'#337ab7'}} onClick={this.toggleDiv} >
                   <Toolbar>
                   <Grid>
                     <Typography variant="h6">
                       CLI &nbsp;&nbsp;&nbsp;>>
                     </Typography>
                     </Grid>
                   </Toolbar>
           </AppBar>

           {(this.state.show)?
           <div id ="show">
                <div style={{height:'300px', backgroundColor:"black",color:"white",fontFamily: "courier",overflow: "scroll"}}>
                <div style={{fontFamily: "courier"}}>Welcome to OneDataShare. Please enter your commands. <br/>logged as {this.props.endpoint.credential.username}.</div>

                <div id="terminalOutput"> {this.state.history.map((name,index)=>{
                                                 if(index%2!==0)
                                                 {
                                                    return(name.map((results,index1) =>{
                                                      return(<span key={index1} style={{display:"inline-block",width:"600px",height: "10px",color:"green"}}>{results}</span>)
                                                    }))
                                                 }

                                                 else
                                                 {
                                                return(<div key={index}><div>>>{name}</div></div>)
                                                 }
                 })}</div>
                 {(this.state.isLoading)?<div> <Bounce color="green" size={20}  /></div>:<div>
              {this.props.endpoint.credential.username}>><input type="text" autoFocus='autoFocus' size='40' style ={{backgroundColor:"black",border:"none",background:"black",outline:"none"}}  onChange={this.textChange} value={this.state.value} onKeyDown={this.KeyPress} id="terminalInput"/></div>}
               </div>
           </div>:null
           }
   </div>): null }
   </React.Fragment>

           );
       };
}

