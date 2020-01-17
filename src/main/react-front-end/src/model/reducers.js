
/*accountInformation
[credentials]
credentials:{key: validation}
*/

/*
queue
[{endpoint1: string, endpoint2: string, speed: number}]
*/

import { LOGIN, LOGOUT, PROMOTE, ENDPOINT_PROGRESS, ENDPOINT_UPDATE, UPDATE_HASH, ACCOUNT_PREFERENCE_TOGGLED, COMPACT_VIEW_PREFERENCE } from './actions';
import { transferOptimizations } from "./actions";
import { DROPBOX_NAME, GOOGLEDRIVE_NAME } from '../constants';
import { maxCookieAge } from '../constants';
import cookie from 'react-cookies';

export const cookies = require("js-cookie");
export const beforeLogin = 0;
export const duringLogin = 1;
export const afterLogin = 2;

const initialState = {
  login: cookie.load('email') ? true : false,
	admin: false,
  email: cookie.load('email') || "noemail",
  hash: cookie.load('hash') || null,
  compactViewEnabled: cookie.load('compactViewEnabled')==='true' || false,
  saveOAuthTokens: (cookie.load('saveOAuthTokens') !== undefined)? JSON.parse(cookie.load('saveOAuthTokens')) : false,

  endpoint1: cookie.load('endpoint1') ? JSON.parse(cookie.load('endpoint1')) : {
    login: false,
    credential: {},
    uri: "",
    side: "left"
  },

	endpoint2: cookie.load('endpoint2') ? JSON.parse(cookie.load('endpoint2')) : {
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
   		const {email, hash, saveOAuthTokens, compactViewEnabled} = action.credential;
      console.log('logging in', email);

      cookie.save('email', email, { expires : maxCookieAge });
		  cookie.save('hash', hash, { expires : maxCookieAge, secure : true, httpOnly : true });
      cookie.save('saveOAuthTokens', saveOAuthTokens, { expires : maxCookieAge, secure: true });
			cookie.save('compactViewEnabled', compactViewEnabled);

    	return Object.assign({}, state, {
    		login: true,
    		email: email,
        hash: hash,
        saveOAuthTokens: saveOAuthTokens,
				compactViewEnabled: compactViewEnabled
      });

    case LOGOUT:
      console.log("logging out");

      cookie.remove('email');
      cookie.remove('hash');
      cookie.remove('admin');
      cookie.remove('endpoint1');
      cookie.remove('endpoint2');
      cookie.remove('saveOAuthTokens');
      cookie.remove(DROPBOX_NAME);
      cookie.remove(GOOGLEDRIVE_NAME);
			cookie.remove('compactViewEnabled');
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
        cookie.save('endpoint1', JSON.stringify({...state.endpoint1, ...action.endpoint}, { expires : maxCookieAge, secure : true }));
          return Object.assign({}, state, {
            endpoint1: {...state.endpoint1, ...action.endpoint},
          });
        }
      else{
        cookie.save('endpoint2', JSON.stringify({...state.endpoint2, ...action.endpoint}), { expires : maxCookieAge, secure : true });
        return Object.assign({}, state, {
          endpoint2: {...state.endpoint2, ...action.endpoint},
        });
      }

    case UPDATE_HASH:
      cookie.remove('hash');
      cookie.save('hash',  action.hash, { expires : maxCookieAge, secure : true, httpOnly : true });
      return Object.assign({}, state, {
                hash: action.hash
              });
		case COMPACT_VIEW_PREFERENCE:
      cookie.save('compactViewEnabled', action.compactViewEnabled);
			return Object.assign({}, state, {
								compactViewEnabled: action.compactViewEnabled
							});

    case ACCOUNT_PREFERENCE_TOGGLED:
      cookie.save('saveOAuthTokens', action.saveOAuthTokens);
      // logout From the endpoints
      cookie.save('endpoint1', JSON.stringify({ ...state.endpoint1, login : false }));
      cookie.save('endpoint2', JSON.stringify({ ...state.endpoint2, login : false }));
      return Object.assign({}, state, {
        saveOAuthTokens: action.saveOAuthTokens,
        endpoint1 : { ...state.endpoint1, login : false },
        endpoint2 : { ...state.endpoint2, login : false },
      });

    default:
      return state
  }
}
