package com.decathlon.ara.service.dto.source;

import com.decathlon.ara.domain.enumeration.Technology;
import java.util.Comparator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import static com.decathlon.ara.service.support.DtoConstants.CODE_MESSAGE;
import static com.decathlon.ara.service.support.DtoConstants.CODE_PATTERN;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Wither
// Keep business key in sync with compareTo(): see https://developer.jboss.org/wiki/EqualsAndHashCode
@EqualsAndHashCode(of = { "code" })
public class SourceDTO implements Comparable<SourceDTO> {

    /**
     * Fake source that encompass all ones, to have a global summary.
     */
    public static final SourceDTO ALL = new SourceDTO("*", "All", "*", null, null, null, false);

    @NotNull(message = "The code is required.")
    @Size(min = 1, max = 16, message = "The code is required and must not exceed {max} characters.")
    @Pattern(regexp = CODE_PATTERN, message = CODE_MESSAGE)
    private String code;

    @NotNull(message = "The name is required.")
    @Size(min = 1, max = 32, message = "The name is required and must not exceed {max} characters.")
    private String name;

    @NotNull(message = "The letter is required.")
    @Size(min = 1, max = 1, message = "The letter is required and must not exceed {max} characters.")
    private String letter;

    @NotNull(message = "The technology is required.")
    private Technology technology;

    @NotNull(message = "The Version Control System URL is required.")
    @Size(min = 1, max = 256,
            message = "The Version Control System URL is required and must not exceed {max} characters.")
    @Pattern(regexp = ".*\\{\\{branch}}.*", message = "The Version Control System URL must have a {{branch}} placeholder.")
    private String vcsUrl;

    @NotNull(message = "The default branch is required.")
    @Size(min = 1, max = 16, message = "The default branch is required and must not exceed {max} characters.")
    private String defaultBranch;

    private boolean postmanCountryRootFolders;

    @Override
    public int compareTo(SourceDTO other) {
        // Keep business key in sync with @EqualsAndHashCode
        Comparator<SourceDTO> codeComparator = comparing(SourceDTO::getCode, nullsFirst(naturalOrder()));
        return nullsFirst(codeComparator).compare(this, other);
    }

}
