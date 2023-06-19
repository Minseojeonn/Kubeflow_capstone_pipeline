import {Viewer} from "@toast-ui/react-editor";

export default function ViewerYoutube({content}){
    // prop 으로 받는 content엔 youtube iframe 태그가 포함됨
    // youtube 공유용 iframe 태그를 렌더링해서 보여줄 수 있도록 설정
    return (
        <div>
            <Viewer>
                initialValue = {content}

                customHTMLRenderer={{
                    htmlBlock: {
                        iframe(node) {
                            return [
                                {
                                    type: "openTag",
                                    tagName: "iframe",
                                    outerNewLine: true,
                                    attributes: node.attrs,
                                },
                                {type: "html", content: node.childrenHTML},
                                {
                                    type: "closeTag",
                                    tagName: "iframe",
                                    outerNewLine: false,
                                },
                            ];
                        },
                    }
                }}
            </Viewer>
        </div>
    );
}