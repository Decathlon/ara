package com.decathlon.ara.service.dto.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Wither;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Wither
public class TypeWithSourceCodeDTO extends TypeDTO {

    // Optional; no size control as we will just check the foreign key exists
    private String sourceCode;

    public TypeWithSourceCodeDTO(String code, String name, boolean isBrowser, boolean isMobile, String sourceCode) {
        super(code, name, isBrowser, isMobile);
        this.sourceCode = sourceCode;
    }

}
