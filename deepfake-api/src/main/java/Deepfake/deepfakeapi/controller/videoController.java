package Deepfake.deepfakeapi.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.jcodec.api.FrameGrab;
import org.jcodec.codecs.h264.H264Decoder;
import org.jcodec.common.Codec;
import org.jcodec.common.io.NIOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

@Slf4j
@Controller
public class VideoController {

    @Async
    public ListenableFuture<Boolean> videoDeepFake(String filePath, String filename) throws Exception{

        boolean isDeepFake = false;
        /*
                동영상의 경우 이미지 추출
        */
        VideoConvertor videoConvertor = new VideoConvertor();
        File videoSource = new File(filePath);
        double videoDuration = extractDuration(videoSource);
        videoConvertor.getImageFrames(videoDuration, videoSource, filename);

        /*
            동영상에서 추출한 이미지 프레임들로 딥페이크 검출 진행
         */
        ArrayList<Double> predictResult = new ArrayList<>();
        DeepFakeDetection deepFakeDetection = new DeepFakeDetection();
        String frames_path = System.getProperty("user.dir") + "\\files\\video_" + filename;
        Path directory = Paths.get(frames_path);
        if(Files.exists(directory) && Files.isDirectory(directory)){
            File[] files = directory.toFile().listFiles();
            ArrayList<File> frameArr = new ArrayList<>(Arrays.asList(files));
            predictResult = deepFakeDetection.detectDeepFake(frameArr);
        }
        // 응답받은 딥페이크 예측값 출력
        System.out.println("prediction result:");
        System.out.println(predictResult);

        /*
            동영상 딥페이크 판단, 1. 하나의 프레임이 0.8 이상이거나 2. 모든 프레임의 평균이 0.65 이상이면
         */
        Double predictSum = 0.0;
        for(Double predict : predictResult){
            if(predict >= 0.8){ // 현재 프레임이 0.8 이상의 값으로 딥페이크 예측이 되었을 때
                isDeepFake = true;
                break;
            }
            predictSum += predict;
        }
        if(isDeepFake != false){
            if(predictSum / predictResult.size() >= 0.65){ // 모든 프레임 평균 예측이 0.65 이상일 때
                isDeepFake = true;
            }
        }
        return new AsyncResult<>(isDeepFake);
    }

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
