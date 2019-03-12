package com.decathlon.ara.postman.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stream {

    /**
     * The body of an HTTP response (or request) as a stream of bytes.<br>
     * Can potentially be big, so while streaming the JSON file, the data is saved to a temporary file on disk and the field is set to false to avoid OutOfMemoryErrors. The file can then be removed (if request was a success) or uploaded (if the request was an error and we need the response stream data to debug it).<br>
     * There is a "type":"Buffer" in the Stream object, but it might be for future expansion, as there is currently no other values.
     */
    private byte[] data;

    /**
     * Temporary file used to temporarily dump the stream from {@code data}.
     *
     * @see #data data for more detailed documentation of the process
     */
    private File tempFile;

}
