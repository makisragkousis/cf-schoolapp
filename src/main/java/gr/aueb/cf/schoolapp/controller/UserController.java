package gr.aueb.cf.schoolapp.controller;

import gr.aueb.cf.schoolapp.core.exceptions.EntityAlreadyExistsException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityInvalidArgumentException;
import gr.aueb.cf.schoolapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.schoolapp.dto.RoleReadOnlyDTO;
import gr.aueb.cf.schoolapp.dto.UserEditDTO;
import gr.aueb.cf.schoolapp.dto.UserInsertDTO;
import gr.aueb.cf.schoolapp.dto.UserReadOnlyDTO;
import gr.aueb.cf.schoolapp.service.IRoleService;
import gr.aueb.cf.schoolapp.service.IUserService;
import gr.aueb.cf.schoolapp.validator.UserEditValidator;
import gr.aueb.cf.schoolapp.validator.UserInsertValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final IRoleService roleService;
    private final UserInsertValidator userInsertValidator;
    private final UserEditValidator userEditValidator;

//    public UserController(IUserService userService, IRoleService roleService) {
//        this.userService = userService;
//        this.roleService = roleService;
//    }

    @GetMapping("/register")
    public String getUserForm(Model model) {
        model.addAttribute("userInsertDTO", UserInsertDTO.empty());
        return "user-form";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("userInsertDTO") UserInsertDTO userInsertDTO,
                               BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        userInsertValidator.validate(userInsertDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            return "user-form";
        }

        try {
            UserReadOnlyDTO readOnlyDTO = userService.saveUser(userInsertDTO);
            redirectAttributes.addFlashAttribute("userReadOnlyDTO", readOnlyDTO);
            return "redirect:/users/success";
        } catch (EntityAlreadyExistsException | EntityInvalidArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user-form";
        }
    }

    @GetMapping("/success")
    public String success(Model model) {
        return "user-success";
    }

    @GetMapping({"", "/"})
    public String getPaginatedUsersDeletedFalse(@PageableDefault(page = 0, size = 5, sort = "username") Pageable pageable,
                                                Model model) {
        Page<UserReadOnlyDTO> usersPage = userService.getPaginatedUsersDeletedFalse(pageable);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("page", usersPage);
        return "users";
    }

    @GetMapping("/edit-user/{uuid}")
    public String getUserEdit(@PathVariable UUID uuid, Model model, RedirectAttributes redirectAttributes) {
        try {
            UserEditDTO userEditDTO = userService.getUserByUuidAndDeletedFalse(uuid);
            model.addAttribute("userEditDTO", userEditDTO);
            return "user-edit";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users";
        }
    }

    @PostMapping("/edit-user")
    public String updateUser(@Valid @ModelAttribute UserEditDTO userEditDTO,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes,
                             Model model) {

        userEditValidator.validate(userEditDTO, bindingResult);
        if (bindingResult.hasErrors()) {
            return "user-edit";
        }

        try {
            UserReadOnlyDTO userReadOnlyDTO = userService.updateUser(userEditDTO);
            redirectAttributes.addFlashAttribute("userReadOnlyDTO", userReadOnlyDTO);
            return "redirect:/users/update-success";

        } catch (EntityNotFoundException | EntityAlreadyExistsException | EntityInvalidArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user-edit";
        }
    }

    @PostMapping("/delete-user/{uuid}")
    public String deleteUser(@PathVariable UUID uuid, RedirectAttributes redirectAttributes) {
        try {
            UserReadOnlyDTO userReadOnlyDTO = userService.deleteUserByUuid(uuid);
            redirectAttributes.addFlashAttribute("userReadOnlyDTO", userReadOnlyDTO);
            return "redirect:/users/delete-user-success";

        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users";
        }
    }

    @GetMapping("/delete-user-success")
    public String deleteUserSuccess(Model model) {
        if (!model.containsAttribute("userReadOnlyDTO")) {
            return "redirect:/users";
        }
        return "delete-user-success";
    }

    @ModelAttribute("roles")
    public List<RoleReadOnlyDTO> roles() {
        return roleService.findAllRolesSortedByName();
    }
}
