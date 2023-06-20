package Deepfake.deepfakeapi.service;

import Deepfake.deepfakeapi.domain.HtmlFile;
import Deepfake.deepfakeapi.repository.BbsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BbsService {

    private final BbsRepository bbsRepository;

    @Transactional
    public void saveHtml(HtmlFile htmlEntity){
        bbsRepository.saveHtml(htmlEntity);
    }

    /*
        가장 최근에 등록된 글 리턴
     */
    public HtmlFile getRecentHtml(){
        return bbsRepository.findRecentHtml();
    }
}
