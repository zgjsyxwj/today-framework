package cn.taketoday.jdbc;

/**
 * Created by zsoltjanos on 01/08/15.
 */
public interface UserInserter {

  public void insertUser(Query insertQuery, int idx);
}
