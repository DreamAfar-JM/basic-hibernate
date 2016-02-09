/**
 * 
 */
package org.jjm.basic.dao;

import java.lang.reflect.ParameterizedType;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.jjm.basic.model.Pager;
import org.jjm.basic.model.SystemContext;

/**
 * @ClassName: BaseDao
 * @Description:
 * @author: 蒋金敏
 * @date: 2016年2月6日 下午7:48:02
 * @version: V1.0
 */
@SuppressWarnings("unchecked")
public class BaseDao<T> implements IBaseDao<T> {

	private SessionFactory sessionFactory;

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Inject
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 创建一个Class的对象来获取泛型的class
	 */
	private Class<T> clz;

	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	public Class<T> getClz() {
		if (clz == null) {
			// 获取泛型的Class对象
			clz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
		}
		return clz;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#add(java.lang.Object)
	 */
	@Override
	public T add(T t) {
		getSession().save(t);
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#update(java.lang.Object)
	 */
	@Override
	public void update(T t) {
		getSession().update(t);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#delete(int)
	 */
	@Override
	public void delete(int id) {
		getSession().delete(this.load(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#load(int)
	 */
	@Override
	public T load(int id) {
		return (T) getSession().load(getClz(), id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#list(java.lang.String, java.lang.Object[])
	 */
	@Override
	public List<T> list(String hql, Object[] args) {
		return this.list(hql, args, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#list(java.lang.String, java.lang.Object)
	 */
	@Override
	public List<T> list(String hql, Object arg) {
		return this.list(hql, new Object[] { arg });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#list(java.lang.String)
	 */
	@Override
	public List<T> list(String hql) {
		return this.list(hql, null);
	}

	private String initSort(String hql) {
		// 处理排序
		String order = SystemContext.getOrder();
		String sort = SystemContext.getSort();
		if (sort != null && !"".equals(sort.trim())) {
			hql += " order by " + sort;
			if (!"desc".equals(order)) {
				hql += " asc ";
			} else {
				hql += " desc ";
			}
		}
		return hql;
	}

	@SuppressWarnings("rawtypes")
	private void setAliasParameter(Query query, Map<String, Object> alias) {
		// 处理别名
		if (alias != null) {
			Set<String> keySet = alias.keySet();
			for (String key : keySet) {
				Object val = alias.get(key);
				if (val instanceof Collection) {
					// 查询条件是列表
					query.setParameterList(key, (Collection) val);
				} else {
					query.setParameter(key, val);
				}
			}
		}
	}

	private void setParameter(Query query, Object[] args) {
		// 处理查询数据
		if (args != null && args.length > 0) {
			int index = 0;
			for (Object arg : args) {
				query.setParameter(index++, arg);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#list(java.lang.String, java.lang.Object[], java.util.Map)
	 */
	@Override
	public List<T> list(String hql, Object[] args, Map<String, Object> alias) {
		// 处理排序
		hql = initSort(hql);
		Query query = getSession().createQuery(hql);
		// 处理别名
		setAliasParameter(query, alias);
		// 处理查询数据
		setParameter(query, args);

		return query.list();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#list(java.lang.String, java.util.Map)
	 */
	@Override
	public List<T> listByAlias(String hql, Map<String, Object> alias) {
		return this.list(hql, null, alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#find(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Pager<T> find(String hql, Object[] args) {
		return this.find(hql, args, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#find(java.lang.String, java.lang.Object)
	 */
	@Override
	public Pager<T> find(String hql, Object arg) {
		return this.find(hql, new Object[] { arg });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#find(java.lang.String)
	 */
	@Override
	public Pager<T> find(String hql) {
		return this.find(hql, null);
	}

	@SuppressWarnings("rawtypes")
	private void setPagers(Query query, Pager pages) {
		Integer pageSize = SystemContext.getPageSize();
		Integer pageOffset = SystemContext.getPageOffset();
		if (pageOffset == null || pageOffset < 0) {
			pageOffset = 0;
		}
		if (pageSize == null || pageSize < 0) {
			pageSize = 15;
		}
		pages.setOffset(pageOffset);
		pages.setSize(pageSize);
		query.setFirstResult(pageOffset).setMaxResults(pageSize);
	}

	private String getCountHql(String hql, boolean isHql) {
		String end = hql.substring(hql.indexOf("from"));
		String c = "select count(*) " + end;
		if (isHql) {
			c.replaceAll("fetch", "");
		}
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#find(java.lang.String, java.lang.Object[], java.util.Map)
	 */
	@Override
	public Pager<T> find(String hql, Object[] args, Map<String, Object> alias) {
		// 处理排序
		String cq = getCountHql(hql, true);
		hql = initSort(hql);
		cq = initSort(cq);
		Query cquery = getSession().createQuery(cq);
		Query query = getSession().createQuery(hql);
		// 设置别名参数
		setAliasParameter(cquery, alias);
		setAliasParameter(query, alias);
		// 设置参数
		setParameter(cquery, args);
		setParameter(query, args);

		// 设置分页信息
		Pager<T> pages = new Pager<T>();
		setPagers(query, pages);

		// 查询数据
		List<T> datas = query.list();
		pages.setDatas(datas);

		// 设置total
//		long total = ((BigInteger) cquery.uniqueResult()).longValue();
		long total = (Long) cquery.uniqueResult();
		pages.setTotal(total);

		return pages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#find(java.lang.String, java.util.Map)
	 */
	@Override
	public Pager<T> findByAlias(String hql, Map<String, Object> alias) {
		return this.find(hql, null, alias);
	}

	@Override
	public Object queryObject(String hql, Object[] args, Map<String, Object> alias) {
		Query query = getSession().createQuery(hql);
		setAliasParameter(query, alias);
		setParameter(query, args);

		return query.uniqueResult();
	}

	@Override
	public Object queryObjectByAlias(String hql, Map<String, Object> alias) {
		return this.queryObject(hql, null, alias);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#queryObject(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Object queryObject(String hql, Object[] args) {
		return this.queryObject(hql, args, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#queryObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object queryObject(String hql, Object arg) {
		return this.queryObject(hql, new Object[] { arg });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#queryObject(java.lang.String)
	 */
	@Override
	public Object queryObject(String hql) {
		return this.queryObject(hql, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#updateByHql(java.lang.String, java.lang.Object[])
	 */
	@Override
	public void updateByHql(String hql, Object[] args) {
		Query query = getSession().createQuery(hql);
		setParameter(query, args);
		query.executeUpdate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#updateByHql(java.lang.String, java.lang.Object)
	 */
	@Override
	public void updateByHql(String hql, Object arg) {
		this.updateByHql(hql, new Object[] { arg });
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#updateByHql(java.lang.String)
	 */
	@Override
	public void updateByHql(String hql) {
		this.updateByHql(hql, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#listBySql(java.lang.String, java.lang.Object[], java.util.Map, java.lang.Class,
	 * boolean)
	 */
	@Override
	public <N extends Object>List<N> listBySql(String sql, Object[] args, Map<String, Object> alias, Class<? extends Object> clz,
			boolean hasEntity) {
		sql = initSort(sql);
		SQLQuery sqlQuery = getSession().createSQLQuery(sql);
		setAliasParameter(sqlQuery, alias);
		setParameter(sqlQuery, args);
		if (hasEntity) {
			sqlQuery.addEntity(clz);
		} else {
			sqlQuery.setResultTransformer(Transformers.aliasToBean(clz));
		}

		return sqlQuery.list();
	}

	@Override
	public <N extends Object>List<N> listBySql(String sql, Object[] args, Class<? extends Object> clz, boolean hasEntity) {
		return this.listBySql(sql, args, null, clz, hasEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#listBySql(java.lang.String, java.lang.Object, java.lang.Class, boolean)
	 */
	@Override
	public <N extends Object>List<N> listBySql(String sql, Object arg, Class<? extends Object> clz, boolean hasEntity) {
		return this.listBySql(sql, new Object[] { arg }, clz, hasEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#listBySql(java.lang.String, java.lang.Class, boolean)
	 */
	@Override
	public <N extends Object>List<N> listBySql(String sql, Class<? extends Object> clz, boolean hasEntity) {
		return this.listBySql(sql, null, clz, hasEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#listBySql(java.lang.String, java.util.Map, java.lang.Class, boolean)
	 */
	@Override
	public <N extends Object>List<N> listByAliasSql(String sql, Map<String, Object> alias, Class<? extends Object> clz, boolean hasEntity) {
		return this.listBySql(sql, null, alias, clz, hasEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#findBySql(java.lang.String, java.lang.Object[], java.util.Map, java.lang.Class,
	 * boolean)
	 */
	@Override
	public <N extends Object>Pager<N> findBySql(String sql, Object[] args, Map<String, Object> alias, Class<? extends Object> clz,
			boolean hasEntity) {
		String cq = getCountHql(sql, false);
		cq = initSort(cq);
		sql = initSort(sql);

		SQLQuery csqlQuery = getSession().createSQLQuery(cq);
		SQLQuery sqlQuery = getSession().createSQLQuery(sql);

		setAliasParameter(csqlQuery, alias);
		setAliasParameter(sqlQuery, alias);

		setParameter(csqlQuery, args);
		setParameter(sqlQuery, args);

		Pager<N> pages = new Pager<N>();
		setPagers(sqlQuery, pages);
		if (hasEntity) {
			sqlQuery.addEntity(clz);
		} else {
			sqlQuery.setResultTransformer(Transformers.aliasToBean(clz));
		}

		List<N> datas = sqlQuery.list();
		pages.setDatas(datas);
		long total = ((BigInteger) csqlQuery.uniqueResult()).longValue();
		pages.setTotal(total);

		return pages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#findBySql(java.lang.String, java.util.Map, java.lang.Class, boolean)
	 */
	@Override
	public <N extends Object>Pager<N> findByAliasSql(String sql, Map<String, Object> alias, Class<? extends Object> clz, boolean hasEntity) {
		return this.findBySql(sql, null, alias, clz, hasEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#findBySql(java.lang.String, java.lang.Object[], java.lang.Class, boolean)
	 */
	@Override
	public <N extends Object>Pager<N> findBySql(String sql, Object[] args, Class<? extends Object> clz, boolean hasEntity) {

		return this.findBySql(sql, args, null, clz, hasEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#findBySql(java.lang.String, java.lang.Object, java.lang.Class, boolean)
	 */
	@Override
	public <N extends Object>Pager<N> findBySql(String sql, Object arg, Class<? extends Object> clz, boolean hasEntity) {
		return this.findBySql(sql, new Object[] { arg }, clz, hasEntity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jjm.basic.dao.IBaseDao#findBySql(java.lang.String, java.lang.Class, boolean)
	 */
	@Override
	public <N extends Object>Pager<N> findBySql(String sql, Class<? extends Object> clz, boolean hasEntity) {
		return this.findBySql(sql, null, clz, hasEntity);
	}

}
