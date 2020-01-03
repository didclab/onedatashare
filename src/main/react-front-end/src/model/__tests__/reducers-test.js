import { onedatashareModel as reducer, initialState } from '../reducers';
import { LOGIN, LOGOUT, PROMOTE, ENDPOINT_PROGRESS, ENDPOINT_UPDATE, UPDATE_HASH, ACCOUNT_PREFERENCE_TOGGLED, COMPACT_VIEW_PREFERENCE }
  from '../actions';
describe('Reducer', () => {
  it('should return the initial state', () => {
    expect(reducer(undefined, {})).toEqual(initialState);
  });

  it('should handle LOGIN', () => {
    const action = {
      type: LOGIN,
      credential: {
         email: 'admin@',
         hash: 'xdcffffsss',
         remember: true,
         saveOAuthTokens: true,
         compactViewEnabled: false
      },
    };
    const expectedState = {
      login: true,
      email: 'admin@',
      hash: 'xdcffffsss',
      saveOAuthTokens: true,
      compactViewEnabled: false
    };
    expect(reducer([], action)).toEqual(expectedState)
  });

  it('should handle LOGOUT', () => {
    const action = { type: LOGOUT };
    const expectedState = {
      login: false,
      admin: false,
      hash: "",
      email: "noemail",
      saveOAuthTokens: undefined
    }
    expect(reducer([], action)).toEqual(expectedState)
  });

  it('should handle PROMOTE', () => {
    const state = {
      ...initialState,
      login: true,
      email: 'user@gmail.com',
    };
    const action = { type: PROMOTE };
    const expectedState = { ...state, admin: true }
    expect(reducer(state, action)).toEqual(expectedState)
  });

  describe('ENDPOINT_PROGRESS', () => {
    it('should handle when action side is left', () => {
      const state = { ...initialState, endpoint1: {src: 'abc', login: true}, endpoint2: { src: 'xyz', login: true}};
      const action = {
        type: ENDPOINT_PROGRESS,
        side: 'left',
        progress: true
      };
      const expectedState = { ...state,  endpoint1: {...state.endpoint1, loginProgress: action.progress} }
      expect(reducer(state, action)).toEqual(expectedState)
    });

    it('should handle when action side is right', () => {
      const state = { ...initialState, endpoint1: {src: 'abc', login: true}, endpoint2: { src: 'xyz', login: true}};
      const action = {
        type: ENDPOINT_PROGRESS,
        side: 'right',
        progress: true
      };
      const expectedState = { ...state,  endpoint2: {...state.endpoint2, loginProgress: action.progress} }
      expect(reducer(state, action)).toEqual(expectedState)
    });
  });

  describe('ENDPOINT_UPDATE', () => {
    it('should handle when action side is left', () => {
      const state = { ...initialState, endpoint1: {src: 'abc', login: true}, endpoint2: { src: 'xyz', login: true}};
      const action = {
        type: ENDPOINT_UPDATE,
        side: 'left',
        endpoint: {src: 'new'}
      };
      const expectedState = { ...state,  endpoint1: {src: 'new', login: true }}
      expect(reducer(state, action)).toEqual(expectedState)
    });

    it('should handle when action side is right', () => {
      const state = { ...initialState, endpoint1: {src: 'abc', login: true}, endpoint2: { src: 'xyz', login: true}};
      const action = {
        type: ENDPOINT_UPDATE,
        side: 'right',
        endpoint: {src: 'new'}
      };
      const expectedState = { ...state,  endpoint2: {src: 'new', login: true}}
      expect(reducer(state, action)).toEqual(expectedState)
    });
  });

  it('should handle UPDATE_HASH', () => {
    const state = { ...initialState, login: true };
    const action = {
      type: UPDATE_HASH,
      hash: 'shgjdaad'
    };
    const expectedState = { ...state, hash: 'shgjdaad' }
    expect(reducer(state, action)).toEqual(expectedState)
  });

  it('should handle COMPACT_VIEW_PREFERENCE', () => {
    const state = { ...initialState, login: true };
    const action = {
      type: COMPACT_VIEW_PREFERENCE,
      compactViewEnabled: true
    };
    const expectedState = { ...state, compactViewEnabled: true }
    expect(reducer(state, action)).toEqual(expectedState)
  });

  it('should handle ACCOUNT_PREFERENCE_TOGGLED', () => {
    const state = { ...initialState, endpoint1: {src: 'abc', login: true}, endpoint2: { src: 'xyz', login: true}};
    const action = {
      type: ACCOUNT_PREFERENCE_TOGGLED,
      saveOAuthTokens: true
    };
    const expectedState = { ...state,
       saveOAuthTokens: action.saveOAuthTokens,
       endpoint1 : { src: 'abc', login : false },
       endpoint2 : { src: 'xyz', login : false }}
    expect(reducer(state, action)).toEqual(expectedState)
  });
})
