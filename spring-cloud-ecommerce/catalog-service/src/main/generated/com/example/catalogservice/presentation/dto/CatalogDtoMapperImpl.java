package com.example.catalogservice.presentation.dto;

import com.example.catalogservice.domain.service.dto.response.CatalogInfoResponseDto;
import com.example.catalogservice.presentation.dto.response.CatalogInfoResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2022-05-17T00:32:11+0900",
    comments = "version: 1.4.2.Final, compiler: javac, environment: Java 11.0.11 (AdoptOpenJDK)"
)
@Component
public class CatalogDtoMapperImpl implements CatalogDtoMapper {

    @Override
    public CatalogInfoResponse from(CatalogInfoResponseDto catalogInfoResponseDto) {
        if ( catalogInfoResponseDto == null ) {
            return null;
        }

        CatalogInfoResponse catalogInfoResponse = new CatalogInfoResponse();

        catalogInfoResponse.setProductId( catalogInfoResponseDto.getProductId() );
        catalogInfoResponse.setQty( catalogInfoResponseDto.getQty() );
        catalogInfoResponse.setUnitPrice( catalogInfoResponseDto.getUnitPrice() );
        catalogInfoResponse.setTotalPrice( catalogInfoResponseDto.getTotalPrice() );
        catalogInfoResponse.setOrderId( catalogInfoResponseDto.getOrderId() );
        catalogInfoResponse.setUserId( catalogInfoResponseDto.getUserId() );

        return catalogInfoResponse;
    }
}
