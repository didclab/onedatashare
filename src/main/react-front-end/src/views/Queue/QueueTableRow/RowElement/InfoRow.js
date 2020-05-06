import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import AppBar from "@material-ui/core/AppBar";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import TabContent from "../TabContent";
import React, {useState} from "react";

const InfoRow = ({
                     resp,
                     span,
                 })  => {
    const [selectedTab, setSelectedTab] = useState(false);
    return (
        <TableRow className={"QueueRow"}>
            <TableCell className={"infoCell"} colSpan={span} style={{fontSize: '1rem', backgroundColor: '#e8e8e8'}}>
                <div className="infoBox" style={{marginBottom: '0.5%'}}>
                    <AppBar position="static" style={{boxShadow: 'unset'}}>
                        <Tabs value={selectedTab ? 1 : 0} onChange={() => setSelectedTab(!selectedTab)}
                              style={{backgroundColor: '#e8e8e8'}}>
                            <Tab style={{backgroundColor: '#428bca', margin: '0.5%', borderRadius: '4px'}}
                                 label="Formatted"/>
                            <Tab style={{backgroundColor: '#428bca', margin: '0.5%', borderRadius: '4px'}}
                                 label="JSON"/>
                        </Tabs>
                    </AppBar>
                    <div className={"detailedInfo"}>
                        <TabContent resp={resp} selectedTab={selectedTab}/>
                    </div>
                </div>
            </TableCell>
        </TableRow>
    );
};

export default InfoRow;