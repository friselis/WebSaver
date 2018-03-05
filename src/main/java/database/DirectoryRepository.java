package database;

import org.springframework.data.jpa.repository.JpaRepository;

/** Provides access to directories data table. */
public interface DirectoryRepository extends JpaRepository<Directory, Long> {

    //@Nullable
    Directory findByUserId(Long userId);
    //Directory findByDirectoryName(String directoryName);

}

