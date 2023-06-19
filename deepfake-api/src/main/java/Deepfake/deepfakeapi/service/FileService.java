package Deepfake.deepfakeapi.service;


import Deepfake.deepfakeapi.domain.FileEntity;
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

    @Transactional
    public void updateDeepFake(String filename){
        FileEntity file = fileRepository.findByfileName(filename);
        file.setIsDeepfake(1);
    }

    public FileEntity getFile(Long id){
        return fileRepository.findOne(id);
    }




}
