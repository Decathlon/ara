package com.decathlon.ara.service.mapper;

import java.util.Collection;
import java.util.List;

/**
 * Contract for a generic dto to entity mapper.
 *
 * @param <D> - DTO type parameter.
 * @param <E> - Entity type parameter.
 */
public interface EntityMapper<D, E> {

    E toEntity(D dto);

    D toDto(E entity);

    List<E> toEntity(Collection<D> dtoList);

    List<D> toDto(Collection<E> entityList);

}
