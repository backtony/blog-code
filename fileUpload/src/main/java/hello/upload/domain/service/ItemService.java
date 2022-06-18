package hello.upload.domain.service;

import hello.upload.domain.dto.DownloadResponse;
import hello.upload.domain.dto.ItemForm;
import hello.upload.domain.dto.UploadFile;
import hello.upload.domain.entity.AttachFile;
import hello.upload.domain.entity.ImageFile;
import hello.upload.domain.entity.Item;
import hello.upload.domain.repository.ItemRepository;
import hello.upload.file.FileStore;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final FileStore fileStore;


    public Long save(ItemForm form) throws IOException {

        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        List<UploadFile> imageFiles = fileStore.storeFiles(form.getImageFiles());


        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setAttachFile(new AttachFile(attachFile));
        for (UploadFile imageFile : imageFiles) {
            ImageFile file = new ImageFile(imageFile);
            file.setItem(item);
            item.getImageFiles().add(file);
        }

        Item save = itemRepository.save(item);
        return save.getId();
    }


    public Item findItem(Long id) {
        return itemRepository.findById(id).orElseThrow(RuntimeException::new);

    }

    public Resource getResource(String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    public DownloadResponse download(Long itemId) throws MalformedURLException {
        Item item = itemRepository.findById(itemId).orElseThrow(RuntimeException::new);
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();

        String encodeUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        // 다운로드 규약
        String contentDisposition = "attachment; filename=\"" + encodeUploadFileName + "\"";
        UrlResource urlResource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        return new DownloadResponse(contentDisposition, urlResource);


    }
}
