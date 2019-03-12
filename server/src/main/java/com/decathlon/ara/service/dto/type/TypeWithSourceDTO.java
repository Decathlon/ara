package com.decathlon.ara.service.dto.type;

import com.decathlon.ara.service.dto.source.SourceDTO;
import javax.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TypeWithSourceDTO extends TypeDTO {

    @Valid
    private SourceDTO source;

    public TypeWithSourceDTO(String code) {
        super(code, null, false, false);
    }

}
