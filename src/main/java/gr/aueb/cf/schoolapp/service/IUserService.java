package gr.aueb.cf.schoolapp.service;

import gr.aueb.cf.schoolapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.schoolapp.dto.UserEditDTO;
import gr.aueb.cf.schoolapp.dto.UserInsertDTO;
import gr.aueb.cf.schoolapp.dto.UserReadOnlyDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IUserService {
    UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
        throws EntityAlreadyExistsException, EntityInvalidArgumentException;

    UserEditDTO getUserByUuidAndDeletedFalse(UUID uuid)
        throws EntityNotFoundException;

    UserReadOnlyDTO updateUser(UserEditDTO editDTO)
        throws EntityAlreadyExistsException, EntityInvalidArgumentException, EntityNotFoundException;

    UserReadOnlyDTO deleteUserByUuid(UUID uuid)
        throws EntityNotFoundException;

    boolean isUserExistsByUsername(String username);

    Page<UserReadOnlyDTO> getPaginatedUsersDeletedFalse(Pageable pageable);
}
