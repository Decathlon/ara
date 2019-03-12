package com.decathlon.ara.service.mapper;

import com.decathlon.ara.service.dto.communication.CommunicationDTO;
import com.decathlon.ara.domain.Communication;
import org.mapstruct.Mapper;

@Mapper
public interface CommunicationMapper extends EntityMapper<CommunicationDTO, Communication> {

    // All methods are parameterized for EntityMapper

}
