package org.folio.rest.model;


import java.io.Serializable;
import java.util.Objects;

public class FolioLogin implements Serializable {

  private static final long serialVersionUID = 6377886804058840280L;
  private String username;
  private String xOkapiToken;
  private String refreshToken;

  @Override
  public String toString() {
    return "FolioLogin{" +
      "username='" + username + '\'' +
      ", xOkapiToken='" + xOkapiToken + '\'' +
      ", refreshToken='" + refreshToken + '\'' +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FolioLogin that = (FolioLogin) o;
    return Objects.equals(username, that.username) &&
      Objects.equals(xOkapiToken, that.xOkapiToken) &&
      Objects.equals(refreshToken, that.refreshToken);
  }

  @Override
  public int hashCode() {

    return Objects.hash(username, xOkapiToken, refreshToken);
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getxOkapiToken() {
    return xOkapiToken;
  }

  public void setxOkapiToken(String xOkapiToken) {
    this.xOkapiToken = xOkapiToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public FolioLogin(String username, String xOkapiToken, String refreshToken) {
    this.username = username;
    this.xOkapiToken = xOkapiToken;
    this.refreshToken = refreshToken;
  }
}
