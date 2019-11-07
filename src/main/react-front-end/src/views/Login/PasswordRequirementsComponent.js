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
      <List style={{width: '100%',maxWidth: 360}}>
        {this.props.validations.map((item, key) =>
          <ListItem style={{display:'flex', justifyContent:'flex-end', background: "white", height: 15}}>
        <ListItemText primary={item.msg}  style={{color: item.containsError ? 'red':'green'}}  edge="start"/>
        </ListItem>
        )}
        </List>
      }
    </div>
    );
  }
}
