import React, { useState } from 'react';
import {Box, Menu, MenuItem, Accordion, AccordionDetails, AccordionSummary, Typography, List, ListItem, Collapse} from "@material-ui/core";
import { Link } from 'react-router-dom';
import { userListPageUrl, historyPageUrl, newNotifications } from '../constants';

const AdminDropdown = (props) => {
    const [menuAnchor, setAnchor] = useState(null);
    const [open, setOpen] = useState(false);
    const openMenu = (event) =>{
        console.log(event.currentTarget);
        setAnchor(event.currentTarget);
    }
    const closeMenu = () =>{
        setAnchor(null);
    }

    const mobileMenu = () => {
        setOpen(!open);
    }

    if(props.mobile){
        return (
            <div>
                <ListItem onClick={mobileMenu} >
                    <p className={"navbarButton"}>Admin</p>
                </ListItem>
                <Collapse in={open}>
                    <List style={{backgroundColor: "#505c6b"}}>
                        <ListItem>
                            <Link to={userListPageUrl} id="NavAdminClients" href={userListPageUrl} className={"navbarButton"}>
                                User Information
                            </Link>
                        </ListItem>
                        <ListItem>
                            <Link to={historyPageUrl} id="NavAdminHistory" href={historyPageUrl} className={"navbarButton"}>
                                Transfer History
                            </Link>
                        </ListItem>
                        <ListItem>
                            <Link to={newNotifications} id="NavAdminSendNotifications" href={newNotifications} className={"navbarButton"}>
                                Send Notifications
                            </Link>
                        </ListItem>
                    </List>
                </Collapse>
            </div>
        );
    }
    return(
        <Box marginLeft="5%">
            <p className={"navbarButton"}
               onClick={(event) => openMenu(event)}
            >Admin
            </p>
            <Menu
                open={Boolean(menuAnchor)}
                anchorEl={menuAnchor}
                onClose={closeMenu}
                autoFocus={false}
                variant={"menu"}
                disableScrollLock
                getContentAnchorEl={null}
                anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
                transformOrigin={{ vertical: "top", horizontal: "center" }}
                style={{zIndex: 1401}}
            >
                <MenuItem id="NavAdminClients" component={Link} to={userListPageUrl} href={userListPageUrl}>User Information</MenuItem>
                <MenuItem id="NavAdminHistory" component={Link} to={historyPageUrl} href={historyPageUrl}>Transfer History</MenuItem>
                <MenuItem id="NavAdminSendNotifications" component={Link} to={newNotifications} href={newNotifications}>Send Notifications</MenuItem>
            </Menu>
        </Box>
    );
};

export default AdminDropdown;