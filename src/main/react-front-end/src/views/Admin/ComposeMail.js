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
            selected: [],
            users: [],
            filteredUsers: [],
            filterValue: ''
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
        const { filteredUsers, selectAll } = this.state;
        if (event.target.checked) {
            const newSelecteds = filteredUsers.map(n => n.email);
            this.setState({ selected: newSelecteds, selectAll: !selectAll });
            return;
        }
        this.setState({ selected: [], selectAll: !selectAll });
    };

    handleFilter = event => {
        const { users } = this.state;
        const filteredUsers = users.filter((user) => user.firstName.indexOf(event.target.value) > -1 || user.lastName.indexOf(event.target.value) > -1 || user.email.indexOf(event.target.value) > -1).map((user) => (user));
        this.setState({ filteredUsers, filterValue: event.target.value });
    }

    clearSelected = () => {
        this.setState({ selected: [], selectAll: false, filteredUsers: this.state.users, filterValue: '' });
    }

    smClose = () => {
        this.setState({ show: false });
    }

    smOpen = () => {
        this.setState({ show: true, filterValue: '', filteredUsers: this.state.users });
    }

    async componentDidMount() {
        const users = await getAllUsers(cookies.get('email'))
        if (users && users.length > 0) {
            this.setState({ users, filteredUsers: users });
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
        const { users, selected, filteredUsers } = this.state;
        return (
            <div>
                <h3 style={{ marginTop: 20 }}>
                    <Label style={styles.pageHeading}>
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
                    bsSize="large"
                    aria-labelledby="contained-modal-title-lg"
                    style={{ fontFamily: 'Ubuntu' }}
                >
                    <Modal.Header style={styles.headerStyle} >
                        <div style={{ flex: 1 }}>
                            <h4 bsStyle='primary' style={styles.modalHeading}>Select Users</h4>
                        </div>
                        <div style={{ flex: 2 }}>
                            <FormGroup style={styles.formGroup}>
                                <FormControl type="text" placeholder={`Search by name or Email`} style={styles.searchBar} onChange={this.handleFilter} value={this.state.filterValue} />
                            </FormGroup>
                        </div>
                        <div style={{ flex: 1, alignItems: 'flex-end' }}>
                            <button class="close" onClick={this.smClose}>x </button>
                        </div>
                    </Modal.Header>
                    <Modal.Body>
                        {filteredUsers && filteredUsers.length > 0 ? (
                            <Table responsive hover>
                                <thead style={styles.tableHead}>
                                    <tr>
                                        <th>
                                            <FormGroup controlId={'selectAll'} style={{ marginBottom: 0 }}>
                                                <Checkbox checked={this.state.selectAll && selected.length === filteredUsers.length} style={{ margin: 0 }} onChange={this.handleSelectAllClick} ></Checkbox>
                                            </FormGroup>
                                        </th>
                                        <th>Username</th>
                                        <th>Email</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {
                                        filteredUsers.map((user, index) => {
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
                        <Button bsStyle={'primary'} style={styles.primary} onClick={this.smClose}>Select</Button>
                        <Button onClick={this.clearSelected}>Clear</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        )
    }
}
const styles = {
    headerStyle: { display: 'flex', flexDirection: 'row', justifyContent: 'space-between' },
    modalCloseButton: {
        flex: 1, alignItems: 'flex-end', justifyContent: 'flex-end', color: 'black', width: 30
    },
    modalHeading: { flex: 2, marginBottom: 0, color: '#6c7ae0', fontStyle: 'bold' },
    pageHeading: { backgroundColor: '#073642', color: 'white', padding: 7 },
    searchBar: { marginBottom: 0, borderTop: 0, borderRight: 0, borderLeft: 0, borderRadius: 0, boxShadow: 'none' },
    formGroup: { marginBottom: 0 },
    tableHead: { backgroundColor: '#6c7ae0', color: 'white', fontWeight: 'medium' },
    primary: { backgroundColor: '#6c7ae0', borderColor: '#6c7ae0' }
};

export default ComposeMail;

