package com.xuxin.summer.web.utils;

import jakarta.servlet.ServletException;

import java.util.regex.Pattern;

/**
 * description:
 *
 * @author xuxin
 * @since 2024/10/31
 */
public class PathUtils {

    public static Pattern compile(String path) throws ServletException {
        String regPath = path.replaceAll("\\{([a-zA-Z][a-zA-Z0-9]*)\\}", "(?<$1>[^/]*)");
        if (regPath.indexOf('{') >= 0 || regPath.indexOf('}') >= 0) {
            throw new ServletException("Invalid path: " + path);
        }
        return Pattern.compile("^" + regPath + "$");
    }
}
