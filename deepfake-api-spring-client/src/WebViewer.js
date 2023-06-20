import '@toast-ui/editor/dist/toastui-editor-viewer.css';
import { Viewer } from '@toast-ui/react-editor';
import axios from "axios";
import {useState} from "react";
import {useEffect} from "react";

export default function WebViewer() {
    // 마크다운
    const markdown = '## 게시글';
    const [html, setHtml] = useState();

    useEffect(() => {
        getHtml();
    }, []);

    const getHtml = () => {
        axios({
            method: "get",
            url: "/bbs",
        })
            .then((response) => {
                if(response.status === 200){
                    console.log(response.data);
                    setHtml(response.data);
                }else{
                    alert("게시판 글을 가져오는데 실패하였습니다.");
                }
            })
            .catch((error) => {
            })
    }

    return (
        <div>
            <Viewer initialValue={markdown} />
            <div dangerouslySetInnerHTML={{ __html: html }} />
        </div>
    );
}