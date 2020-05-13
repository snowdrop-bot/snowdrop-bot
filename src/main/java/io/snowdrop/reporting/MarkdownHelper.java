package io.snowdrop.reporting;

import java.util.Arrays;
import java.util.Set;

import io.snowdrop.reporting.model.Issue;
import net.steppschuh.markdowngenerator.MarkdownSerializationException;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.code.Code;
import net.steppschuh.markdowngenerator.text.heading.Heading;

public class MarkdownHelper {
    public static void populateNestedLists(Set<Issue> items, UnorderedList unorderedList) {
        for (Issue item : items) {
            unorderedList.getItems().add(new Text(item.getTitle().concat(" - ")));
        }
    }

    public static void populateNestedLists(Object[] items, UnorderedList unorderedList) {
        for (Object item : items) {
            if (item instanceof String) {
                unorderedList.getItems().add(item);
            } else if (item instanceof Object[]) {
                UnorderedList sublist = new UnorderedList();
                unorderedList.getItems().add(sublist);
                populateNestedLists((Object[]) item, sublist);
            }
        }
    }

    public static String toMarkdown(Object... items) {
        String mdContents;
        UnorderedList unorderedList = new UnorderedList();
        populateNestedLists(items, unorderedList);
        try {
            mdContents = unorderedList.serialize();
        } catch (MarkdownSerializationException msEx) {
            mdContents = new Code(Arrays.toString(msEx.getStackTrace())).toString();
        }
        return mdContents;
    }

    public static StringBuilder addHeadingTitle(String title, int level) {
        return new StringBuilder()
                .append(new Heading(title, level));
    }

    public static StringBuilder addText(String txt) {
        return new StringBuilder()
                .append(new Text(txt));
    }
}
