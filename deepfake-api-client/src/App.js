import React, {useEffect, useState} from 'react';
import { Link } from "react-router-dom";
import {Button} from "@material-ui/core";
import {useNavigate} from "react-router-dom";

function App() {

    const navigate = useNavigate();

    const goWebEditor = () => {
        navigate('/webEditor')
    }

    const VideoUpload = () => {
        navigate('./VideoUpload')
    }

    return (
        <div align={"center"}>
            <Button type={"primary"} variant={"contained"} onClick={goWebEditor}>
                웹에디터
            </Button>
            <Button type={"primary"} variant={"contained"} onClick={VideoUpload}>
                동영상 업로드
            </Button>
        </div>
    );
}

export default App;
