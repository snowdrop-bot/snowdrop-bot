package io.snowdrop.google.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.UpdateTextStyleRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Line implements IText<Line>, IBullets<Line> {

  private static final Logger LOGGER = LoggerFactory.getLogger(Line.class);
  private static final String NEWLINE = "\n";
  private static final String TAB = "\t";

  private int startIndex;
  private List<Str> strings = new ArrayList<>();
  private Str current;

  private boolean ended;

  private boolean bulletsOn;
  private boolean bulletsOff;

  public Line(int startIndex) {
    this.startIndex = startIndex;
    this.current = new Str(startIndex);
  }

  public int getStartIndex() {
    return startIndex;
  }

  public String getText() {
    return strings.stream().map(Str::getText).collect(Collectors.joining());
  }

  public int getEndIndex() {
    return strings.stream().mapToInt(Str::getEndIndex).max().orElse(startIndex);
  }

  @Override
  public Line tab() {
    write(TAB);
    return this;
  }

  @Override
  public Line tab(int num) {
    for (int i=0; i<num; i++) {
      write(TAB);
    }
    return this;
  }

  @Override
  public Line write(String s) {
    strings.add(new Str(getEndIndex()).write(s));
    return this;
  }

  @Override
  public Line bold(String s) {
    strings.add(new Str(getEndIndex()).bold(s));
    return this;
  }

  @Override
  public Line italic(String s) {
    strings.add(new Str(getEndIndex()).italic(s));
    return this;
  }

  @Override
  public Line underline(String s) {
    strings.add(new Str(getEndIndex()).underline(s));
    return this;
  }

  @Override
  public Line link(String s) {
    strings.add(new Str(getEndIndex()).link(s));
    return this;
  }

  @Override
  public Line link(String s, String url) {
    strings.add(new Str(getEndIndex()).link(s, url));
    return this;
  }

  @Override
  public Line newline() {
    strings.add(new Str(getEndIndex()).newline());
    ended = true;
    return this;
  }

  @Override
  public Line bulletsOn() {
    this.bulletsOn = true;
    return this;
  }

  @Override
  public Line bulletsOff() {
    this.bulletsOff = true;
    return this;
  }

  public boolean isBulletsOn() {
    return bulletsOn;
  }

  public boolean isBulletsOff() {
    return bulletsOff;
  }

  public InsertTextRequest getInsertTextRequest() {
    LOGGER.info("{}: {}{}", getStartIndex(), getText().replaceAll(Pattern.quote(NEWLINE), ".").replaceAll(Pattern.quote(TAB), "-"), getEndIndex());
    return new InsertTextRequest().setText(getText()).setLocation(new Location().setIndex(startIndex));
  }

  public List<UpdateTextStyleRequest> getUpdateTextStyleRequests() {
    return strings.stream().flatMap(s -> s.getUpdateTextStyleRequests().stream()).collect(Collectors.toList());
  }

}
