import { useRef } from 'react';
import {useNavigate} from "react-router-dom";
import $ from 'jquery';
// Toast 에디터
import { Editor } from '@toast-ui/react-editor';
import '@toast-ui/editor/dist/toastui-editor.css';
import {Button} from "@material-ui/core";
import React from "react";
import axios from "axios";
import youtubeicon from './free-icon-youtube-3128307.png'
import {useState} from "react";

export default function ToastEditor() {

    let inputRef;
    const editorRef = useRef();
    const navigate = useNavigate();
    const [source, setSource] = useState();
    const [file, setFile] = useState(null)

    /*
        youtube 영상 공유를 위한 툴바 아이콘 생성
     */
    const myCustomEl = document.createElement('span');
    myCustomEl.style = 'cursor: pointer;' // 커서 모양 : 손가락 모양

    // youtube icon img 태그 삽입
    const icon = document.createElement('img');
    icon.setAttribute('src', youtubeicon);
    icon.setAttribute('width', '32');
    myCustomEl.appendChild(icon);

    // youtube icon 클릭 시 팝업 바디 생성
    const container = document.createElement('div');
    const description = document.createElement('p');
    description.textContent = "Youtube 주소를 입력하세요";

    const urlInput = document.createElement('input');
    urlInput.style.width = '100%';

    // 팝업 input 창에 내용 입력 시 호출됨
    urlInput.addEventListener('keyup', (e) => {

        // 엔터를 쳤을 때 입력값이 Youtube url 인지 정규식으로 검사
        if (e.key === 'Enter') {
            if((/https:\/\/youtu.be\/.{11,}/).test(e.target.value)
                || (/https:\/\/www.youtube.com\/watch\?v=.{11,}/).test(e.target.value)) {

                // iframe 태그 생성
                let str = '<iframe width="560" height="315" src="https://www.youtube-nocookie.com/embed/'
                    + e.target.value.slice(-11)
                    + '" title="YouTube video player" border="0px" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe>';

                // 마크다운 모드에서 iframe 태그 삽입 후, 팝업을 닫고 위지윅 모드로 변환
                editorRef.current?.getInstance().changeMode('markdown');
                editorRef.current?.getInstance().insertText(str);
                editorRef.current?.getInstance().eventEmitter.emit('closePopup');
                editorRef.current?.getInstance().changeMode('wysiwyg');
            }
        }
    });
    // 팝업 창의 child tag 추가
    container.appendChild(description);
    container.appendChild(urlInput);

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        const url = URL.createObjectURL(file)
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onloadend = () => {
            setSource(url);
            setFile(file);
        }

        let htmlCode = '<video className="VideoInput_video" width="100%" height="50%" controls src="${source}"/>'
        let str = '<div dangerouslySetInnerHTML={{ __html: htmlCode }}></div>'

        //let str = "<video src='${url}' controls autoplay/>"
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

        axios({
            method: "post",
            url: "/upload",
            data: formData
        })
            .then((response) => {
                if(response.status === 200){
                    alert("글 등록 성공");
                    navigate("/");
                }else{
                    alert("글 등록에 실패했습니다.");
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
                    // youtube 삽입을 위바 툴바 유투브 버튼 커스터마이징
                    [{
                        name: 'Youtube',
                        tooltip: 'Youtube',
                        el: myCustomEl,
                        popup: {
                            body: container,
                            style: {width: 'auto'},
                        }
                    }],
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
                                callback(url, '사진 대체 텍스트 입력');
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