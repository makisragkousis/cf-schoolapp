package gr.aueb.cf.schoolapp.service;

import gr.aueb.cf.schoolapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.schoolapp.dto.UserEditDTO;
import gr.aueb.cf.schoolapp.dto.UserInsertDTO;
import gr.aueb.cf.schoolapp.dto.UserReadOnlyDTO;
import gr.aueb.cf.schoolapp.mapper.Mapper;
import gr.aueb.cf.schoolapp.model.Role;
import gr.aueb.cf.schoolapp.model.User;
import gr.aueb.cf.schoolapp.repository.RoleRepository;
import gr.aueb.cf.schoolapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final Mapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(rollbackFor = { EntityAlreadyExistsException.class, EntityInvalidArgumentException.class })
    public UserReadOnlyDTO saveUser(UserInsertDTO userInsertDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException {
        try {
            if (userRepository.findByUsername(userInsertDTO.username()).isPresent()) {
                throw new EntityAlreadyExistsException("User with username=" + userInsertDTO.username() + " already exists");
            }
            User user = mapper.mapToUserEntity(userInsertDTO);
            user.setPassword(passwordEncoder.encode(userInsertDTO.password()));
            Role role = roleRepository.findById(userInsertDTO.roleId())
                    .orElseThrow(() -> new EntityInvalidArgumentException("Role id=" + userInsertDTO.roleId() + " invalid"));
            role.addUser(user);
            userRepository.save(user);
            log.info("Save succeeded for user with username={}.", userInsertDTO.username());
            return mapper.mapToUserReadOnlyDTO(user);
        } catch (EntityAlreadyExistsException e) {
            log.error("Save failed. User with username={} already exists", userInsertDTO.username());
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.error("Save failed. Invalid arguments for user with username={}", userInsertDTO.username());
            throw e;
        }
    }

    @Override
    public UserEditDTO getUserByUuidAndDeletedFalse(UUID uuid)
            throws EntityNotFoundException {

        try {
            User user = userRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("User with uuid= " + uuid + "not found."));
            log.debug("Found user with username={} successfully.", uuid);
            return mapper.mapToUserEditDTO(user);
        } catch (EntityNotFoundException e) {
            log.warn("User with uuid={} not found.", uuid);
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('EDIT_USERS')")
    @Transactional(rollbackFor = {EntityNotFoundException.class, EntityInvalidArgumentException.class,
            EntityAlreadyExistsException.class})
    public UserReadOnlyDTO updateUser(UserEditDTO editDTO)
            throws EntityAlreadyExistsException, EntityInvalidArgumentException, EntityNotFoundException {

        try {
            User user = userRepository.findByUuidAndDeletedFalse(editDTO.uuid())
                    .orElseThrow(() -> new EntityNotFoundException("User with uuid= " + editDTO.uuid() + " not found."));

            if (!Objects.equals(user.getUsername(), editDTO.username())) {
                if (isUserExistsByUsername(editDTO.username())) {
                    throw new EntityAlreadyExistsException("User with username= " + editDTO.username() + " already exists.");
                }
            }

            user.setUsername(editDTO.username());

            if (!Objects.equals(user.getRole().getId(), editDTO.roleID())) {
                Role role = roleRepository.findById(editDTO.roleID())
                        .orElseThrow(() -> new EntityInvalidArgumentException("Role id= " + editDTO.roleID() + " not found"));

                Role oldRole = user.getRole();

                if (oldRole != null) {
                    oldRole.removeUser(user);
                }

                role.addUser(user);
            }

            userRepository.save(user);
            log.info("User with uuid={} updated successfully", editDTO.uuid());
            return mapper.mapToUserReadOnlyDTO(user);

        } catch (EntityNotFoundException e) {
            log.warn("Update failed for user with uuid={}. Uuid not found.", editDTO.uuid());
            throw e;
        } catch (EntityAlreadyExistsException e) {
            log.warn("Update failed for user with username={}. User already exists.", editDTO.username());
            throw e;
        } catch (EntityInvalidArgumentException e) {
            log.warn("Update failed for user with roleId={}. RoleId not found.", editDTO.roleID());
            throw e;
        }
    }

    @Override
    @PreAuthorize("hasAuthority('DELETE_USERS')")
    @Transactional(rollbackFor = {EntityNotFoundException.class})
    public UserReadOnlyDTO deleteUserByUuid(UUID uuid)
            throws EntityNotFoundException {

        try {
            User user = userRepository.findByUuidAndDeletedFalse(uuid)
                    .orElseThrow(() -> new EntityNotFoundException("User with uuid= " + uuid + " not found."));

            user.softDelete();

            log.info("User with uuid={}, deleted successfully.", uuid);
            return mapper.mapToUserReadOnlyDTO(user);

        } catch (EntityNotFoundException e) {
            log.warn("Deletion failed for user with uuid={}.", uuid);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUserExistsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('VIEW_USERS')")
    public Page<UserReadOnlyDTO> getPaginatedUsersDeletedFalse(Pageable pageable) {
        Page<User> usersPage = userRepository.findAllByDeletedFalse(pageable);
        log.debug("Get paginated user not deleted returned successfully page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());
        return usersPage.map(mapper::mapToUserReadOnlyDTO);
    }
}
