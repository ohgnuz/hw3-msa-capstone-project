package hwmsacapstoneteam.domain;

import hwmsacapstoneteam.domain.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "pays", path = "pays")
public interface PayRepository extends PagingAndSortingRepository<Pay, Long> {}
