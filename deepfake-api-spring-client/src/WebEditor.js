import { useRef } from 'react';
import {useNavigate} from "react-router-dom";
import $ from 'jquery';
// Toast 에디터
import { Editor } from '@toast-ui/react-editor';
import '@toast-ui/editor/dist/toastui-editor.css';
import {Button} from "@material-ui/core";
import React from "react";
import axios from "axios";
import {useState} from "react";

export default function ToastEditor() {

    let inputRef;
    const editorRef = useRef();
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
            setFile(file);
        }

        let str = '<div dangerouslySetInnerHTML={{ __html: htmlCode }}></div>'

        // 마크다운 모드에서 iframe 태그 삽입 후, 팝업을 닫고 위지윅 모드로 변환
        editorRef.current?.getInstance().changeMode('markdown');
        editorRef.current?.getInstance().insertText(str);
        //editorRef.current?.getInstance().changeMode('wysiwyg');
    }

    /*
        등록 버튼 누를 시 서버로 html 글을 post
     */
    const register = () => {
        const content = editorRef.current?.getInstance().getHTML()
        console.log(content);

        const formData = new FormData();
        formData.append('content', content);

        alert("글 등록 성공!");
        navigate("/");

        axios({
            method: "post",
            url: "/upload",
            data: formData
        })
            .then((response) => {
                if(response.status !== 200){
                    alert("서버에 파일 업로드하는 것을 실패했습니다.")
                }
            })
            .catch((error) => {
            })
    }

    return (
        <div>
            <h3>게시글 작성</h3>
            <Editor
                ref={editorRef}
                placeholder="내용을 입력해주세요."
                previewStyle="vertical" // 미리보기 스타일 지정
                height="700px" // 에디터 창 높이
                initialEditType="wysiwyg" // 초기 입력모드 설정(디폴트 markdown)
                toolbarItems={[
                    // 툴바 옵션 설정
                    ['heading', 'bold', 'italic', 'strike'],
                    ['hr', 'quote'],
                    ['ul', 'ol', 'task', 'indent', 'outdent'],
                    ['table', 'image', 'link'],
                    ['code', 'codeblock'],
                ]}
                // youtube 삽입 iframe 태그 사용 설정
                customHTMLRenderer={{
                    htmlBlock: {
                        iframe(node){
                            return [
                                {
                                    type: 'openTag',
                                    tagName: 'iframe',
                                    outerNewLine: true,
                                    attributes: node.attrs
                                },
                                {type: 'html', content: node.childrenHTML},
                                {type: 'closeTag', tagName: 'iframe', outerNewLine: true}
                            ];
                        }
                    }
                }}
                hooks={{
                    addImageBlobHook: (blob, callback) => {

                        const formData = new FormData();
                        formData.append('image', blob);

                        let url = 'http://localhost:9999/img/';
                        $.ajax({
                            type: 'POST',
                            enctype: 'multipart/form-data',
                            url: '/image',
                            data: formData,
                            processData: false,
                            contentType: false,
                            cache: false,
                            timeout: 600000,
                            success: function(fileName) {
                                url += fileName;
                                callback(url, 'blocked');
                            },
                            error: function(e) {
                                //
                                // callback('image_load_fail', '사진 대체 텍스트 입력');
                            }
                        });
                    }
                }}
            ></Editor>
            <input
                ref={refParam => inputRef = refParam}
                className="VideoInput_input"
                type="file"
                onChange={handleFileChange}
                accept=".mov,.mp4"
                style={{display: "none"}}
            />
            <div align={"center"}>
                <Button size={"large"} color="secondary" variant={"contained"} onClick={register}>
                    등록
                </Button>
            </div>
        </div>
    );
}