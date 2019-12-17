
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

export const cookies = require("js-cookie");
export const beforeLogin = 0;
export const duringLogin = 1;
export const afterLogin = 2;

const initialState = {
	login: cookies.get('email') ? true : false,
	admin: false,
	email: cookies.get('email') || "noemail" ,
  hash: cookies.get('hash') || null,
	compactViewEnabled: cookies.get('compactViewEnabled')==='true' || false,
  saveOAuthTokens: (cookies.get('saveOAuthTokens') !== undefined)? JSON.parse(cookies.get('saveOAuthTokens')) : false,

	endpoint1: cookies.get('endpoint1') ? JSON.parse(cookies.get('endpoint1')) : {
		login: false,
		credential: {},
		uri: "",
    side: "left"
	},

	endpoint2: cookies.get('endpoint2') ? JSON.parse(cookies.get('endpoint2')) : {
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

      cookies.set('email', email, { expires : maxCookieAge });
		  cookies.set('hash', hash, { expires : maxCookieAge });
      cookies.set('saveOAuthTokens', saveOAuthTokens, { expires : maxCookieAge });
			cookies.set('compactViewEnabled', compactViewEnabled);

    	return Object.assign({}, state, {
    		login: true,
    		email: email,
        hash: hash,
        saveOAuthTokens: saveOAuthTokens,
				compactViewEnabled: compactViewEnabled
      });

    case LOGOUT:
      console.log("logging out");
      cookies.remove('email');
      cookies.remove('hash');
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
        cookies.set('endpoint1', JSON.stringify({...state.endpoint1, ...action.endpoint}, { expires : maxCookieAge }));
          return Object.assign({}, state, {
            endpoint1: {...state.endpoint1, ...action.endpoint},
          });
        }
      else{
        cookies.set('endpoint2', JSON.stringify({...state.endpoint2, ...action.endpoint}), { expires : maxCookieAge });
        return Object.assign({}, state, {
          endpoint2: {...state.endpoint2, ...action.endpoint},
        });
      }

    case UPDATE_HASH:
      cookies.remove('hash');
      cookies.set('hash',  action.hash, { expires : maxCookieAge });
      return Object.assign({}, state, {
                hash: action.hash
              });
		case COMPACT_VIEW_PREFERENCE:
			cookies.set('compactViewEnabled', action.compactViewEnabled);
			return Object.assign({}, state, {
								compactViewEnabled: action.compactViewEnabled
							});

    case ACCOUNT_PREFERENCE_TOGGLED:
      cookies.set('saveOAuthTokens', action.saveOAuthTokens);
      // logout From the endpoints
      cookies.set('endpoint1', JSON.stringify({ ...state.endpoint1, login : false }));
      cookies.set('endpoint2', JSON.stringify({ ...state.endpoint2, login : false }));
      return Object.assign({}, state, {
        saveOAuthTokens: action.saveOAuthTokens,
        endpoint1 : { ...state.endpoint1, login : false },
        endpoint2 : { ...state.endpoint2, login : false },
      });

    default:
      return state
  }
}
