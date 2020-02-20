package io.snowdrop.google.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.services.docs.v1.model.CreateParagraphBulletsRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.UpdateTextStyleRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentBuilder implements IText<ContentBuilder>, IBullets<ContentBuilder> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentBuilder.class);

  private int startIndex=1;
  private List<Line> lines = new ArrayList<>();
  private Line current = new Line(startIndex);

  public int getStartIndex() {
    return startIndex;
  }

  public String getText() {
    return lines.stream().map(Line::getText).collect(Collectors.joining());
  }

  public int getEndIndex() {
    return lines.stream().mapToInt(Line::getEndIndex).max().orElse(startIndex);
  }

  @Override
  public ContentBuilder tab() {
     current.tab();
     return this;
  }

  @Override
  public ContentBuilder tab(int num) {
    current.tab(num);
    return this;
  }

  @Override
  public ContentBuilder write(String s) {
    current.write(s);
    return this;
  }

  @Override
  public ContentBuilder bold(String s) {
    current.bold(s);
    return this;
  }

  @Override
  public ContentBuilder italic(String s) {
    current.italic(s);
    return this;
  }

  @Override
  public ContentBuilder underline(String s) {
    current.underline(s);
    return this;
  }

  @Override
  public ContentBuilder link(String s) {
    current.link(s);
    return this;
  }

  @Override
  public ContentBuilder link(String s, String url) {
    current.link(s, url);
    return this;
  }


  @Override
  public ContentBuilder newline() {
    lines.add(current.newline());
    current = new Line(getEndIndex());
    return this;
  }

  public ContentBuilder bulletsOn() {
    current.bulletsOn();
    return this;
  }

  public ContentBuilder bulletsOff() {
    current.bulletsOff();
    return this;
  }

  public List<InsertTextRequest> getInsertTextRequests() {
    return lines.stream().map(l -> l.getInsertTextRequest()).collect(Collectors.toList());
  }

  public List<CreateParagraphBulletsRequest> getParagrahBulletsRequests() {
    List<CreateParagraphBulletsRequest> requests = new ArrayList<CreateParagraphBulletsRequest>();
    int bulletStartIndex = 1;
    Line previous = null;
    for (Line line : lines) {
      if (line.isBulletsOn()) {
        bulletStartIndex = line.getStartIndex();
      } else if (line.isBulletsOff()) {
        LOGGER.info("Bullets: {} - {}.", bulletStartIndex, previous.getStartIndex());
        requests.add(new CreateParagraphBulletsRequest().setRange(new Range().setStartIndex(bulletStartIndex + 1).setEndIndex(previous.getStartIndex())).setBulletPreset("BULLET_ARROW_DIAMOND_DISC"));
      }
      previous = line;
    }
    return requests;
  }

  public List<UpdateTextStyleRequest> getUpdateTextStyleRequests() {
    return lines.stream().flatMap(l -> l.getUpdateTextStyleRequests().stream()).collect(Collectors.toList());
  }

  public List<Request> getAllRequests() {
    List<Request> requests = new ArrayList<>();
    for (InsertTextRequest insertTextRequest : getInsertTextRequests()) {
      requests.add(new Request().setInsertText(insertTextRequest));
    }
    //Style
    for (UpdateTextStyleRequest styleRequest : getUpdateTextStyleRequests()) {
      requests.add(new Request().setUpdateTextStyle(styleRequest));
    }
    //Bullets
    for (CreateParagraphBulletsRequest bulletRequest : getParagrahBulletsRequests()) {
      requests.add(new Request().setCreateParagraphBullets(bulletRequest));
    }
    return requests;
  }
}
