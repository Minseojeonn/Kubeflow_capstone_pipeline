package Deepfake.deepfakeapi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;

public class DeepFakeDetection {

    /*
        딥페이크 검출 진행과 검출 결과값을 리스트로 하여 반환
     */
    public ArrayList<Double> detectDeepFake(ArrayList<File> fileArr) throws Exception {

        ArrayList<Double> predictResult = new ArrayList<>(); // 모든 검출 결과값을 저장할 array
        ArrayList<File> fileBatch = new ArrayList<>(); // 파일이 많은 경우 batchSize 단위로 잘라 서버에 전송
        if(!fileArr.isEmpty()){
            int i = 0;
            for (File file : fileArr) {
                if(i == 5){ // batchsize 6개로 지정
                    ArrayList<Double> predictArr = deepFake(fileBatch); // 파일 batch 만큼 보내어 딥페이크 검출 진행
                    for(Double predict : predictArr){
                        predictResult.add(predict);
                    }

                    fileBatch.clear();
                    i = 0;
                }
                if (file.isFile()) {
                    fileBatch.add(file);
                }
                i++;
            }
            ArrayList<Double> predictArr = deepFake(fileBatch);
            for(Double predict : predictArr){
                predictResult.add(predict);
            }
        }
        return predictResult;
    }

    /*
        딥페이크 검출 서버로 연결하여 예측값 응답받기
     */
    public ArrayList<Double> deepFake(ArrayList<File> imgFileArray) throws Exception{

        String efficientModel = "http://dslabjbnu.iptime.org:8083/predictions/efficient";
        String crossEfficientModel = "http://dslabjbnu.iptime.org:8082/predictions/cross_efficient";

        RestTemplate restTemplate = makeRestTemplate(true); // api 호출을 위한 restTemplate

        // http 헤더 세팅
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON); // json 형태 전송

        // 전송할 Body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // 전송할 이미지 데이터
        Map<String, List<String>> instance = new HashMap<String, List<String>>();
        List<String> imageArr = new ArrayList<>();
        for(File file : imgFileArray){
            imageArr.add(getBase64String(file)); //이미지파일을 json 형태로 보내기 위해 base64로 인코딩
        }
        instance.put("body", imageArr); // 전송할 이미지 base64 배열
        body.add("instances", instance);

        // header, body 를 담아 requestMessage 생성
        HttpEntity<?> requestMessage = new HttpEntity<>(body, httpHeaders);

        // API 서버에 요청 보낸 후 응답 받기
        int modelNumber = 2;
        ResponseEntity<String> predictResponse1 = restTemplate.postForEntity(efficientModel, requestMessage, String.class); // efficient_vit model
        ResponseEntity<String> predictResponse2 = restTemplate.postForEntity(crossEfficientModel, requestMessage, String.class); // cross_efficient_vit model

        // 두 모델에서의 예측 결과 응답
        ArrayList<Double> predictArr1 = parsingResponse(predictResponse1.getBody());
        ArrayList<Double> predictArr2 = parsingResponse(predictResponse2.getBody());

        // 앙상블 소프트 보팅 결과
        ArrayList<Double> softVotingResult = new ArrayList<>();
        for(int i=0; i<predictArr1.size(); i++){
            softVotingResult.add((predictArr1.get(i) + predictArr2.get(i)) / (double)modelNumber);
        }
        return softVotingResult;
    }

    /*
        String 형 response 에서 prediction 실수값들을 파싱하여 array 저장
     */
    private ArrayList<Double> parsingResponse(String response) throws Exception{

        ArrayList<Double> predictArr = new ArrayList<>(); // prediction 값을 저장할 리스트

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);

        JsonNode predictionsNode = rootNode.get("predictions"); // response 에서 predictions value 가져오기
        if (predictionsNode != null && predictionsNode.isArray()) {
            for (JsonNode predictionNode : predictionsNode) {
                if (predictionNode != null && predictionNode.isArray()) {
                    for (JsonNode valueNode : predictionNode) {
                        if (valueNode.isDouble()) {
                            double value = valueNode.asDouble();
                            predictArr.add(value);
                        }
                    }
                }
            }
        }
        return predictArr;
    }

    /*
        이미지 파일을 Base64 로 인코딩
     */
    private String getBase64String(File file) throws Exception {

        byte[] bytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(bytes);
    }

    /*
        다른 서버 api 호출에 필요한 RestTemplate 객체 생성
     */
    private RestTemplate makeRestTemplate(boolean ignoreSsl)throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // https 서버 호출
        if(ignoreSsl){
            // http client builder
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

            // 모든 인증서를 신뢰하도록 설정
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true).build();
            httpClientBuilder.setSSLContext(sslContext);

            // Https 인증 요청시 호스트네임 유효성 검사를 진행하지 않게 한다.
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory).build();

            PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            httpClientBuilder.setConnectionManager(connMgr); // http client builder 에 설정

            // build 하여 httpclient 생성, RestTemplate 와 HttpClient 연결
            HttpClient httpClient = httpClientBuilder.build();
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);

            RestTemplate restTemplate = new RestTemplate(requestFactory);
            return restTemplate;

        }else{ // http 서버 호출
            return new RestTemplate();
        }
    }
}
