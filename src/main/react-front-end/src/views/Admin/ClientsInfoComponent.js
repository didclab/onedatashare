import React, { Component } from "react";
import {
  getUsers,
  updateAdminRightsApiCall,
  getAdmins,
} from "../../APICalls/APICalls";

import Paper from "@mui/material/Paper";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";

import Person from "@mui/icons-material/Person";
import People from "@mui/icons-material/People";
import Done from "@mui/icons-material/Done";
import Clear from "@mui/icons-material/Clear";
import Button from "@mui/material/Button";
import Dialog from "@mui/material/Dialog";
import DialogActions from "@mui/material/DialogActions";
import DialogContent from "@mui/material/DialogContent";
import DialogContentText from "@mui/material/DialogContentText";
import DialogTitle from "@mui/material/DialogTitle";
import { eventEmitter } from "../../App";
import { store } from "../../App.js";
import TablePagination from "@mui/material/TablePagination";
import TableFooter from "@mui/material/TableFooter";
import TablePaginationActions from "../TablePaginationActions";
import TableSortLabel from "@mui/material/TableSortLabel";
import { updateGAPageView } from "../../analytics/ga";

import { styled } from "@mui/system";

import "./ClientsInfoComponent.css";

const styles = (theme) => ({
  root: {
    width: "fit-content",
  },
  toolbar: {
    paddingLeft: "300px",
    paddingRight: "300px",
  },
  tablePaginationCaption: {
    fontSize: "15px",
  },
  tablePaginationSelect: {
    fontSize: "15px",
    lineHeight: "20px",
  },
});

export default class ClientsInfoComponent extends Component {
  constructor() {
    super();
    this.state = {
      users: [],
      totalUsersCount: 0,
      admins: [],
      totalAdminsCount: 0,
      userTblPage: 0,
      userTblRowsPerPage: 10,
      userTblRowsPerPageOptions: [10, 20, 50, 100],
      userTblOrder: "asc",
      userTblOrderBy: "email",
      adminTblPage: 0,
      adminTblRowsPerPage: 10,
      adminTblRowsPerPageOptions: [10, 20, 50, 100],
      adminTblOrder: "asc",
      adminTblOrderBy: "email",
    };
    this.getUserInfo = this.getUserInfo.bind(this);
    this.getAdminInfo = this.getAdminInfo.bind(this);
    this.getUserInfo();
    this.getAdminInfo();

    updateGAPageView();
  }

  componentDidMount() {
    document.title = "OneDataShare - Client Info";
  }

  getUserInfo = () =>
    getUsers(
      this.state.userTblPage,
      this.state.userTblRowsPerPage,
      this.state.userTblOrderBy,
      this.state.userTblOrder,
      (resp) => {
        //success
        this.setState({ users: resp.users, totalUsersCount: resp.totalCount });
      },
      (resp) => {
        //failed
        console.log("Error encountered in getUsers request to API layer");
      }
    );

  getAdminInfo = () =>
    getAdmins(
      this.state.adminTblPage,
      this.state.adminTblRowsPerPage,
      this.state.adminTblOrderBy,
      this.state.adminTblOrder,
      (resp) => {
        //success
        console.log(resp.users.length + "---");
        this.setState({
          admins: resp.users,
          totalAdminsCount: resp.totalCount,
        });
      },
      (resp) => {
        //failed
        console.log("Error encountered in getUsers request to API layer");
      }
    );

  // Shows the user a confirmation popup to confirm the update request.
  updateAdminRights(event, row) {
    var popupMsg = "";
    var isAdmin = event.target.checked;
    if (isAdmin) {
      popupMsg =
        "Please confirm if " +
        row.firstName +
        " " +
        row.lastName +
        " must be granted admin privileges.";
      this.setState({
        showIsAdminPopup: true,
        adminChangePopupMsg: popupMsg,
        targetUser: row.email,
        isAdmin: true,
        firstName: row.firstName,
        lastName: row.lastName,
      });
    } else {
      popupMsg =
        "Please confirm if admin privileges of " +
        row.firstName +
        " " +
        row.lastName +
        " must be revoked.";
      this.setState({
        showIsAdminPopup: true,
        adminChangePopupMsg: popupMsg,
        targetUser: row.email,
        isAdmin: false,
        firstName: row.firstName,
        lastName: row.lastName,
      });
    }
  }

  // The actual call to update the admin information is triggered after the user selects "Yes" in the confirmation popup
  // The user information is retrieved from the state
  updateAdminRightsUsingStateInfo(email, isAdmin) {
    updateAdminRightsApiCall(email, isAdmin).then((resp) => {
      if (resp) {
        eventEmitter.emit(
          "errorOccured",
          "Admin privileges is " +
            (this.state.isAdmin ? "granted for " : "revoked for ") +
            this.state.firstName +
            " " +
            this.state.lastName
        );
      } else {
        eventEmitter.emit("errorOccured", "Error while updating the user");
      }
      getUsers(
        this.state.userTblPage,
        this.state.userTblRowsPerPage,
        this.state.userTblOrderBy,
        this.state.userTblOrder,
        (resp) => {
          this.setState({
            users: resp.users,
            showIsAdminPopup: false,
            adminChangePopupMsg: "",
            targetUser: "",
            firstName: "",
            lastName: "",
          });
        },
        (error) => {
          console.log("Error encountered in getUsers request to API layer");
        }
      );
    });
  }
  handleClose = () => {
    this.setState({
      showIsAdminPopup: false,
      adminChangePopupMsg: "",
      targetUser: "",
      firstName: "",
      lastName: "",
    });
  };

  handleUserTblChangePage = (event, page) => {
    this.setState({ userTblPage: page }, this.getUserInfo);
  };

  handleUserTblChangeRowsPerPage = (event) => {
    this.setState(
      { userTblPage: 0, userTblRowsPerPage: parseInt(event.target.value) },
      this.getUserInfo
    );
  };

  handleUserTblRequestSort = (property) => {
    const orderBy = property;
    let order = "desc";
    if (
      this.state.userTblOrderBy === property &&
      this.state.userTblOrder === "desc"
    ) {
      order = "asc";
    }
    this.setState({ userTblOrder: order, userTblOrderBy: orderBy });
    this.getUserInfo();
  };

  handleAdminsTblChangePage = (event, page) => {
    this.setState({ adminTblPage: page });
    this.getAdminInfo();
  };

  handleAdminsTblChangeRowsPerPage = (event) => {
    this.setState({
      adminTblPage: 0,
      adminTblRowsPerPage: parseInt(event.target.value),
    });
    this.getAdminInfo();
  };

  handleAdminsTblRequestSort = (property) => {
    const orderBy = property;
    let order = "desc";
    if (
      this.state.adminTblOrderBy === property &&
      this.state.adminTblOrder === "desc"
    ) {
      order = "asc";
    }
    this.setState({ adminTblOrder: order, adminTblOrderBy: orderBy });
    this.getAdminInfo();
  };

  render() {
    return (
      <Paper className="clients-info-paper">
        <h2 className="clients-info-title">
          <People className="clients-info-icon" />
          Users
        </h2>
        <Table className="clients-info-table">
          <TableHead>
            <TableRow>
              <TableCell key="user-email">
                <TableSortLabel
                  active={this.state.userTblOrderBy === "email"}
                  direction={this.state.userTblOrder}
                  onClick={() => this.handleUserTblRequestSort("email")}
                >
                  Email
                </TableSortLabel>
              </TableCell>
              <TableCell key="user-firstName">
                <TableSortLabel
                  active={this.state.userTblOrderBy === "firstName"}
                  direction={this.state.userTblOrder}
                  onClick={() => this.handleUserTblRequestSort("firstName")}
                >
                  First Name
                </TableSortLabel>
              </TableCell>
              <TableCell key="user-lastName">
                <TableSortLabel
                  active={this.state.userTblOrderBy === "lastName"}
                  direction={this.state.userTblOrder}
                  onClick={() => this.handleUserTblRequestSort("lastName")}
                >
                  Last Name
                </TableSortLabel>
              </TableCell>
              <TableCell key="user-organization">
                <TableSortLabel
                  active={this.state.userTblOrderBy === "organization"}
                  direction={this.state.userTblOrder}
                  onClick={() => this.handleUserTblRequestSort("organization")}
                >
                  Organization
                </TableSortLabel>
              </TableCell>
              <TableCell key="user-dateSignedUp">
                <TableSortLabel
                  active={this.state.userTblOrderBy === "dateSignedUp"}
                  direction={this.state.userTblOrder}
                  onClick={() => this.handleUserTblRequestSort("dateSignedUp")}
                >
                  Signed Up
                </TableSortLabel>
              </TableCell>
              <TableCell key="user-validated">
                <TableSortLabel
                  active={this.state.userTblOrderBy === "validated"}
                  direction={this.state.userTblOrder}
                  onClick={() => this.handleUserTblRequestSort("validated")}
                >
                  Validated
                </TableSortLabel>
              </TableCell>
              <TableCell key="user-lastActivity">
                <TableSortLabel
                  active={this.state.userTblOrderBy === "lastActivity"}
                  direction={this.state.userTblOrder}
                  onClick={() => this.handleUserTblRequestSort("lastActivity")}
                >
                  Last Activity
                </TableSortLabel>
              </TableCell>
              <TableCell key="user-admin">
                <TableSortLabel
                  active={this.state.userTblOrderBy === "admin"}
                  direction={this.state.userTblOrder}
                  onClick={() => this.handleUserTblRequestSort("admin")}
                >
                  Admin
                </TableSortLabel>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {this.state.users.map((row) => (
              <TableRow key={row.email}>
                <TableCell>{row.email}</TableCell>
                <TableCell>{row.firstName}</TableCell>
                <TableCell>{row.lastName}</TableCell>
                <TableCell>{row.organization}</TableCell>
                <TableCell>
                  {new Date(row.dateSignedUp).toDateString()}
                </TableCell>
                <TableCell>{row.validated ? <Done /> : <Clear />}</TableCell>
                <TableCell>
                  {new Date(row.lastActivity).toDateString()}
                </TableCell>
                <TableCell>
                  <input
                    type="checkbox"
                    checked={row.admin}
                    onChange={(event) => this.updateAdminRights(event, row)}
                  />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
          <TableFooter>
            <TableRow>
              <TablePagination
                rowsPerPageOptions={this.state.userTblRowsPerPageOptions}
                colSpan={8}
                count={this.state.totalUsersCount}
                rowsPerPage={this.state.userTblRowsPerPage}
                page={this.state.userTblPage}
                onPageChange={this.handleUserTblChangePage}
                onRowsPerPageChange={this.handleUserTblChangeRowsPerPage}
                ActionsComponent={TablePaginationActions}
                classes={{
                  select: styles.tablePaginationSelect,
                  selectIcon: styles.tablePaginationSelect,
                  actions: styles.tablePaginationActions,
                  caption: styles.tablePaginationCaption,
                }}
              />
            </TableRow>
          </TableFooter>
        </Table>
        <h2 className="clients-info-title">
          <Person className="clients-info-icon" />
          Admins
        </h2>
        <Table className="clients-info-table">
          <TableHead>
            <TableRow>
              <TableCell key="admin-email">
                <TableSortLabel
                  active={this.state.adminTblOrderBy === "email"}
                  direction={this.state.adminTblOrder}
                  onClick={() => this.handleAdminsTblRequestSort("email")}
                >
                  Email
                </TableSortLabel>
              </TableCell>
              <TableCell key="admin-firstName">
                <TableSortLabel
                  active={this.state.adminTblOrderBy === "firstName"}
                  direction={this.state.adminTblOrder}
                  onClick={() => this.handleAdminsTblRequestSort("firstName")}
                >
                  First Name
                </TableSortLabel>
              </TableCell>
              <TableCell key="admin-lastName">
                <TableSortLabel
                  active={this.state.adminTblOrderBy === "lastName"}
                  direction={this.state.adminTblOrder}
                  onClick={() => this.handleAdminsTblRequestSort("lastName")}
                >
                  Last Name
                </TableSortLabel>
              </TableCell>
              <TableCell key="admin-organization">
                <TableSortLabel
                  active={this.state.adminTblOrderBy === "organization"}
                  direction={this.state.adminTblOrder}
                  onClick={() =>
                    this.handleAdminsTblRequestSort("organization")
                  }
                >
                  Organization
                </TableSortLabel>
              </TableCell>
              <TableCell key="admin-dateSignedUp">
                <TableSortLabel
                  active={this.state.adminTblOrderBy === "dateSignedUp"}
                  direction={this.state.adminTblOrder}
                  onClick={() =>
                    this.handleAdminsTblRequestSort("dateSignedUp")
                  }
                >
                  Signed Up
                </TableSortLabel>
              </TableCell>
              <TableCell key="admin-validated">
                <TableSortLabel
                  active={this.state.adminTblOrderBy === "validated"}
                  direction={this.state.adminTblOrder}
                  onClick={() => this.handleAdminsTblRequestSort("validated")}
                >
                  Validated
                </TableSortLabel>
              </TableCell>
              <TableCell key="admin-lastActivity">
                <TableSortLabel
                  active={this.state.adminTblOrderBy === "lastActivity"}
                  direction={this.state.adminTblOrder}
                  onClick={() =>
                    this.handleAdminsTblRequestSort("lastActivity")
                  }
                >
                  Last Activity
                </TableSortLabel>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {this.state.admins.map((row) => (
              <TableRow key={row.email}>
                <TableCell>{row.email}</TableCell>
                <TableCell>{row.firstName}</TableCell>
                <TableCell>{row.lastName}</TableCell>
                <TableCell>{row.organization}</TableCell>
                <TableCell>
                  {new Date(row.dateSignedUp).toDateString()}
                </TableCell>
                <TableCell>{row.validated ? <Done /> : <Clear />}</TableCell>
                <TableCell>
                  {new Date(row.lastActivity).toDateString()}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
          <TableFooter>
            <TableRow>
              <TablePagination
                rowsPerPageOptions={this.state.adminTblRowsPerPageOptions}
                colSpan={7}
                count={this.state.totalAdminsCount}
                rowsPerPage={this.state.adminTblRowsPerPage}
                page={this.state.adminTblPage}
                onPageChange={this.handleAdminsTblChangePage}
                onRowsPerPageChange={this.handleAdminsTblChangeRowsPerPage}
                ActionsComponent={TablePaginationActions}
                classes={{
                  select: styles.tablePaginationSelect,
                  selectIcon: styles.tablePaginationSelect,
                  actions: styles.tablePaginationActions,
                  caption: styles.tablePaginationCaption,
                }}
              />
            </TableRow>
          </TableFooter>
        </Table>
        <Dialog
          open={this.state.showIsAdminPopup}
          onClose={this.handleClose}
          aria-labelledby="alert-dialog-title"
          aria-describedby="alert-dialog-description"
        >
          <DialogTitle id="alert-dialog-title">
            {"Confirm Admin Privileges Update"}
          </DialogTitle>
          <DialogContent>
            <DialogContentText id="alert-dialog-description">
              {this.state.adminChangePopupMsg}
            </DialogContentText>
          </DialogContent>
          <DialogActions>
            <Button onClick={this.handleClose} color="primary">
              No
            </Button>
            <Button
              onClick={() =>
                this.confirmUpdateAdminRights(this.state.isAdminRow)
              }
              color="primary"
              autoFocus
            >
              Yes
            </Button>
          </DialogActions>
        </Dialog>
      </Paper>
    );
  }
}

// export default AdminInfo;
