package hello.upload.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Data
@Getter
@AllArgsConstructor
public class DownloadResponse {
    private String contentDisposition;
    private Resource resource;
}
