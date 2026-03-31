package cz.cyberrange.platform.training.service.mapping.mapstruct;

import cz.cyberrange.platform.training.api.responses.PageResultResource;
import org.springframework.data.domain.Page;

/**
 * ParentMapper is parent class for mappers which contains only one method for creating pagination.
 */
public interface ParentMapper {

  default PageResultResource.Pagination createPagination(Page<?> objects) {
    PageResultResource.Pagination pageMetadata = new PageResultResource.Pagination();
    pageMetadata.setNumber(objects.getNumber());
    pageMetadata.setNumberOfElements(objects.getNumberOfElements());
    pageMetadata.setSize(objects.getSize());
    pageMetadata.setTotalElements(objects.getTotalElements());
    pageMetadata.setTotalPages(objects.getTotalPages());
    return pageMetadata;
  }
}
