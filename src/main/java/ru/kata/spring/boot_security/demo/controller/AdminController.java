package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String adminPage(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin";
    }

    @GetMapping("/create")
    public String showCreateUserForm(Model model) {
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("roles", roles);
        model.addAttribute("user", new User());
        return "create-user";
    }

    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("user") User user,
                             @RequestParam("roles") List<Long> roleIds,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRoles());
            return "create-user";
        }

        Set<Role> roles = roleIds.stream()
                .map(roleId -> roleService.getRoleById(roleId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            bindingResult.rejectValue("roles", "error.user", "At least one role must be selected.");
            model.addAttribute("roles", roleService.getAllRoles());
            return "create-user";
        }

        user.setRoles(roles);
        userService.addUser(user);
        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        Optional<User> optionalUser = userService.getUserById(id);
        if (!optionalUser.isPresent()) {
            model.addAttribute("errorMessage", "User not found with ID: " + id);
            return "error";
        }

        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("user", optionalUser.get());
        model.addAttribute("roles", roles);
        return "edit-user";
    }

    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") User user,
                             @RequestParam("roles") List<Long> roleIds,
                             BindingResult bindingResult,
                             Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRoles());
            return "edit-user";
        }

        if (roleIds == null || roleIds.isEmpty()) {
            bindingResult.rejectValue("roles", "error.user", "At least one role must be selected.");
            model.addAttribute("roles", roleService.getAllRoles());
            return "edit-user";
        }

        Set<Role> roles = roleIds.stream()
                .map(roleId -> roleService.getRoleById(roleId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            bindingResult.rejectValue("roles", "error.user", "Invalid roles selected.");
            model.addAttribute("roles", roleService.getAllRoles());
            return "edit-user";
        }

        user.setRoles(roles);
        userService.updateUser(user);
        return "redirect:/admin";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}