import React from "react";
import { TableContainer } from "@mui/material";
import { styled } from "@mui/system";
import Table from "@mui/material/Table";
import TableBody from "@mui/material/TableBody";
import TableCell from "@mui/material/TableCell";
import TableHead from "@mui/material/TableHead";
import TableRow from "@mui/material/TableRow";
import Input from "@mui/material/Input";
import Paper from "@mui/material/Paper";
import IconButton from "@mui/material/IconButton";
// Icons
import EditIcon from "@mui/icons-material/EditOutlined";
import DeleteOutlinedIcon from "@mui/icons-material/DeleteOutlined";
import DoneIcon from "@mui/icons-material/DoneAllTwoTone";
import HistoryIcon from "@mui/icons-material/History";
import AddCircleOutlineOutlinedIcon from "@mui/icons-material/AddCircleOutlineOutlined";
import SaveAltIcon from "@mui/icons-material/SaveAlt";
import Logo from "../Support/logo-blue.png";

// Define styled components
const Root = styled(TableContainer)(({ theme }) => ({
  width: "80%",
  marginTop: theme.spacing(3),
  overflowX: "auto",
  marginLeft: "auto",
  marginRight: "auto",
}));

const StyledTable = styled(Table)({
  minWidth: 650,
});

const SelectTableCell = styled(TableCell)({
  width: 60,
});

const TableCellStyled = styled(TableCell)({
  width: 130,
  height: 40,
});

const InputStyled = styled(Input)({
  width: 130,
  height: 40,
});

const HeaderCell = styled(TableCell)({
  fontWeight: "bold",
  fontSize: "medium",
});

const HideText = styled("div")({
  textSecurity: "disc",
  cursor: "pointer",
  "&:hover": {
    textSecurity: "none",
    cursor: "pointer",
  },
});

// This function takes in the data and convert to an object.
const createData = (
  key,
  userId,
  type,
  accountId,
  endpointCredential,
  token
) => ({
  id: key,
  userId,
  type,
  accountId,
  endpointCredential,
  token,
  isEditMode: false,
});

// Handles input type and normal text display.
const CustomTableCell = ({ row, name, onChange }) => {
  const { isEditMode } = row;
  return (
    <TableCellStyled align="left" style={{ fontSize: "small" }}>
      {isEditMode ? (
        <InputStyled
          value={row[name]}
          name={name}
          onChange={(e) => onChange(e, row)}
          style={{ fontSize: "small" }}
        />
      ) : (
        <HideText className={name === "token" ? "hidetext" : null}>
          {row[name]}
        </HideText>
      )}
    </TableCellStyled>
  );
};

export default function Endpoint_DB() {
  // The data imported
  var data = [
    ["UserInfo#1", 159, 6.0, 24, "y9reh4389y34"],
    ["UserInfo#2", 237, 9.0, 37, "hih3298ruiwe"],
    ["UserInfo#3", 262, 16.0, 24, "hr43fifkj"],
    ["UserInfo#4", 159, 6.0, 24, "ugyr78wgefybfbwe"],
    ["UserInfo#5", 237, 9.0, 37, "hr4hweiufke"],
    ["UserInfo#6", 262, 16.0, 24, "ANSDKDW"],
  ];

  // st stores the objects of data as a list.
  var st = [];

  // k keep tracks of index
  var k = 0;
  for (let e of data) {
    st = st.concat(createData(k, e[0], e[1], e[2], e[3], e[4]));
    k += 1;
  }

  // Create states for current data and previous data for undo operations
  const [rows, setRows] = React.useState(st);
  const [previous, setPrevious] = React.useState(st);

  const onToggleEditMode = (id) => {
    // Save in the database from here.

    // This saves the current rows
    setRows((state) => {
      return rows.map((row) => {
        if (row.id === id) {
          return { ...row, isEditMode: !row.isEditMode };
        }
        return row;
      });
    });
  };

  // Changes from inputs
  const onChange = (e, row) => {
    if (!previous[row.id]) {
      setPrevious(rows);
    }
    const value = e.target.value;
    const name = e.target.name;
    const { id } = row;
    const newRows = rows.map((row) => {
      if (row.id === id) {
        return { ...row, [name]: value };
      }
      return row;
    });
    setRows(newRows);
  };

  // Function called when item deleted.
  const deleteItem = (id) => {
    var newRow = [];
    setPrevious((state) => {
      for (let e of rows) {
        if (e.id === id) {
          newRow = rows.filter((item) => item !== e);
        }
      }
    });
    setPrevious(newRow);
    setRows(newRow);
  };

  // Undo operations.
  const onRevert = (id) => {
    var newRows = [];
    var i = 0;
    for (let e of rows) {
      if (e.id === id) {
        newRows = newRows.concat(previous[i]);
      } else {
        newRows = newRows.concat(e);
      }
      i += 1;
    }
    setRows(newRows);
  };

  // Function called when row is added.
  function add_row() {
    var new_id = 0;
    if (rows.length !== 0) {
      for (let e of rows) {
        if (e.id > new_id) {
          new_id = e.id;
        }
      }
      new_id += 1;
    }
    var newR = rows.concat(createData(new_id, "", "", "", "", ""));
    // Save in the database from here
    setRows((state) => {
      return newR.map((row) => {
        if (row.id === new_id) {
          return { ...row, isEditMode: !row.isEditMode };
        }
        return row;
      });
    });
    setPrevious(newR);
  }

  function download_csv() {
    let csv = "";
    let header = Object.keys(rows[0]).join(",");
    let values = rows.map((o) => Object.values(o).join(",")).join("\n");
    csv += header + "\n" + values;
    saveFile(csv);
  }

  //Export as csv file
  let saveFile = (data) => {
    // Convert the text to BLOB.
    const textToBLOB = new Blob([data], { type: "text/plain" });
    const sFileName = "Credentials.csv"; // The file to save the data.

    let newLink = document.createElement("a");
    newLink.download = sFileName;

    if (window.webkitURL != null) {
      newLink.href = window.webkitURL.createObjectURL(textToBLOB);
    } else {
      newLink.href = window.URL.createObjectURL(textToBLOB);
      newLink.style.display = "none";
      document.body.appendChild(newLink);
    }

    newLink.click();
  };

  return (
    <Root component={Paper}>
      <center>
        <img src={Logo} style={{ width: 100, margin: 5 }} alt="Logo" />{" "}
        <h4 style={{ fontWeight: "bold", color: "#172753" }}>
          Endpoint Credentials
        </h4>
      </center>
      <TableCell style={{ borderBottom: "0px" }}>
        <IconButton
          aria-label="add_row"
          onClick={() => {
            add_row();
          }}
        >
          <AddCircleOutlineOutlinedIcon style={{ fontSize: "large" }} />
        </IconButton>
      </TableCell>

      <TableCell style={{ borderBottom: "0px" }}>
        <IconButton
          aria-label="add_row"
          onClick={() => {
            download_csv();
          }}
        >
          <SaveAltIcon style={{ fontSize: "large" }} />
        </IconButton>
      </TableCell>

      <StyledTable aria-label="caption table">
        <TableHead>
          <TableRow>
            <TableCell align="left" />
            <HeaderCell align="left">userId</HeaderCell>
            <HeaderCell align="left">type</HeaderCell>
            <HeaderCell align="left">accountId</HeaderCell>
            <HeaderCell align="left">endpointCredential</HeaderCell>
            <HeaderCell align="left">token</HeaderCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.id}>
              <SelectTableCell>
                {row.isEditMode ? (
                  <>
                    <IconButton
                      aria-label="done"
                      onClick={() => onToggleEditMode(row.id)}
                    >
                      <DoneIcon />
                    </IconButton>
                    <IconButton
                      aria-label="revert"
                      onClick={() => onRevert(row.id)}
                    >
                      <HistoryIcon />
                    </IconButton>
                  </>
                ) : (
                  <>
                    <IconButton
                      aria-label="delete"
                      onClick={() => onToggleEditMode(row.id)}
                    >
                      <EditIcon />
                    </IconButton>

                    <IconButton
                      aria-label="del_row"
                      onClick={() => deleteItem(row.id)}
                    >
                      <DeleteOutlinedIcon />
                    </IconButton>
                  </>
                )}
              </SelectTableCell>
              <CustomTableCell {...{ row, name: "userId", onChange }} />
              <CustomTableCell {...{ row, name: "type", onChange }} />
              <CustomTableCell {...{ row, name: "accountId", onChange }} />
              <CustomTableCell
                {...{ row, name: "endpointCredential", onChange }}
              />
              <CustomTableCell {...{ row, name: "token", onChange }} />
            </TableRow>
          ))}
        </TableBody>
      </StyledTable>
    </Root>
  );
}
