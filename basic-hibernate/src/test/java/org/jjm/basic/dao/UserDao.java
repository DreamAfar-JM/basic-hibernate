package org.jjm.basic.dao;

import org.jjm.basic.model.User;
import org.springframework.stereotype.Repository;

@Repository("userDao")
public class UserDao extends BaseDao<User> implements IUserDao {

}
