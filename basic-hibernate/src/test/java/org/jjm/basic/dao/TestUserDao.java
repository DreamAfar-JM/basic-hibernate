package org.jjm.basic.dao;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jjm.basic.model.Pager;
import org.jjm.basic.model.SystemContext;
import org.jjm.basic.model.User;
import org.jjm.basic.test.util.AbstractDbUnitTestCase;
import org.jjm.basic.test.util.EntitiesHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.orm.hibernate4.SessionHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/beans.xml")
// @TestExecutionListeners({DbUnitTestExecutionListener.class,DependencyInjectionTestExecutionListener.class})
public class TestUserDao extends AbstractDbUnitTestCase {

	@Inject
	private SessionFactory sessionFactory;
	@Inject
	private IUserDao userDao;

	@Before
	public void setUp() throws DataSetException, SQLException, IOException {
		Session session = sessionFactory.openSession();
		TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(session));
		this.backupAllTable();
	}

	@Test
	// @DatabaseSetup("/t_user.xml")
	public void testLoad() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);
		User user = userDao.load(1);
		System.out.println(user.getUsername());
		EntitiesHelper.assertUser(user);
	}

	@Test(expected = ObjectNotFoundException.class)
	public void testDelete() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);
		userDao.delete(1);
		User u = userDao.load(1);
		System.out.println(u.getUsername());
	}

	@Test
	public void testListByArgs() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);

		SystemContext.setOrder("desc");
		SystemContext.setSort("id");
		List<User> actualList = userDao.list(" from User where id>? and id<? ", new Object[] { 1, 4 });
		List<User> expectedList = Arrays.asList(new User(3, "admin3"), new User(2, "admin2"));

		EntitiesHelper.assertUsers(expectedList, actualList);
	}

	@Test
	public void testListByArgsAndAlias() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);

		SystemContext.setOrder("asc");
		SystemContext.setSort("id");
		HashMap<String, Object> alias = new HashMap<String, Object>();
		alias.put("ids", Arrays.asList(1, 2, 3, 5, 6, 8, 9, 10));
		List<User> actualList = userDao.list(" from User where id>? and id<? and id in(:ids)", new Object[] { 1, 5 },
				alias);
		List<User> expectedList = Arrays.asList(new User(2, "admin2"), new User(3, "admin3"));
		assertNotNull(actualList);
		assertTrue(actualList.size() == 2);
		EntitiesHelper.assertUsers(expectedList, actualList);
	}

	@Test
	public void testFindByArgs() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);

		SystemContext.setOrder("desc");
		SystemContext.setSort("id");
		SystemContext.setPageSize(3);
		SystemContext.setPageOffset(0);
		Pager<User> actuals = userDao.find(" from User where id>=? and id<=? ", new Object[] { 1, 10 });
		List<User> expected = Arrays.asList(new User(10, "admin10"), new User(9, "admin9"), new User(8, "admin8"));
		assertNotNull(actuals);
		assertTrue(actuals.getTotal() == 10);
		assertTrue(actuals.getSize() == 3);
		assertTrue(actuals.getOffset() == 0);
		EntitiesHelper.assertUsers(actuals.getDatas(), expected);
	}

	@Test
	public void testFindByArgsAndAlias() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);

		SystemContext.setOrder("asc");
		SystemContext.setSort("id");
		SystemContext.setPageSize(3);
		SystemContext.setPageOffset(0);
		HashMap<String, Object> alias = new HashMap<String, Object>();
		alias.put("ids", Arrays.asList(1, 2, 4, 5, 6, 8, 10));
		Pager<User> actuals = userDao.find(" from User where id>=? and id<=? and id in(:ids) ", new Object[] { 1, 10 },
				alias);
		List<User> expected = Arrays.asList(new User(1, "admin1"), new User(2, "admin2"), new User(4, "admin4"));
		assertNotNull(actuals);
		assertTrue(actuals.getTotal() == 7);
		assertTrue(actuals.getSize() == 3);
		assertTrue(actuals.getOffset() == 0);
		EntitiesHelper.assertUsers(actuals.getDatas(), expected);
	}

	@Test
	public void testListSQLByArgs() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);

		SystemContext.setOrder("desc");
		SystemContext.setSort("id");
		List<User> actuals = userDao.listBySql(" select * from t_user where id>? and id<? ", new Object[] { 1, 4 },User.class,true);
		List<User> expected = Arrays.asList(new User(3, "admin3"), new User(2, "admin2"));
		assertNotNull(actuals);
		assertTrue(actuals.size()==2);
		EntitiesHelper.assertUsers(actuals, expected);
	}

	@Test
	public void testListSQLByArgsAndAlias() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);

		SystemContext.setOrder("asc");
		SystemContext.setSort("id");
		HashMap<String, Object> alias = new HashMap<String, Object>();
		alias.put("ids", Arrays.asList(1, 2, 4, 5, 6, 8, 10));
		List<User> actuals = userDao.listBySql(" select * from t_user where id>=? and id<? and id in(:ids) ", new Object[] { 1, 5 },
				alias,User.class,true);
		List<User> expected = Arrays.asList(new User(1, "admin1"), new User(2, "admin2"), new User(4, "admin4"));
		assertNotNull(actuals);
		EntitiesHelper.assertUsers(actuals, expected);
	}
	
	@Test
	public void testFindSQLByArgs() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);

		SystemContext.setOrder("desc");
		SystemContext.setSort("id");
		SystemContext.setPageSize(3);
		SystemContext.setPageOffset(0);
		Pager<User> actuals = userDao.findBySql(" select * from t_user where id>=? and id<=? ", new Object[] { 1, 10 },User.class,true);
		List<User> expected = Arrays.asList(new User(10, "admin10"), new User(9, "admin9"), new User(8, "admin8"));
		assertNotNull(actuals);
		assertTrue(actuals.getTotal() == 10);
		assertTrue(actuals.getSize() == 3);
		assertTrue(actuals.getOffset() == 0);
		EntitiesHelper.assertUsers(actuals.getDatas(), expected);
	}

	@Test
	public void testFindSQLByArgsAndAlias() throws DatabaseUnitException, SQLException {
		IDataSet ds = createDateSet("t_user");
		DatabaseOperation.CLEAN_INSERT.execute(dbunitCon, ds);

		SystemContext.setOrder("asc");
		SystemContext.setSort("id");
		SystemContext.setPageSize(3);
		SystemContext.setPageOffset(0);
		HashMap<String, Object> alias = new HashMap<String, Object>();
		alias.put("ids", Arrays.asList(1, 2, 4, 5, 6, 8, 10));
		Pager<User> actuals = userDao.findBySql(" select * from t_user where id>=? and id<=? and id in(:ids) ", new Object[] { 1, 10 },
				alias,User.class,true);
		List<User> expected = Arrays.asList(new User(1, "admin1"), new User(2, "admin2"), new User(4, "admin4"));
		assertNotNull(actuals);
		assertTrue(actuals.getTotal() == 7);
		assertTrue(actuals.getSize() == 3);
		assertTrue(actuals.getOffset() == 0);
		EntitiesHelper.assertUsers(actuals.getDatas(), expected);
	}

	
	@After
	public void tearDown() throws FileNotFoundException, DatabaseUnitException, SQLException {
		SessionHolder sessionHolder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
		Session session = sessionHolder.getSession();
		session.flush();
		TransactionSynchronizationManager.unbindResource(sessionFactory);
		this.resumeTable();
	}
}
