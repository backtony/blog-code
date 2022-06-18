package hello.upload.domain.entity;

import hello.upload.domain.dto.UploadFile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class AttachFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ATTACHFILE_ID")
    private Long id;

    private String uploadFileName;
    private String storeFileName;

    public AttachFile(UploadFile uploadFile) {
        this.uploadFileName = uploadFile.getUploadFileName();
        this.storeFileName = uploadFile.getStoreFileName();
    }
}
