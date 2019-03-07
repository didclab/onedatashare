import React, { Component } from 'react';
import PropTypes from 'prop-types';
import AccountControlComponent from '../views/Login/AccountControlComponent';
import CreateAccountComponent from '../views/Login/CreateAccountComponent';


export default class AccountControlContainer extends Component {

  static propTypes = {
    store: PropTypes.object
  }
  
  constructor(props){
    super(props);
  }

  componentDidMount(){
    window.addEventListener("resize", this.resize.bind(this));
    this.resize();
  }

  componentWillUnmount(){
    window.removeEventListener("resize", this.resize.bind(this));
  }


  resize() {
      this.setState({isSmall: window.innerWidth <= 760});
  }
  render(){
  	const height = window.innerHeight+"px";
  	return(
            <AccountControlComponent current={null} accounts={[]}/>
        </div>  
  	);
  }
}