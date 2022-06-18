package hello.upload.domain.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ITEM_ID")
    private Long id;
    private String itemName;

    @OneToOne(cascade = CascadeType.PERSIST)
    private AttachFile attachFile;

    @OneToMany(mappedBy = "item",cascade = CascadeType.ALL)
    private List<ImageFile> imageFiles = new ArrayList<>();

}
