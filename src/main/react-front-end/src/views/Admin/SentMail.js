import React, { Component } from 'react';
import { Link } from '@material-ui/core';
import { Label, Glyphicon, Table, Button, Modal, ButtonToolbar, Col, Alert, OverlayTrigger, Tooltip, Form, FormControl, FormGroup, ControlLabel, HelpBlock, Checkbox } from 'react-bootstrap';
import './SentMail.css';
import { getAllMails, sendEmailNotification, getAllUsers, deleteMail } from '../../APICalls/APICalls';
import { cookies } from "../../model/reducers";

const viewtooltip = (
    <Tooltip id="tooltip">
        <p>{'View/Resend Mail'}</p>
    </Tooltip>
);

const deletetooltip = (
    <Tooltip id="tooltip">
        <p>{'Delete Mail'}</p>
    </Tooltip>
);

class SentMail extends Component {

    constructor(props) {
        super(props);
        this.state = {
            showErrorChip: false,
            showSuccessChip: false,
            mails: [],
            errorMsg: '',
            successMsg: '',
            currentView: null,
            viewMail: false,
            isValidMessage: true,
            isValidRecipients: true,
            isValidSubject: true,
            showDelete: false,
            showSelectUsers: false,
            unSelectedUsers: [],
            selectedUsers: [],
            checkedSelectedUsers: [],
            checkedUnSelectedUsers: [],
            selectAllCheckedUSUsers: false,
            selectAllCheckedSUsers: false,
            showSUErrorMsg: null
        }
    }
    closeDelete = () => {
        this.setState({ showDelete: false });
    }

    isSelected = (type, email) => {
        if (type === 'selected') {
            return this.state.checkedSelectedUsers.indexOf(email) !== -1;
        } else {
            return this.state.checkedUnSelectedUsers.indexOf(email) !== -1;
        }
    }

    handleCheckBoxClick = (event, type, email) => {
        let checkedUsers = [];
        let newList = [];
        if (type === 'selected') {
            checkedUsers = this.state.checkedSelectedUsers;
        } else {
            checkedUsers = this.state.checkedUnSelectedUsers;
        }
        const selectedIndex = checkedUsers.indexOf(email);
        if (selectedIndex === -1) {
            newList = newList.concat(checkedUsers, email);
        } else if (selectedIndex === 0) {
            newList = newList.concat(checkedUsers.slice(1));
        } else if (selectedIndex === checkedUsers.length - 1) {
            newList = newList.concat(checkedUsers.slice(0, -1));
        } else if (selectedIndex > 0) {
            newList = newList.concat(
                checkedUsers.slice(0, selectedIndex),
                checkedUsers.slice(selectedIndex + 1),
            );
        }
        if (type === 'selected') {
            this.setState({ checkedSelectedUsers: newList, selectAllCheckedSUsers: newList.length === this.state.selectedUsers.length });
        } else {
            this.setState({ checkedUnSelectedUsers: newList, selectAllCheckedUSUsers: newList.length === this.state.unSelectedUsers.length });
        }
    }

    handleSelectAllClick = (event, type) => {
        const { selectAllCheckedSUsers, selectAllCheckedUSUsers, unSelectedUsers, selectedUsers } = this.state;
        if (type === 'selected') {
            if (event.target.checked) {
                this.setState({ checkedSelectedUsers: selectedUsers, selectAllCheckedSUsers: !selectAllCheckedSUsers });
            } else {
                this.setState({ checkedSelectedUsers: [], selectAllCheckedSUsers: !selectAllCheckedSUsers });
            }
        } else {
            if (event.target.checked) {
                this.setState({ checkedUnSelectedUsers: unSelectedUsers, selectAllCheckedUSUsers: !selectAllCheckedUSUsers });
            } else {
                this.setState({ checkedUnSelectedUsers: [], selectAllCheckedUSUsers: !selectAllCheckedUSUsers });
            }
        }
    };

    moveToUnselectedList = () => {
        const { checkedSelectedUsers, selectedUsers, unSelectedUsers } = this.state;
        const newSelectedUsers = selectedUsers.filter(user => !this.state.checkedSelectedUsers.includes(user));
        const newUnSelectedUsers = unSelectedUsers.concat(checkedSelectedUsers);
        this.setState({ selectedUsers: newSelectedUsers, checkedSelectedUsers: [], unSelectedUsers: newUnSelectedUsers });
    }

    moveToSelectedList = () => {
        const { checkedUnSelectedUsers, selectedUsers, unSelectedUsers } = this.state;
        const newUnSelectedUsers = unSelectedUsers.filter(user => !this.state.checkedUnSelectedUsers.includes(user));
        const newSelectedUsers = selectedUsers.concat(checkedUnSelectedUsers);
        const showSUErrorMsg = newSelectedUsers.length === 0 ? 'Please select atleast 1 user to send email.' : null;
        this.setState({ showSUErrorMsg, selectedUsers: newSelectedUsers, checkedUnSelectedUsers: [], unSelectedUsers: newUnSelectedUsers });
    }

    showDelete = (mail) => {
        this.setState({ showDelete: true, deleteMailID: mail.uuid });
    }

    getSentMails = async () => {
        const mails = await getAllMails(cookies.get('email'));
        this.setState({ mails });
    }

    async componentDidMount() {
        await this.getSentMails();
        const users = await getAllUsers(cookies.get('email'))
        if (users && users.length > 0) {
            this.setState({ users, filteredUsers: users });
        } else {
            console.log("empty user list");
        }
        console.log("Get user list");
    }

    viewMail = (mail) => {
        this.setState({ viewMail: true, currentView: mail });
    }

    deleteMail = async () => {
        const result = await deleteMail(this.state.deleteMailID);
        if (result.status === 200) {
            this.setState({ showSuccessChip: true, showDelete: false, successMsg: "Mail Deleted Successfully." });
            await this.getSentMails();
        } else {
            this.setState({ showErrorChip: true, showDelete: false, errorMsg: result.statusText });
        }
        console.log(result);
    }

    onSubjectChange = event => {
        this.setState({ currentView: { ...this.state.currentView, subject: event.target.value } });
    }

    showSelectUsersClose = () => {
        this.setState({ showSelectUsers: false });
    }
    showSelectUsersOpen = () => {
        let unSelectedUsers = [];
        if (this.state.currentView && this.state.currentView.recipients && this.state.filteredUsers) {
            unSelectedUsers = this.state.filteredUsers.filter(user => !this.state.currentView.recipients.includes(user.email));
        }
        unSelectedUsers = unSelectedUsers.map(user => user.email);
        this.setState({ showSelectUsers: true, showSUErrorMsg: null, unSelectedUsers, selectedUsers: this.state.currentView.recipients });
    }

    onMessageChange = event => {
        this.setState({ currentView: { ...this.state.currentView, message: event.target.value } });
    }

    closeMailBox = () => {
        this.setState({ viewMail: false });
    }

    onSelectUsers = () => {
        const { selectedUsers } = this.state;
        if (selectedUsers.length === 0) {
            this.setState({ showSUErrorMsg: 'Please select atleast 1 user to send email.' });
        } else {
            this.setState({ showSelectUsers: false, currentView: { ...this.state.currentView, recipients: selectedUsers } });
        }
    }

    onResend = async () => {
        const { subject, recipients, message, html } = this.state.currentView;
        const result = await sendEmailNotification(cookies.get('email'), subject, message, recipients, html);
        if (result.status === 200) {
            this.setState({ showSuccessChip: true, successMsg: "Mail sent successfully.", viewMail: false });
            await this.getSentMails();
        } else {
            this.setState({ showErrorChip: true, errorMsg: result.response, viewMail: false });
        }
    }

    closeAlert = () => {
        this.setState({ showErrorChip: false, showSuccessChip: false });
    }

    render() {
        const { showSuccessChip, showErrorChip, errorMsg, successMsg, isValidMessage, isValidRecipients, isValidSubject } = this.state;
        const mails = this.state.mails.sort((a, b) => {
            return new Date(a.sentDateTime).getTime() -
                new Date(b.sentDateTime).getTime()
        }).reverse();
        const tooltip = (
            this.state.currentView ?
                <Tooltip id="tooltip">
                    {this.state.currentView.recipients && this.state.currentView.recipients.map((mail, index) => {
                        return (
                            <p>{mail}{'\n'}</p>
                        );
                    })}
                </Tooltip>
                : <div></div>);

        return (
            <div style={{ display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', flexDirection: 'row', justifyContent: 'space-between' }}>
                    <h2 style={{ marginTop: 20, flex: 4 }}>
                        <Label style={styles.pageHeading}>
                            <Glyphicon glyph="check" /> Sent Mail
                        </Label>
                    </h2>
                    {showErrorChip ? <Alert bsStyle="danger" style={styles.alertStyle} >
                        <p>{errorMsg}</p>
                        <button class="close" onClick={this.closeAlert}>x</button>
                    </Alert> : ''
                    }
                    {showSuccessChip ?
                        <Alert bsStyle="success" style={styles.alertStyle} >
                            <p>{successMsg}</p>
                            <button class="close" onClick={this.closeAlert}>x</button>
                        </Alert> : ''}
                </div>
                {mails && mails.length > 0 ?
                    <div style={{ flex: 3, overflowY: 'scroll' }}>
                        <Table responsive hover>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Date</th>
                                    <th>Recipients</th>
                                    <th>Subject</th>
                                    <th>Message</th>
                                    <th>isHtml</th>
                                    <th>Options</th>
                                </tr>
                            </thead>
                            <tbody>
                                {
                                    mails.map((mail, index) => {
                                        return (
                                            <tr>
                                                <td>{index + 1}</td>
                                                <td>{new Date(mail.sentDateTime).toLocaleDateString()}</td>
                                                <td>{mail.recipients.length === 1 ? '1 user' : `${mail.recipients.length} users`}</td>
                                                <td>{mail.subject}</td>
                                                <td>{mail.message.substring(0, 100)}</td>
                                                <td>{mail.html === true ? <Glyphicon glyph='ok' style={{ color: 'green' }} /> : <Glyphicon glyph='remove' style={{ color: 'red' }} />}</td>
                                                <td>
                                                    <ButtonToolbar>
                                                        <OverlayTrigger placement="bottom" overlay={viewtooltip}>
                                                            <Glyphicon glyph='edit' style={{ color: 'teal', fontSize: 18, margin: 7 }} onClick={() => this.viewMail(mail)} />
                                                        </OverlayTrigger>
                                                        <OverlayTrigger placement="bottom" overlay={deletetooltip}>
                                                            <Glyphicon glyph='trash' style={{ color: 'red', fontSize: 18, margin: 7 }} onClick={() => this.showDelete(mail)} />
                                                        </OverlayTrigger>
                                                    </ButtonToolbar>
                                                </td>
                                            </tr>
                                        )
                                    })
                                }
                            </tbody>
                        </Table>
                    </div> : <div></div>
                }
                {this.state.currentView ?
                    <Modal
                        show={this.state.viewMail}
                        onHide={this.closeMailBox}
                        bsSize="large"
                        aria-labelledby="contained-modal-title-lg"
                    >
                        <Modal.Header closeButton>
                            <Modal.Title id="contained-modal-title-lg">
                                <Glyphicon glyph="envelope" /> Mail Box</Modal.Title>
                        </Modal.Header>
                        <Modal.Body>
                            <Form>
                                <FormGroup validationState={isValidRecipients ? 'none' : 'error'}>
                                    <ControlLabel>To</ControlLabel>
                                    <OverlayTrigger placement="right" overlay={tooltip}>
                                        <FormControl.Static style={{ width: '15%' }}>
                                            {this.state.currentView && this.state.currentView.recipients && this.state.currentView.recipients.length > 0 ? <Link href="#" onClick={this.showSelectUsersOpen}>{`${this.state.currentView.recipients.length} users selected`}</Link> : <Link href="#">No recipients </Link>}
                                        </FormControl.Static>
                                    </OverlayTrigger>
                                    {isValidRecipients ? '' : <HelpBlock>{'Recipients cannot be empty.'}</HelpBlock>}
                                </FormGroup>
                                <FormGroup controlId={'subject'} validationState={isValidSubject ? 'none' : 'error'}>
                                    <ControlLabel>Subject</ControlLabel>
                                    <FormControl
                                        id="formControlsText"
                                        type="text"
                                        label="Text"
                                        placeholder="Enter subject"
                                        value={this.state.currentView.subject}
                                        onChange={this.onSubjectChange} />
                                    {isValidSubject ? '' : <HelpBlock>{'Please enter a valid subject.'}</HelpBlock>}
                                </FormGroup>
                                <FormGroup controlId={"message"} validationState={isValidMessage ? 'none' : 'error'} >
                                    <ControlLabel>Message</ControlLabel>
                                    <textarea class="form-control" placeholder={'Enter Message'} rows="10" value={this.state.currentView.message} onChange={this.onMessageChange}></textarea>
                                    {isValidMessage ? '' : <HelpBlock>{'Please enter a valid message.'}</HelpBlock>}
                                </FormGroup>
                            </Form>
                        </Modal.Body>
                        <Modal.Footer>
                            <Button bsStyle='success' onClick={this.onResend}>Resend</Button>
                            <Button onClick={this.closeMailBox}>Cancel</Button>
                        </Modal.Footer>
                    </Modal>
                    : <div></div>}

                <Modal
                    show={this.state.showSelectUsers}
                    onHide={this.showSelectUsersClose}
                    bsSize="large"
                    aria-labelledby="select_user_modal"
                >
                    <Modal.Header closeButton>
                        <Modal.Title id="select_user_modal">Select Users</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <div style={{ width: '100%' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-around' }}>
                                <Col lg={5}>
                                    <h5>{'Selected Users'}</h5>
                                    <Table responsive bsClass={'table'} style={{ marginBottom: 0 }}>
                                        <thead style={styles.tableHead}>
                                            <tr>
                                                <th>
                                                    <FormGroup controlId={'selectAll'} style={{ marginBottom: 0 }}>
                                                        <Checkbox checked={this.state.selectAllCheckedSUsers && this.state.selectedUsers.length > 0 && this.state.selectedUsers.length === this.state.checkedSelectedUsers.length} style={{ margin: 0 }} onChange={event => this.handleSelectAllClick(event, 'selected')} ></Checkbox>
                                                    </FormGroup>
                                                </th>
                                                <th>Email</th>
                                            </tr>
                                        </thead>
                                    </Table>
                                    <div style={{ display: 'block', overflowY: 'scroll', height: 300, width: '100%' }}>
                                        <Table responsive >
                                            <tbody style={{ display: 'table', width: '100%' }}>
                                                {
                                                    this.state.selectedUsers.map((user, index) => {
                                                        const isItemSelected = this.isSelected('selected', user);
                                                        //const isItemSelected = false;
                                                        const labelId = `enhanced-table-checkbox-${index}`;
                                                        return (
                                                            <tr >
                                                                <td>  <FormGroup controlId={labelId} style={{ marginBottom: 0 }}>
                                                                    <Checkbox checked={isItemSelected} style={{ margin: 0 }} onChange={event => this.handleCheckBoxClick(event, 'selected', user)}></Checkbox>
                                                                </FormGroup></td>
                                                                <td>{user}</td>
                                                            </tr>
                                                        )
                                                    })
                                                }
                                            </tbody>
                                        </Table>
                                    </div>
                                </Col>
                                <Col lg={2}>
                                    <div style={{ display: 'flex', height: 300, flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                                        <Button disabled={!this.state.checkedSelectedUsers || this.state.checkedSelectedUsers.length === 0} onClick={this.moveToUnselectedList}> {'>'} </Button>
                                        <Button disabled={!this.state.checkedUnSelectedUsers || this.state.checkedUnSelectedUsers.length === 0} onClick={this.moveToSelectedList}> {'<'} </Button>
                                    </div>
                                </Col>
                                <Col lg={5}>
                                    <h5>{'Remaining Users'}</h5>
                                    <Table responsive style={{ marginBottom: 0 }}>
                                        <thead style={styles.tableHead}>
                                            <tr>
                                                <th>
                                                    <FormGroup controlId={'selectAll'} style={{ marginBottom: 0 }}>
                                                        <Checkbox checked={this.state.selectAllCheckedUSUsers && this.state.unSelectedUsers.length > 0 && this.state.checkedUnSelectedUsers.length === this.state.unSelectedUsers.length} style={{ margin: 0 }} onChange={event => this.handleSelectAllClick(event, 'unselected')} ></Checkbox>
                                                    </FormGroup>
                                                </th>
                                                <th>Email</th>
                                            </tr>
                                        </thead>
                                    </Table>
                                    <div style={{ display: 'block', overflowY: 'scroll', height: 300, width: '100%' }}>
                                        <Table responsive >
                                            <tbody style={{ display: 'table', width: '100%' }}>
                                                {
                                                    this.state.unSelectedUsers.map((user, index) => {
                                                        const isItemSelected = this.isSelected('unselected', user);
                                                        //const isItemSelected = false;
                                                        const labelId = `enhanced-table-checkbox-${index}`;
                                                        return (
                                                            <tr >
                                                                <td>  <FormGroup controlId={labelId} style={{ marginBottom: 0 }}>
                                                                    <Checkbox checked={isItemSelected} style={{ margin: 0 }} onChange={event => this.handleCheckBoxClick(event, 'unselected', user)}></Checkbox>
                                                                </FormGroup></td>
                                                                <td>{user}</td>
                                                            </tr>
                                                        )
                                                    })
                                                }
                                            </tbody>
                                        </Table>
                                    </div>
                                </Col>
                            </div>
                        </div>
                        {this.state.showSUErrorMsg ?
                            <Alert bsStyle="danger" style={styles.alertStyle} >
                                <p>{this.state.showSUErrorMsg}</p>
                            </Alert> : ''}
                    </Modal.Body>
                    <Modal.Footer>
                        <Button bsStyle={'primary'} onClick={this.onSelectUsers}>Select</Button>
                        <Button onClick={this.showSelectUsersClose}>Close</Button>
                    </Modal.Footer>
                </Modal>

                <Modal
                    show={this.state.showDelete}
                    onHide={this.closeDelete}
                    bsSize="small"
                    aria-labelledby="contained-modal-title-sm"
                >
                    <Modal.Header closeButton>
                        <Modal.Title id="contained-modal-title-sm">Move to Trash</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <p>Are you sure wanna move to trash?</p>
                        <p>{`Mail id:${this.state.deleteMailID}`} </p>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button bsStyle='danger' onClick={this.deleteMail}>Yes</Button>
                        <Button onClick={this.closeDelete}>No</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        )
    }
};

const styles = {
    pageHeading: { backgroundColor: 'transparent', color: '#073642', padding: 7 },
    alertStyle: { display: 'flex', flex: 2, flexDirection: 'row', justifyContent: 'space-between', marginTop: 10 },
    tableHead: { backgroundColor: '#073642', color: 'white', fontWeight: 'medium', width: '100%', display: 'table' },
}

export default SentMail;
