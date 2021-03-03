package io.imast.work4j.model;

import java.util.regex.Pattern;

/**
 * The jobs constants
 * 
 * @author davitp
 */
public class Jobs {
    
    /**
     * The name regex pattern
     */
    public static final Pattern NAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]+$");
    
    /**
     * The type regex pattern
     */
    public static final Pattern TYPE_REGEX = NAME_REGEX;
    
    /**
     * The folder regex pattern
     */
    public static final Pattern FOLDER_REGEX = Pattern.compile("^(\\/[A-Za-z0-9_]+)*\\/$");
}
