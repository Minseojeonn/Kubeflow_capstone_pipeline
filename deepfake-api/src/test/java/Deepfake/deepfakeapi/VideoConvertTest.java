package Deepfake.deepfakeapi;

import Deepfake.deepfakeapi.controller.FileController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class VideoConvertTest {

    @Autowired FileController fileController;

    @Test
    public void videoToImageFrames() throws Exception{

        fileController.apiTest2();

    }
}
