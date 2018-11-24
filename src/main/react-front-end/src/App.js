import React, { Component } from 'react';
import './App.css';
import MainComponent from "./MainComponent";
import AccountControlComponent from "./views/Login/AccountControlComponent.js";
import OauthProcessComponent from "./views/OauthProcessComponent";
import { createStore } from 'redux';
import { onedatashareModel } from './model/reducers';
import  { Route, Switch, Redirect } from 'react-router-dom';
import Favicon from 'react-favicon';
//import './lightTheme.css';

export const store = createStore(onedatashareModel);
class App extends Component {

  constructor(){
    super();
    this.state={loaded: false};
  }

  componentDidMount(){
    document.title = "OneDataShare - Home";
    this.setState({loaded: true});
  }

  render() {
    const { loggedIn } = this.state;
    return (
      <div>
        <Favicon url="http://oflisback.github.io/react-favicon/public/img/github.ico"/>
        <Switch>
          <Route path='/account' component={AccountControlComponent}/>
          <Route path='/oauth/:id' component={OauthProcessComponent}/>
          <Route exact path='/*/' component={MainComponent}/>
        </Switch>
      </div>
    );
  }
}

export default App;
