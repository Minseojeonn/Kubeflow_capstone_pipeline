package Deepfake.deepfakeapi.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Setter
@Getter
public class HtmlFile {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 10000)
    private String content; // html 파일 내용
}
