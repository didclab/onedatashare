import React from 'react';
import { useLocation } from 'react-router-dom';
import TransferComponent from './TransferComponent';

const TransferComponentWrapper = props => {
    const location = useLocation();
    return <TransferComponent location={location} {...props} />
}

export default TransferComponentWrapper;