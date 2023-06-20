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

    const goViewer = () => {
        navigate('./webViewer')
    }
    const videoUploadResult = () => {
        navigate('./VideoUploadResult')
    }


    return (
        <div align={"center"}>
            <Button type={"primary"} variant={"contained"} onClick={goWebEditor}>
                웹에디터
            </Button>
            <Button type={"primary"} variant={"contained"} onClick={VideoUpload}>
                동영상 업로드
            </Button>
            <Button type={"primary"} variant={"contained"} onClick={goViewer}>
                게시글 보기
            </Button>
            <Button type={"primary"} variant={"contained"} onClick={videoUploadResult}>
                동영상 업로드 결과보기
            </Button>
        </div>
    );
}

export default App;
