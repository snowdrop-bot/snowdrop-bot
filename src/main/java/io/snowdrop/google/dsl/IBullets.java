package io.snowdrop.google.dsl;

public interface IBullets<T> {

  /*
   * Turn bullets on.
   *
   * @return
   */
  T bulletsOn();

  /*
   * Turn bullets off
   *
   * @return
   */
  T bulletsOff();
}
