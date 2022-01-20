package com.decathlon.ara.coreapi.repository;

import com.decathlon.ara.coreapi.domain.AraProject;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "projects", path = "projects")
public interface AraProjectRepository extends PagingAndSortingRepository<AraProject, String> {
    
}
