package io.snowdrop.google;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import com.google.api.services.docs.v1.model.CreateParagraphBulletsRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Link;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.TextStyle;
import com.google.api.services.docs.v1.model.UpdateTextStyleRequest;

import org.locationtech.jts.algorithm.Length;

public class DocContentBuilder {

  public static final String TAB = "\t";
  public static final String NEWLINE = "\n";

  private final int startIndex;
  private int offset;
  private int endIndex;

  private final List<Entry> entries = new ArrayList<>();

  public DocContentBuilder() {
    this(1);
  }

  public DocContentBuilder(final int startIndex) {
    this.startIndex = startIndex;
    this.offset = startIndex;
  }

  public DocContentBuilder write(final String text) {
    return write(text, null);
  }

  public DocContentBuilder write(final String text, final TextStyle style) {
    this.entries.add(new TextEntry(text, style, null));
    return this;
  }

  public DocContentBuilder write(final String text, final TextStyle style, String fields) {
    this.entries.add(new TextEntry(text, style, fields));
    return this;
  }

  public DocContentBuilder link(String url) {
    return write(url, new TextStyle().setLink(new Link().setUrl(url)), "link");
  }

  public DocContentBuilder bold(String text) {
    return write(text, new TextStyle().setBold(true), "bold");
  }

  public DocContentBuilder italic(String text) {
    return write(text, new TextStyle().setItalic(true), "italic");
  }

  public DocContentBuilder tab() {
    return write(TAB);
  }

  public DocContentBuilder tab(int num) {
    for (int i = 0; i < num; i++) {
      write(TAB);
    }
    return this;
  }

  public DocContentBuilder newline() {
    return write(NEWLINE);
  }

  public DocContentBuilder bulletsOn() {
    this.entries.add(new BulletsOn());
    return this;
  }

  public DocContentBuilder bulletsOff() {
    this.entries.add(new BulletsOff());
    return this;
  }

  public int getStartIndex() {
    return startIndex;
  }

  public int getEndIndex() {
    return endIndex;
  }

  public void setEndIndex(int endIndex) {
    this.endIndex = endIndex;
  }

  public List<Request> build() {
    final List<Request> requests = new ArrayList<>();
    final List<Request> formating = new ArrayList<>();
    final Queue<Integer> bulletsStartIndex = new LinkedList<>();
    final Stack<Line> lines = new Stack<Line>();
    final AtomicInteger currentLineOffset = new AtomicInteger(1);
    final StringBuilder lb = new StringBuilder();

    entries.forEach(e -> {
      if (e instanceof BulletsOn) {
        Line lastLine = lines.peek();
        while (lastLine.text.isEmpty() && !lines.isEmpty()) {
          lastLine = lines.pop();
        }
        bulletsStartIndex.add(lastLine.offset + lastLine.text.length() + 1);
      } else if (e instanceof BulletsOff) {
        BulletsOff bulletsOff = (BulletsOff) e;
        bulletsOff.setStartIndex(bulletsStartIndex.poll());
        Line lastLine = lines.peek();
        while (lastLine.text.isEmpty() && !lines.isEmpty()) {
          lastLine = lines.pop();
        }
        formating.addAll(bulletsOff.toRequests(lastLine.offset + 1));
      } else if (e instanceof TextEntry) {
        TextEntry textEntry = (TextEntry) e;
        String[] line = textEntry.text.split(Pattern.quote(NEWLINE));
        if (line.length == 0) {
          lines.push(new Line(currentLineOffset.get(), lb.toString()));
          currentLineOffset.set(currentLineOffset.get() + lb.toString().length() + 1);
          lb.setLength(0);
          System.out.println("Line: " + lines.peek().offset + ":" + lines.peek().text);
        } else if (line.length == 1) {
          lb.append(line[0]);
        } else {
          for (int i = 1; i < line.length; i++) {
            lines.push(new Line(currentLineOffset.get(), lb.toString()));
            currentLineOffset.set(currentLineOffset.get() + lb.toString().length());
            System.out.println("Line: " + lines.peek().offset + ":" + lines.peek().text);
            lb.setLength(0);
            lb.append(line[i]);
          }
        }

        requests.addAll(e.toRequests(offset));
        offset += e.length();
        this.endIndex = offset;
      }
    });

    requests.addAll(formating);
    return requests;
  }

  public interface Entry {
    int length();

    List<Request> toRequests(int offset);
  }

  public class BulletsOn implements Entry {

    @Override
    public int length() {
      return 0;
    }

    @Override
    public List<Request> toRequests(int offset) {
      return Collections.EMPTY_LIST;
    }
  }

  public class BulletsOff implements Entry {

    private int startIndex = 1;

    @Override
    public int length() {
      return 0;
    }

    public void setStartIndex(int startIndex) {
      this.startIndex = startIndex;
    }

    @Override
    public List<Request> toRequests(int offset) {
      System.out.println("Bullets:" + startIndex + "-" + offset + " / " + getEndIndex());
      return Arrays.asList(new Request().setCreateParagraphBullets(
          new CreateParagraphBulletsRequest().setRange(new Range().setStartIndex(startIndex).setEndIndex(offset))
              .setBulletPreset("BULLET_ARROW_DIAMOND_DISC")));
    }
  }

  public class TextEntry implements Entry {

    private final String text;
    private final TextStyle style;
    private final String fields;

    public TextEntry(final String text, final TextStyle style, final String fields) {
      this.text = text;
      this.style = style;
      this.fields = fields;
    }

    public String getText() {
      return text;
    }

    public List<Request> toRequests(int offset) {
      final List<Request> result = new ArrayList<>();
      if (text != null && !text.isEmpty()) {
//        System.out.println("Text:" + offset + "-" + (offset + length()) + " / " + getEndIndex() + " :" + text);
        result.add(insert(offset, text));
        if (style != null) {
          result.add(style(offset, text, style, fields));
        }
      }
      return result;
    }

    private Request insert(int offset, final String str) {
      return new Request()
          .setInsertText(new InsertTextRequest().setText(str).setLocation(new Location().setIndex(offset)));
    }

    private Request style(int offset, final String str, final TextStyle textStyle, String fields) {
      UpdateTextStyleRequest styleRequest = new UpdateTextStyleRequest().setTextStyle(textStyle)
          .setRange(new Range().setStartIndex(offset).setEndIndex(offset + str.length()));
      if (fields != null && !fields.isEmpty()) {
        styleRequest = styleRequest.setFields(fields);
      }
      return new Request().setUpdateTextStyle(styleRequest);
    }

    @Override
    public int length() {
      return text.replaceAll(Pattern.quote(NEWLINE), " ").replaceAll(Pattern.quote(TAB), " ").length();
    }
  }

  public class Line {
    private int offset = 1;
    private String text;

    public Line(int offset, String text) {
      this.offset = offset;
      this.text = text;
    }

    public int getOffset() {
      return offset;
    }

    public void setOffset(int offset) {
      this.offset = offset;
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }
  }
}
