/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */



/*accountInformation
[credentials]
credentials:{key: validation}
*/

/*
queue
[{endpoint1: string, endpoint2: string, speed: number}]
*/

import { LOGIN, LOGOUT, PROMOTE, ENDPOINT_PROGRESS, ENDPOINT_UPDATE, ACCOUNT_PREFERENCE_TOGGLED, COMPACT_VIEW_PREFERENCE } from './actions';
import { transferOptimizations } from "./actions";
import { DROPBOX_NAME, GOOGLEDRIVE_NAME } from '../constants';
import Cookies from 'universal-cookie';
export const cookies = new Cookies();

export const beforeLogin = 0;
export const duringLogin = 1;
export const afterLogin = 2;

const initialState = {
	login: cookies.get('email') ? true : false,
	admin: cookies.get('admin') ? true : false,
	email: cookies.get('email'),
	compactViewEnabled: cookies.get('compactViewEnabled') === 'true' || false,
  saveOAuthTokens: cookies.get('saveOAuthTokens') === 'true' || false,
	endpoint1: cookies.get('endpoint1') ? cookies.get('endpoint1') : {
		login: false,
		credential: {},
		uri: "",
    side: "left"
	},

	endpoint2: cookies.get('endpoint2') ? cookies.get('endpoint2') : {
    login: false,
		credential: {},
		uri: "",
    side: "right",

	},

	queue: [],
	transferOptions : {
		useTransferOptimization : transferOptimizations.None,
		overwriteExistingFiles : true,
		verifyFileInterity : false,
		encryptDataChannel : false,
		compressDataChannel : false
	}
}



export function onedatashareModel(state = initialState, action) {
  // For now, don't handle any actions
  // and just return the state given to us.
  switch (action.type) {
    case LOGIN:
   		const {email, token, saveOAuthTokens, compactViewEnabled, admin, expiresIn} = action.credential;
      console.debug(`logging in  ${email}. Access token is valid for ${expiresIn} seconds`);
      cookies.set('email', email, { maxAge : expiresIn });
      cookies.set('saveOAuthTokens', saveOAuthTokens, { maxAge : expiresIn });
      cookies.set('compactViewEnabled', compactViewEnabled, { maxAge : expiresIn });
      //Only set the admin cookie if admin
      if(admin){
        cookies.set('admin', admin, { maxAge : expiresIn });
      }
      
    	return Object.assign({}, state, {
    		login: true,
    		email: email,
        token: token,
        admin: admin,
        saveOAuthTokens: saveOAuthTokens,
				compactViewEnabled: compactViewEnabled
      });

    case LOGOUT:
      console.debug("logging out");
      cookies.remove('email');
      cookies.remove('admin');
      cookies.remove('endpoint1');
      cookies.remove('endpoint2');
      cookies.remove('saveOAuthTokens');
      cookies.remove(DROPBOX_NAME);
      cookies.remove(GOOGLEDRIVE_NAME);
			cookies.remove('compactViewEnabled');
      window.location.replace('/');

      return Object.assign({}, state, {
        login: false,
        admin: false,
        hash: "",
        email: "noemail",
        saveOAuthTokens: undefined
      });

    case PROMOTE:
      return Object.assign({}, state, {
        admin: true,
      });

    case ENDPOINT_PROGRESS:
      if(action.side === "left")
        return Object.assign({}, state, {
          endpoint1: {...state.endpoint1, loginProgress: action.progress},
        });
      else
        return Object.assign({}, state, {
          endpoint2: {...state.endpoint2, loginProgress: action.progress},
        });

    case ENDPOINT_UPDATE:
      if(action.side === "left"){
        cookies.set('endpoint1', {...state.endpoint1, ...action.endpoint});
          return Object.assign({}, state, {
            endpoint1: {...state.endpoint1, ...action.endpoint},
          });
        }
      else{
        cookies.set('endpoint2', {...state.endpoint2, ...action.endpoint});
        return Object.assign({}, state, {
          endpoint2: {...state.endpoint2, ...action.endpoint},
        });
      }

    case COMPACT_VIEW_PREFERENCE:
      cookies.set('compactViewEnabled', action.compactViewEnabled );
      return Object.assign({}, state, {
                compactViewEnabled: action.compactViewEnabled
              });

    case ACCOUNT_PREFERENCE_TOGGLED:
      cookies.set('saveOAuthTokens', action.saveOAuthTokens );
      // logout From the endpoints
      cookies.set('endpoint1', { ...state.endpoint1, login : false } );
      cookies.set('endpoint2', { ...state.endpoint2, login : false } );
      return Object.assign({}, state, {
        saveOAuthTokens: action.saveOAuthTokens,
        endpoint1 : { ...state.endpoint1, login : false },
        endpoint2 : { ...state.endpoint2, login : false },
      });

    default:
      return state
  }
}