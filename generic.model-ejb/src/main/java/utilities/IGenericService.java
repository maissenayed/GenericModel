package utilities;

import java.io.Serializable;

import javax.persistence.EntityExistsException;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;

public interface IGenericService<T> {
	T create(T entity) throws EntityExistsException, IllegalStateException,
	IllegalArgumentException, TransactionRequiredException;
	
    T read(Serializable primaryKey) throws IllegalStateException,
	IllegalArgumentException;

Boolean update(T entity) throws IllegalStateException,
	IllegalArgumentException, TransactionRequiredException;

Boolean delete(T entity) throws IllegalStateException,
	IllegalArgumentException, TransactionRequiredException,
	PersistenceException;

}
