package Deepfake.deepfakeapi.controller;
import lombok.extern.slf4j.Slf4j;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.springframework.stereotype.Controller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Controller
public class VideoController {

    /*
        동영상의 이미지 프레임을 자르고 딥페이크 검출 진행
     */
    public int videoDeepFake(String filePath, String filename) throws Exception{

        int isDeepFake = 0;

        // 동영상에서 이미지 프레임 추출
        VideoConvertor videoConvertor = new VideoConvertor();
        File videoSource = new File(filePath);
        double videoDuration = extractDuration(videoSource);
        videoConvertor.getImageFrames(videoDuration, videoSource, filename);


        // 동영상에서 추출한 이미지 프레임들로 딥페이크 검출 진행
        ArrayList<Double> predictResult = new ArrayList<>();
        DeepFakeDetection deepFakeDetection = new DeepFakeDetection();
        String frames_path = System.getProperty("user.dir") + "\\files\\video_" + filename; // 추출한 이미지가 저장된 디렉토리
        Path directory = Paths.get(frames_path);
        if(Files.exists(directory) && Files.isDirectory(directory)){
            File[] files = directory.toFile().listFiles(); // 디렉토리에 있는 모든 이미지 파일 가져옴

            ArrayList<File> frameArr = new ArrayList<>(Arrays.asList(files));
            predictResult = deepFakeDetection.detectDeepFake(frameArr); // 딥페이크 검출 진행
        }
        // 응답받은 딥페이크 예측값 출력
        System.out.println("prediction result:");
        System.out.println(predictResult);


        // 동영상 딥페이크 판단, 1. 하나의 프레임이 0.8 이상이거나 2. 모든 프레임의 평균이 0.65 이상이면
        Double predictSum = 0.0;
        for(Double predict : predictResult){
            if(predict >= 0.8){ // 현재 프레임이 0.8 이상의 값으로 딥페이크 예측이 되었을 때
                isDeepFake = 1;
                break;
            }
            predictSum += predict;
        }
        if(isDeepFake != 1){
            if(predictSum / predictResult.size() >= 0.65){ // 모든 프레임 평균 예측이 0.65 이상일 때
                isDeepFake = 1;
            }
        }
        return isDeepFake;
    }

    /*
        동영상 총 길이 추출
     */
    public double extractDuration(File videoSource) throws IOException {
        try{
            FrameGrab frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(videoSource));
            double durationInSeconds = frameGrab.getVideoTrack().getMeta().getTotalDuration();
            log.info("video duration: {} seconds", durationInSeconds);
            return durationInSeconds;
        }catch(Exception e){
            log.warn("duration extraction fail");
        }
        return 0;
    }
}
