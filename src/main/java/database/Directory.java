package database;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/** Describes the table of directories. */
@Entity(name="directories")
public final class Directory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;

    public String path;
    public Long userId;
}