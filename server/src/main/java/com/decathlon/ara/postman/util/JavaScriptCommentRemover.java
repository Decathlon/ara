package com.decathlon.ara.postman.util;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Value;

public class JavaScriptCommentRemover {

    private State state;
    private State lastStringState;
    private int lastCodeStart;
    private List<Section> codeSections;

    /**
     * Tool to remove comments from a JavaScript file.<br>
     * It is smart enough to take care of removing line comments (that are not just before a block comment ending) and
     * block comments (that do not start in line comments), as well as not confusing comment starts embedded in strings
     * (either double- or single-quoted strings, escaped character are well considered too in such strings).<br>
     * As such, it is 100% accurate (considering it is not a real parser, but a fast state-machine).
     *
     * @param sourceCode the JavaScript source code where to remove comments
     * @return the same JavaScript source code without its comments
     */
    public String removeComments(String sourceCode) {
        state = State.IN_CODE;
        lastCodeStart = 0;
        codeSections = new ArrayList<>();

        for (int index = 0; index < sourceCode.length(); index++) {
            char character = sourceCode.charAt(index);
            handleCharacter(character, index);
        }

        if (lastCodeStart != -1) {
            codeSections.add(new Section(lastCodeStart, sourceCode.length()));
        }

        StringBuilder builder = new StringBuilder();
        for (Section section : codeSections) {
            builder.append(sourceCode.subSequence(section.getBegin(), section.getEnd()));
        }
        return builder.toString();
    }

    private void handleCharacter(char character, int index) {
        switch (state) {
            case IN_CODE:
                inCode(character);
                break;

            case IN_STRING_SINGLE_QUOTE:
                inString(character, State.IN_STRING_SINGLE_QUOTE, '\'');
                break;

            case IN_STRING_DOUBLE_QUOTES:
                inString(character, State.IN_STRING_DOUBLE_QUOTES, '"');
                break;

            case IN_STRING_ESCAPE:
                inStringEscape();
                break;

            case AT_SLASH_IN_CODE:
                atSlashInCode(character, index);
                break;

            case IN_LINE_COMMENT:
                inLineComment(character, index);
                break;

            case IN_BLOCK_COMMENT:
                inBlockComment(character);
                break;

            case MAYBE_AT_BLOCK_COMMENT_END:
                maybeAtBlockCommentEnd(character, index);
                break;
        }
    }

    private void inCode(char character) {
        if (character == '"') {
            state = State.IN_STRING_DOUBLE_QUOTES;
        } else if (character == '\'') {
            state = State.IN_STRING_SINGLE_QUOTE;
        } else if (character == '/') {
            state = State.AT_SLASH_IN_CODE;
        }
    }

    private void inString(char character, State currentStringState, char closingStringCharacter) {
        lastStringState = currentStringState;
        if (character == closingStringCharacter) {
            state = State.IN_CODE;
        } else if (character == '\\') {
            state = State.IN_STRING_ESCAPE;
        }
    }

    private void inStringEscape() {
        // The character is ignored
        state = lastStringState;
    }

    private void atSlashInCode(char character, int index) {
        if (character == '/') {
            codeSections.add(new Section(lastCodeStart, index - 1));
            lastCodeStart = -1;
            state = State.IN_LINE_COMMENT;
        } else if (character == '*') {
            codeSections.add(new Section(lastCodeStart, index - 1));
            lastCodeStart = -1;
            state = State.IN_BLOCK_COMMENT;
        } else {
            state = State.IN_CODE;
        }
    }

    private void inLineComment(char character, int index) {
        if (character == '\r' || character == '\n') {
            state = State.IN_CODE;
            lastCodeStart = index;
        }
    }

    private void inBlockComment(char character) {
        if (character == '*') {
            state = State.MAYBE_AT_BLOCK_COMMENT_END;
        }
    }

    private void maybeAtBlockCommentEnd(char character, int index) {
        if (character == '/') {
            state = State.IN_CODE;
            lastCodeStart = index + 1;
        } else {
            state = State.IN_BLOCK_COMMENT;
        }
    }

    enum State {
        /**
         * Default state: not in a comment, nor in a String.
         */
        IN_CODE,

        /**
         * Inside a 'string' contained in code ({@link #IN_CODE}), but not contained by a comment (not
         * {@link #IN_LINE_COMMENT} nor {@link #IN_BLOCK_COMMENT}).
         */
        IN_STRING_SINGLE_QUOTE,

        /**
         * Inside a "string" contained in code ({@link #IN_CODE}), but not contained by a comment (not
         * {@link #IN_LINE_COMMENT} nor {@link #IN_BLOCK_COMMENT}).
         */
        IN_STRING_DOUBLE_QUOTES,

        /**
         * After a '&#92;' in a string (inside {@link #IN_STRING_SINGLE_QUOTE} or {@link #IN_STRING_DOUBLE_QUOTES}).<br>
         * Unicode escapes ('&#92;u' followed by 4 hexadecimal digits) have no special treatment because in case of an
         * invalid hexadecimal number (less than 4 digits) followed by string end, it will not compile, but editors will
         * still understand/highlight the error as a string end.
         */
        IN_STRING_ESCAPE,

        /**
         * After a '/' in code: COULD start a comment (if followed by '/' or '*'), or just be a division character...
         */
        AT_SLASH_IN_CODE,

        /**
         * Inside a // line comment.
         */
        IN_LINE_COMMENT,

        /**
         * Inside a /* block comment *&#47;.
         */
        IN_BLOCK_COMMENT,

        /**
         * After a '*' in code: maybe a comment-block end, if followed by a slash, or not if followed by anything else.
         */
        MAYBE_AT_BLOCK_COMMENT_END
    }

    @Value
    @AllArgsConstructor
    private static final class Section {

        private final int begin;
        private final int end;

    }

}
