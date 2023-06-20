package Deepfake.deepfakeapi.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
public class FileEntity {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    private String origFilename; // 받은 파일의 기존 이름
    @NotNull
    private String storedFilename; // 저장 이름

    @NotNull
    private String filePath; // 서버에 저장된 경로

    private int isDeepfake = 2; // 딥페이크 검출 여부

}
