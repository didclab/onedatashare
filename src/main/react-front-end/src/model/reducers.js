
/*accountInformation
[credentials]
credentials:{key: validation}
*/

/*
queue
[{endpoint1: string, endpoint2: string, speed: number}]
*/

import { LOGIN, LOGOUT, PROMOTE, ENDPOINT_PROGRESS, ENDPOINT_UPDATE, UPDATE_HASH } from './actions';
import { transferOptimizations } from "./actions";
import { DROPBOX_NAME, GOOGLEDRIVE_NAME } from '../constants';

export const cookies = require("js-cookie");

export const beforeLogin = 0;
export const duringLogin = 1;
export const afterLogin = 2;
const initialState = {
	login: cookies.get('email') ? true : false,
	admin: false,
	email: cookies.get('email') || "noemail" ,
  hash: cookies.get('hash') || null,
  saveOAuthOption: false,

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
        side: "right"
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
    	console.log('logging in')
   		const {email, hash, saveOAuthOption} = action.credential;
      console.log(email);
    	cookies.set('email', email, {maxAge: 7200});
      cookies.set('hash', hash, {maxAge: 7200});
      cookies.set('saveOAuthOption', saveOAuthOption, {maxAge: 7200})
    	return Object.assign({}, state, {
    		login: true,
    		email: email,
        hash: hash,
        saveOAuthOption: saveOAuthOption
    	});
    case LOGOUT:
      console.log("logging out");
      cookies.remove('email');
      cookies.remove('hash');
      cookies.remove('admin');
      cookies.remove('endpoint1');
      cookies.remove('endpoint2');
      cookies.remove('saveOAuthOption');
      cookies.remove(DROPBOX_NAME);
      cookies.remove(GOOGLEDRIVE_NAME);
      window.location.replace('/');

      return Object.assign({}, state, {
        login: false,
        admin: false,
        hash: "",
        email: "noemail",
        saveOAuthOption: false
      });
    case PROMOTE:
      return Object.assign({}, state, {
        admin: true,
      });
    case ENDPOINT_PROGRESS:
      if(action.side == "left")
        return Object.assign({}, state, {
          endpoint1: {...state.endpoint1, loginProgress: action.progress},
        });
      else
        return Object.assign({}, state, {
          endpoint2: {...state.endpoint2, loginProgress: action.progress},
        });
    case ENDPOINT_UPDATE:
      if(action.side == "left"){
        console.log(JSON.stringify({...state.endpoint1, ...action.endpoint}));
        cookies.set('endpoint1', JSON.stringify({...state.endpoint1, ...action.endpoint}), {maxAge: 7200});
          return Object.assign({}, state, {
            endpoint1: {...state.endpoint1, ...action.endpoint},
          });
        }
      else{
        cookies.set('endpoint2', JSON.stringify({...state.endpoint2, ...action.endpoint}), {maxAge: 7200});
        return Object.assign({}, state, {
          endpoint2: {...state.endpoint2, ...action.endpoint},
        });
      }
    case UPDATE_HASH:
        cookies.remove('hash');
     cookies.set('hash',  action.hash, {maxAge: 7200});
         return Object.assign({}, state, {
                  hash: action.hash
                });

    default:
      return state
  }
}