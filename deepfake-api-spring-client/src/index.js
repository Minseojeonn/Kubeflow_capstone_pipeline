import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import App from "./App";
import WebEditor from "./WebEditor";
import VideoUpload from "./VideoUpload";

//const root = ReactDOM.createRoot(document.getElementById('root'));
ReactDOM.render(
    <BrowserRouter>
        <Routes>
            <Route path={"/"} element={<App />} />
            <Route path={"/webEditor"} element={<WebEditor/>} />
            <Route path={"/VideoUpload"} element={<VideoUpload/>} />
        </Routes>
    </BrowserRouter>,
    document.getElementById('root')
);