package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Source;
import com.decathlon.ara.service.dto.source.SourceDTO;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Source.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
class SourceTransformer {

    /**
     * Transform the given Source DO to a SourceDTO object.
     * <p>
     * Returns an empty SourceDTO if the parameter is null.
     *
     * @param source the DO to transform
     * @return the result DTO.
     */
    SourceDTO toDto(Source source) {
        SourceDTO result = new SourceDTO();
        if (null != source) {
            result.setCode(source.getCode());
            result.setName(source.getName());
            result.setLetter(String.valueOf(source.getLetter()));
            result.setTechnology(source.getTechnology());
            result.setVcsUrl(source.getVcsUrl());
            result.setDefaultBranch(source.getDefaultBranch());
            result.setPostmanCountryRootFolders(source.isPostmanCountryRootFolders());
        }
        return result;
    }
}
