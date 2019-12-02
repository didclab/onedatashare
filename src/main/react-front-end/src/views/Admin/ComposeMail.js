import React, { Component } from 'react';
import { Label, Glyphicon, Button, Alert, Table, Checkbox, Badge, InputGroup, Modal, ButtonToolbar, FormGroup, ControlLabel, FormControl, Form } from 'react-bootstrap';
import './ComposeMail.css';
import { getAllUsers, sendEmailNotification } from '../../APICalls/APICalls';
import { cookies } from "../../model/reducers";

class ComposeMail extends Component {

    constructor(props) {
        super(props);
        this.state = {
            show: false,
            selectAll: false,
            selected: []
        }
    }

    handleClick = (event, email) => {
        const { selected } = this.state;
        const selectedIndex = selected.indexOf(email);
        let newSelected = [];

        if (selectedIndex === -1) {
            newSelected = newSelected.concat(selected, email);
        } else if (selectedIndex === 0) {
            newSelected = newSelected.concat(selected.slice(1));
        } else if (selectedIndex === selected.length - 1) {
            newSelected = newSelected.concat(selected.slice(0, -1));
        } else if (selectedIndex > 0) {
            newSelected = newSelected.concat(
                selected.slice(0, selectedIndex),
                selected.slice(selectedIndex + 1),
            );
        }
        this.setState({ selected: newSelected });
    };

    handleSelectAllClick = event => {
        const { users, selectAll } = this.state;
        if (event.target.checked) {
            const newSelecteds = users.map(n => n.email);
            this.setState({ selected: newSelecteds, selectAll: !selectAll });
            return;
        }
        this.setState({ selected: [], selectAll: !selectAll });
    };

    clearSelected = () => {
        this.setState({ selected: [] });
    }

    smClose = () => {
        this.setState({ show: false });
    }

    smOpen = () => {
        this.setState({ show: true });
    }

    async componentDidMount() {
        const users = await getAllUsers(cookies.get('email'))
        if (users && users.length > 0) {
            this.setState({ users });
        } else {
            console.log("empty user list");
        }
        console.log("Get user list");
    }

    isSelected = (email) => {
        const { selected } = this.state;
        return selected.indexOf(email) !== -1;
    }

    render() {
        const { users, selected } = this.state;
        return (
            <div>
                <h3 style={{ marginTop: 20 }}>
                    <Label style={{ backgroundColor: '#073642', color: 'white', padding: 7 }}>
                        <Glyphicon glyph="pencil" /> Compose Mail
                    </Label>
                </h3>
                <br></br>
                <Form>
                    <FormGroup controlId={'to'}>
                        <ControlLabel>To</ControlLabel>
                        <FormControl.Static>
                            {selected && selected.length > 0 ? <a onClick={this.smOpen}>{`${selected.length} users selected`}</a> : <a onClick={this.smOpen}>Select recipients </a>}
                        </FormControl.Static>
                    </FormGroup>
                    <FormGroup controlId={'subject'}>
                        <ControlLabel>Subject</ControlLabel>
                        <FormControl
                            id="formControlsText"
                            type="text"
                            label="Text"
                            placeholder="Enter text" />
                    </FormGroup>
                    <FormGroup controlId="message" >
                        <ControlLabel>Message</ControlLabel>
                        <textarea class="form-control" placeholder={'Enter Message'} rows="10"></textarea>
                    </FormGroup>
                    <ButtonToolbar style={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <Button bsStyle="default" bsClass="clearButton">Clear</Button>
                        <Button bsStyle="primary" bsClass='button'>Send</Button>
                    </ButtonToolbar>
                </Form>
                <Modal
                    show={this.state.show}
                    onHide={this.smClose}
                    bsSize="medium"
                    aria-labelledby="contained-modal-title-lg"
                    style={{ fontFamily: 'Monaco' }}
                >
                    <Modal.Header style={{ display: 'flex', flexDirection: 'row' }}>
                        <Modal.Title id="contained-modal-title-lg" style={{ flex: 3 }}>Select Users</Modal.Title>
                        <FormGroup controlId={'search'} style={{ flex: 2, justifyContent: 'flex-end', width: '30%' }}>
                            <InputGroup>
                                <FormControl type="text" />
                                <InputGroup.Addon>
                                    <Glyphicon glyph="search" />
                                </InputGroup.Addon>
                            </InputGroup>
                        </FormGroup>
                    </Modal.Header>
                    <Modal.Body>
                        {users && users.length > 0 ? (
                            <Table responsive hover>
                                <thead>
                                    <tr>
                                        <th>
                                            <FormGroup controlId={'selectAll'} style={{ marginBottom: 0 }}>
                                                <Checkbox checked={this.state.selectAll} style={{ margin: 0 }} onChange={this.handleSelectAllClick} ></Checkbox>
                                            </FormGroup>
                                        </th>
                                        <th>Username</th>
                                        <th>email</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {
                                        users.map((user, index) => {
                                            const isItemSelected = this.isSelected(user.email);
                                            const labelId = `enhanced-table-checkbox-${index}`;
                                            return (
                                                <tr>
                                                    <td>  <FormGroup controlId={labelId} style={{ marginBottom: 0 }}>
                                                        <Checkbox checked={isItemSelected} style={{ margin: 0 }} onChange={event => this.handleClick(event, user.email)}></Checkbox>
                                                    </FormGroup></td>
                                                    <td>{`${user.firstName} ${user.lastName}`}</td>
                                                    <td>{user.email}</td>
                                                </tr>
                                            )
                                        })
                                    }
                                </tbody>
                            </Table>
                        ) : <div> No users</div>}

                    </Modal.Body>
                    <Modal.Footer>
                        <Button bsStyle={'primary'} onClick={this.props.onHide}>Select</Button>
                        <Button onClick={this.smClose}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        )
    }
}
export default ComposeMail;