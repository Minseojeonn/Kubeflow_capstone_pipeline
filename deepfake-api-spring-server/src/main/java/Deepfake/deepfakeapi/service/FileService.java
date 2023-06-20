package Deepfake.deepfakeapi.service;


import Deepfake.deepfakeapi.domain.FileEntity;
import Deepfake.deepfakeapi.domain.HtmlFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import Deepfake.deepfakeapi.repository.FileRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;

    /*
        파일 저장하기
     */
    @Transactional
    public Long saveFile(FileEntity file){
        fileRepository.save(file);
        return file.getId();
    }

    /*
        파일 딥페이크 검출 여부 갱신
     */
    @Transactional
    public void updateDeepFake(String filename, int isDeepFake){
        FileEntity file = fileRepository.findByfileName(filename).get();
        file.setIsDeepfake(isDeepFake);
    }

    /*
        해당 파일의 딥페이크 여부 리턴
     */
    public int isFileDeepfake(String filename){
        FileEntity file = fileRepository.findByfileName(filename).get();
        return file.getIsDeepfake();
    }

    public FileEntity getRecentVideo(){
        List<FileEntity> files = fileRepository.findAll();
        for(int i=files.size()-1; i >= 0; i--){
            if(files.get(i).getFilePath().contains(".mp4")){
                return files.get(i);
            }
        }
        return null;
    }
}
