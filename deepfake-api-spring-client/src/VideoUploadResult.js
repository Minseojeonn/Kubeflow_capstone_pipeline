import '@toast-ui/editor/dist/toastui-editor-viewer.css';
import { Viewer } from '@toast-ui/react-editor';
import axios from "axios";
import {useState} from "react";
import {useEffect} from "react";

export default function VideoUploadResult() {

    const [deepfake, setDeepfake] = useState();

    useEffect(() => {
        getVideoUploadResult();
    }, []);

    const getVideoUploadResult = () => {
        axios({
            method: "get",
            url: "/video",
        })
            .then((response) => {
                if(response.status === 200){
                    console.log(response.data);
                    setDeepfake(response.data);
                }else{
                    alert("동영상 업로드 결과를 가져오는 데 실패");
                }
            })
            .catch((error) => {
            })
    }

    return (
        <div align={"center"}>
            <font size={6}>{deepfake}</ font>
        </div>
    );
}