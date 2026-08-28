package gr.aueb.cf.schoolapp.validator;

import gr.aueb.cf.schoolapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.schoolapp.dto.UserEditDTO;
import gr.aueb.cf.schoolapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEditValidator implements Validator {
    private final UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserEditDTO.class == clazz;
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserEditDTO userEditDTO = (UserEditDTO) target;

        try {
            // 1. Φέρνουμε τον παλιό χρήστη με βάση το UUID
            UserEditDTO savedUser = userService.getUserByUuidAndDeletedFalse(userEditDTO.uuid());

            // 2. Αν ο παλιός χρήστης βρέθηκε ΚΑΙ το username του είναι διαφορετικό από το νέο
            if (savedUser != null && !savedUser.username().equals(userEditDTO.username())) {

                // 3. Ελέγχουμε αν το νέο username είναι ήδη πιασμένο
                if (userService.isUserExistsByUsername(userEditDTO.username())) {
                    log.warn("Update failed. User with username={} already exists", userEditDTO.username());
                    errors.rejectValue("username", "username.user.exists");
                }
            }
        } catch (EntityNotFoundException e) {
            // Αν το UUID δε βρεθεί καν, χτυπάμε λάθος στο UUID
            log.warn("Update failed. User with uuid={} not found", userEditDTO.uuid());
            errors.rejectValue("uuid", "uuid.user.notfound");
        }
    }
}
