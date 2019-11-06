import React, { Component } from 'react';
import { Container, Grid, TextField, makeStyles, Button, Avatar, Chip, FormControlLabel, Switch, Fab, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@material-ui/core';
import EnhancedTable from './EnhancedTable';
import { titleBlue } from '../../color';
import { getAllUsers, sendEmailNotification } from '../../APICalls/APICalls';
import { cookies } from "../../model/reducers";

const useStyles = makeStyles(theme => ({
    root: {
        background: 'linear-gradient(45deg, #FE6B8B 30%, #FF8E53 90%)',
        border: 0,
        borderRadius: 3,
        boxShadow: '0 3px 5px 2px rgba(255, 105, 135, .3)',
        color: 'white',
        height: 48,
        padding: '0 30px',
    },
    errorChip: {
        color: '#a8323a',
        backgroundColor: '#a8323a',
        borderColor: '#a8323a'
    },
    fab: {
        margin: theme.spacing(1),
    },
    extendedIcon: {
        marginRight: theme.spacing(1),
    },
}));


class NotificationsComponent extends Component {

    constructor(props) {
        super(props);
        this.state = {
            users: [],
            number: "No recipients selected",
            selectedList: [],
            subject: '',
            isValidSubject: true,
            isValidMessage: true,
            isValidRecipients: true,
            message: '',
            showErrorChip: false,
            showSuccessChip: false,
            errorMsg: '',
            successMsg: '',
            isHtml: false,
            open: false
        }
        this.externalWindow = null;
        this.refs = React.createRef();
    }

    handleSubjectChange = (event) => {
        if (event.target.value.length > 0) {
            this.setState({ isValidSubject: true })
        }
        this.setState({ subject: event.target.value });
    }

    handleIsHTMLChange = (event) => {
        this.setState({ isHtml: event.target.checked })
    }

    handleDialogBox = () => {
        this.externalWindow = window.open('', '', 'width=750,height=550,left=150,top=150');
        this.externalWindow.document.write(this.state.message);
    }

    handleDialogClose = () => {
        this.setState({ open: false });
    }

    clearChip = () => {
        this.setState({ showErrorChip: false, showSuccessChip: false });
    }

    handleMessageChange = (event) => {
        this.setState({ message: event.target.value });
    }

    getSelectedList = (list) => {
        console.log(list);
        let value, isValidRecipients;
        isValidRecipients = true;
        if (list.length > 0) {
            value = `Selected ${list.length} users.`;
        } else {
            value = "No recipients selected"
        }
        this.setState({ number: value, selectedList: list, isValidRecipients });
    }

    onSend = async () => {
        const isValidParams = this.validateInputs();
        if (isValidParams) {
            const result = await sendEmailNotification(cookies.get('email'), this.state.subject, this.state.message, this.state.selectedList, false)
            if (result.status === 200) {
                this.setState({ showSuccessChip: true, successMsg: "Mail sent successfully.", subject: '', message: '', selectedList: [], number: "No recipients selected" });
            } else {
                this.setState({ showErrorChip: true, errorMsg: result.response });
            }
            console.log(result);
        }
    }

    onClear = () => {
        //console.log("clear function");
        // console.log(this.refs.child.clearSelected());
        this.setState({
            selectedList: [],
            subject: '',
            isValidSubject: true,
            isValidMessage: true,
            isValidRecipients: true,
            message: '',
            number: "No recipients selected",
            showErrorChip: false,
            showSuccessChip: false,
            successMsg: '',
            errorMsg: ''
        })
    }

    validateInputs = () => {
        let isValid = true;
        if (!this.state.selectedList || this.state.selectedList.length < 1) {
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
            this.setState({ users });
        } else {
            console.log("empty user list");
        }
        console.log("Get user list");
    }

    render() {
        const { users } = this.state
        // const classes = useStyles();
        return (

            <div style={{ display: 'flex', flex: 1 }}>
                <Container maxWidth="lg">
                    <Grid
                        container
                        direction="row"
                        justify="space-between"
                        alignItems="center"
                    >
                        <div>
                            <h2 style={{ color: titleBlue }}>
                                Notifications
                            </h2>
                        </div>
                        {
                            this.state.showErrorChip ?
                                <div>
                                    <Chip
                                        variant="outlined"
                                        size="large"
                                        avatar={<Avatar>E</Avatar>}
                                        label={this.state.errorMsg}
                                        clickable
                                        color="secondary"
                                        onDelete={this.clearChip}
                                    />
                                </div>
                                :
                                (this.state.showSuccessChip ? <div><Chip
                                    variant="outlined"
                                    size="large"
                                    avatar={<Avatar>S</Avatar>}
                                    label={this.state.successMsg}
                                    clickable
                                    color="primary"
                                    onDelete={this.clearChip}
                                /></div> : <div></div>)
                        }

                    </Grid>
                    <Grid container spacing={3}>
                        <Grid item lg={6} xs={12} >
                            <EnhancedTable users={users} getSelectedList={this.getSelectedList} />
                        </Grid>
                        <Grid item lg={6} xs={12} >
                            <Grid
                                container
                                direction="row"
                                justify="flex-start"
                                alignItems="center"
                            >
                                <div>
                                    <h3 style={{ align: 'center', color: titleBlue }} >
                                        Compose Mail
                                </h3>
                                </div>
                            </Grid>
                            <TextField
                                error={!this.state.isValidRecipients}
                                id="standard-full-width"
                                label="To"
                                style={{ margin: 8 }}
                                placeholder="Select the recipients"
                                helperText={this.state.isValidRecipients ? "" : "Please select recipients."}
                                fullWidth
                                disabled
                                margin="normal"
                                value={this.state.number}
                            />
                            <TextField
                                error={!this.state.isValidSubject}
                                id="standard-full-width"
                                label="Subject"
                                style={{ margin: 8 }}
                                placeholder="Enter the subject"
                                helperText={this.state.isValidSubject ? "" : "Subject cannot be empty."}
                                fullWidth
                                margin="normal"
                                value={this.state.subject}
                                onChange={this.handleSubjectChange}
                            />
                            <Grid
                                container
                                direction="row"
                                justify="space-between"
                                alignItems="center"
                            >
                                <div style={{ marginLeft: 8 }}>
                                    <FormControlLabel
                                        label="Sending HTML mail?"
                                        control={
                                            <Switch
                                                checked={this.state.isHtml}
                                                onChange={this.handleIsHTMLChange}
                                                value="checkedB"
                                                color="primary"
                                            />
                                        }
                                    />
                                </div>
                                {this.state.isHtml ? <div>
                                    <Fab variant="extended" aria-label="like" color="primary" size={"small"} onClick={this.handleDialogBox} className={useStyles.fab}>
                                        preview
                                    </Fab>
                                    <Dialog
                                        open={this.state.open}
                                        onClose={this.handleDialogClose}
                                        scroll={"paper"}
                                        aria-labelledby="scroll-dialog-title"
                                        aria-describedby="scroll-dialog-description"
                                    >
                                        <DialogTitle id="scroll-dialog-title">Content Preview</DialogTitle>
                                        <DialogContent dividers={true}>
                                            <DialogContentText
                                                id="scroll-dialog-description"
                                                tabIndex={-1}
                                            >
                                                <span dangerouslySetInnerHTML={{ __html: this.state.message }} >
                                                </span>
                                            </DialogContentText>
                                        </DialogContent>
                                        <DialogActions>
                                            <Button onClick={this.handleDialogClose} color="primary">
                                                Close
                                            </Button>
                                        </DialogActions>
                                    </Dialog>
                                </div> : <div></div>}

                            </Grid>
                            <TextField
                                error={!this.state.isValidMessage}
                                id="standard-textarea"
                                label="Message"
                                placeholder="Enter the message"
                                helperText={this.state.isValidMessage ? "" : "Message cannot be empty."}
                                style={{ marginLeft: 8, marginTop: 0 }}
                                multiline
                                fullWidth
                                rows={20}
                                margin="normal"
                                value={this.state.message}
                                onChange={this.handleMessageChange}
                            />
                            <Grid
                                container
                                direction="row"
                                justify="flex-end"
                                align="flex-start">
                                <Button variant="outlined" color="default" onClick={this.onClear} >
                                    Clear
                                </Button>
                                <Button variant="contained" color="primary" onClick={this.onSend} >
                                    Send
                                </Button>
                            </Grid>
                        </Grid>
                    </Grid>
                </Container>
            </div>
        )
    }
}
export default NotificationsComponent;
