package Deepfake.deepfakeapi.controller;

import Deepfake.deepfakeapi.domain.FileEntity;
import Deepfake.deepfakeapi.domain.HtmlFile;
import Deepfake.deepfakeapi.service.BbsService;
import Deepfake.deepfakeapi.service.FileService;
import Deepfake.deepfakeapi.utility.MD5Generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.*;


@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final BbsService bbsService;
    private final VideoController videoController;

    /*
        동영상 파일 업로드
     */
    @PostMapping("/uploadVideo")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile files) throws Exception{

        // 파일이 존재하면
        if(!files.isEmpty()){
            try {
                // 기존 파일이름을 다른 파일 이름으로 저장하기 위해 변환
                String origFileName = files.getOriginalFilename();
                String extension = origFileName.substring(origFileName.lastIndexOf("."));
                String filename = new MD5Generator(origFileName).toString();

                // 서버에 저장될 위치
                String savedPath = System.getProperty("user.dir") + "\\files";
                if(!new java.io.File(savedPath).exists()){
                    try{
                        new File(savedPath).mkdir();
                    }catch(Exception e){
                        e.getStackTrace();
                    }
                }

                // 파일을 로컬에 저장
                String filePath = savedPath + "\\" + filename + extension;
                files.transferTo(new File(filePath));

                // DB에 저장될 파일 정보 객체 생성
                FileEntity file = new FileEntity();
                file.setOrigFilename(origFileName);
                file.setStoredFilename(filename);
                file.setFilePath(filePath);
                fileService.saveFile(file);

                // 동영상의 딥페이크 검출
                int result = videoController.videoDeepFake(filePath, origFileName);
                System.out.println("video Deepfake detection result:");
                System.out.println(result);
                fileService.updateDeepFake(filename, result); // 해당 파일 딥페이크 여부 갱신

                return new ResponseEntity<>("Success", HttpStatus.OK);

            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>("File not exists", HttpStatus.OK);
    }

    /*
        html 파일 업로드 ( 이미지가 포함될 수 있는 html)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("content") String html) throws Exception{

        // html 파일 저장
        HtmlFile htmlEntity = new HtmlFile();
        htmlEntity.setContent(html);
        bbsService.saveHtml(htmlEntity);

        // html 에서 parsing 한 이미지 파일 이름을 저장할 리스트
        ArrayList<String> imgArray = new ArrayList<>();

        // html 파일로부터 img Tag 만 추출하기 -> 이미지만 추출
        Document doc = Jsoup.parse(html);
        Elements imgs = doc.getElementsByTag("img");
        if(imgs.size() > 0){
            for(Element img : imgs){
                String src = img.attr("src"); // img 태그의 src 속성
                String[] splited = src.split("/"); // http://localhost:9999/img/example.png
                String imgName = splited[splited.length - 1]; // example.png 추출
                imgArray.add(imgName);
            }
        }

        // 실제 이미지 파일 가져오기
        ArrayList<File> imgFileArray = new ArrayList<>(); // 이미지 파일을 저장할 array
        if(!imgArray.isEmpty()){ // 추출된 img가 존재하는지 확인
            for(String img : imgArray){
                File file = new File("C:/Users/HAYOUNG LEE/Desktop/deepfake-api/files/" + img); // 서버에 저장된 경로에서 파일 가져오기
                imgFileArray.add(file);
            }
        }

        // html 에서 추출한 이미지에 대해 딥페이크 검출
        if(!imgFileArray.isEmpty()){
            DeepFakeDetection deepFakeDetection = new DeepFakeDetection();
            ArrayList<Double> predictResult = deepFakeDetection.detectDeepFake(imgFileArray); // 딥페이크 검출 결과

            System.out.println("prediction result:");
            System.out.println(predictResult);

            for(int i=0; i<predictResult.size(); i++){
                File deepFakeFile = imgFileArray.get(i);
                if(predictResult.get(i) >= 0.65){ // 딥페이크 검출값이 0.65 이상일 때 해당 이미지는 딥페이크 판단
                    fileService.updateDeepFake(deepFakeFile.getName().replace(".png", ""), 1); // 해당 이미지 파일의 딥페이크 여부 1로 갱신
                }else{
                    fileService.updateDeepFake(deepFakeFile.getName().replace(".png", ""), 0);

                }
            }
        }

        return new ResponseEntity<>("success", HttpStatus.OK);
    }

    /*
        이미지가 에디터에 삽입될 때마다 서버로 업로드되어 저장 ( 글 등록과 다름 )
     */
    @PostMapping("/image")
    public ResponseEntity<?> image(@RequestParam("image") MultipartFile file) throws Exception{
        String origFileName = file.getOriginalFilename();
        String filename = new MD5Generator(origFileName).toString();

        // 서버에 저장될 위치
        String savedPath = System.getProperty("user.dir") + "\\files";
        if(!new java.io.File(savedPath).exists()){
            try{
                new File(savedPath).mkdir();
            }catch(Exception e){
                e.getStackTrace();
            }
        }
        // 저장
        String filePath = savedPath + "\\" + filename + ".png";
        file.transferTo(new File(filePath));

        // DB에 저장될 파일 정보 객체 생성
        FileEntity savedFile = new FileEntity();
        savedFile.setOrigFilename(origFileName);
        savedFile.setStoredFilename(filename);
        savedFile.setFilePath(filePath);
        fileService.saveFile(savedFile);

        return new ResponseEntity<>(filename + ".png", HttpStatus.OK);
    }
}
