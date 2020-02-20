package io.snowdrop.google.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Link;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.TextStyle;
import com.google.api.services.docs.v1.model.UpdateTextStyleRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Str implements IText<Str> {

  private static final Logger LOGGER = LoggerFactory.getLogger(Str.class);

  private static final String TAB = "\t";
  private static final String NEWLINE = "\n";
  private static final String BOLD = "bold";
  private static final String ITALIC = "italic";
  private static final String UNDERLINED = "underlined";
  private static final String LINK = "link";

  private int startIndex;
  private StringBuilder builder = new StringBuilder();
  private List<String> formattings = new ArrayList<>();

  public Str(int startIndex) {
    this.startIndex = startIndex;
  }

  public Str(int startIndex, String str) {
    this.startIndex = startIndex;
    this.builder.append(str);
  }

  public int getStartIndex() {
    return startIndex;
  }

  public int getEndIndex() {
    return startIndex + builder.length();
  }

  public String getText() {
    return builder.toString();
  }

  public boolean isEmpty() {
    return builder.length() == 0;
  }

  @Override
  public Str tab() {
    return write(TAB);
  }

  @Override
  public Str tab(int num) {
    for (int i = 0; i < num; i++) {
      tab();
    }
    return this;
  }

  @Override
  public Str write(String s) {
    builder.append(s);
    return this;
  }

  @Override
  public Str bold(String s) {
    write(s);
    formattings.add(BOLD);
    return this;
  }

  @Override
  public Str italic(String s) {
    write(s);
    formattings.add(ITALIC);
    return this;
  }

  @Override
  public Str underline(String s) {
    write(s);
    formattings.add(UNDERLINED);
    return this;
  }

  @Override
  public Str link(String s) {
    write(s);
    formattings.add(LINK);
    return this;
  }

  @Override
  public Str link(String s, String url) {
    write(s);
    formattings.add(LINK + " " + url);
    return this;
  }


  @Override
  public Str newline() {
    write(NEWLINE);
    return this;
  }

  public InsertTextRequest getInsertTextRequest() {
    return new InsertTextRequest().setText(getText()).setLocation(new Location().setIndex(startIndex));
  }

  public List<UpdateTextStyleRequest> getUpdateTextStyleRequests() {
    List<UpdateTextStyleRequest> requests = new ArrayList<>();
    for (String formating : formattings) {
      if (BOLD.equals(formating)) {
        LOGGER.info("Bold: {}-{}: {}", getStartIndex(), getEndIndex(), getText());
        requests.add(new UpdateTextStyleRequest().setTextStyle(new TextStyle().setBold(true)).setFields(formating));
      } else if (ITALIC.equals(formating)) {
        LOGGER.info("Italic: {}-{}: {}", getStartIndex(), getEndIndex(), getText());
        requests.add(new UpdateTextStyleRequest().setTextStyle(new TextStyle().setItalic(true)).setFields(formating));
      } else if (UNDERLINED.equals(formating)) {
        LOGGER.info("Underlined: {}-{}: {}", getStartIndex(), getEndIndex(), getText());
        requests.add(new UpdateTextStyleRequest().setTextStyle(new TextStyle().setUnderline(true)).setFields(formating));
      } else if (LINK.equals(formating)) {
        LOGGER.info("Link: {}-{}: {}", getStartIndex(), getEndIndex(), getText());
        requests.add(new UpdateTextStyleRequest().setTextStyle(new TextStyle().setLink(new Link().setUrl(getText()))).setFields(formating));
      } else if (formating.startsWith(LINK)) {
        LOGGER.info("Link: {}-{}: {}", getStartIndex(), getEndIndex(), getText());
        requests.add(new UpdateTextStyleRequest().setTextStyle(new TextStyle().setLink(new Link().setUrl(formating.substring(LINK.length() + 1).trim()))).setFields(LINK));
      }

    }

    return requests.stream().map(r -> r.setRange(new Range().setStartIndex(startIndex).setEndIndex(getEndIndex()))).collect(Collectors.toList());
  }
}
