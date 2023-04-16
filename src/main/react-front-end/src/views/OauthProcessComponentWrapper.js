import React from 'react';
import { useLocation, useParams } from 'react-router-dom';
import OauthProcessComponent from './OauthProcessComponent';

const OauthProcessComponentWrapper = props => {
    const location = useLocation();
    const matchedParams = useParams();
    return <OauthProcessComponent location={location} matchedParams={matchedParams} {...props} />
}

export default OauthProcessComponentWrapper;