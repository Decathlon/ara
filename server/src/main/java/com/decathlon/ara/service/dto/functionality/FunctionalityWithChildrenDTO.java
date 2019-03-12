package com.decathlon.ara.service.dto.functionality;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class FunctionalityWithChildrenDTO extends FunctionalityDTO {

    private List<FunctionalityWithChildrenDTO> children;

}
