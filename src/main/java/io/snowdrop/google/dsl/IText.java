package io.snowdrop.google.dsl;

public interface IText<T> {

  /*
   * Adds a tab
   *
   * @return the {@link Writable}
   */
  public T tab();

  /*
   * Adds multiple tabs
   *
   * @param num the number of tabs to add
   *
   * @return the {@link Writable}
   */
  public T tab(int num);

  /*
   * Writes a string
   *
   * @param s the string to write
   *
   * @return the {@link Writable}
   */
  public T write(String s);

  /*
   * Writes a string in bold.
   *
   * @param s the string to write
   *
   * @return the {@link Writable}
   */
  public T bold(String s);

  /*
   * Writes a string in italic
   *
   * @param s the string to write
   *
   * @return the {@link Writable}
   */
  public T italic(String s);

  /*
   * Writes a string underlined
   *
   * @param s the string to write
   *
   * @return the {@link Writable}
   */
  public T underline(String s);

  /*
   * Writes a link
   *
   * @param s the string to write
   *
   * @return the {@link Writable}
   */
  public T link(String s);

  /*
   * Writes a link
   *
   * @param s the string to write
   * @param url the lik url
   *
   * @return the {@link Writable}
   */
  public T link(String s, String url);

  /*
   * Writes a newline
   *
   * @return the {@link Writable}
   */
  public T newline();

}
