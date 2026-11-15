package ru.urfu.music.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.SequenceGenerator;
import org.hibernate.proxy.HibernateProxy;

@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_seq")
    @SequenceGenerator(name = "entity_seq", sequenceName = "entity_seq", allocationSize = 1)
    private Long id;

    public Long getId() {
        return id;
    }

    private static Class<?> entityClass(Object entity) {
        return entity instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass() : entity.getClass();
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other != null && entityClass(this) == entityClass(other)
                && getId() != null && getId().equals(((BaseEntity) other).getId());
    }

    @Override
    public final int hashCode() {
        return entityClass(this).hashCode();
    }
}
