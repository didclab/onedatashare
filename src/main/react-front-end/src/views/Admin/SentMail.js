import React, { Component } from 'react';
import { Label, Glyphicon, Table, Button, Modal, ButtonToolbar, Alert, OverlayTrigger, Tooltip, Form, FormControl, FormGroup, ControlLabel, HelpBlock, Checkbox } from 'react-bootstrap';
import './SentMail.css';
import { getAllMails, sendEmailNotification, deleteMail } from '../../APICalls/APICalls';
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
            showDelete: false
        }
    }
    closeDelete = () => {
        this.setState({ showDelete: false });
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
    }

    viewMail = (mail) => {
        this.setState({ viewMail: true, currentView: mail });
    }

    deleteMail = async () => {
        const result = await deleteMail(this.state.deleteMailID);
        if (result.status === 200) {
            this.setState({ showSuccessChip: true, successMsg: "Mail Deleted Successfully." });
            await this.getSentMails();
        } else {
            this.setState({ showErrorChip: true, errorMsg: result.response });
        }
        console.log(result);
    }

    onSubjectChange = event => {
        this.setState({ currentView: { ...this.state.currentView, subject: event.target.value } });
    }

    onMessageChange = event => {
        this.setState({ currentView: { ...this.state.currentView, message: event.target.value } });
    }

    closeMailBox = () => {
        this.setState({ viewMail: false });
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
        const { mails, showSuccessChip, showErrorChip, errorMsg, successMsg, isValidMessage, isValidRecipients, isValidSubject } = this.state;
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
                {this.state.currentView ? <Modal
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
                            <FormGroup controlId={'to'} validationState={isValidRecipients ? 'none' : 'error'}>
                                <ControlLabel>To</ControlLabel>
                                <OverlayTrigger placement="right" overlay={tooltip}>
                                    <FormControl.Static style={{ width: '15%' }}>
                                        {this.state.currentView && this.state.currentView.recipients && this.state.currentView.recipients.length > 0 ? <a href={null} onClick={this.smOpen}>{`${this.state.currentView.recipients.length} users selected`}</a> : <a href={null}>No recipients </a>}
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
                            <FormGroup controlId="message" validationState={isValidMessage ? 'none' : 'error'} >
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
    alertStyle: { display: 'flex', flex: 2, flexDirection: 'row', justifyContent: 'space-between', marginTop: 10 }
}

export default SentMail;
