package Deepfake.deepfakeapi.controller;

import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.scale.AWTUtil;
import org.springframework.scheduling.annotation.Async;

import javax.imageio.ImageIO;
import javax.validation.constraints.NotNull;
import java.awt.image.BufferedImage;
import java.io.File;

public class VideoThread extends Thread {
    private int threadNo;
    private int threadSize;
    private double plusSize;
    private double videoDuration;
    private File video;
    private String savedPath;

    public VideoThread(File video, int threadSize, int threadNo, double plusSize, double videoDuration, String savedPath){
        this.video = video;
        this.threadSize = threadSize;
        this.threadNo = threadNo;
        this.plusSize = plusSize;
        this.videoDuration = videoDuration;
        this.savedPath = savedPath;
    }

    @Override
    public void run(){
        FrameGrab frameGrab;

        try{
            frameGrab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(video));

            int t = 0;
            while(true){
                if(t % threadSize == threadNo){
                    double startSec = t * plusSize;
                    if(startSec > videoDuration){
                        break;
                    }

                    System.out.println(threadNo + " " + startSec);

                    int frameCnt = 1;
                    frameGrab.seekToSecondPrecise(startSec);

                    for(int j=0; j<frameCnt; j++){
                        Picture picture = frameGrab.getNativeFrame();

                        BufferedImage bufferedImage = AWTUtil.toBufferedImage(picture);
                        //ImageIO.write(bufferedImage, "png", new File("C:/Users/HAYOUNG LEE/Desktop/deepfake-api/files/video/frame" + t + ".png"));
                        ImageIO.write(bufferedImage, "png", new File(savedPath + "/frame" + t + ".png"));
                    }
                }
                t += 1;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
