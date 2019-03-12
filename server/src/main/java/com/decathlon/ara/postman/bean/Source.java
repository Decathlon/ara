package com.decathlon.ara.postman.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Source {

    /**
     * This class should really be an Item, but it duplicates a lot of information, so we only retrieve UUID here.<br/>
     * UUID of the folder or request, to match an item with its execution and failure(s).
     */
    private String id;

}
