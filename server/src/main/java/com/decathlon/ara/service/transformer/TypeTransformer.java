package com.decathlon.ara.service.transformer;

import com.decathlon.ara.domain.Type;
import com.decathlon.ara.service.dto.type.TypeWithSourceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provide transformation utilities (DTO - DO and DO - DTO) for the Type.
 *
 * @author Sylvain Nieuwlandt
 * @since 3.0.1
 */
@Service
class TypeTransformer {

    @Autowired
    private SourceTransformer sourceTransformer;

    /**
     * Transform the given Type DO to a TypeWithSourceDTO object.
     * <p>
     * Returns an empty TypeWithSourceDTO if the parameter is null.
     *
     * @param type the DO to transform
     * @return the result DTO.
     */
    TypeWithSourceDTO toDtoWithSource(Type type) {
        TypeWithSourceDTO result = new TypeWithSourceDTO();
        if (null != type) {
            result.setCode(type.getCode());
            result.setName(type.getName());
            result.setBrowser(type.isBrowser());
            result.setMobile(type.isMobile());
            result.setSource(sourceTransformer.toDto(type.getSource()));
        }
        return result;
    }
}
