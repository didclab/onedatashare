import React, { Component } from 'react';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Icon from '@material-ui/core/Icon';
import { Error } from '@material-ui/icons';

export default class PasswordRequirementsComponent extends Component {
  render() {
    return (
      <div>
        {this.props.showList &&
          <div style={{ width: '100%', color: 'red', fontSize: 10 }}>
            <Error style={{ fontSize: 11 }} />
            {this.props.errorMsg}
          </div>
        }
      </div>
    );
  }
}
