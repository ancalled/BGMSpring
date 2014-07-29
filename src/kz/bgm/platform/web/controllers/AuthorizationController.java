package kz.bgm.platform.web.controllers;

import kz.bgm.platform.model.domain.AdminUser;
import kz.bgm.platform.model.service.MainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "/main/auth")
public class AuthorizationController {


    @Autowired
    private MainService mainService;

    @RequestMapping(value = "/admin-auth")
    public String showCustomers(@RequestParam(value = "user") String user,
                                @RequestParam(value = "pass") String pass) {

        AdminUser admin = mainService.getAdmin(user, pass);
        if (admin != null) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(true);
            session.setAttribute("admin", admin);
            return "redirect:/mvc/main/index";
        } else {
            return "/login";
        }

    }

}
