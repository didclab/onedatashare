import React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import { withStyles } from '@material-ui/core/styles';
import TableCell from '@material-ui/core/TableCell';
import Paper from '@material-ui/core/Paper';
import { AutoSizer, Column, Table } from 'react-virtualized';
import {TableContainer} from "@material-ui/core";
import Logo from "../Support/logo-blue.png";
import TextField from '@material-ui/core/TextField';


const styles = (theme) => ({
  flexContainer: {
    display: 'flex',
    boxSizing: 'border-box',
  },
  hidetext: {
    textSecurity: 'disc',
    cursor: 'pointer',
    '&:hover': {
      textSecurity: 'none',
      cursor: 'pointer',
    },
  },
  table: {
    // temporary right-to-left patch, waiting for
    // https://github.com/bvaughn/react-virtualized/issues/454
    '& .ReactVirtualized__Table__headerRow': {
      flip: false,
      paddingRight: theme.direction === 'rtl' ? '0 !important' : undefined,
    },
  },
  tableRow: {
    cursor: 'pointer',
  },
  tableRowHover: {
    '&:hover': {
      backgroundColor: theme.palette.grey[200],
    },
  },
  tableCell: {
    flex: 1,
  },
  noClick: {
    cursor: 'initial',
  },
});

class Endpoint_DB extends React.PureComponent {
  static defaultProps = {
    headerHeight: 48,
    rowHeight: 48,
  };

  getRowClassName = ({ index }) => {
    const { classes, onRowClick } = this.props;

    return clsx(classes.tableRow, classes.flexContainer, {
      [classes.tableRowHover]: index !== -1 && onRowClick != null,
    });
  };

  cellRenderer = ({ cellData, columnIndex }) => {
    const { columns, classes, rowHeight, onRowClick } = this.props;
    return (
      <TableCell
        component="div"
        className={clsx(classes.tableCell, classes.flexContainer, {
          [classes.noClick]: onRowClick == null,
        })}
        variant="body"
        style={{ height: rowHeight, fontSize:'small' }}
        align={(columnIndex != null && columns[columnIndex].numeric) || false ? 'right' : 'left'}
      >
        {cellData}
      </TableCell>
    );
  };

  headerRenderer = ({ label, columnIndex }) => {
    const { headerHeight, columns, classes } = this.props;

    return (
      <TableCell
        component="div"
        className={clsx(classes.tableCell, classes.flexContainer, classes.noClick)}
        variant="head"
        style={{ height: headerHeight }}
        align={columns[columnIndex].numeric || false ? 'right' : 'left'}
      >
        <span style={{fontWeight:"bold", fontSize:"medium"}}>{label}</span>
      </TableCell>
    );
  };

  render() {
    const { classes, columns, rowHeight, headerHeight, ...tableProps } = this.props;
    return (
      <AutoSizer>
        {({ height, width }) => (
          <Table
            height={height}
            width={width}
            rowHeight={rowHeight}
            gridStyle={{
              direction: 'inherit',
            }}
            headerHeight={headerHeight}
            className={classes.table}
            {...tableProps}
            rowClassName={this.getRowClassName}
          >
            {columns.map(({ dataKey, ...other }, index) => {
              return (
                <Column
                  key={dataKey}
                  headerRenderer={(headerProps) =>
                    this.headerRenderer({
                      ...headerProps,
                      columnIndex: index,
                    })
                  }
                  {...console.log(dataKey == 'password')}
                  className={dataKey == 'password' ? classes.hidetext : classes.flexContainer}
                  cellRenderer={this.cellRenderer}
                  dataKey={dataKey}
                  {...other}
                />
              );
            })}
          </Table>
        )}
      </AutoSizer>
    );
  }
}

Endpoint_DB.propTypes = {
  classes: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(
    PropTypes.shape({
      dataKey: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
      numeric: PropTypes.bool,
      width: PropTypes.number.isRequired,
    }),
  ).isRequired,
  headerHeight: PropTypes.number,
  onRowClick: PropTypes.func,
  rowHeight: PropTypes.number,
};

const VirtualizedTable = withStyles(styles)(Endpoint_DB);

// ---

const sample = [
  ['User#1', 159, 6.0, "EP1", "sample"],
  ['User#2', 237, 9.0, "EP2", "sample"],
  ['User#3', 262, 16.0, "EP3", "sample"],
  ['User#1', 159, 6.0, "EP4", "sample"],
];

function createData(userId, type, accountId, endpointCredential, password) {
  return { userId, type, accountId, endpointCredential, password };
}

const rows = [];

for (let e of sample) {
  rows.push(createData(e[0], e[1], e[2], e[3], e[4]));
}

console.log(rows)

export default function ReactVirtualizedTable() {
  var cols = [
    {
      width: 200,
      label: 'userId',
      dataKey: 'userId',
    },
    {
      width: 200,
      label: 'type',
      dataKey: 'type',
    },
    {
      width: 200,
      label: 'accountId',
      dataKey: 'accountId',
    },
    {
      width: 200,
      label: 'endpointCredential',
      dataKey: 'endpointCredential',
    },
    {
      width: 200,
      label: 'Password',
      dataKey: 'password',
    },
  ]
  var currentWidth = 0
  for(let e of cols){
    currentWidth += e.width
  }
  return (
    <TableContainer component={Paper} style={{ height: '85%', width: currentWidth, marginLeft: "auto", marginRight:"auto", overflow:"hidden" }}>
      <center><img src={Logo} style={{width: "12%", margin:3}}/> <h4 style={{fontWeight:"bold", color: "#172753"}}>Endpoint Credentials</h4></center>
      <VirtualizedTable
        rowCount={rows.length}
        rowGetter={({ index }) => rows[index]}
        columns={cols}
      />
    </TableContainer>
  );
}