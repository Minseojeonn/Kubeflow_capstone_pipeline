import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import App from "./App";
import WebEditor from "./WebEditor";
import VideoUpload from "./VideoUpload";
import WebViewer from "./WebViewer";
import VideoUploadResult from "./VideoUploadResult";

//const root = ReactDOM.createRoot(document.getElementById('root'));
ReactDOM.render(
    <BrowserRouter>
        <Routes>
            <Route path={"/"} element={<App />} />
            <Route path={"/webEditor"} element={<WebEditor/>} />
            <Route path={"/VideoUpload"} element={<VideoUpload/>} />
            <Route path={"/webViewer"} element={<WebViewer/>} />
            <Route path={"/VideoUploadResult"} element={<VideoUploadResult/>} />
        </Routes>
    </BrowserRouter>,
    document.getElementById('root')
);