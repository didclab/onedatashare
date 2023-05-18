import React from 'react';
import { useLocation } from 'react-router-dom';
import AccountControlComponent from './AccountControlComponent';

const AccountControlComponentWrapper = props => {
    const location = useLocation();
    return <AccountControlComponent location={location} {...props} />
}

export default AccountControlComponentWrapper;