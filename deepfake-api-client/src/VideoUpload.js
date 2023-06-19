import React from 'react';
import Typography from "@material-ui/core/Typography";
import {useState} from "react";
import {Button} from "@material-ui/core";
import axios from "axios";
import {useNavigate} from "react-router-dom";

export default function VideoUpload(){

    let inputRef;
    const navigate = useNavigate();

    const [source, setSource] = useState();
    const [file, setFile] = useState(null)

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        const url = URL.createObjectURL(file)
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onloadend = () => {
            setSource(url);
            setFile(file)
        }
    }

    const sendToServer = () => {
        const formData = new FormData();
        formData.append('file', file);
        console.log(file)

        axios({
            method:"post",
            url:"/uploadVideo",
            data: formData
        })
            .then((response)=>{
                if(response.status === 200){
                    alert("파일 업로드 성공!")
                    navigate("/")
                } else {
                    alert("서버에 파일 업로드하는 것을 실패했습니다.")
                }
            })
            .catch((error) => {
            })

    }

    return (
        <div align={"center"} className="videoInput" style={{maxWidth: '700px', margin: '2rem auto'}}>
            <div style={{textAlign: 'center', marginBottom: '2rem'}}>
                <Typography variant={"h2"}>Upload Video</Typography>
            </div>
            <input
                ref={refParam => inputRef = refParam}
                className="VideoInput_input"
                type="file"
                onChange={handleFileChange}
                accept=".mov,.mp4"
                style={{display: "none"}}
            />
            <Button type={"primary"} variant={"contained"} onClick={() => inputRef.click()}>
                Choose
            </Button>
            <Button type={"primary"} variant={"contained"} onClick={sendToServer}>
                Upload
            </Button>
            {source && (
                <video
                    className="VideoInput_video"
                    width="100%"
                    height="30%"
                    controls
                    src={source}
                />
            )}
        </div>
    );
}
