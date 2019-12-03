import React, { Component } from 'react';
import { Label, Glyphicon, Button, Alert, Table, Checkbox, HelpBlock, InputGroup, Modal, ButtonToolbar, FormGroup, ControlLabel, FormControl, Form, PageHeader } from 'react-bootstrap';
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
            filterValue: '',
            ishtml: false,
            subject: '',
            message: '',
            isValidSubject: true,
            isValidMessage: true,
            isValidRecipients: true,
            showErrorChip: false,
            showSuccessChip: false,
            errorMsg: '',
            successMsg: ''
        }
        this.externalWindow = null;
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
        this.setState({ selected: newSelected, isValidRecipients: true });
    };

    handleSelectAllClick = event => {
        const { filteredUsers, selectAll } = this.state;
        if (event.target.checked) {
            const newSelecteds = filteredUsers.map(n => n.email);
            this.setState({ selected: newSelecteds, selectAll: !selectAll, isValidRecipients: true });
            return;
        }
        this.setState({ selected: [], selectAll: !selectAll, isValidRecipients: true });
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
    onSubjectChange = event => {
        this.setState({ subject: event.target.value, isValidSubject: true });
    }
    onMessageChange = event => {
        this.setState({ message: event.target.value, isValidMessage: true });
    }

    smOpen = () => {
        this.setState({ show: true, filterValue: '', filteredUsers: this.state.users });
    }

    onSend = async () => {
        const isValidParams = this.validateInputs();
        if (isValidParams) {
            const result = await sendEmailNotification(cookies.get('email'), this.state.subject, this.state.message, this.state.selected, this.state.ishtml)
            if (result.status === 200) {
                this.setState({ showSuccessChip: true, successMsg: "Mail sent successfully.", subject: '', message: '', selected: [] });
            } else {
                this.setState({ showErrorChip: true, errorMsg: result.response });
            }
            console.log(result);
        }
    }

    validateInputs = () => {
        let isValid = true;
        if (!this.state.selected || this.state.selected.length < 1) {
            this.setState({ isValidRecipients: false })
            return false;
        }
        if (!this.state.subject || this.state.subject.length < 1) {
            this.setState({ isValidSubject: false })
            return false;
        }
        if (!this.state.message || this.state.message.length < 1) {
            this.setState({ isValidMessage: false })
            return false;
        }
        return isValid;
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

    handleisHtml = event => {
        this.setState({ ishtml: event.target.checked });
    }

    closeAlert = () => {
        this.setState({ showErrorChip: false, showSuccessChip: false });
    }

    isSelected = (email) => {
        const { selected } = this.state;
        return selected.indexOf(email) !== -1;
    }

    onClear = () => {
        this.setState({ selected: [], subject: '', message: '', successMsg: '', errorMsg: '', showErrorChip: false, showSuccessChip: false, isValidMessage: true, isValidRecipients: true, isValidSubject: true });
    }

    handleDialogBox = () => {
        this.externalWindow = window.open('', '', 'width=750,height=550,left=150,top=150');
        this.externalWindow.document.write(this.state.message);
    }

    render() {
        const { users, selected, filteredUsers, isValidSubject, isValidMessage, isValidRecipients } = this.state;
        return (
            <div>
                <div style={{ display: 'flex', flexDirection: 'row', justifyContent: 'space-between' }}>
                    <h2 style={{ marginTop: 20, flex: 4 }}>
                        <Label style={styles.pageHeading}>
                            <Glyphicon glyph="pencil" /> Compose Mail
                        </Label>
                    </h2>
                    {this.state.showErrorChip ? <Alert bsStyle="danger" onDismiss={this.handleDismiss} style={styles.alertStyle} >
                        <p>{this.state.errorMsg}</p>
                        <button class="close" onClick={this.closeAlert}>x</button>
                    </Alert> : ''
                    }
                    {this.state.showSuccessChip ?
                        <Alert bsStyle="success" onDismiss={this.handleDismiss} style={styles.alertStyle} >
                            <p>{this.state.successMsg}</p>
                            <button class="close" onClick={this.closeAlert}>x</button>
                        </Alert> : ''
                    }
                </div>
                <Form>
                    <FormGroup controlId={'to'} validationState={isValidRecipients ? 'none' : 'error'}>
                        <ControlLabel>To</ControlLabel>
                        <FormControl.Static>
                            {selected && selected.length > 0 ? <a onClick={this.smOpen}>{`${selected.length} users selected`}</a> : <a onClick={this.smOpen}>Select recipients </a>}
                        </FormControl.Static>
                        {isValidRecipients ? '' : <HelpBlock>{'Recipients cannot be empty.'}</HelpBlock>}
                    </FormGroup>
                    <FormGroup controlId={'subject'} validationState={isValidSubject ? 'none' : 'error'}>
                        <ControlLabel>Subject</ControlLabel>
                        <FormControl
                            id="formControlsText"
                            type="text"
                            label="Text"
                            placeholder="Enter subject"
                            value={this.state.subject}
                            onChange={this.onSubjectChange} />
                        {isValidSubject ? '' : <HelpBlock>{'Please enter a valid subject.'}</HelpBlock>}
                    </FormGroup>
                    <div style={{ display: 'flex', flexDirection: 'row', justifyContent: 'space-between' }}>
                        <FormGroup controlId={'isHtml'}>
                            <Checkbox checked={this.state.ishtml} style={{ margin: 0 }} onChange={this.handleisHtml} ><b>Sending HTML Mail?</b></Checkbox>
                        </FormGroup>
                        {this.state.ishtml ? <a onClick={this.handleDialogBox}>Preview</a> : <div></div>
                        }
                    </div>
                    <FormGroup controlId="message" validationState={isValidMessage ? 'none' : 'error'} >
                        <ControlLabel>Message</ControlLabel>
                        <textarea class="form-control" placeholder={'Enter Message'} rows="10" value={this.state.message} onChange={this.onMessageChange}></textarea>
                        {isValidMessage ? '' : <HelpBlock>{'Please enter a valid message.'}</HelpBlock>}
                    </FormGroup>
                    <ButtonToolbar style={styles.ButtonToolbar}>
                        <Button bsStyle="default" onClick={this.onClear} >Clear</Button>
                        <Button bsStyle="primary" style={{ backgroundColor: '#073642' }} onClick={this.onSend}>Send</Button>
                    </ButtonToolbar>
                </Form>
                <Modal
                    show={this.state.show}
                    onHide={this.smClose}
                    bsSize="large"
                    aria-labelledby="contained-modal-title-lg"
                    style={{ fontFamily: 'Ubuntu', fontWeight: 'Bold' }}
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
                            <button class="close" onClick={this.smClose}>x</button>
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
    pageHeading: { backgroundColor: 'transparent', color: '#073642', padding: 7 },
    searchBar: { marginBottom: 0, borderTop: 0, borderRight: 0, borderLeft: 0, borderRadius: 0, boxShadow: 'none' },
    formGroup: { marginBottom: 0 },
    tableHead: { backgroundColor: '#6c7ae0', color: 'white', fontWeight: 'medium' },
    primary: { backgroundColor: '#6c7ae0', borderColor: '#6c7ae0' },
    ButtonToolbar: { display: 'flex', justifyContent: 'flex-end' },
    alertStyle: { display: 'flex', flex: 2, flexDirection: 'row', justifyContent: 'space-between', marginTop: 10 }

};

export default ComposeMail;

