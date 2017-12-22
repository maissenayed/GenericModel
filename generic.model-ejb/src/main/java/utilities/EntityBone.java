package utilities;

import java.io.Serializable;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;


//root class that any JPA entity extends from 
@MappedSuperclass
public abstract class EntityBone implements Serializable{
		@Transient public static final long serialVersionUID = 196919661993L;
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		private int id;
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntityBone other = (EntityBone) obj;
			if (id != other.id)
				return false;
			return true;
		}
	

}
