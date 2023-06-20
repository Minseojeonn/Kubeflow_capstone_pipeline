package Deepfake.deepfakeapi.repository;


import Deepfake.deepfakeapi.domain.HtmlFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BbsRepository {

    private final EntityManager em;

    public void saveHtml(HtmlFile htmlEntity){
        if(htmlEntity.getId() == null){
            em.persist(htmlEntity);
        }else{
            em.merge(htmlEntity);
        }
    }

    public HtmlFile findRecentHtml(){
        List<HtmlFile> htmlList = em.createQuery("select h from HtmlFile h", HtmlFile.class).getResultList();
        if(!htmlList.isEmpty()){
            return htmlList.get(htmlList.size() - 1);
        }else{
            return null;
        }
    }
}
