package io.snowdrop.google;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.TextStyle;
import com.google.api.services.docs.v1.model.UpdateTextStyleRequest;

public class DocContentBuilder {

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

  public void write(final String text) {
    write(text, null);
  }

  public void write(final String text, final TextStyle style) {
    this.entries.add(new Entry(text, style, null));
  }

  public void write(final String text, final TextStyle style, String fields) {
    this.entries.add(new Entry(text, style, fields));
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
    entries.forEach(e -> {
      requests.addAll(e.toRequests(offset));
      offset += e.text.length();
      this.endIndex = offset;
    });
    return requests;
  }

  public class Entry {

    private final String text;
    private final TextStyle style;
    private final String fields;

    public Entry(final String text, final TextStyle style, final String fields) {
      this.text = text;
      this.style = style;
      this.fields = fields;
    }

    public List<Request> toRequests(int offset) {
      final List<Request> result = new ArrayList<>();
      if (text != null && !text.isEmpty()) {
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
  }

}
