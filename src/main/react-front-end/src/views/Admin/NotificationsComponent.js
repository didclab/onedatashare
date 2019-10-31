import React, { Component } from 'react';
import { Container, Grid, Paper, TextField, makeStyles, Button, Icon } from '@material-ui/core';
import EnhancedTable from './EnhancedTable';
import { titleBlue } from '../../color';
import { purple } from '@material-ui/core/colors';
import { getAllUsers } from '../../APICalls/APICalls';

const useStyles = makeStyles(theme => ({
    container: {
        display: 'flex',
        flexWrap: 'wrap',
    },
    textField: {
        marginLeft: theme.spacing(1),
        marginRight: theme.spacing(1),
        width: 200,
    },
    dense: {
        marginTop: 19,
    },
    menu: {
        width: 200,
    },
}));

class NotificationsComponent extends Component {

    constructor(props) {
        super(props);
        this.state = {
            users: []
        }
    }

    async componentDidMount() {
        const users = await getAllUsers("dhayanid@buffalo.edu")
        if (users && users.length > 0) {
            this.setState({ users });
        } else {
            console.log("empty user list");
        }
        console.log("Get user list");
    }

    render() {
        // const classes = useStyles();
        const { users } = this.state
        return (
            <div style={{ display: 'flex', flex: 1 }}>
                <Container maxWidth="lg">
                    <Grid
                        container
                        direction="row"
                        justify="flex-start"
                        alignItems="center"
                    >
                        <div>
                            <h2 style={{ color: titleBlue }}>
                                Notifications
                            </h2>
                        </div>
                    </Grid>
                    <Grid container spacing={3}>
                        <Grid item lg={6} xs={12} >
                            <EnhancedTable users={users} />
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
                                id="standard-full-width"
                                label="Subject"
                                style={{ margin: 8 }}
                                placeholder="Enter the subject"
                                helperText=""
                                fullWidth
                                margin="normal"
                            />
                            <TextField
                                id="standard-textarea"
                                label="Message"
                                placeholder="Enter the message"
                                style={{ margin: 8 }}
                                multiline
                                fullWidth
                                rows={20}
                                margin="normal"
                            />
                            <div>
                                <input type="file"></input>
                            </div>
                            <Grid
                                container
                                direction="row"
                                justify="flex-end"
                                align="flex-end">
                                <Button variant="outlined" color="default" >
                                    Clear
                                </Button>
                                <Button variant="contained" color="primary" >
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