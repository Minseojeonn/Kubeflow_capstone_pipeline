package Deepfake.deepfakeapi.controller;

import Deepfake.deepfakeapi.domain.FileEntity;
import Deepfake.deepfakeapi.domain.HtmlFile;
import Deepfake.deepfakeapi.service.BbsService;
import Deepfake.deepfakeapi.service.FileService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequiredArgsConstructor
public class BbsController {

    private final BbsService bbsService;
    private final FileService fileService;

    /*
        사용자 예시를 위한 html 파일 리턴
     */
    @GetMapping("/bbs")
    public ResponseEntity<?> showHtml(){
        HtmlFile htmlFile = bbsService.getRecentHtml();
        if(htmlFile == null){
            return new ResponseEntity<>("<div>no content<div>", HttpStatus.OK);
        }
        String htmlContent = htmlFile.getContent();

        // html 내용에 딥페이크 이미지가 있는지 검사
        Document doc = Jsoup.parse(htmlContent);
        Elements imgs = doc.getElementsByTag("img");
        if(imgs.size() > 0){
            int idx = 0;
            for(Element img : imgs) {
                String src = img.attr("src"); // img 태그의 src 속성
                String[] splited = src.split("/"); // http://localhost:9999/img/example.png
                String imgName = splited[splited.length - 1]; // example.png 추출
                int isDeepFake = fileService.isFileDeepfake(imgName.replace(".png", "")); // 해당 이미지의 딥페이크 여부
                if (isDeepFake == 1) { // 딥페이크 이미지일 때
                    Element imgComponent = (Element)imgs.get(idx);
                    imgComponent.removeAttr("src"); // 이미지가 띄워지지 않도록 제거함
                }
                idx++;
            }
        }
        // 딥페이크 검사 후 리턴할 html
        String htmlResult = doc.outerHtml();
        return new ResponseEntity<>(htmlResult, HttpStatus.OK);
    }

    /*
        동영상 검출 결과
     */
    @GetMapping("/video")
    public ResponseEntity<?> returnVideo(){
        FileEntity recentVideo = fileService.getRecentVideo();
        if(recentVideo == null){
            return new ResponseEntity<>("no video is uploaded", HttpStatus.OK);
        }else if(recentVideo.getIsDeepfake() == 2){
            return new ResponseEntity<>("deepfake detecting", HttpStatus.OK);
        }else if(recentVideo.getIsDeepfake() == 1){
            return new ResponseEntity<>("deepfake video", HttpStatus.OK);
        }else{
            return new ResponseEntity<>("no deepfake video", HttpStatus.OK);
        }
    }
}
