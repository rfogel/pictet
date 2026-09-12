package com.pictet.common.exception;

public abstract class ErrorMessage {

    public static final String BOOK_NOT_FOUND = "Book not found";
    public static final String INVALID_VALUES = "Invalid values";
    public static final String SESSION_NOT_FOUND = "Session not found";
    public static final String INVALID_OPTION = "Invalid option";
    public static final String BOOK_TITLE_CANNOT_BE_NULL_OR_BLANK = "Book title cannot be null or blank";
    public static final String BOOK_SECTIONS_CANNOT_BE_NULL_OR_EMPTY = "Book sections cannot be null or empty";
    public static final String BOOK_MUST_HAVE_ONLY_ONE_STARTING_SECTION = "Book must have only one starting section";
    public static final String BOOK_MUST_HAVE_AT_LEAST_ONE_ENDING_SECTION = "Book must have at least one ending section";
    public static final String NON_ENDING_SECTIONS_MUST_HAVE_OPTIONS = "Non ending sections must have options";
    public static String getInvalidOptionMessage(String description, String sectionId, String goToId) {
        return "Option with text '" + description + "' in section '" + sectionId + "' points to a non-existing section with id '" + goToId + "'";
    }
}
