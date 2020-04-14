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
        loading: false,
      };
      this.history=[];
      this.commandhistory=[];
      this.autosuggest=[];
      this.temp=[];
  this.toggleDiv=this.toggleDiv.bind(this);
  this.KeyPress = this.KeyPress.bind(this);
  this.textChange = this.textChange.bind(this);
  }
  toggleDiv=()=>
  {
  const{show}=this.state;
  this.setState({show:!show,value:'',response:''});
  this.history=[];
  this.commandhistory=[];
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
               this.history.push(inputelement);
               this.commandhistory.push(inputelement);
               this.commandhistory.push(',');
               this.history.push('welcome to OneDataShare, Please enter commands to submit');
           }
           else if(inputelement==="clear"){
                     var myNode = document.getElementById("terminalOutput");
                       while (myNode.firstChild) {
                          myNode.removeChild(myNode.firstChild);
                       }
                   }
            else if(inputelement==="history")
            {
                if(this.commandhistory.length===0)
                {
                  this.history.push(inputelement);
                  this.history.push('History is empty. Keep entering commands.');
                }
                else
                {
                this.history.push(inputelement);
                  this.history.push(this.commandhistory);
                  //this.commandhistory.push(',');
                 }
            }
           else
           {
         		this.history.push(inputelement);
         		this.commandhistory.push(inputelement);
         		this.commandhistory.push(',');
         		 CliInterface(
                         				inputelement,
                         				(resp) => {
                         				     var newArr = Object.values(resp);
                                             var parseArr = newArr[0].split("\n")
                                            parseArr.pop();
                         				     this.history.push(parseArr);
                         					this.setState({response: resp, loading: false});
                         				}, (error) => {
                         					eventEmitter.emit("errorOccured", error);
                         					this.setState({loading: false});
                         				}
                         			)

            }
            this.setState({
                                      value:''
                                    });

      }

      /*else if(e.keyCode === 9)
      {


         var files='ls /';

         CliInterface(
                        files,
                        (resp) => {
                            this.autosuggest = resp;
                            console.log(this.autosuggest);
                            console.log(typeof this.autosuggest);
                            console.log(typeof this.autosuggest[0]);
                            var words = this.autosuggest[0].split(" ");
                            console.log(typeof words);
                            console.log(words);
                            //for(i=0;i<)

                        }, (error) => {
                            eventEmitter.emit("errorOccured", error);
                            this.setState({loading: false});
                        }
                       )



        //console.log(myJSON);



      }*/

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
                <div style={{fontFamily: "courier"}}>Welcome to OneDataShare, Please enter your commands. Enter help for more information</div>
                <div id="terminalOutput"> {this.history.map((name,index)=>{
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

