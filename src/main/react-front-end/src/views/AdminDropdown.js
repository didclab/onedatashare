import React, { useState } from 'react';
import {Box, Menu, MenuItem, List, ListItem, Collapse} from "@material-ui/core";
import { Link } from 'react-router-dom';
import { siteURLS} from "../constants";

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
                            <Link to={siteURLS.userListPageUrl} id="NavAdminClients" href={siteURLS.userListPageUrl} className={"navbarButton"}>
                                User Information
                            </Link>
                        </ListItem>
                        <ListItem>
                            <Link to={siteURLS.historyPageUrl} id="NavAdminHistory" href={siteURLS.historyPageUrl} className={"navbarButton"}>
                                Transfer History
                            </Link>
                        </ListItem>
                        <ListItem>
                            <Link to={siteURLS.newNotificationsUrl} id="NavAdminSendNotifications" href={siteURLS.newNotificationsUrl} className={"navbarButton"}>
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
                <MenuItem id="NavAdminClients" component={Link} to={siteURLS.userListPageUrl} href={siteURLS.userListPageUrl}>User Information</MenuItem>
                <MenuItem id="NavAdminHistory" component={Link} to={siteURLS.historyPageUrl} href={siteURLS.historyPageUrl}>Transfer History</MenuItem>
                <MenuItem id="NavAdminSendNotifications" component={Link} to={siteURLS.newNotificationsUrl} href={siteURLS.newNotificationsUrl}>Send Notifications</MenuItem>
            </Menu>
        </Box>
    );
};

export default AdminDropdown;