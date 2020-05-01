import React, { Component } from 'react';
import AppBar from '@material-ui/core/AppBar';
import Toolbar from '@material-ui/core/Toolbar';
import Typography from '@material-ui/core/Typography';
import Grid from '@material-ui/core/Grid';
import {CliInterface} from '../APICalls/EndpointAPICalls';
import {eventEmitter} from "../App";

export default class Terminal extends Component{
  constructor(props)
  {
  super(props)
  this.state={show:false};
  this.state = {
        value:'',
        history:[],
        host:'',
        uname:'',
        epw:'',
        port:'',

      };
  this.toggleDiv=this.toggleDiv.bind(this);
  this.KeyPress = this.KeyPress.bind(this);
  this.textChange = this.textChange.bind(this);
  this.onLoginSuccess=this.onLoginSuccess.bind(this);
  }
  componentDidMount(){
      	eventEmitter.on("sftploginsuccessmessage", this.onLoginSuccess);

    	}

  onLoginSuccess(url,username,epw,port){
  		this.setState({
                host: url.slice(7),
                uname:username,
                epw:epw,
                port:port
              })

        //console.log('hiii');
        //console.log(this.state.epw)
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
         		 CliInterface(
                         				inputelement,this.state.host,this.state.uname,this.state.epw,this.state.port,
                         				(resp) => {
                         				     var newArr = Object.values(resp);
                                             var parseArr = newArr[0].split("\n")
                                            parseArr.pop();
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
           <div>
           <AppBar position="static" style={{backgroundColor:'#337ab7'}} onClick={this.toggleDiv} >
                   <Toolbar>
                   <Grid>
                     <Typography variant="h6">
                       Command Line Interface &nbsp;&nbsp;&nbsp;>>
                     </Typography>
                     </Grid>
                   </Toolbar>
           </AppBar>
           {(this.state.show)?
           <div id ="show">
                <div style={{height:'300px', backgroundColor:"black",color:"white",fontFamily: "courier",overflow: "scroll"}}>
                <div style={{fontFamily: "courier"}}>Welcome to OneDataShare, Please enter your commands.</div>
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
              >><input type="text" autoFocus='autoFocus' size='40' style ={{backgroundColor:"black",border:"none",background:"black",outline:"none"}}  onChange={this.textChange} value={this.state.value} onKeyDown={this.KeyPress} id="terminalInput"/>
               </div>
           </div>:null
           }
   </div>

           );
       };
}

