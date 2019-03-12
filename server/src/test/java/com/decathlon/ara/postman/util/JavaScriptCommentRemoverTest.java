package com.decathlon.ara.postman.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class JavaScriptCommentRemoverTest {

    @InjectMocks
    private JavaScriptCommentRemover cut;

    @Test
    public void removeComments_should_work() {
        // GIVEN
        String source = "" +
                "var a = // Comment\n" + // Line comment
                "1 / /* Block /\n" + // Block comment containing a line-comment to ignore...
                "comment //too */ 2; // line/*comment\r\n" + // Line comment to remove, ignoring the block comment start
                "var b = /* 42 */ 3;\n"; // Simple block comment

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo("" +
                "var a = \n" +
                "1 /  2; \r\n" +
                "var b =  3;\n");
    }

    @Test
    public void removeComments_should_understand_line_comments_ending_at_all_operating_systems_line_separators() {
        // GIVEN
        String source = "" +
                "var a; // Comment\n" +
                "var b; // Comment\r" +
                "var c; // Comment\r\n" +
                "var d; // Comment";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo("" +
                "var a; \n" +
                "var b; \r" +
                "var c; \r\n" +
                "var d; ");
    }

    @Test
    public void removeComments_should_return_line_anyway_when_ending_with_line_comment() {
        // GIVEN
        String source = "var a; // Comment";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo("var a; ");
    }

    @Test
    public void removeComments_should_return_line_anyway_when_ending_with_block_comment() {
        // GIVEN
        String source = "var a; /* Comment";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo("var a; ");
    }

    @Test
    public void removeComments_should_ignore_comments_in_strings_with_double_quotes() {
        // GIVEN
        String expected = "var s = \"/*not...*/ //...comments\";";
        String source = expected + "//comment-to-remove";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo(expected);
    }

    @Test
    public void removeComments_should_ignore_comments_in_strings_with_single_quotes() {
        // GIVEN
        String expected = "var s = '/*not...*/ //...comments';";
        String source = expected + "//comment-to-remove";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo(expected);
    }

    @Test
    public void removeComments_should_understand_escaped_characters_in_strings_with_double_quotes() {
        // GIVEN
        // \" should not be understood as String ending, so comments in string should not be code comments
        // Two escapes separated by a character to check we do not just blindly ignore even nor odd characters!
        String expected = "var s = \"\\\"/*not...*/ ' //...comments\\\" \\\" /* nor this*/\";";
        final String source = expected + "//comment-to-remove";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo(expected);
    }

    @Test
    public void removeComments_should_understand_escaped_characters_in_strings_with_single_quotes() {
        // GIVEN
        // \' should not be understood as String ending, so comments in string should not be code comments
        // Two escapes separated by a character to check we do not just blindly ignore even nor odd characters!
        final String expected = "var s = '\\'/*not...*/ \" //...comments \\' \\' /* nor this*/';";
        String source = expected + "//comment-to-remove";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo(expected);
    }

    @Test
    public void removeComments_should_end_block_commands_at_correct_positions() {
        // GIVEN
        String source = "var a; /* not the end:* nor this:/ nor this:// but this one yes:*/ var b;";
        // \" should not be understood as String ending, so comments in string should not be code comments

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo("var a;  var b;");
    }

    @Test
    public void removeComments_should_return_empty_source_when_it_is_only_a_comment() {
        // GIVEN
        String source = "// Comment";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEmpty();
    }

    @Test
    public void removeComments_should_return_all_source_when_there_is_no_comment_nor_strings() {
        // GIVEN
        String source = "var a;";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo(source);
    }

    @Test
    public void removeComments_should_work_twice_in_a_row() {
        // GIVEN
        // Finish in a comment: next call should forget/reinitialize this state
        cut.removeComments("// Comment");
        String source = "var a;";

        // WHEN
        String withoutComments = cut.removeComments(source);

        // THEN
        assertThat(withoutComments).isEqualTo(source);
    }

}
