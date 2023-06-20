package Deepfake.deepfakeapi.repository;

import Deepfake.deepfakeapi.domain.FileEntity;
import Deepfake.deepfakeapi.domain.HtmlFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FileRepository {

    private final EntityManager em;

    public void save(FileEntity file){
        if(file.getId() == null){
            em.persist(file);
        }else{
            em.merge(file);
        }
    }

    public Optional<FileEntity> findByfileName(String filename){
        return em.createQuery("select f from FileEntity f where f.storedFilename = :filename", FileEntity.class)
                .setParameter("filename", filename)
                .getResultList().stream().findFirst();
    }

    public List<FileEntity> findAll(){
        return em.createQuery("select f from FileEntity f", FileEntity.class).getResultList();
    }

}
