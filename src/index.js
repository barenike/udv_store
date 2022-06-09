import React from 'react';
import ReactDOM from 'react-dom';
import './src/main/resources/static/src/index.css/index.css';
import App from './main/resources/static/src/App.js';
import reportWebVitals from './main/resources/static/src/reportWebVitals.js';
import {AuthProvider} from "./main/resources/static/src/context/AuthProvider.js";
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import {Provider} from "react-redux";
import {store,persistor} from './main/resources/static/src/redux/store.js'
import { PersistGate } from 'redux-persist/integration/react'

ReactDOM.render(
    <React.StrictMode>
  <Provider store={store}>
      <Router>
      <AuthProvider>
          <Routes>
              <Route path="/*" element={<App />} />
          </Routes>
      </AuthProvider>
          </Router>
  </Provider>
    </React.StrictMode>,
  document.getElementById('root')
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
