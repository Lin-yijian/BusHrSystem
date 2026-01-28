import Immutable from "immutable";

const $$initialState = Immutable.fromJS({
  demoValue1: {},
  demoValue2: true
});

export default (state = $$initialState, action) => {
  switch (action.type) {
    case "PLATFORM_DATA_DEMO_VALUE":
      return state.merge(action.payload);
    default:
      return state;
  }
};
