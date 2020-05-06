package io.snowdrop.reporting.model;

import java.util.Map;
import java.util.TreeMap;

public class IssueOpen {
    public static Map<String, Boolean> open = new TreeMap<String, Boolean>(String.CASE_INSENSITIVE_ORDER) {{
        put("open", true);
    }};

    public static boolean isOpen(final String pstatus) {
        if (open.containsKey(pstatus)) {
            return open.get(pstatus);
        } else {
            return false;
        }
    }
}
