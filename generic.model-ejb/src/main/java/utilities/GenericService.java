package utilities;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public abstract class GenericService<T extends EntityBone> {
	private Class<T> entityClass;

	@PersistenceContext
	private EntityManager em;

	public GenericService(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	public T create(final T entity) throws EntityExistsException, IllegalStateException, IllegalArgumentException,
			TransactionRequiredException {
		em.persist(entity);
		em.flush();
		return entity;
	}

	public T read(final Serializable primaryKey) throws IllegalStateException, IllegalArgumentException {
		return em.find(entityClass, primaryKey);
	}

	public Boolean update(final T entity)
			throws IllegalStateException, IllegalArgumentException, TransactionRequiredException {
		em.merge(entity);
		em.flush();
		return true;
	}

	public Boolean delete(final T entity)
			throws IllegalStateException, IllegalArgumentException, TransactionRequiredException, PersistenceException {
		em.remove(em.contains(entity) ? entity : em.merge(entity));
		;
		em.flush();
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<T> findAll() {
		CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
		cq.select(cq.from(entityClass));
		return em.createQuery(cq).getResultList();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<T> findRange(int index, int maxResult) {
		CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
		cq.select(cq.from(entityClass));
		Query q = em.createQuery(cq).setFirstResult(((index - 1) * maxResult)).setMaxResults(maxResult);

		return q.getResultList();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int count() {
		CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
		Root<T> rt = cq.from(entityClass);
		cq.select(em.getCriteriaBuilder().count(rt));
		Query q = em.createQuery(cq);
		return ((Long) q.getSingleResult()).intValue();
	}

	// Using the unchecked because JPA does not have a
	// ery.getSingleResult()<T> method
	@SuppressWarnings("unchecked")
	protected T findOneResult(String namedQuery, Map<String, Object> parameters) {
		T result = null;
		try {
			Query query = em.createNamedQuery(namedQuery);
			// Method that will populate parameters if they are passed not null
			// and empty
			if (parameters != null && !parameters.isEmpty()) {
				populateQueryParameters(query, parameters);
			}
			result = (T) query.getSingleResult();
		} catch (Exception e) {
			System.out.println("Error while running query: " + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	private void populateQueryParameters(Query query, Map<String, Object> parameters) {
		for (Entry<String, Object> entry : parameters.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
	}

	// Search by SearchCriteria
	public List<T> search(List<SearchCriteria> params) {
		// CriteriaBuilder creation
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<T> query = builder.createQuery(entityClass);
		Root<T> r = query.from(entityClass);
		// create predicate object
		Predicate predicate = builder.conjunction();
		// get searchCriteria class to create condition for our search query
		for (SearchCriteria param : params) {
			if (param.getOperation().equalsIgnoreCase(">")) {
				predicate = builder.and(predicate,
						builder.greaterThanOrEqualTo(r.get(param.getKey()), param.getValue().toString()));
			} else if (param.getOperation().equalsIgnoreCase("<")) {
				predicate = builder.and(predicate,
						builder.lessThanOrEqualTo(r.get(param.getKey()), param.getValue().toString()));
			} else if (param.getOperation().equalsIgnoreCase(":")) {
				if (r.get(param.getKey()).getJavaType() == String.class) {
					predicate = builder.and(predicate,
							builder.like(r.get(param.getKey()), "%" + param.getValue() + "%"));
				} else {
					predicate = builder.and(predicate, builder.equal(r.get(param.getKey()), param.getValue()));
				}
			}
		}
		query.where(predicate);
		// create our query and do the search
		List<T> result = em.createQuery(query).getResultList();
		return result;
	}

	// search by paginateur of 10
	public List<T> paginateur(int id) {

		int pageNumber = id;
		int pageSize = 10;
		if (pageNumber != 1) {
			pageNumber = (id - 1) * pageSize;
		}
		// create criteria builder from entity manager
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		// create query for to get the number of object instance in the database
		CriteriaQuery<Long> countQ = criteriaBuilder.createQuery(Long.class);
		countQ.select(criteriaBuilder.count(countQ.from(entityClass)));
		Long count = em.createQuery(countQ).getSingleResult();
		// create criteriaquery select from the object class
		CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
		Root<T> from = criteriaQuery.from(entityClass);
		CriteriaQuery<T> select = criteriaQuery.select(from);
		// critera with condition
		TypedQuery<T> typedQuery = em.createQuery(select);
		while (pageNumber < count.intValue()) {
			typedQuery.setFirstResult(pageNumber - 1);
			typedQuery.setMaxResults(pageSize);
			System.out.println("Current page: " + id);
			pageNumber += pageSize;
		}
		List<T> ResultList = typedQuery.getResultList();
		System.out.println(pageNumber);
		return ResultList;
	}

}
