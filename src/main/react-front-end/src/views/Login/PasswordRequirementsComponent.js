import React, { Component } from 'react';
import { Button, Modal } from 'react-bootstrap';
import List from '@material-ui/core/List';
import ListItem from '@material-ui/core/ListItemIcon';
import ListItemIcon from '@material-ui/core/ListItemIcon';
import ListItemText from '@material-ui/core/ListItemText';
import Checkbox from '@material-ui/core/Checkbox';

export default class PasswordRequirementsComponent extends Component {
  render() {
    return (
      <div>
      { this.props.showList &&
      <List>
        {this.props.validations.map((item, key) =>
          <ListItem style={{background: "white", height: 40}}>
          <ListItemIcon>
          <Checkbox
            checked={!item.containsError}
            disabled
          />
        </ListItemIcon>
        <ListItemText primary={item.msg} />
        </ListItem>
        )}
        </List>
      }
    </div>
    );
  }
}
